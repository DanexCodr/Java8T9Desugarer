package desugarer;

public final class ModuleInfoTransformer implements SourceTransformer {
    @Override
    public String transform(String source, SourceContext context) {
        String fileName = context.getFileName();
        if (fileName == null || !fileName.endsWith("module-info.java")) {
            return source;
        }
        StringBuilder out = new StringBuilder();
        out.append("/* module-info.java desugared for Java 8.\n");
        out.append(" * Original contents:\n");
        String[] lines = source.split("\\r?\\n", -1);
        for (String line : lines) {
            out.append(" * ").append(line).append("\n");
        }
        out.append(" */\n");
        return out.toString();
    }
}
