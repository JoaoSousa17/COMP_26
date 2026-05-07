package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;

import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {

    private static final Set<String> OLLIR_KEYWORDS = Set.of(
            "bool", "i32", "V", "array", "String", "final", "goto", "if", "import",
            "interface", "new", "package", "private", "protected", "public", "ret",
            "static", "this", "ldc", "invokespecial", "invokevirtual", "invokestatic",
            "arraylength", "getfield", "putfield", "getstatic", "putstatic",
            ".method", ".construct", ".field", "extends"
    );

    private final AccumulatorMap<String> temporaries;
    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
    }

    public String nextTemp() {
        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {
        var nextTempNum = temporaries.add(prefix) - 1;
        return prefix + nextTempNum;
    }

    public String nextLabel(String prefix) {
        var nextLabelNum = temporaries.add(prefix) - 1;
        return prefix + nextLabelNum;
    }

    public String toOllirType(JmmNode typeNode) {
        TYPE.checkOrThrow(typeNode);
        return toOllirType(types.convertType(typeNode));
    }

    /**
     * Converts a JmmType to its OLLIR type suffix (e.g. ".i32", ".bool", ".V",
     * ".array.i32", ".array.array.i32", ".ClassName").
     */
    public String toOllirType(JmmType type) {
        if (type instanceof JmmPrimitiveType prim) {
            return "." + toPrimitiveOllirType(prim);
        }
        if (type instanceof JmmArrayType arr) {
            // Recursively build the type: one ".array" per dimension, then the base type
            String baseType = toOllirType(arr.itemType());
            return ".array".repeat(arr.dimension()) + baseType;
        }
        if (type instanceof JmmClassType cls) {
            String name = cls.name();
            int dot = name.lastIndexOf('.');
            return "." + (dot >= 0 ? name.substring(dot + 1) : name);
        }
        throw new RuntimeException("Cannot convert type to OLLIR: " + type);
    }

    private String toPrimitiveOllirType(JmmPrimitiveType type) {
        return switch (type.name().toLowerCase()) {
            case "int"     -> "i32";
            case "boolean" -> "bool";
            case "void"    -> "V";
            default        -> throw new RuntimeException("Unknown primitive type: " + type.name());
        };
    }

    public String sanitizeId(String id) {
        if (OLLIR_KEYWORDS.contains(id)) {
            return '"' + id + '"';
        }
        if (id.startsWith("$")) {
            return '"' + id + '"';
        }
        return id;
    }
}
