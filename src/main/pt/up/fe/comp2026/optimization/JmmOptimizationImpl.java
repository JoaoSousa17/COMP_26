package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2026.CompilerConfig;

import java.util.Collections;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());
        var ollirCode = visitor.visit(semanticsResult.getRootNode());
        System.out.println("\nOLLIR:\n\n" + ollirCode);
        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult transformAst(JmmSemanticsResult semanticsResult) {
        if (!CompilerConfig.getOptimize(semanticsResult.config())) {
            return semanticsResult;
        }

        // Phase 1: Constant Propagation + Constant Folding loop (to fixed point)
        boolean changed;
        do {
            var propagation = new ConstantPropagationVisitor();
            propagation.visit(semanticsResult.getRootNode());

            var folding = new ConstantFoldingVisitor();
            folding.visit(semanticsResult.getRootNode());

            changed = propagation.hasChanged() || folding.hasChanged();
        } while (changed);

        // Phase 2: Dead Code Elimination loop (to fixed point, separate from prop/fold)
        do {
            var dce = new DeadCodeEliminationVisitor();
            dce.visit(semanticsResult.getRootNode());
            changed = dce.hasChanged();
        } while (changed);

        // Phase 3: One more round of Prop+Fold in case DCE exposed new opportunities,
        // then DCE again — repeat until truly stable
        boolean anyChange;
        do {
            anyChange = false;

            do {
                var propagation = new ConstantPropagationVisitor();
                propagation.visit(semanticsResult.getRootNode());
                var folding = new ConstantFoldingVisitor();
                folding.visit(semanticsResult.getRootNode());
                changed = propagation.hasChanged() || folding.hasChanged();
                if (changed) anyChange = true;
            } while (changed);

            do {
                var dce = new DeadCodeEliminationVisitor();
                dce.visit(semanticsResult.getRootNode());
                changed = dce.hasChanged();
                if (changed) anyChange = true;
            } while (changed);

        } while (anyChange);

        return semanticsResult;
    }

    @Override
    public OllirResult transformOllir(OllirResult ollirResult) {
        if (ollirResult.config().getOrDefault("debug", "false").equals("true")) {
            System.out.println("\nOLLIR CODE:");
            System.out.println(ollirResult.getOllirCode());
        }

        int n = CompilerConfig.getRegisterAllocation(ollirResult.config());
        if (n != -1) {
            RegisterAllocator allocator = new RegisterAllocator();
            for (Method method : ollirResult.getOllirClass().getMethods()) {
                if (!method.isConstructMethod()) {
                    allocator.allocate(method, n, ollirResult.reports());
                }
            }
        }
        return ollirResult;
    }
}
