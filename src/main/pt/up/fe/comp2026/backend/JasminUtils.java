package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.type.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static org.specs.comp.ollir.OperationType.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.HashMap;
import java.util.Map;

public class JasminUtils {

    private final OllirResult ollirResult;
    private final Map<String, String> fullClassnames;

    public JasminUtils(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        fullClassnames = new HashMap<>();
        fullClassnames.put("this", ollirResult.getOllirClass().getClassName());

        for (var fullImport : ollirResult.getOllirClass().getImports()) {
            var parts = fullImport.split("\\.");
            var key = parts[parts.length - 1];
            fullClassnames.put(key, fullImport.replace('.', '/'));
        }
    }

    /**
     * Returns the JVM type prefix for load/store instructions.
     * INT32 and BOOLEAN → "i", references/arrays → "a", VOID → ""
     */
    public String getTypePrefix(Type type) {
        if (type instanceof BuiltinType bt) {
            return switch (bt.getKind()) {
                case INT32, BOOLEAN -> "i";
                case VOID -> "";
                case STRING -> "a";
            };
        }
        if (type instanceof ClassType || type instanceof ArrayType) {
            return "a";
        }
        throw new RuntimeException("Unknown type for prefix: " + type);
    }

    /**
     * Returns the JVM type descriptor (e.g. "I", "Z", "V", "Ljava/lang/String;", "[I").
     */
    public String getTypeDescriptor(Type type) {
        if (type instanceof BuiltinType bt) {
            return switch (bt.getKind()) {
                case INT32 -> "I";
                case BOOLEAN -> "Z";
                case VOID -> "V";
                case STRING -> "Ljava/lang/String;";
            };
        }
        if (type instanceof ArrayType at) {
            return "[".repeat(Math.max(1, at.getNumDimensions())) + getTypeDescriptor(at.getElementType());
        }
        if (type instanceof ClassType ct) {
            return "L" + resolveClassName(ct.getName()) + ";";
        }
        throw new RuntimeException("Unknown type for descriptor: " + type);
    }

    /**
     * Resolves a simple or qualified class name to JVM path format (slashes).
     * Checks the imports map first, then tries java.lang, then uses the name as-is.
     */
    public String resolveClassName(String name) {
        // Already fully qualified with slashes
        if (name.contains("/")) return name;

        // Check imports map (simple name → full path)
        var fromImports = fullClassnames.get(name);
        if (fromImports != null) return fromImports;

        // Try dots-to-slashes for dotted names (e.g. "java.lang.Object")
        if (name.contains(".")) return name.replace('.', '/');

        // Fallback: try java.lang package
        try {
            Class.forName("java.lang." + name);
            return "java/lang/" + name;
        } catch (ClassNotFoundException ignored) {}

        // Use as-is (might be the current class or an unresolved name)
        String pkg = ollirResult.getOllirClass().getPackage();
        if (pkg != null && !pkg.isBlank()) {
            return pkg.replace('.', '/') + "/" + name;
        }
        return name;
    }

    /**
     * Returns the Jasmin modifier string for a method/class access modifier.
     * DEFAULT is treated as public for Jasmin compatibility.
     */
    public String getModifier(AccessModifier accessModifier) {
        return switch (accessModifier) {
            case DEFAULT, PUBLIC -> "public ";
            case PRIVATE -> "private ";
            case PROTECTED -> "protected ";
        };
    }

    public String getLoad(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();
        if (prefix.isEmpty()) return "";
        if (value <= 3) return prefix + "load_" + value;
        return prefix + "load " + value;
    }

    public String getStore(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();
        if (prefix.isEmpty()) return "";
        if (value <= 3) return prefix + "store_" + value;
        return prefix + "store " + value;
    }

    public boolean isZeroLiteral(Element elem) {
        return elem instanceof LiteralElement lit && lit.getLiteral().equals("0");
    }

    public boolean isComparisonOp(OperationType op) {
        return switch (op) {
            case LTH, GTH, EQ, NEQ, LTE, GTE -> true;
            default -> false;
        };
    }

    public String zeroCompareBranch(OperationType opType, boolean reversed) {
        return switch (opType) {
            case LTH -> reversed ? "ifgt" : "iflt";
            case GTH -> reversed ? "iflt" : "ifgt";
            case EQ  -> "ifeq";
            case NEQ -> "ifne";
            case LTE -> reversed ? "ifge" : "ifle";
            case GTE -> reversed ? "ifle" : "ifge";
            default  -> throw new NotImplementedException(opType);
        };
    }

    public String twoValueCompareBranch(OperationType opType) {
        return switch (opType) {
            case LTH -> "if_icmplt";
            case GTH -> "if_icmpgt";
            case EQ  -> "if_icmpeq";
            case NEQ -> "if_icmpne";
            case LTE -> "if_icmple";
            case GTE -> "if_icmpge";
            default  -> throw new NotImplementedException(opType);
        };
    }
}