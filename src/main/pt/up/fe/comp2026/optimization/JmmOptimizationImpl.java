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

        // Run all AST optimizations to a fixed point.
        // Order per iteration:
        //   1. Constant Propagation
        //   2. Constant Folding
        //   3. Branch Elimination  (needs folded literals to work)
        //   4. Dead Code Elimination
        boolean anyChange;
        do {
            anyChange = false;

            // Constant Propagation + Folding loop
            boolean changed;
            do {
                var propagation = new ConstantPropagationVisitor();
                propagation.visit(semanticsResult.getRootNode());

                var folding = new ConstantFoldingVisitor();
                folding.visit(semanticsResult.getRootNode());

                changed = propagation.hasChanged() || folding.hasChanged();
                if (changed) anyChange = true;
            } while (changed);

            // Branch Elimination — runs after folding to catch literal conditions
            do {
                var branchElim = new BranchEliminationVisitor();
                branchElim.visit(semanticsResult.getRootNode());
                changed = branchElim.hasChanged();
                if (changed) anyChange = true;
            } while (changed);

            // Dead Code Elimination
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
