package desugarer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SourceContext {
    private final String fileName;
    private final ImportAnalyzer imports;
    private final boolean verbose;
    private final Set<String> neededImports = new LinkedHashSet<>();
    private final List<String> warnings = new ArrayList<>();

    SourceContext(String fileName, ImportAnalyzer imports, boolean verbose) {
        this.fileName = fileName;
        this.imports = imports;
        this.verbose = verbose;
    }

    public String getFileName() {
        return fileName;
    }

    public ImportAnalyzer getImports() {
        return imports;
    }

    public void addImport(String fqn) {
        neededImports.add(fqn);
    }

    public Set<String> getNeededImports() {
        return neededImports;
    }

    public void warn(String message) {
        warnings.add(message);
        if (verbose) {
            System.err.println("Warning: " + message);
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
