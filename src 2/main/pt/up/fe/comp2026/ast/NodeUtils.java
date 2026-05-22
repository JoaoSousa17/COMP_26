package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Utility class for reading common attributes from AST nodes.
 * Provides typed accessors for position, integer, and boolean attributes,
 * all with configurable defaults for missing values.
 */
public class NodeUtils {

    /**
     * Returns the source line number of the given node.
     * Returns -1 if the attribute is absent.
     */
    public static int getLine(JmmNode node) {
        return getIntegerAttribute(node, "lineStart", "-1");
    }

    /**
     * Returns the source column number of the given node.
     * Returns -1 if the attribute is absent.
     */
    public static int getColumn(JmmNode node) {
        return getIntegerAttribute(node, "colStart", "-1");
    }

    /**
     * Reads a named attribute from a node and parses it as an integer.
     *
     * @param node       the AST node to read from.
     * @param attribute  the attribute name.
     * @param defaultVal string representation of the default value if the attribute is absent.
     * @return the integer value of the attribute, or the parsed default.
     */
    public static int getIntegerAttribute(JmmNode node, String attribute, String defaultVal) {
        String line = node.getOptional(attribute).orElse(defaultVal);
        return Integer.parseInt(line);
    }

    /**
     * Reads a named attribute from a node and parses it as a boolean.
     *
     * @param node       the AST node to read from.
     * @param attribute  the attribute name.
     * @param defaultVal string representation of the default value if the attribute is absent.
     * @return the boolean value of the attribute, or the parsed default.
     */
    public static boolean getBooleanAttribute(JmmNode node, String attribute, String defaultVal) {
        String line = node.getOptional(attribute).orElse(defaultVal);
        return Boolean.parseBoolean(line);
    }
}
