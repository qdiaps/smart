package io.github.qdiaps.infrastructure.config;

public final class Config {
    public static final String PROGRAM_NAME = "SMART";


    // Arduino
    public static final int ARDUINO_BOOT_DELAY_SECONDS = 3;
    public static final int DEFAULT_BAUD_RATE = 9600;


    // UI
    public static final int INFO_MESSAGE_DELAY = 3;
    public static final int WARN_MESSAGE_DELAY = 5;


    public static String getVersion() {
        String version = Config.class.getPackage().getImplementationVersion();
        return version != null ? version : "DEV-BUILD";
    }
}
