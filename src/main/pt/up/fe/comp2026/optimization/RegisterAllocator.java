package pt.up.fe.comp2026.optimization;

import org.antlr.v4.codegen.model.chunk.ListLabelRef;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class RegisterAllocator {

    public void allocate(Method method, int n, List<Report> reports) {
        method.buildCFG();

        List<Instruction> instructions = method.getInstructions();
        if (instructions.isEmpty()) return;

        // Compute USE and DEF sets for every instruction
        Map<Instruction, Set<String>> use = computeUse(method);
        Map<Instruction, Set<String>> def = computeDef(method);

        // Backward data flow: liveOut[n] = variables alive right after instruction n
        Map<Instruction, Set<String>> liveOut = livenessAnalysis(instructions, use, def);

        // Build interference graph (nodes = LOCAL vars, edges = simultaneous liveness)
        Map<String, Set<String>> graph = buildInterferenceGraph(method, instructions, def, liveOut);

        // Collect only LOCAL-scope variable names
        Set<String> locals = new HashSet<>();
        for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
            if (entry.getValue().getScope() == VarScope.LOCAL) {
                locals.add(entry.getKey());
            }
        }

        if (locals.isEmpty()) return;

        int numParams = method.getParams().size();
        int baseOffset = numParams + (method.isStaticMethod() ? 0 : 1);
        // K = colors available for locals: n encodes (this + params + locals), so subtract only params
        int K = (n == 0) ? Integer.MAX_VALUE : n - numParams;

        if (n >= 1 && K <= 0) {
            reports.add(Report.newError(Stage.LLIR_OPTIMIZATION, -1, -1,
                    "Method '" + method.getMethodName() + "': -r=" + n +
                    " is too small (params+this already use " + baseOffset + " slot(s)). " +
                    "Minimum required: " + (baseOffset + 1), null));
            return;
        }

        // Graph coloring: assign a color to each local variable
        Map<String, Integer> coloring = colorGraph(locals, graph, K);

        if (coloring == null) {
            int minNeeded = baseOffset + computeMinColors(locals, graph);
            reports.add(Report.newError(Stage.LLIR_OPTIMIZATION, -1, -1,
                    "Method '" + method.getMethodName() + "': cannot allocate with -r=" + n +
                    ". Minimum JVM local slots required: " + minNeeded, null));
            return;
        }

        applyColoring(method, coloring, baseOffset);
    }

    private Map<Instruction, Set<String>> computeUse(Method method) {
        Map<Instruction, Set<String>> use = new HashMap<>();
        for (Instruction instruction : method.getInstructions()) {
            use.put(instruction, getUse(instruction, method));
        }
        return use;
    }

    private Set<String> getUse(Instruction instruction, Method method) {
        switch (instruction.getInstType()) {
            case ASSIGN: {
                var assign = (AssignInstruction) instruction;
                Set<String> vars = new HashSet<>(getUse(assign.getRhs(), method));

                if (assign.getDest() instanceof ArrayOperand)
                    vars.addAll(extractVars(assign.getDest(), method));
                return vars;
            }
            case NOPER: {
                // SingleOpInstruction: a = b (simple copy)
                SingleOpInstruction si = (SingleOpInstruction) instruction;
                return extractVars(si.getSingleOperand(), method);
            }
            case BINARYOPER: {
                BinaryOpInstruction bi = (BinaryOpInstruction) instruction;
                Set<String> vars = new HashSet<>(extractVars(bi.getLeftOperand(), method));
                vars.addAll(extractVars(bi.getRightOperand(), method));
                return vars;
            }
            case UNARYOPER: {
                UnaryOpInstruction ui = (UnaryOpInstruction) instruction;
                return extractVars(ui.getOperand(), method);
            }
            case CALL: {
                CallInstruction call = (CallInstruction) instruction;
                Set<String> vars = new HashSet<>(extractVars(call.getCaller(), method));
                for (Element arg : call.getArguments())
                    vars.addAll(extractVars(arg, method));
                return  vars;
            }
            case RETURN:  {
                ReturnInstruction ret = (ReturnInstruction) instruction;
                return ret.getOperand()
                        .map(op -> extractVars(op, method))
                        .orElse(Collections.emptySet());
            }
            case BRANCH: {
                CondBranchInstruction branch = (CondBranchInstruction) instruction;

                Set<String> vars = new HashSet<>();
                for (Element op : branch.getOperands())
                    vars.addAll(extractVars(op, method));
                return vars;
            }
            case PUTFIELD:  {
                PutFieldInstruction pf = (PutFieldInstruction) instruction;
                Set<String> vars = new HashSet<>(extractVars(pf.getObject(), method));
                vars.addAll(extractVars(pf.getValue(), method));
                return vars;
            }
            case GETFIELD:  {
                GetFieldInstruction gf = (GetFieldInstruction) instruction;
                return extractVars(gf.getObject(), method);
            }
            default:
                return Collections.emptySet();
        }
    }

    private Map<Instruction, Set<String>> computeDef(Method method) {
        Map<Instruction, Set<String>> def = new HashMap<>();
        for (Instruction instruction : method.getInstructions()) {
            if (instruction instanceof AssignInstruction) {
                AssignInstruction assign = (AssignInstruction) instruction;
                Element dest = assign.getDest();
                if (!(dest instanceof ArrayOperand) && dest instanceof Operand op && isLocal(((Operand) dest).getName(), method)) {
                    def.put(instruction, Collections.singleton(((Operand) dest).getName()));
                    continue;
                }
            }
            def.put(instruction, Collections.emptySet());
        }
        return def;
    }

    private Map<Instruction, Set<String>> livenessAnalysis(
            List<Instruction> instructions,
            Map<Instruction, Set<String>> use,
            Map<Instruction, Set<String>> def
    ) {
        var liveIn = new HashMap<Instruction, Set<String>>();
        var liveOut = new HashMap<Instruction, Set<String>>();
        for (var instruction : instructions) {
            liveIn.put(instruction, new HashSet<>());
            liveOut.put(instruction, new HashSet<>());
        }

        boolean changed;
        do {
            changed = false;
            // backwards propagation
            for (int i = instructions.size() -1; i >= 0; i--) {
                Instruction instruction = instructions.get(i);
                // LiveOut[n] = union of LiveIn of all successor instructions
                Set<String> newOut = new HashSet<>();
                for (Instruction succ : instruction.getSuccessorsAsInst())
                    newOut.addAll(liveIn.get(succ));

                // LiveIn[n] = USE[n] U (LiveOut[n] - DEF[n])
                Set<String> newIn = new HashSet<>(use.get(instruction));
                for (String v : newOut)
                    if (!def.get(instruction).contains(v))
                        newIn.add(v);

                if (!newOut.equals(liveOut.get(instruction)) || !newIn.equals(liveIn.get(instruction))) {
                    liveOut.put(instruction, newOut);
                    liveIn.put(instruction, newIn);
                    changed = true;
                }
            }
        } while (changed);
        return liveOut;
    }

    private Map<String, Set<String>> buildInterferenceGraph(
            Method method,
            List<Instruction> instructions,
            Map<Instruction, Set<String>> def,
            Map<Instruction, Set<String>> liveOut
    ) {
        // one node per LOCAL variable
        Map<String, Set<String>> graph = new HashMap<>();
        for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
            if (entry.getValue().getScope() == VarScope.LOCAL) {
                graph.put(entry.getKey(), new HashSet<>());
            }
        }

        for (var instruction : instructions) {
            for (var d : def.get(instruction)) {
                if (!isLocal(d, method)) continue;
                for (String v : liveOut.get(instruction)) {
                    if (!v.equals(d) && isLocal(v, method)) {
                        graph.get(d).add(v);
                        graph.get(v).add(d);
                    }
                }
            }
        }
        return graph;
    }

    private Map<String, Integer> colorGraph(Set<String> locals, Map<String, Set<String>> graph, int K) {
        List<String> sorted = new ArrayList<>(locals);
        sorted.sort((a, b) ->
                graph.getOrDefault(b, Collections.emptySet()).size()
                - graph.getOrDefault(a, Collections.emptySet()).size());

        Map<String, Integer> coloring = new HashMap<>();

        for (String v : sorted) {
            Set<Integer> used = new HashSet<>();
            for (String neighbor : graph.getOrDefault(v, Collections.emptySet())) {
                if (coloring.containsKey(neighbor)) {
                    used.add(coloring.get(neighbor));
                }
            }

            int color = 0;
            while (used.contains(color)) color++;

            if (K != Integer.MAX_VALUE && color >= K) return null;

            coloring.put(v, color);
        }
        return coloring;
    }

    private int computeMinColors(Set<String> locals, Map<String, Set<String>> graph) {
        Map<String, Integer> full = colorGraph(locals, graph, Integer.MAX_VALUE);
        if (full == null || full.isEmpty()) return 0;
        return full.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

    private void applyColoring(Method method, Map<String, Integer> coloring, int baseOffset) {
        for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
            Descriptor desc = entry.getValue();
            if (desc.getScope() == VarScope.LOCAL) {
                int color = coloring.get(entry.getKey());
                desc.setVirtualReg(baseOffset + coloring.get(entry.getKey()));
            }
        }
    }

    // -------
    // Helpers
    // -------

    private Set<String> extractVars(Element element, Method method) {
        if (element instanceof LiteralElement) return Collections.emptySet();

        if (element instanceof ArrayOperand) {
            ArrayOperand ao = (ArrayOperand) element;
            Set<String> vars = new HashSet<>();
            if (isLocalOrParam(ao.getName(), method))
                vars.add(ao.getName());
            for (Element idx : ao.getIndexOperands()) {
                if (idx instanceof Operand && isLocalOrParam(((Operand) idx).getName(), method))
                    vars.add(((Operand) idx).getName());
            }
            return vars;
        }

        if (element instanceof Operand) {
            String name = ((Operand) element).getName();
            if (isLocalOrParam(name, method)) return Collections.singleton(name);
        }

        return Collections.emptySet();
    }

    private boolean isLocalOrParam(String name, Method method) {
        Descriptor desc = method.getVarTable().get(name);
        return desc != null && desc.getScope() == VarScope.LOCAL || desc.getScope() == VarScope.PARAMETER;
    }

    private boolean isLocal(String name, Method method) {
        Descriptor desc = method.getVarTable().get(name);
        return desc != null && desc.getScope() == VarScope.LOCAL;
    }
}
