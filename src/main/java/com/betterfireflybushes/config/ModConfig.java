package com.betterfireflybushes.config;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModConfig.class);
    private static final String CONFIG_FILE_NAME = "better-firefly-bushes.properties";

    @Getter @Setter private boolean enableTimeBasedControl = true;
    @Getter @Setter private int animationStartTime = 12000;
    @Getter @Setter private int animationEndTime = 23000;
    @Getter @Setter private int frozenFrame = 0;

    private final Path configPath;

    public ModConfig(Path configDir) {
        this.configPath = configDir.resolve(CONFIG_FILE_NAME);
    }

    /**
     * Load from disk or create default config
     */
    public void load() {
        if (!Files.exists(configPath)) {
            LOGGER.info("Configuration file not found, creating default configuration at: {}", configPath);
            save();
            return;
        }

        Properties props = new Properties();
        try {
            props.load(Files.newInputStream(configPath));
            enableTimeBasedControl = Boolean.parseBoolean(props.getProperty("enableTimeBasedControl", "true"));
            animationStartTime = Integer.parseInt(props.getProperty("animationStartTime", "12000"));
            animationEndTime = Integer.parseInt(props.getProperty("animationEndTime", "23000"));
            frozenFrame = Integer.parseInt(props.getProperty("frozenFrame", "0"));
            LOGGER.info("Configuration loaded successfully from: {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration from: {}", configPath, e);
            LOGGER.info("Using default configuration values");
        }
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("enableTimeBasedControl", String.valueOf(enableTimeBasedControl));
        props.setProperty("animationStartTime", String.valueOf(animationStartTime));
        props.setProperty("animationEndTime", String.valueOf(animationEndTime));
        props.setProperty("frozenFrame", String.valueOf(frozenFrame));

        try {
            Files.createDirectories(configPath.getParent());
            props.store(Files.newOutputStream(configPath), "Better Firefly Bushes Mod Configuration");
            LOGGER.info("Configuration saved to: {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration to: {}", configPath, e);
        }
    }
}
