package io.github.qdiaps.domain.model;

public record DataPoint(
        float time,
        float tempTz,
        float tempTt,
        float out
) {
    public DataPoint {
        if (time < 0) {
            throw new IllegalArgumentException("Time cannot be negative: " + time);
        }
        validateRange("tempTz", tempTz);
        validateRange("tempTt", tempTt);
        validateRange("out", out);
    }

    private static void validateRange(String field, float value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(field + " is out of acceptable range [0, 100]: " + value);
        }
    }
}
