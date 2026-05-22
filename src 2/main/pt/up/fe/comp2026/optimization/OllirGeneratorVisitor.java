package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
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
        addVisit(PROGRAM,       this::visitProgram);
        addVisit(PACKAGE_DECL,  this::visitPackageDecl);
        addVisit(CLASS_DECL,    this::visitClass);
        addVisit(VAR_DECL,      this::visitVarDecl);
        addVisit(PARAM,         this::visitParam);
        addVisit(METHOD_DECL,   this::visitMethodDecl);

        // ── 3.1.2 Statements ────────────────────────────────────────────
        addVisit(RETURN_STMT,   this::visitReturn);
        addVisit(IF_ELSE_STMT,  this::visitIfElseStmt);
        addVisit(IF_STMT,       this::visitIfStmt);
        addVisit(WHILE_STMT,    this::visitWhileStmt);
        addVisit(ASSIGN_STMT,   this::visitAssignStmt);
        addVisit(BLOCK,         this::visitBlock);
        addVisit(EXPR_STMT,     this::visitExprStmt);
        addVisit(ARRAY_STORE_STMT, this::visitArrayStoreStmt);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Programme / class structure
    // ══════════════════════════════════════════════════════════════════════

    private String visitProgram(JmmNode node, Void unused) {
        // Emit imports first (needed by OLLIR parser), then the rest
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
        return ""; // package is not emitted in OLLIR
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

        // Emit field declarations
        for (var field : table.getFields()) {
            code.append(".field public ")
                    .append(ollirTypes.sanitizeId(field.name()))
                    .append(ollirTypes.toOllirType(field.type()))
                    .append(";\n");
        }
        code.append(NL);

        code.append(buildConstructor()).append(NL);

        for (var classMember : node.getChildren(CLASS_MEMBER)) {
            for (var method : classMember.getChildren(METHOD_DECL)) {
                code.append(visit(method));
            }
        }

        code.append(R_BRACKET);
        return code.toString();
    }

    private String buildConstructor() {
        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Method structure
    // ══════════════════════════════════════════════════════════════════════

    private String visitVarDecl(JmmNode varDecl, Void unused) {
        return varDecl.get("name") +
                ollirTypes.toOllirType(varDecl.getObject("typeNode", JmmNode.class)) + ";";
    }

    private String visitParam(JmmNode param, Void unused) {
        return param.get("name") +
                ollirTypes.toOllirType(param.getObject("typeNode", JmmNode.class));
    }

    private String visitMethodDecl(JmmNode node, Void unused) {
        // Robust lookup — orElseThrow() crashes if signature doesn't match exactly
        var sig = TypeUtils.with(table).getMethodDeclSignature(node);
        var methodOpt = table.getMethod(sig);
        if (methodOpt.isEmpty()) {
            // Fallback: match by name alone
            String methodName = node.get("name");
            var byName = table.getMethods(methodName);
            if (byName.isEmpty()) return ""; // unknown method, skip
            currentMethod = byName.get(0);
        } else {
            currentMethod = methodOpt.get();
        }
        exprVisitor.currentMethod = currentMethod;

        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = NodeUtils.getBooleanAttribute(node, "isPublic", "false");
        if (isPublic) code.append("public ");

        if (node.getObject("isStatic", Boolean.class)) code.append("static ");

        code.append(ollirTypes.sanitizeId(node.get("name")));

        // Parameters — separated by commas, wrapped in ()
        var params = node.getChildren(PARAM);
        String paramsCode = params.stream()
                .map(p -> visitParam(p, null))
                .collect(Collectors.joining(", "));
        code.append("(").append(paramsCode).append(")");

        // Return type
        code.append(ollirTypes.toOllirType(currentMethod.returnType()));
        code.append(L_BRACKET);

        // Statements — skip VAR_DECL and PARAM children, visit everything else
        for (var child : node.getChildren()) {
            if (VAR_DECL.check(child) || PARAM.check(child)) continue;
            // Skip the return-type node (stored as object attribute, but may also appear as child)
            if (TYPE.check(child)) continue;
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

    // ══════════════════════════════════════════════════════════════════════
    // 3.1.2 — Return statement
    // ══════════════════════════════════════════════════════════════════════

    private String visitReturn(JmmNode node, Void unused) {
        JmmType retType = currentMethod.returnType();

        // void return
        if (JmmPrimitiveType.VOID.equals(retType)) {
            return "ret.V;\n";
        }

        // non-void: evaluate the expression
        var expr = exprVisitor.visit(node.getChild(0));
        StringBuilder code = new StringBuilder();
        code.append(expr.getComputation());
        code.append("ret").append(ollirTypes.toOllirType(retType))
                .append(SPACE).append(expr.getCode()).append(END_STMT);
        return code.toString();
    }

    // ══════════════════════════════════════════════════════════════════════
    // 3.1.2 — Conditional instructions
    //
    // OLLIR if-else pattern:
    //
    //   if (cond) goto labelTrue;
    //   <else branch>
    //   goto labelEnd;
    //   labelTrue:
    //   <then branch>
    //   labelEnd:
    //
    // This pattern keeps the "fall-through" path as the else branch, which
    // is the conventional OLLIR/JVM style (condition is tested and we jump
    // to the TRUE branch; otherwise we fall through to the false/else branch).
    // ══════════════════════════════════════════════════════════════════════

    /**
     * if (cond) thenStmt else elseStmt
     *
     * Grammar (IfElseStmt): child 0 = cond, child 1 = then stmt, child 2 = else stmt
     */
    private String visitIfElseStmt(JmmNode node, Void unused) {
        String labelTrue = ollirTypes.nextLabel("ifTrue_");
        String labelEnd  = ollirTypes.nextLabel("ifEnd_");

        var cond = exprVisitor.visit(node.getChild(0));

        StringBuilder code = new StringBuilder();
        code.append(cond.getComputation());

        // Jump to then-branch if condition is true
        code.append("if (").append(cond.getCode()).append(") goto ").append(labelTrue).append(END_STMT);

        // Else branch (fall-through)
        String elseCode = visit(node.getChild(2));
        code.append(indentBlock(elseCode));
        code.append("goto ").append(labelEnd).append(END_STMT);

        // Then branch
        code.append(labelTrue).append(":").append(NL);
        String thenCode = visit(node.getChild(1));
        code.append(indentBlock(thenCode));

        // End label
        code.append(labelEnd).append(":").append(NL);

        return code.toString();
    }

    /**
     * if (cond) thenStmt       (no else)
     *
     * OLLIR pattern:
     *   if (cond) goto labelTrue;
     *   goto labelEnd;
     *   labelTrue:
     *   <then branch>
     *   labelEnd:
     */
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

    // ══════════════════════════════════════════════════════════════════════
    // 3.1.2 — While loop
    //
    // OLLIR while pattern:
    //
    //   whileLoop:
    //   if (cond) goto whileBody;
    //   goto whileEnd;
    //   whileBody:
    //   <body>
    //   goto whileLoop;
    //   whileEnd:
    // ══════════════════════════════════════════════════════════════════════

    /**
     * while (cond) body
     *
     * Grammar (WhileStmt): child 0 = cond, child 1 = body stmt
     * (Optional child 2 = else stmt for the non-standard while-else extension,
     *  currently not generated by CP2 core.)
     */
    private String visitWhileStmt(JmmNode node, Void unused) {
        String labelLoop = ollirTypes.nextLabel("whileLoop_");
        String labelBody = ollirTypes.nextLabel("whileBody_");
        String labelEnd  = ollirTypes.nextLabel("whileEnd_");

        StringBuilder code = new StringBuilder();

        // ── Loop header ──
        code.append(labelLoop).append(":").append(NL);

        var cond = exprVisitor.visit(node.getChild(0));
        code.append(cond.getComputation());

        // If condition true → enter body; otherwise → exit
        code.append("if (").append(cond.getCode()).append(") goto ").append(labelBody).append(END_STMT);
        code.append("goto ").append(labelEnd).append(END_STMT);

        // ── Loop body ──
        code.append(labelBody).append(":").append(NL);
        code.append(indentBlock(visit(node.getChild(1))));
        code.append("goto ").append(labelLoop).append(END_STMT);

        // ── Exit ──
        code.append(labelEnd).append(":").append(NL);

        return code.toString();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Other statements
    // ══════════════════════════════════════════════════════════════════════

    private String visitAssignStmt(JmmNode node, Void unused) {
        String varName = node.get("var");
        JmmType varType = resolveVarType(varName);
        String typeStr = ollirTypes.toOllirType(varType);

        var rhs = exprVisitor.visit(node.getChild(0));
        StringBuilder code = new StringBuilder();
        code.append(rhs.getComputation());

        // Field assignment → putfield
        if (isField(varName)) {
            String className = table.getClassName();
            code.append("putfield(this.").append(className).append(", ")
                    .append(ollirTypes.sanitizeId(varName)).append(typeStr).append(", ")
                    .append(rhs.getCode()).append(").V").append(END_STMT);
        } else {
            code.append(ollirTypes.sanitizeId(varName)).append(typeStr)
                    .append(SPACE).append(ASSIGN).append(typeStr).append(SPACE)
                    .append(rhs.getCode()).append(END_STMT);
        }

        return code.toString();
    }

    /** array[idx] = value */
    private String visitArrayStoreStmt(JmmNode node, Void unused) {
        String varName = node.get("name");
        int numChildren = node.getNumChildren();

        // Last child is the value; all others are index expressions
        StringBuilder code = new StringBuilder();

        // For 1D arrays: children = [index, value]
        var index = exprVisitor.visit(node.getChild(0));
        var value = exprVisitor.visit(node.getChild(numChildren - 1));

        code.append(index.getComputation());
        code.append(value.getComputation());

        // Element type (strip one array dimension)
        JmmType arrType = resolveVarType(varName);
        JmmType elemType = (arrType instanceof pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType arr)
                ? inferArrayElemType(arr)
                : JmmPrimitiveType.INT;
        String elemTypeStr = ollirTypes.toOllirType(elemType);

        code.append(ollirTypes.sanitizeId(varName)).append(".array").append(elemTypeStr)
                .append("[").append(index.getCode()).append("]").append(elemTypeStr)
                .append(SPACE).append(ASSIGN).append(elemTypeStr).append(SPACE)
                .append(value.getCode()).append(END_STMT);

        return code.toString();
    }

    /** { stmt* } */
    private String visitBlock(JmmNode node, Void unused) {
        return node.getChildren()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining());
    }

    /** expr; */
    private String visitExprStmt(JmmNode node, Void unused) {
        var result = exprVisitor.visit(node.getChild(0));
        // The computation already emits any side-effects; the code part (if any)
        // is a dangling value — we just discard it.
        return result.getComputation();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Infers the element type of a JmmArrayType without calling getItemType()
     * (which is not available in this API version).
     */
    private JmmType inferArrayElemType(pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType arr) {
        String s = arr.toString();
        if (s.contains("BOOLEAN")) return JmmPrimitiveType.BOOLEAN;
        return JmmPrimitiveType.INT;
    }

    /** Returns true if {@code name} is a class field (not a local or parameter). */
    private boolean isField(String name) {
        if (currentMethod != null) {
            if (currentMethod.getParameter(name).isPresent()) return false;
            if (currentMethod.getLocalVariable(name).isPresent()) return false;
        }
        return table.getField(name).isPresent();
    }

    /**
     * Resolves the JmmType of a local variable / parameter / field.
     * Falls back to int if not found (should not happen after semantic analysis).
     */
    private JmmType resolveVarType(String name) {
        if (currentMethod != null) {
            var p = currentMethod.getParameter(name);
            if (p.isPresent()) return p.get().type();
            var l = currentMethod.getLocalVariable(name);
            if (l.isPresent()) return l.get().type();
        }
        var f = table.getField(name);
        if (f.isPresent()) return f.get().type();
        return JmmPrimitiveType.INT; // safe fallback
    }

    /**
     * Adds three-space indentation to each non-empty line of a block of OLLIR code.
     * Used to keep generated output readable inside if/while bodies.
     */
    private String indentBlock(String block) {
        if (block == null || block.isBlank()) return block;
        return block.lines()
                .map(l -> l.isBlank() ? l : "   " + l)
                .collect(Collectors.joining(NL, "", NL));
    }
}

