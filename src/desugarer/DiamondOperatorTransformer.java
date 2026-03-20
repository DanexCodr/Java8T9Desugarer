package desugarer;

public final class DiamondOperatorTransformer implements SourceTransformer {
    @Override
    public String transform(String source, SourceContext context) {
        return SourceDesugarer.transformCodeSegments(source, this::transformCode, context);
    }

    private String transformCode(String code, SourceContext context) {
        StringBuilder out = new StringBuilder(code.length());
        int index = 0;
        while (index < code.length()) {
            int newIndex = SourceDesugarer.findKeyword(code, null, "new", index);
            if (newIndex < 0) {
                out.append(code.substring(index));
                break;
            }
            int typeStart = SourceDesugarer.skipWhitespace(code, newIndex + 3);
            int typeEnd = parseQualifiedName(code, typeStart);
            if (typeEnd == typeStart) {
                out.append(code.substring(index, newIndex + 3));
                index = newIndex + 3;
                continue;
            }
            String typeName = code.substring(typeStart, typeEnd);
            int afterType = SourceDesugarer.skipWhitespace(code, typeEnd);
            if (!code.startsWith("<>", afterType)) {
                out.append(code.substring(index, typeEnd));
                index = typeEnd;
                continue;
            }
            int afterDiamond = SourceDesugarer.skipWhitespace(code, afterType + 2);
            if (afterDiamond >= code.length() || code.charAt(afterDiamond) != '(') {
                out.append(code.substring(index, afterType + 2));
                index = afterType + 2;
                continue;
            }
            int closeParen = SourceDesugarer.findMatching(code, null, afterDiamond, '(', ')');
            if (closeParen < 0) {
                out.append(code.substring(index));
                break;
            }
            int afterParen = SourceDesugarer.skipWhitespace(code, closeParen + 1);
            if (afterParen >= code.length() || code.charAt(afterParen) != '{') {
                out.append(code.substring(index, closeParen + 1));
                index = closeParen + 1;
                continue;
            }

            String typeArgs = inferTypeArgs(code, typeName, newIndex);
            if (typeArgs == null) {
                context.warn("Unable to infer diamond type args for " + typeName
                        + " near index " + newIndex + "; using Object");
                typeArgs = "Object";
            }

            out.append(code, index, afterType);
            out.append("<").append(typeArgs).append(">");
            index = afterType + 2;
        }
        return out.toString();
    }

    private int parseQualifiedName(String code, int start) {
        int i = start;
        while (i < code.length()) {
            char c = code.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c == '.' || c == '$') {
                i++;
            } else {
                break;
            }
        }
        return i;
    }

    private String inferTypeArgs(String code, String typeName, int newIndex) {
        int statementStart = Math.max(
                Math.max(code.lastIndexOf(';', newIndex), code.lastIndexOf('{', newIndex)),
                code.lastIndexOf('}', newIndex));
        if (statementStart < 0) {
            statementStart = 0;
        } else {
            statementStart += 1;
        }
        String segment = code.substring(statementStart, newIndex);
        String search = typeName + "<";
        int idx = segment.lastIndexOf(search);
        while (idx >= 0) {
            int genericStart = idx + typeName.length();
            int genericEnd = findMatchingAngle(segment, genericStart);
            if (genericEnd > genericStart) {
                return segment.substring(genericStart + 1, genericEnd).trim();
            }
            idx = segment.lastIndexOf(search, idx - 1);
        }
        return null;
    }

    private int findMatchingAngle(String segment, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
