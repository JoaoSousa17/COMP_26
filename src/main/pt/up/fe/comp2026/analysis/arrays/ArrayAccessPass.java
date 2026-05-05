package pt.up.fe.comp2026.analysis.arrays;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.AnalysisVisitorWithTable;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class ArrayAccessPass extends AnalysisVisitorWithTable {

    private MethodSymbol currentMethod;

    public ArrayAccessPass(SymbolTable table) {
        super(table);
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL,     this::visitMethodDecl);
        addVisit(JmmKind.ARRAY_LOAD_EXPR, this::visitArrayLoad);
        addVisit(JmmKind.ARRAY_STORE_STMT,this::visitArrayStore);
        addVisit(JmmKind.NEW_ARRAY_EXPR,  this::visitNewArrayExpr);
        addVisit(JmmKind.LENGTH_EXPR,     this::visitLengthExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = this.table.getMethod(types.getMethodDeclSignature(method)).orElse(null);
        return null;
    }

    // validates: expr[i][j]..
    private Void visitArrayLoad(JmmNode node, SymbolTable table) {
        JmmNode target = node.getChild(0);
        JmmType targetType = types.getExprType(target, currentMethod);

        if (targetType == null) return null; // unresolved: skip

        if (!(targetType instanceof JmmArrayType)) {
            addReport(newError(target, "Array access requires an array type, got '" + targetType +"'"));
            return null;
        }

        // validate each index (children 1..n) must be int
        for (int i = 1; i < node.getNumChildren(); i++) {
            JmmNode indexExpr = node.getChild(i);
            JmmType indexType = types.getExprType(indexExpr, currentMethod);
            if (indexType != null && !indexType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(indexExpr, "Array index must be int, got '" + indexType + "'"));
            }
        }
        return null;
    }

    // validates: name[i][j].. = expr; (writing to array)
    private Void visitArrayStore(JmmNode node, SymbolTable table) {
        String varName = node.get("name");
        JmmType varType = lookupVarType(varName);

        if (varType == null) return null; // undeclared

        if (!(varType instanceof JmmArrayType arrType)) {
            addReport(newError(node, "Array store requires an array type, got '" + varType + "'"));
            return  null;
        }

        int numChildren = node.getNumChildren();
        int numIndices = numChildren - 1; // last child is the value

        // validate each index must be int
        for (int i = 0; i < numIndices; i++) {
            JmmNode indexExpr = node.getChild(i);
            JmmType indexType = types.getExprType(indexExpr, currentMethod);

            if (indexType != null && !indexType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(indexExpr, "Array index must be int, but got '" + indexType + "'"));
            }
        }

        // validate value type matches element type
        JmmNode valueExpr = node.getChild(numChildren - 1);
        JmmType valueType = types.getExprType(valueExpr, currentMethod);

        if (valueType == null) return null;

        int remainingDims = arrType.dimension() - numIndices;
        JmmType elementType = remainingDims <= 1
                ? arrType.itemType()
                : new JmmArrayType(arrType.itemType(), remainingDims);

        if (!types.isTypeCompatible(elementType, valueType)) {
            addReport(newError(valueExpr, "Type mismatch in array store: expected '" + elementType + "', got '" + valueType + "'"));
        }
        return null;
    }

    // new int[expr][expr?]... — each size expression must be int
    private Void visitNewArrayExpr(JmmNode node, SymbolTable table) {
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode sizeExpr = node.getChild(i);
            JmmType sizeType = types.getExprType(sizeExpr, currentMethod);
            if (sizeType != null && !sizeType.equals(JmmPrimitiveType.INT)) {
                addReport(newError(sizeExpr, "Array size must be int, got '" + sizeType + "'"));
            }
        }
        return null;
    }

    // expr.length — expr must be an array
    private Void visitLengthExpr(JmmNode node, SymbolTable table) {
        JmmNode target = node.getChild(0);
        JmmType targetType = types.getExprType(target, currentMethod);
        if (targetType == null) return null;
        if (!(targetType instanceof JmmArrayType)) {
            addReport(newError(target, "Cannot access .length on non-array type '" + targetType + "'"));
        }
        return null;
    }

    private JmmType lookupVarType(String name) {
        if (currentMethod != null) {
            var param = currentMethod.getParameter(name);
            if (param.isPresent()) return param.get().type();

            var local = currentMethod.getLocalVariable(name);
            if (local.isPresent()) return local.get().type();
        }
        return table.getField(name).map(Symbol::type).orElse(null);
    }

}
