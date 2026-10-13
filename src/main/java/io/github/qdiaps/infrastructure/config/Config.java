package io.github.qdiaps.infrastructure.config;

public final class Config {
    public static final String PROGRAM_NAME = "SMART";


    // Arduino
    public static final int ARDUINO_BOOT_DELAY_SECONDS = 3;
    public static final int DEFAULT_BAUD_RATE = 9600;
    public static final int PING_TIMEOUT_MS = 300;
    public static final int PING_MAX_RETRIES = 3;
    public static final int PID_TIMEOUT_MS = 500;
    public static final int PID_MAX_RETRIES = 3;


    // PID Ranges
    public static final float MIN_TEMP_TZ = 0.0f;
    public static final float MAX_TEMP_TZ = 100.0f;
    public static final float MIN_PID_COEFF = 0.0f;
    public static final float MAX_PID_COEFF = 1000.0f;


    // UI
    public static final int INFO_MESSAGE_DELAY = 3;
    public static final int WARN_MESSAGE_DELAY = 5;
    public static final String CHART_TITLE = "Перехідний процес ПІД-регулятора";
    public static final double DEFAULT_CHART_MAX_TIME = 300.0;


    public static String getVersion() {
        String version = Config.class.getPackage().getImplementationVersion();
        return version != null ? version : "DEV-BUILD";
    }
}
