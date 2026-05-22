package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for Java-- symbol table implementations.
 *
 * Provides a general-purpose key-value attribute store shared by all
 * concrete symbol table implementations, and holds a reference to the
 * {@link Importer} used for resolving external class symbol tables.
 */
public abstract class AJmmSymbolTable implements SymbolTable {

    /** General-purpose attribute store for compiler extensions. */
    private final Map<String, Object> attrs;

    /** Resolves symbol tables for imported and implicit-import classes. */
    protected Importer importer;

    public AJmmSymbolTable(Importer importer) {
        this.attrs = new HashMap<>();
        this.importer = importer;
    }

    @Override
    public Collection<String> getAttributes() {
        return attrs.keySet();
    }

    /**
     * Retrieves a stored attribute by name.
     *
     * @param attribute the attribute key.
     * @return the stored value.
     * @throws RuntimeException if the attribute is not present.
     */
    @Override
    public Object getObject(String attribute) {
        var value = attrs.get(attribute);
        SpecsCheck.checkNotNull(value, () -> "SymbolTable does not contain attribute '" + attribute + "'");
        return value;
    }

    /**
     * Stores an attribute value under the given key.
     *
     * @param attribute the attribute key.
     * @param value     the value to store.
     * @return the previous value associated with the key, or {@code null}.
     */
    @Override
    public Object putObject(String attribute, Object value) {
        return attrs.put(attribute, value);
    }
}
