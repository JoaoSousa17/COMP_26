package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.ArrayList;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Branch Elimination: replaces if/else statements whose condition is a
 * known boolean literal with just the taken branch.
 *
 *   if (true)  { thenBody } else { elseBody }  →  thenBody
 *   if (false) { thenBody } else { elseBody }  →  elseBody
 *   if (true)  { thenBody }                    →  thenBody
 *   if (false) { thenBody }                    →  (empty block)
 */
public class BranchEliminationVisitor extends PreorderJmmVisitor<Void, Void> {

    private boolean changed = false;

    public BranchEliminationVisitor() {
        setDefaultValue(() -> null);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(IF_ELSE_STMT, this::visitIfElse);
        addVisit(IF_STMT,      this::visitIf);
    }

    public boolean hasChanged() {
        return changed;
    }

    // if (cond) thenBody else elseBody
    // child 0 = condition, child 1 = thenBody, child 2 = elseBody
    private Void visitIfElse(JmmNode node, Void unused) {
        JmmNode cond = node.getChild(0);
        if (!BOOL_LITERAL.check(cond)) return null;

        boolean condValue = cond.get("value").equals("true");
        // Take child 1 (then) or child 2 (else)
        JmmNode branchBody = condValue ? node.getChild(1) : node.getChild(2);

        // Build a replacement BLOCK containing copies of the branch's children,
        // so we never re-use a node that is still attached to the original tree.
        JmmNode replacement = flattenIntoBlock(branchBody);
        node.replace(replacement);
        changed = true;
        return null;
    }

    // if (cond) thenBody
    // child 0 = condition, child 1 = thenBody
    private Void visitIf(JmmNode node, Void unused) {
        JmmNode cond = node.getChild(0);
        if (!BOOL_LITERAL.check(cond)) return null;

        boolean condValue = cond.get("value").equals("true");

        if (condValue) {
            JmmNode branchBody = node.getChild(1);
            JmmNode replacement = flattenIntoBlock(branchBody);
            node.replace(replacement);
        } else {
            // Never taken — replace with empty block
            node.replace(new JmmNodeImpl(BLOCK));
        }
        changed = true;
        return null;
    }

    /**
     * Creates a new BLOCK node whose children are the children of the given
     * branch body node.
     *
     * If the body is already a BLOCK, we lift its children directly into a
     * new BLOCK (avoiding the cycle that occurs when you try to replace a
     * node with one of its own existing children).
     *
     * If the body is a single statement (not a BLOCK), we wrap it in a new
     * BLOCK — but since we cannot safely re-parent an attached node, we
     * instead create a BLOCK and move the single statement into it.
     *
     * The key insight: we always create a FRESH JmmNodeImpl as the
     * replacement root so the replace() call never tries to set a node as
     * its own ancestor.
     */
    private JmmNode flattenIntoBlock(JmmNode branchBody) {
        JmmNode block = new JmmNodeImpl(BLOCK);

        if (BLOCK.check(branchBody)) {
            // Snapshot children list before we start re-parenting
            var children = new ArrayList<>(branchBody.getChildren());
            for (JmmNode child : children) {
                block.add(child);
            }
        } else {
            // Single statement — move it into the new block
            block.add(branchBody);
        }

        return block;
    }
}
