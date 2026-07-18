package cart.decisiontree.domain.model;

import java.util.List;

public record Question(int column, Object value) {

    public boolean matches(List<?> example) {
        var candidate = example.get(column);
        return isNumeric(candidate) && isNumeric(value)
                ? ((Number) candidate).doubleValue() >= ((Number) value).doubleValue()
                : candidate.equals(value);
    }

    public String describe(List<String> featureNames) {
        var condition = isNumeric(value) ? ">=" : "==";
        return "Is %s %s %s?".formatted(featureNames.get(column), condition, value);
    }

    static boolean isNumeric(Object value) {
        return value instanceof Byte || value instanceof Short || value instanceof Integer
                || value instanceof Long || value instanceof Float || value instanceof Double;
    }
}
