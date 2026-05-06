package pt.up.fe.comp2026.analysis.calls;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.Arrays;
import java.util.List;

public class InstantiationValidationPass extends AnalysisVisitorWithTable {

    private MethodSymbol currentMethod;

    public InstantiationValidationPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.NEW_EXPR,    this::visitNewExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        var sig = types.getMethodDeclSignature(method);
        currentMethod = this.table.getMethod(sig).orElse(null);
        return null;
    }

    private Void visitNewExpr(JmmNode newExpr, SymbolTable table) {
        String className = newExpr.get("name");

        // --- 1. Class must be imported or be the current class ---
        boolean isCurrentClass = className.equals(this.table.getClassName())
                || className.equals(this.table.getFullyQualifiedName());

        boolean isImported = this.table.getImportNames().contains(className)
                || this.table.isImplicitImport(className);

        if (!isCurrentClass && !isImported) {
            addReport(newError(newExpr,
                    "Cannot instantiate class '" + className + "': class is not imported"));
            return null;
        }

        List<JmmNode> argNodes = newExpr.getChildren();

        // --- 2. For the current class, check via symbol table ---
        if (isCurrentClass) {
            String fqn = this.table.getImportedFullyQualifiedName(className).orElse(className);
            var stOpt = this.table.getImportedSymbolTable(fqn);
            if (stOpt.isEmpty()) stOpt = this.table.getImportedSymbolTable(className);
            if (stOpt.isEmpty()) stOpt = this.table.getImplicitImport(className);

            if (stOpt.isPresent()) {
                var st = stOpt.get();
                var allConstructors = st.getMethods("<init>");

                if (!allConstructors.isEmpty()) {
                    var matching = allConstructors.stream()
                            .filter(m -> m.parameters().size() == argNodes.size())
                            .toList();

                    if (matching.isEmpty()) {
                        int expected = allConstructors.get(0).parameters().size();
                        addReport(newError(newExpr,
                                "No constructor found in '" + className + "' that accepts "
                                        + argNodes.size() + " argument(s), expected " + expected));
                        return null;
                    }

                    var constructor = matching.get(0);
                    var params = constructor.parameters();
                    for (int i = 0; i < params.size(); i++) {
                        var expected = params.get(i).type();
                        var actual = types.getExprType(argNodes.get(i), currentMethod);
                        if (actual == null) continue;
                        if (!types.isTypeCompatible(expected, actual)) {
                            addReport(newError(argNodes.get(i),
                                    "Constructor argument " + (i + 1) + " of '" + className
                                            + "': expected '" + expected + "', got '" + actual + "'"));
                        }
                    }
                }
            }
            return null;
        }

        // --- 3. For imported classes, use symbol table first, then reflection ---
        String fqn = this.table.getImportedFullyQualifiedName(className).orElse(className);

        // Tentar via symbol table primeiro (mais preciso que reflexão)
        var stOpt = this.table.getImportedSymbolTable(fqn);
        if (stOpt.isEmpty()) stOpt = this.table.getImportedSymbolTable(className);
        if (stOpt.isEmpty()) stOpt = this.table.getImplicitImport(className);

        if (stOpt.isPresent()) {
            var st = stOpt.get();
            var allConstructors = st.getMethods("<init>");

            if (!allConstructors.isEmpty()) {
                var matching = allConstructors.stream()
                        .filter(m -> m.parameters().size() == argNodes.size())
                        .toList();

                if (matching.isEmpty()) {
                    int expected = allConstructors.get(0).parameters().size();
                    addReport(newError(newExpr,
                            "No constructor found in '" + className + "' that accepts "
                                    + argNodes.size() + " argument(s), expected " + expected));
                    return null;
                }

                // Verificar tipos dos argumentos via symbol table
                var constructor = matching.get(0);
                var params = constructor.parameters();
                for (int i = 0; i < params.size(); i++) {
                    var expected = params.get(i).type();
                    var actual = types.getExprType(argNodes.get(i), currentMethod);
                    if (actual == null) continue;
                    if (!types.isTypeCompatible(expected, actual)) {
                        addReport(newError(argNodes.get(i),
                                "Constructor argument " + (i + 1) + " of '" + className
                                        + "': expected '" + expected + "', got '" + actual + "'"));
                    }
                }
            }
            return null;
        }

        // Fallback: reflexão (apenas quando symbol table não está disponível)
        try {
            Class<?> clazz = Class.forName(fqn);
            var allCtors = clazz.getConstructors();

            // Nenhum construtor declarado = construtor default implícito (0 args)
            if (allCtors.length == 0) return null;

            var matching = Arrays.stream(allCtors)
                    .filter(c -> c.getParameterCount() == argNodes.size())
                    .toList();

            if (matching.isEmpty()) {
                int expected = allCtors[0].getParameterCount();
                addReport(newError(newExpr,
                        "No constructor found in '" + className + "' that accepts "
                                + argNodes.size() + " argument(s), expected " + expected));
            }
            // Tipos não verificados via reflexão — demasiado impreciso
        } catch (ClassNotFoundException e) {
            // Classe não encontrada em nenhum lado — skip conservador
        }

        return null;
    }
}
