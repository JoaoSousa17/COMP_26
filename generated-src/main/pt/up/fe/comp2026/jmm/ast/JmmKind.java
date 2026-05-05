package pt.up.fe.comp2026.jmm.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.Kind;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum JmmKind implements Kind {
    PROGRAM("Program"),
    PACKAGE_DECL("PackageDecl"),
    IMPORT_DECL("ImportDecl"),
    CLASS_DECL("ClassDecl"),
    STMT_ENTRY("StmtEntry"),
    STMT("Stmt"),
    EXPRESSION("Expression"),
    EXPR("Expr"),
    CLASS_MEMBER("ClassMember"),
    FIELD_DECL("FieldDecl"),
    METHOD_DECL("MethodDecl"),
    TYPE("Type"),
    VAR_DECL("VarDecl"),
    PARAM("Param"),
    ASSIGNMENT("Assignment"),
    BLOCK("Block",STMT),
    FOR_STMT("ForStmt",STMT),
    FOR_INIT("ForInit"),
    FOR_COND("ForCond"),
    FOR_UPDATE("ForUpdate"),
    WHILE_STMT("WhileStmt",STMT),
    DO_WHILE_STMT("DoWhileStmt",STMT),
    IF_ELSE_STMT("IfElseStmt",STMT),
    IF_STMT("IfStmt",STMT),
    ASSIGN_STMT("AssignStmt",STMT),
    ARRAY_STORE_STMT("ArrayStoreStmt",STMT),
    RETURN_STMT("ReturnStmt",STMT),
    EXPR_STMT("ExprStmt",STMT),
    PAREN_EXPR("ParenExpr",EXPR),
    IMPLICIT_THIS_CALL_EXPR("ImplicitThisCallExpr",EXPR),
    THIS_EXPR("ThisExpr",EXPR),
    NEW_EXPR("NewExpr",EXPR),
    ARRAY_INITIALIZER("ArrayInitializer",EXPR),
    NEW_ARRAY_EXPR("NewArrayExpr",EXPR),
    PLUS_PLUS_EXPR("PlusPlusExpr",EXPR),
    MINUS_MINUS_EXPR("MinusMinusExpr",EXPR),
    PLUS_EXPR("PlusExpr",EXPR),
    MINUS_EXPR("MinusExpr",EXPR),
    UNARY_EXPR("UnaryExpr",EXPR),
    INTEGER_LITERAL("IntegerLiteral",EXPR),
    BOOL_LITERAL("BoolLiteral",EXPR),
    VAR_REF_EXPR("VarRefExpr",EXPR),
    BINARY_EXPR("BinaryExpr",EXPR),
    LENGTH_EXPR("LengthExpr",EXPR),
    METHOD_CALL_EXPR("MethodCallExpr",EXPR),
    FIELD_ACCESS_EXPR("FieldAccessExpr",EXPR),
    ARRAY_LOAD_EXPR("ArrayLoadExpr",EXPR);
    final String key;
    final JmmKind extend;
    JmmKind(String key, JmmKind extend){this.key = key; this.extend = extend;}
    JmmKind(String key){this(key,null);}

    @Override
    public String getKey() {return key;}

    @Override
    public Optional<Kind> extend() {return Optional.ofNullable(extend);}

    public static Optional<JmmKind> fromString(String kind) {
        for (JmmKind k : JmmKind.values()) {
            if (k.key.equals(kind)) {
                return Optional.of(k);
            }
        }
        return Optional.empty();
    }

    /**
     * Performs a check on all kinds to test and returns false if none matches. Otherwise, returns true.
     *
     * @param node
     * @param kindsToTest
     * @return
     */
    public static boolean check(JmmNode node, Kind... kindsToTest) {

        for (Kind k : kindsToTest) {

            // if any matches, return successfully
            if (k.check(node)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Performs a check an all kinds to test and throws if none matches. Otherwise, does nothing.
     *
     * @param node
     * @param kindsToTest
     */
    public static void checkOrThrow(JmmNode node, Kind... kindsToTest) {
        if (!check(node, kindsToTest)) {
            // throw if none matches
            throw new RuntimeException("Node '" + node + "' is not any of " + Arrays.asList(kindsToTest));
        }
    }
}