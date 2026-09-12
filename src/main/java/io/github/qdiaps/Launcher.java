package io.github.qdiaps;

import org.slf4j.LoggerFactory;

public final class Launcher {
    public static void main(String[] args) {
        var logger = LoggerFactory.getLogger(Launcher.class);
        logger.info("App started");
    }
}
