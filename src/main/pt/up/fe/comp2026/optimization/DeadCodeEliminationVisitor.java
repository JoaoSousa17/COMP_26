package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Dead Code Elimination AST visitor.
 *
 * Only eliminates local variables (declared via VarDecl inside the method body).
 * Fields and parameters are never touched.
 *
 * Single pass per visit() call. JmmOptimizationImpl loops externally until
 * hasChanged() returns false.
 *
 * Two elimination strategies:
 *  1. Global: if a local var never appears as VarRefExpr anywhere -> all its
 *     assignments are dead.
 *  2. Liveness: if a local var is assigned inside an if/else branch and is
 *     not live after the if/else -> that specific assignment is dead.
 *
 * Node removal: node.replace(new JmmNodeImpl(BLOCK)) — replaces with an empty
 * BLOCK node. OllirGeneratorVisitor.visitBlock() on an empty block returns "".
 * The check `if (childCode != null && !childCode.isBlank())` in visitMethodDecl
 * ensures empty blocks produce no OLLIR output.
 */
public class DeadCodeEliminationVisitor extends PreorderJmmVisitor<Void, Void> {

    private boolean changed = false;

    public DeadCodeEliminationVisitor() {
        setDefaultValue(() -> null);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::visitMethodDecl);
    }

    public boolean hasChanged() {
        return changed;
    }

    private Void visitMethodDecl(JmmNode method, Void unused) {
        Set<String> localVars = collectLocalVarNames(method);
        if (localVars.isEmpty()) return null;

        // Single pass: try global elimination first, then liveness-based
        if (!eliminateGloballyDead(method, localVars)) {
            eliminateByLiveness(method, localVars);
        }

        return null;
    }

    private Set<String> collectLocalVarNames(JmmNode method) {
        Set<String> locals = new HashSet<>();
        for (JmmNode child : method.getChildren()) {
            if (VAR_DECL.check(child)) {
                locals.add(child.get("name"));
            }
        }
        return locals;
    }

    // -------------------------------------------------------------------------
    // Pass 1: variables never read anywhere in the method
    // -------------------------------------------------------------------------

    private boolean eliminateGloballyDead(JmmNode method, Set<String> localVars) {
        Set<String> readVars = collectVarRefs(method, localVars);
        boolean anyRemoved = false;

        for (JmmNode assign : new ArrayList<>(method.getDescendants(ASSIGN_STMT))) {
            String lhs = assign.get("var");
            if (!localVars.contains(lhs)) continue;
            if (readVars.contains(lhs)) continue;
            if (rhsHasSideEffects(assign.getChild(0))) continue;
            replaceWithEmpty(assign);
            changed = true;
            anyRemoved = true;
        }

        for (JmmNode store : new ArrayList<>(method.getDescendants(ARRAY_STORE_STMT))) {
            String name = store.get("name");
            if (!localVars.contains(name)) continue;
            if (readVars.contains(name)) continue;
            replaceWithEmpty(store);
            changed = true;
            anyRemoved = true;
        }

        return anyRemoved;
    }

    private Set<String> collectVarRefs(JmmNode root, Set<String> localVars) {
        Set<String> refs = new HashSet<>();
        collectVarRefsRec(root, localVars, refs);
        return refs;
    }

    private void collectVarRefsRec(JmmNode node, Set<String> localVars, Set<String> refs) {
        if (VAR_REF_EXPR.check(node)) {
            String name = node.get("name");
            if (localVars.contains(name)) refs.add(name);
            return;
        }
        for (JmmNode child : node.getChildren()) {
            collectVarRefsRec(child, localVars, refs);
        }
    }

    // -------------------------------------------------------------------------
    // Pass 2: liveness-based (handles assignments inside if/else branches)
    // -------------------------------------------------------------------------

    private boolean eliminateByLiveness(JmmNode method, Set<String> localVars) {
        List<JmmNode> stmts = getStatements(method);
        return processBlock(stmts, new HashSet<>(), localVars);
    }

    private boolean processBlock(List<JmmNode> stmts, Set<String> liveOut,
                                 Set<String> localVars) {
        if (stmts.isEmpty()) return false;
        boolean anyRemoved = false;

        // Compute live-after for each position by backward scan
        List<Set<String>> liveAfter = new ArrayList<>(stmts.size() + 1);
        for (int i = 0; i <= stmts.size(); i++) liveAfter.add(new HashSet<>());
        liveAfter.set(stmts.size(), new HashSet<>(liveOut));

        for (int i = stmts.size() - 1; i >= 0; i--) {
            liveAfter.set(i, computeLiveBefore(stmts.get(i), liveAfter.get(i + 1), localVars));
        }

        // Eliminate dead assignments
        for (int i = 0; i < stmts.size(); i++) {
            JmmNode stmt = stmts.get(i);
            if (!ASSIGN_STMT.check(stmt)) continue;
            String lhs = stmt.get("var");
            if (!localVars.contains(lhs)) continue;
            if (rhsHasSideEffects(stmt.getChild(0))) continue;
            if (!liveAfter.get(i + 1).contains(lhs)) {
                replaceWithEmpty(stmt);
                changed = true;
                anyRemoved = true;
            }
        }

        // Recurse into if/else bodies (not while/for — conservative for loops)
        for (int i = 0; i < stmts.size(); i++) {
            JmmNode stmt = stmts.get(i);
            Set<String> liveAfterStmt = liveAfter.get(i + 1);

            if (IF_ELSE_STMT.check(stmt) || IF_STMT.check(stmt)) {
                List<JmmNode> children = stmt.getChildren();
                // child 0 = condition; children 1..n = branch bodies
                for (int c = 1; c < children.size(); c++) {
                    List<JmmNode> bodyStmts = getStatements(children.get(c));
                    anyRemoved |= processBlock(bodyStmts, liveAfterStmt, localVars);
                }
            } else if (BLOCK.check(stmt)) {
                anyRemoved |= processBlock(getStatements(stmt), liveAfterStmt, localVars);
            }
        }

        return anyRemoved;
    }

    private Set<String> computeLiveBefore(JmmNode stmt, Set<String> liveAfter,
                                          Set<String> localVars) {
        Set<String> result = new HashSet<>(liveAfter);

        if (ASSIGN_STMT.check(stmt)) {
            result.remove(stmt.get("var"));
            result.addAll(collectVarRefs(stmt.getChild(0), localVars));

        } else if (IF_ELSE_STMT.check(stmt) || IF_STMT.check(stmt)) {
            List<JmmNode> children = stmt.getChildren();
            result.addAll(collectVarRefs(children.get(0), localVars)); // condition
            for (int c = 1; c < children.size(); c++) {
                result.addAll(computeLiveBeforeBlock(
                        getStatements(children.get(c)), liveAfter, localVars));
            }

        } else {
            // return, expr-stmt, array-store, while, etc.
            result.addAll(collectVarRefs(stmt, localVars));
        }

        return result;
    }

    private Set<String> computeLiveBeforeBlock(List<JmmNode> stmts, Set<String> liveAfter,
                                               Set<String> localVars) {
        Set<String> live = new HashSet<>(liveAfter);
        for (int i = stmts.size() - 1; i >= 0; i--) {
            live = computeLiveBefore(stmts.get(i), live, localVars);
        }
        return live;
    }

    // -------------------------------------------------------------------------
    // Node removal and helpers
    // -------------------------------------------------------------------------

    /**
     * Replace a statement node with an empty BLOCK.
     * OllirGeneratorVisitor.visitBlock() returns "" for empty blocks.
     * visitMethodDecl skips children where childCode.isBlank().
     */
    private void replaceWithEmpty(JmmNode node) {
        node.replace(new JmmNodeImpl(BLOCK));
    }

    private boolean rhsHasSideEffects(JmmNode expr) {
        if (expr == null) return false;
        if (METHOD_CALL_EXPR.check(expr))        return true;
        if (IMPLICIT_THIS_CALL_EXPR.check(expr)) return true;
        for (JmmNode child : expr.getChildren()) {
            if (rhsHasSideEffects(child)) return true;
        }
        return false;
    }

    private List<JmmNode> getStatements(JmmNode node) {
        List<JmmNode> result = new ArrayList<>();
        for (JmmNode child : node.getChildren()) {
            if (VAR_DECL.check(child) || PARAM.check(child) || TYPE.check(child)) continue;
            result.add(child);
        }
        return result;
    }
}
