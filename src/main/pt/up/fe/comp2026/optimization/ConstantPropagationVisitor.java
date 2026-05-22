package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class ConstantPropagationVisitor extends PreorderJmmVisitor<Void, Void> {

    private boolean changed = false;

    public ConstantPropagationVisitor() {
        setDefaultValue(() -> null);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::processMethod);
    }

    public boolean hasChanged() {
        return changed;
    }

    private Void processMethod(JmmNode method, Void unused) {
        processBlock(stmtsOf(method), new HashMap<>());
        return null;
    }

    // Process a list of statements in order, returning the constants map after all of them.
    private Map<String, JmmNode> processBlock(List<JmmNode> stmts, Map<String, JmmNode> constants) {
        for (var stmt : stmts) {
            constants = processStmt(stmt, constants);
        }
        return constants;
    }

    // Process one statement. Returns an updated constants map.
    private Map<String, JmmNode> processStmt(JmmNode stmt, Map<String, JmmNode> constants) {
        if (ASSIGN_STMT.check(stmt)) {
            String var = stmt.get("var");
            // Propagate into RHS first
            propagateExpr(stmt.getChild(0), constants);
            // Re-fetch child after possible replacement
            JmmNode rhs = stmt.getChild(0);
            // Update map: if RHS is now a literal, var is a new constant
            constants = new HashMap<>(constants);
            if (INTEGER_LITERAL.check(rhs) || BOOL_LITERAL.check(rhs)) {
                constants.put(var, rhs);
            } else {
                constants.remove(var);
            }

        } else if (IF_ELSE_STMT.check(stmt) || IF_STMT.check(stmt)) {
            List<JmmNode> children = stmt.getChildren();
            // Propagate into condition (child 0)
            propagateExpr(children.get(0), constants);
            // Process then-branch (child 1) from pre-branch constants
            Map<String, JmmNode> postThen = processStmt(children.get(1), new HashMap<>(constants));
            // Process else-branch (child 2 if present), otherwise constants unchanged
            Map<String, JmmNode> postElse = constants;
            if (children.size() > 2) {
                postElse = processStmt(children.get(2), new HashMap<>(constants));
            }
            // Join: only keep constants that agree in both paths
            constants = joinConstants(postThen, postElse);

        } else if (WHILE_STMT.check(stmt)) {
            List<JmmNode> children = stmt.getChildren();
            // Propagate into condition (conservative: use pre-loop constants)
            propagateExpr(children.get(0), constants);
            // Propagate into body, but discard what changes inside the loop
            processStmt(children.get(1), new HashMap<>(constants));
            // Invalidate any variables written inside the loop
            Set<String> written = writtenVars(children.get(1));
            constants = new HashMap<>(constants);
            written.forEach(constants::remove);

        } else if (BLOCK.check(stmt)) {
            constants = processBlock(stmtsOf(stmt), new HashMap<>(constants));

        } else {
            // return, expr-stmt, array-store, for, do-while, etc.
            for (var child : new ArrayList<>(stmt.getChildren())) {
                propagateExpr(child, constants);
            }
        }
        return constants;
    }

    // Replace VAR_REF_EXPR nodes that map to a known constant.
    private void propagateExpr(JmmNode expr, Map<String, JmmNode> constants) {
        if (VAR_REF_EXPR.check(expr)) {
            String name = expr.get("name");
            JmmNode template = constants.get(name);
            if (template != null) {
                var newNode = new JmmNodeImpl(template.getKind());
                newNode.put("value", template.get("value"));
                expr.replace(newNode);
                changed = true;
            }
        } else {
            for (var child : new ArrayList<>(expr.getChildren())) {
                propagateExpr(child, constants);
            }
        }
    }

    // Intersection: keep only variables with the same literal value in both maps.
    private Map<String, JmmNode> joinConstants(Map<String, JmmNode> a, Map<String, JmmNode> b) {
        Map<String, JmmNode> result = new HashMap<>();
        for (var entry : a.entrySet()) {
            String var = entry.getKey();
            JmmNode valB = b.get(var);
            if (valB != null && entry.getValue().get("value").equals(valB.get("value"))) {
                result.put(var, entry.getValue());
            }
        }
        return result;
    }

    // Collect all variable names assigned anywhere inside a subtree.
    private Set<String> writtenVars(JmmNode node) {
        Set<String> written = new HashSet<>();
        collectWritten(node, written);
        return written;
    }

    private void collectWritten(JmmNode node, Set<String> written) {
        if (ASSIGN_STMT.check(node)) {
            written.add(node.get("var"));
        }
        for (var child : node.getChildren()) {
            collectWritten(child, written);
        }
    }

    // Returns the statement children of a node (skips VAR_DECL, PARAM, TYPE).
    private List<JmmNode> stmtsOf(JmmNode node) {
        List<JmmNode> result = new ArrayList<>();
        for (var child : node.getChildren()) {
            if (VAR_DECL.check(child) || PARAM.check(child) || TYPE.check(child)) continue;
            result.add(child);
        }
        return result;
    }
}