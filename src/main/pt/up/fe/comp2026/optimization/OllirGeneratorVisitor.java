package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;

import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 *
 * 3.1.2 — Statements:
 *   • Return statements  (visitReturn)
 *   • Conditional instructions  (visitIfElseStmt, visitIfStmt)
 *   • While loops  (visitWhileStmt)
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";

    private final SymbolTable table;
    private final TypeUtils types;
    private final OptUtils ollirTypes;
    private final OllirExprGeneratorVisitor exprVisitor;

    private MethodSymbol currentMethod;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        this.exprVisitor = new OllirExprGeneratorVisitor(table, ollirTypes);
        this.currentMethod = null;
    }

    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM,          this::visitProgram);
        addVisit(PACKAGE_DECL,     this::visitPackageDecl);
        addVisit(CLASS_DECL,       this::visitClass);
        addVisit(VAR_DECL,         this::visitVarDecl);
        addVisit(PARAM,            this::visitParam);
        addVisit(METHOD_DECL,      this::visitMethodDecl);
        addVisit(RETURN_STMT,      this::visitReturn);
        addVisit(IF_ELSE_STMT,     this::visitIfElseStmt);
        addVisit(IF_STMT,          this::visitIfStmt);
        addVisit(WHILE_STMT,       this::visitWhileStmt);
        addVisit(ASSIGN_STMT,      this::visitAssignStmt);
        addVisit(BLOCK,            this::visitBlock);
        addVisit(EXPR_STMT,        this::visitExprStmt);
        addVisit(ARRAY_STORE_STMT, this::visitArrayStoreStmt);
    }

    // ── Programme / class structure ───────────────────────────────────────────

    private String visitProgram(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        for (var child : node.getChildren()) {
            if (IMPORT_DECL.check(child)) {
                var path = child.getObjectAsList("path", String.class);
                code.append("import ").append(String.join(".", path)).append(";\n");
            } else {
                code.append(visit(child));
            }
        }
        return code.toString();
    }

    private String visitPackageDecl(JmmNode node, Void unused) {
        var path = node.getObjectAsList("path", String.class);
        if (path.isEmpty()) return "package ;\n";
        return "package " + String.join(".", path) + ";\n";
    }

    private String visitVarDecl(JmmNode varDecl, Void unused) {
        return varDecl.get("name") +
                ollirTypes.toOllirType(varDecl.getObject("typeNode", JmmNode.class)) + ";";
    }

    private String visitParam(JmmNode param, Void unused) {
        return ollirTypes.sanitizeId(param.get("name")) +
                ollirTypes.toOllirType(param.getObject("typeNode", JmmNode.class));
    }

    private String visitClass(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        code.append(NL).append(table.getClassName());

        String superName = table.getSuperFullyQualifiedName();
        if (superName != null && !superName.isBlank()) {
            int dot = superName.lastIndexOf('.');
            code.append(" extends ").append(dot >= 0 ? superName.substring(dot + 1) : superName);
        }

        code.append(L_BRACKET).append(NL);

        for (var field : table.getFields()) {
            code.append(".field public ")
                    .append(ollirTypes.sanitizeId(field.name()))
                    .append(ollirTypes.toOllirType(field.type()))
                    .append(";\n");
        }
        code.append(NL);

        code.append(buildConstructor(node)).append(NL);

        for (var classMember : node.getChildren(CLASS_MEMBER)) {
            for (var method : classMember.getChildren(METHOD_DECL)) {
                code.append(visit(method));
            }
        }

        code.append(R_BRACKET);
        return code.toString();
    }

    private String buildConstructor(JmmNode classDecl) {
        StringBuilder sb = new StringBuilder();
        sb.append(".construct ().V {\n");
        String superFqn = table.getSuperFullyQualifiedName();
        String superSimple;
        if (superFqn == null || superFqn.isBlank()) {
            superSimple = "Object";
        } else {
            int dot = superFqn.lastIndexOf('.');
            superSimple = dot >= 0 ? superFqn.substring(dot + 1) : superFqn;
        }
        sb.append("    invokespecial(this.").append(superSimple).append(", \"<init>\").V;\n");

        for (var classMember : classDecl.getChildren(CLASS_MEMBER)) {
            for (var fieldDecl : classMember.getChildren(FIELD_DECL)) {
                JmmNode initExpr = null;
                for (JmmNode child : fieldDecl.getChildren()) {
                    if (!TYPE.check(child)) { initExpr = child; break; }
                }
                if (initExpr == null) continue;

                String fieldName = fieldDecl.get("name");
                var fieldSym = table.getField(fieldName);
                if (fieldSym.isEmpty()) continue;
                String typeStr = ollirTypes.toOllirType(fieldSym.get().type());

                var initResult = exprVisitor.visit(initExpr);
                if (initResult.getComputation() != null && !initResult.getComputation().isBlank()) {
                    for (String line : initResult.getComputation().lines().toList()) {
                        sb.append("    ").append(line).append("\n");
                    }
                }
                sb.append("    putfield(this.").append(table.getClassName())
                        .append(", ").append(ollirTypes.sanitizeId(fieldName)).append(typeStr)
                        .append(", ").append(initResult.getCode()).append(").V;\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    // ── Method structure ──────────────────────────────────────────────────────

    private String visitMethodDecl(JmmNode node, Void unused) {
        var sig = TypeUtils.with(table).getMethodDeclSignature(node);
        var methodOpt = table.getMethod(sig);
        if (methodOpt.isEmpty()) {
            var byName = table.getMethods(node.get("name"));
            if (byName.isEmpty()) return "";
            currentMethod = byName.get(0);
        } else {
            currentMethod = methodOpt.get();
        }
        exprVisitor.currentMethod = currentMethod;

        StringBuilder code = new StringBuilder(".method ");

        var visibility = currentMethod.visibility();
        if (visibility == Visibility.PUBLIC)         code.append("public ");
        else if (visibility == Visibility.PRIVATE)   code.append("private ");
        else if (visibility == Visibility.PROTECTED) code.append("protected ");

        if (node.getObject("isStatic", Boolean.class)) code.append("static ");

        code.append(ollirTypes.sanitizeId(node.get("name")));

        String paramsCode = node.getChildren(PARAM).stream()
                .map(p -> visitParam(p, null))
                .collect(Collectors.joining(", "));
        code.append("(").append(paramsCode).append(")");

        code.append(ollirTypes.toOllirType(currentMethod.returnType()));
        code.append(L_BRACKET);

        for (var child : node.getChildren()) {
            if (VAR_DECL.check(child) || PARAM.check(child) || TYPE.check(child)) continue;
            String childCode = visit(child);
            if (childCode != null && !childCode.isBlank()) {
                code.append("   ").append(childCode.replace("\n", "\n   "));
            }
        }

        code.append(R_BRACKET).append(NL);

        currentMethod = null;
        exprVisitor.currentMethod = null;
        return code.toString();
    }

    // ── 3.1.2 Return ─────────────────────────────────────────────────────────

    private String visitReturn(JmmNode node, Void unused) {
        JmmType retType = currentMethod.returnType();

        if (JmmPrimitiveType.VOID.equals(retType)) {
            return "ret.V;\n";
        }

        var expr = exprVisitor.visit(node.getChild(0));
        StringBuilder code = new StringBuilder();
        code.append(expr.getComputation());
        code.append("ret").append(ollirTypes.toOllirType(retType))
                .append(SPACE).append(expr.getCode()).append(END_STMT);
        return code.toString();
    }

    // ── 3.1.2 Conditionals ───────────────────────────────────────────────────

    private String visitIfElseStmt(JmmNode node, Void unused) {
        String labelTrue = ollirTypes.nextLabel("ifTrue_");
        String labelEnd  = ollirTypes.nextLabel("ifEnd_");

        var cond = exprVisitor.visit(node.getChild(0));
        StringBuilder code = new StringBuilder();
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(labelTrue).append(END_STMT);

        code.append(indentBlock(visit(node.getChild(2))));
        code.append("goto ").append(labelEnd).append(END_STMT);

        code.append(labelTrue).append(":").append(NL);
        code.append(indentBlock(visit(node.getChild(1))));
        code.append(labelEnd).append(":").append(NL);

        return code.toString();
    }

    private String visitIfStmt(JmmNode node, Void unused) {
        String labelTrue = ollirTypes.nextLabel("ifTrue_");
        String labelEnd  = ollirTypes.nextLabel("ifEnd_");

        var cond = exprVisitor.visit(node.getChild(0));
        StringBuilder code = new StringBuilder();
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(labelTrue).append(END_STMT);
        code.append("goto ").append(labelEnd).append(END_STMT);

        code.append(labelTrue).append(":").append(NL);
        code.append(indentBlock(visit(node.getChild(1))));
        code.append(labelEnd).append(":").append(NL);

        return code.toString();
    }

    // ── 3.1.2 While ──────────────────────────────────────────────────────────

    private String visitWhileStmt(JmmNode node, Void unused) {
        String labelLoop = ollirTypes.nextLabel("whileLoop_");
        String labelBody = ollirTypes.nextLabel("whileBody_");
        String labelEnd  = ollirTypes.nextLabel("whileEnd_");

        StringBuilder code = new StringBuilder();
        code.append(labelLoop).append(":").append(NL);

        var cond = exprVisitor.visit(node.getChild(0));
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(labelBody).append(END_STMT);
        code.append("goto ").append(labelEnd).append(END_STMT);

        code.append(labelBody).append(":").append(NL);
        code.append(indentBlock(visit(node.getChild(1))));
        code.append("goto ").append(labelLoop).append(END_STMT);

        code.append(labelEnd).append(":").append(NL);
        return code.toString();
    }

    // ── Other statements ──────────────────────────────────────────────────────

    private String visitAssignStmt(JmmNode node, Void unused) {
        String varName = node.get("var");
        JmmType varType = resolveVarType(varName);
        String typeStr = ollirTypes.toOllirType(varType);

        exprVisitor.expectedReturnType = varType;
        var rhs = exprVisitor.visit(node.getChild(0));
        exprVisitor.expectedReturnType = null;
        StringBuilder code = new StringBuilder();
        code.append(rhs.getComputation());

        if (isField(varName)) {
            code.append("putfield(this.").append(table.getClassName()).append(", ")
                    .append(ollirTypes.sanitizeId(varName)).append(typeStr).append(", ")
                    .append(rhs.getCode()).append(").V").append(END_STMT);
        } else {
            code.append(ollirTypes.sanitizeId(varName)).append(typeStr)
                    .append(SPACE).append(ASSIGN).append(typeStr).append(SPACE)
                    .append(rhs.getCode()).append(END_STMT);
        }

        return code.toString();
    }

    private String visitArrayStoreStmt(JmmNode node, Void unused) {
        String varName = node.get("name");
        int numChildren = node.getNumChildren();

        var index = exprVisitor.visit(node.getChild(0));
        var value = exprVisitor.visit(node.getChild(numChildren - 1));

        StringBuilder code = new StringBuilder();
        code.append(index.getComputation());
        code.append(value.getComputation());

        JmmType arrType = resolveVarType(varName);
        JmmType elemType = (arrType instanceof pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType arr)
                ? inferArrayElemType(arr)
                : JmmPrimitiveType.INT;
        String elemTypeStr = ollirTypes.toOllirType(elemType);

        String arrayVarCode;
        if (isField(varName)) {
            String arrOllirType = ollirTypes.toOllirType(arrType);
            String tmpArr = ollirTypes.nextTemp() + arrOllirType;
            code.append(tmpArr).append(SPACE).append(ASSIGN).append(arrOllirType).append(SPACE)
                    .append("getfield(this.").append(table.getClassName()).append(", ")
                    .append(ollirTypes.sanitizeId(varName)).append(arrOllirType)
                    .append(")").append(arrOllirType).append(END_STMT);
            arrayVarCode = tmpArr;
        } else {
            arrayVarCode = ollirTypes.sanitizeId(varName) + ".array" + elemTypeStr;
        }

        code.append(arrayVarCode)
                .append("[").append(index.getCode()).append("]").append(elemTypeStr)
                .append(SPACE).append(ASSIGN).append(elemTypeStr).append(SPACE)
                .append(value.getCode()).append(END_STMT);

        return code.toString();
    }

    private String visitBlock(JmmNode node, Void unused) {
        return node.getChildren().stream().map(this::visit).collect(Collectors.joining());
    }

    private String visitExprStmt(JmmNode node, Void unused) {
        return exprVisitor.visit(node.getChild(0)).getComputation();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JmmType inferArrayElemType(pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType arr) {
        String s = arr.toString();
        if (s.contains("BOOLEAN")) return JmmPrimitiveType.BOOLEAN;
        return JmmPrimitiveType.INT;
    }

    private boolean isField(String name) {
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return false;
            if (currentMethod.getLocalVariable(name).isPresent()) return false;
        }
        return table.getField(name).isPresent();
    }

    private JmmType resolveVarType(String name) {
        if (currentMethod != null) {
            var p = currentMethod.getParameter(name);
            if (p.isPresent()) return p.get().type();
            var l = currentMethod.getLocalVariable(name);
            if (l.isPresent()) return l.get().type();
        }
        var f = table.getField(name);
        if (f.isPresent()) return f.get().type();
        return JmmPrimitiveType.INT;
    }

    private String indentBlock(String block) {
        if (block == null || block.isBlank()) return block;
        return block.lines()
                .map(l -> l.isBlank() ? l : "   " + l)
                .collect(Collectors.joining(NL, "", NL));
    }
}