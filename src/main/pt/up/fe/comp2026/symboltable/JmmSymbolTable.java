package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the Java-- symbol table.
 *
 * Stores all information collected during symbol table construction:
 * imports, the class's fully qualified name, its superclass, fields, and methods.
 * Also supports arbitrary key-value attribute storage for extensibility.
 *
 * Import resolution supports three tiers:
 *   1. Explicit imports declared in the source file.
 *   2. Implicit imports handled by the {@link Importer}.
 *   3. Fully qualified names resolved directly through the importer.
 */
public class JmmSymbolTable extends AJmmSymbolTable {

    private final List<String> imports;
    private final String classQualifiedName;
    private final String superQualifiedName;
    private final Map<String, Symbol> fields;
    private final Map<Signature, MethodSymbol> methods;

    // TODO: Check if some uses of importNames can be replaced with getDeclaredClasses()
    /** Set of simple class names extracted from all import declarations. */
    private final Set<String> importNames;

    /** General-purpose attribute store for compiler extensions. */
    private final Map<String, Object> attrs;

    public JmmSymbolTable(List<String> imports, String classQualifiedName,
                          String superQualifiedName, List<Symbol> fields,
                          List<MethodSymbol> methods,
                          Importer importer) {
        super(importer);
        this.imports = imports;
        this.classQualifiedName = classQualifiedName;
        this.superQualifiedName = superQualifiedName;
        this.fields  = fields.stream().collect(Collectors.toMap(Symbol::name, s -> s));
        this.methods = methods.stream().collect(Collectors.toMap(MethodSymbol::signature, m -> m));
        this.importNames = calcImportNames(imports);
        this.attrs = new HashMap<>();
    }

    /** Extracts the simple (last-segment) name from each fully qualified import string. */
    private Set<String> calcImportNames(List<String> imports) {
        return imports.stream()
                .map(s -> s.split("\\."))
                .map(sarray -> sarray[sarray.length - 1])
                .collect(Collectors.toSet());
    }

    @Override
    public String getFullyQualifiedName() {
        return this.classQualifiedName;
    }

    @Override
    public String getSuperFullyQualifiedName() {
        return this.superQualifiedName;
    }

    /**
     * Returns the set of all class names visible in this compilation unit:
     * the simple names of all imports plus the current class's FQN.
     */
    public Set<String> getDeclaredClasses() {
        var declaredClasses = new HashSet<>(importNames);
        declaredClasses.add(this.classQualifiedName);
        return declaredClasses;
    }

    /**
     * Returns the set of simple class names from all import declarations
     * (i.e. only the last segment of each FQN, not the full path).
     */
    public Set<String> getImportNames() {
        return importNames;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public List<Symbol> getFields() {
        return fields.values().stream().toList();
    }

    @Override
    public List<MethodSymbol> getMethods() {
        return methods.values().stream().toList();
    }

    /** Returns all methods with the given simple name, regardless of signature. */
    @Override
    public List<MethodSymbol> getMethods(String name) {
        return methods.values().stream().filter(m -> m.name().equals(name)).toList();
    }

    @Override
    public Optional<MethodSymbol> getMethod(Signature signature) {
        return Optional.ofNullable(methods.getOrDefault(signature, null));
    }

    @Override
    public Optional<Symbol> getField(String name) {
        return Optional.ofNullable(fields.getOrDefault(name, null));
    }

    /**
     * Finds the fully qualified import name whose last segment matches {@code simpleName}.
     * Accepts both a plain simple name and a dot-prefixed match.
     */
    @Override
    public Optional<String> getImportedFullyQualifiedName(String simpleName) {
        var dotName = "." + simpleName;
        return imports.stream().filter(i -> i.equals(simpleName) || i.endsWith(dotName)).findFirst();
    }

    @Override
    public String toString() {
        return print();
    }

    @Override
    public Collection<String> getAttributes() {
        return attrs.keySet();
    }

    @Override
    public Object getObject(String attribute) {
        var value = attrs.get(attribute);
        SpecsCheck.checkNotNull(value, () -> "SymbolTable does not contain attribute '" + attribute + "'");
        return value;
    }

    @Override
    public Object putObject(String attribute, Object value) {
        return attrs.put(attribute, value);
    }

    /**
     * Resolves the symbol table for a given class name.
     * Tries, in order: the explicit import list, FQN resolution via the importer,
     * and finally implicit import resolution.
     *
     * @param className simple name or FQN of the class to resolve.
     * @return the corresponding symbol table, if available.
     */
    public Optional<SymbolTable> getImportedSymbolTable(String className) {
        if (!this.imports.contains(className)) {
            // className is already a FQN — attempt direct resolution
            return this.importer.getSymbolTableOf(className);
        }
        // Resolve simple name to FQN and look up
        var fullyQualifiedName = this.getImportedFullyQualifiedName(className);
        if (fullyQualifiedName.isPresent()) {
            return this.importer.getSymbolTableOf(fullyQualifiedName.get());
        }
        // Fall back to implicit import
        return this.importer.tryImplicitImport(className);
    }

    /** Returns true if the given class name is resolvable as an implicit import. */
    public boolean isImplicitImport(String className) {
        return importer.isImplicitImport(className);
    }

    /** Returns the symbol table for the given implicit import class, if available. */
    public Optional<SymbolTable> getImplicitImport(String className) {
        return this.importer.tryImplicitImport(className);
    }
}
