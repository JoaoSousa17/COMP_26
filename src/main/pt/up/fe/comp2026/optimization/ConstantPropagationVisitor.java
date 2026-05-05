package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class ConstantPropagationVisitor extends PreorderJmmVisitor<Void, Void> {

    private Map<String, JmmNode> safeConstants = new HashMap<>();
    private boolean changed = false;

    public ConstantPropagationVisitor() {
        setDefaultValue(() -> null);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::scanMethod);
        addVisit(VAR_REF_EXPR, this::replaceIfConstant);
    }

    public boolean hasChanged() {
        return changed;
    }

    // SCAN
    private Void scanMethod(JmmNode methodNode, Void unused) {
        safeConstants.clear();

        // find variables assigned anywhere inside control flow (unsafe)
        Set<String> unsafeVars = new HashSet<>();
        for (var cfNode : methodNode.getDescendants(IF_ELSE_STMT)) collectAssignedVars(cfNode, unsafeVars);
        for (var cfNode : methodNode.getDescendants(IF_STMT)) collectAssignedVars(cfNode, unsafeVars);
        for (var cfNode : methodNode.getDescendants(WHILE_STMT)) collectAssignedVars(cfNode, unsafeVars);
        for (var cfNode : methodNode.getDescendants(FOR_STMT)) collectAssignedVars(cfNode, unsafeVars);
        for (var cfNode : methodNode.getDescendants(DO_WHILE_STMT)) collectAssignedVars(cfNode, unsafeVars);

        // count assignments per variable and record literal RHS
        Map<String, Integer> assignCount = new HashMap<>();
        Map<String, JmmNode> literalRhs = new HashMap<>();

        for (var assign : methodNode.getDescendants(ASSIGN_STMT)) {
            String var = assign.get("var");
            assignCount.merge(var, 1, Integer::sum);

            JmmNode rhs = assign.getChild(0);
            if (rhs.isInstance(INTEGER_LITERAL) || rhs.isInstance(BOOL_LITERAL)) {
                // only store if not yet overwritten by a non-literal in a later iteration
                if (!literalRhs.containsKey(var) || assignCount.get(var) == 1) {
                    literalRhs.put(var, rhs);
                } else {
                    literalRhs.remove(var); // has a non-literal assignment - not foldable
                }
            }
        }

        // a variable is safe if assigned exactly once, with a literal, outside control flow
        for (var entry : assignCount.entrySet()) {
            String var1 = entry.getKey();
            if (entry.getValue() == 1 && !unsafeVars.contains(var1) && literalRhs.containsKey(var1)) {
                safeConstants.put(var1, literalRhs.get(var1));
            }
        }
        return null;
    }

    private void collectAssignedVars(JmmNode controlFlowNode, Set<String> result) {
        for (var assign : controlFlowNode.getDescendants(ASSIGN_STMT)) {
            result.add(assign.get("var"));
        }
    }

    // REPLACE
    private Void replaceIfConstant(JmmNode varRefNode, Void unused) {
        String name = varRefNode.get("name");

        if (!safeConstants.containsKey(name)) {
            return null;
        }
        JmmNode template = safeConstants.get(name);

        // create a new node - a node can only have one parent
        var newNode = new JmmNodeImpl(template.getKind());
        newNode.put("value", template.get("value"));

        varRefNode.replace(newNode);

        return null;
    }
}
