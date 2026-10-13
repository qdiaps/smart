package io.github.qdiaps.domain.model;

import java.util.Locale;

public record PidParameters(
        float tempTz,
        float kp,
        float ki,
        float kd
) {
    public String toCommandString() {
        return String.format(Locale.US, "pid:%s;%s;%s;%s",
                formatValue(tempTz),
                formatValue(kp),
                formatValue(ki),
                formatValue(kd));
    }

    private static String formatValue(float val) {
        if (val == (long) val) {
            return String.format(Locale.US, "%.1f", val);
        }
        return Float.toString(val);
    }
}
