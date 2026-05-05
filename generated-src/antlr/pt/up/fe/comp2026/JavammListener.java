// Generated from pt/up/fe/comp2026/Javamm.g4 by ANTLR 4.5.3

    package pt.up.fe.comp2026;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JavammParser}.
 */
public interface JavammListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JavammParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(JavammParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(JavammParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#stmtEntry}.
	 * @param ctx the parse tree
	 */
	void enterStmtEntry(JavammParser.StmtEntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#stmtEntry}.
	 * @param ctx the parse tree
	 */
	void exitStmtEntry(JavammParser.StmtEntryContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(JavammParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(JavammParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void enterImportDecl(JavammParser.ImportDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void exitImportDecl(JavammParser.ImportDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#packageDecl}.
	 * @param ctx the parse tree
	 */
	void enterPackageDecl(JavammParser.PackageDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#packageDecl}.
	 * @param ctx the parse tree
	 */
	void exitPackageDecl(JavammParser.PackageDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void enterClassDecl(JavammParser.ClassDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void exitClassDecl(JavammParser.ClassDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#classMember}.
	 * @param ctx the parse tree
	 */
	void enterClassMember(JavammParser.ClassMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#classMember}.
	 * @param ctx the parse tree
	 */
	void exitClassMember(JavammParser.ClassMemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#fieldDecl}.
	 * @param ctx the parse tree
	 */
	void enterFieldDecl(JavammParser.FieldDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#fieldDecl}.
	 * @param ctx the parse tree
	 */
	void exitFieldDecl(JavammParser.FieldDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(JavammParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(JavammParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(JavammParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(JavammParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(JavammParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(JavammParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void enterMethodDecl(JavammParser.MethodDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void exitMethodDecl(JavammParser.MethodDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(JavammParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(JavammParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Block}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterBlock(JavammParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Block}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitBlock(JavammParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ForStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterForStmt(JavammParser.ForStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ForStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitForStmt(JavammParser.ForStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code WhileStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhileStmt(JavammParser.WhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code WhileStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhileStmt(JavammParser.WhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DoWhileStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterDoWhileStmt(JavammParser.DoWhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DoWhileStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitDoWhileStmt(JavammParser.DoWhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfElseStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterIfElseStmt(JavammParser.IfElseStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfElseStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitIfElseStmt(JavammParser.IfElseStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterIfStmt(JavammParser.IfStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitIfStmt(JavammParser.IfStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterAssignStmt(JavammParser.AssignStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitAssignStmt(JavammParser.AssignStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayStoreStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterArrayStoreStmt(JavammParser.ArrayStoreStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayStoreStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitArrayStoreStmt(JavammParser.ArrayStoreStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ReturnStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(JavammParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ReturnStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(JavammParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(JavammParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprStmt}
	 * labeled alternative in {@link JavammParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(JavammParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#forInit}.
	 * @param ctx the parse tree
	 */
	void enterForInit(JavammParser.ForInitContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#forInit}.
	 * @param ctx the parse tree
	 */
	void exitForInit(JavammParser.ForInitContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#forCond}.
	 * @param ctx the parse tree
	 */
	void enterForCond(JavammParser.ForCondContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#forCond}.
	 * @param ctx the parse tree
	 */
	void exitForCond(JavammParser.ForCondContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavammParser#forUpdate}.
	 * @param ctx the parse tree
	 */
	void enterForUpdate(JavammParser.ForUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavammParser#forUpdate}.
	 * @param ctx the parse tree
	 */
	void exitForUpdate(JavammParser.ForUpdateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PlusPlusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusPlusExpr(JavammParser.PlusPlusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PlusPlusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusPlusExpr(JavammParser.PlusPlusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LengthExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLengthExpr(JavammParser.LengthExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LengthExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLengthExpr(JavammParser.LengthExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BinaryExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpr(JavammParser.BinaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BinaryExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpr(JavammParser.BinaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(JavammParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(JavammParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayLoadExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterArrayLoadExpr(JavammParser.ArrayLoadExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayLoadExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitArrayLoadExpr(JavammParser.ArrayLoadExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MinusMinusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMinusMinusExpr(JavammParser.MinusMinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MinusMinusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMinusMinusExpr(JavammParser.MinusMinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PlusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusExpr(JavammParser.PlusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PlusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusExpr(JavammParser.PlusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BoolLiteral}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolLiteral(JavammParser.BoolLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BoolLiteral}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolLiteral(JavammParser.BoolLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NewArrayExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNewArrayExpr(JavammParser.NewArrayExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NewArrayExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNewArrayExpr(JavammParser.NewArrayExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarRefExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVarRefExpr(JavammParser.VarRefExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarRefExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVarRefExpr(JavammParser.VarRefExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NewExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNewExpr(JavammParser.NewExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NewExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNewExpr(JavammParser.NewExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ImplicitThisCallExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterImplicitThisCallExpr(JavammParser.ImplicitThisCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ImplicitThisCallExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitImplicitThisCallExpr(JavammParser.ImplicitThisCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldAccessExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccessExpr(JavammParser.FieldAccessExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldAccessExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccessExpr(JavammParser.FieldAccessExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(JavammParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(JavammParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ThisExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterThisExpr(JavammParser.ThisExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ThisExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitThisExpr(JavammParser.ThisExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntegerLiteral}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(JavammParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntegerLiteral}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(JavammParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayInitializer}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitializer(JavammParser.ArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayInitializer}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitializer(JavammParser.ArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MinusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMinusExpr(JavammParser.MinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MinusExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMinusExpr(JavammParser.MinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodCallExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallExpr(JavammParser.MethodCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodCallExpr}
	 * labeled alternative in {@link JavammParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallExpr(JavammParser.MethodCallExprContext ctx);
}