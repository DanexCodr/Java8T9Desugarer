package desugarer;

public interface SourceTransformer {
    String transform(String source, SourceContext context);
}
