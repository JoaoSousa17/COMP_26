package pt.up.fe.comp2026.analysis.entityops;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Analysis pass that reports uses of undeclared variables.
 *
 * A variable reference is considered valid if the name resolves to any of:
 *   - A parameter or local variable of the enclosing method.
 *   - A field of the current class.
 *   - An explicitly imported class name.
 *   - An implicitly imported class name.
 *   - The name (simple or fully qualified) of the current class itself.
 *
 * Falls back to AST-based lookup if the symbol table method resolution fails.
 */
public class UndeclaredVariablePass extends AnalysisVisitorWithTable {

    /** The symbol table entry for the current method, used for parameter/local lookup. */
    private MethodSymbol currentMethod;

    /** The AST node of the current method, used as a fallback when symbol table resolution fails. */
    private JmmNode currentMethodNode;

    public UndeclaredVariablePass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL,  this::visitMethodDecl);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    /**
     * Tracks the current method. Attempts signature-based resolution first;
     * falls back to matching by name and static flag if that fails.
     */
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethodNode = method;
        try {
            var sig = types.getMethodDeclSignature(method);
            currentMethod = this.table.getMethod(sig).orElse(null);
        } catch (Exception e) {
            currentMethod = null;
        }

        if (currentMethod == null) {
            // Fallback: match by name and static flag
            String methodName = method.get("name");
            boolean isStatic = method.getBoolean("isStatic", false);
            currentMethod = this.table.getMethods(methodName).stream()
                    .filter(m -> m.isStatic() == isStatic)
                    .findFirst()
                    .orElse(this.table.getMethods(methodName).stream().findFirst().orElse(null));
        }

        return null;
    }

    /**
     * Checks that the referenced variable name is declared in an accessible scope.
     * Resolution order: method parameters → method locals → class fields →
     * explicit imports → implicit imports → current class name.
     */
    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        String name = varRefExpr.get("name");

        // Check method parameters and locals via symbol table
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return null;
            if (currentMethod.getLocalVariable(name).isPresent()) return null;
        }

        // Fallback: check parameters and locals directly from the AST
        if (currentMethodNode != null) {
            boolean inParams = currentMethodNode.getChildren(JmmKind.PARAM).stream()
                    .anyMatch(p -> p.get("name").equals(name));
            if (inParams) return null;

            boolean inLocals = currentMethodNode.getChildren(JmmKind.VAR_DECL).stream()
                    .anyMatch(v -> v.get("name").equals(name));
            if (inLocals) return null;
        }

        // Check class fields
        if (this.table.getField(name).isPresent()) return null;

        // Check explicit and implicit imports
        if (this.table.getImportNames().contains(name)) return null;
        if (this.table.isImplicitImport(name)) return null;

        // Allow references to the current class itself (simple name, FQN, or simple of FQN)
        String className = this.table.getClassName();
        if (name.equals(className)) return null;

        String fqn = this.table.getFullyQualifiedName();
        if (name.equals(fqn)) return null;

        String simpleOfFqn = fqn.contains(".") ? fqn.substring(fqn.lastIndexOf('.') + 1) : fqn;
        if (name.equals(simpleOfFqn)) return null;

        addReport(newError(varRefExpr, "Undeclared variable '" + name + "'"));
        return null;
    }
}
