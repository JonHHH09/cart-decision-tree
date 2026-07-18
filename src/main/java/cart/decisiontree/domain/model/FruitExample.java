package cart.decisiontree.domain.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record FruitExample(String color, String shape, int weightGrams, String skin, String fruit) {

    private static final int MAX_TEXT_LENGTH = 32;
    private static final int MAX_WEIGHT_GRAMS = 10_000;

    public FruitExample {
        color = normalize("color", color);
        shape = normalize("shape", shape);
        skin = normalize("skin", skin);
        fruit = normalize("fruit", fruit);
        if (weightGrams <= 0 || weightGrams > MAX_WEIGHT_GRAMS) {
            throw new IllegalArgumentException("weightGrams must be between 1 and 10000.");
        }
    }

    public static FruitExample unlabeled(String color, String shape, int weightGrams, String skin) {
        return new FruitExample(color, shape, weightGrams, skin, "unknown");
    }

    public List<?> trainingRow() {
        return List.of(color, shape, weightGrams, skin, fruit);
    }

    public List<?> features() {
        return List.of(color, shape, weightGrams, skin);
    }

    private static String normalize(String field, String value) {
        var normalized = Objects.requireNonNull(value, field + " is required.").strip().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || normalized.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(field + " must contain between 1 and 32 characters.");
        }
        return normalized;
    }
}
