package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashMap;
import java.util.Map;

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

        // Count assignments per variable and record literal RHS.
        // A variable is safe to propagate if:
        //   - assigned exactly once in the entire method, AND
        //   - that assignment's RHS is a literal (int or bool)
        //
        // We do NOT exclude variables assigned inside control flow,
        // because even inside an if-branch, if it's the only assignment,
        // the value is always the same literal wherever the var is read.
        Map<String, Integer> assignCount = new HashMap<>();
        Map<String, JmmNode> literalRhs = new HashMap<>();

        for (var assign : methodNode.getDescendants(ASSIGN_STMT)) {
            String var = assign.get("var");
            int count = assignCount.merge(var, 1, Integer::sum);

            JmmNode rhs = assign.getChild(0);
            if (count == 1 && (rhs.isInstance(INTEGER_LITERAL) || rhs.isInstance(BOOL_LITERAL))) {
                literalRhs.put(var, rhs);
            } else {
                // More than one assignment, or non-literal first assignment — not propagatable
                literalRhs.remove(var);
            }
        }

        // A variable is safe if assigned exactly once with a literal
        for (var entry : assignCount.entrySet()) {
            String var = entry.getKey();
            if (entry.getValue() == 1 && literalRhs.containsKey(var)) {
                safeConstants.put(var, literalRhs.get(var));
            }
        }

        return null;
    }

    // REPLACE
    private Void replaceIfConstant(JmmNode varRefNode, Void unused) {
        String name = varRefNode.get("name");

        if (!safeConstants.containsKey(name)) {
            return null;
        }
        JmmNode template = safeConstants.get(name);

        // create a new node — a node can only have one parent
        var newNode = new JmmNodeImpl(template.getKind());
        newNode.put("value", template.get("value"));

        varRefNode.replace(newNode);
        changed = true;

        return null;
    }
}
