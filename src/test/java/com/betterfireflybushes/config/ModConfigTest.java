package com.betterfireflybushes.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTest {

    @TempDir Path tempDir;

    private ModConfig config;

    @BeforeEach
    void setUp() {
        config = new ModConfig(tempDir);
    }

    @Test
    void testDefaultValues() {
        assertTrue(config.isEnableTimeBasedControl(),
            "Default enableTimeBasedControl should be true");
        assertEquals(12000, config.getAnimationStartTime(),
            "Default animationStartTime should be 12000");
        assertEquals(23000, config.getAnimationEndTime(),
            "Default animationEndTime should be 23000");
        assertEquals(0, config.getFrozenFrame(),
            "Default frozenFrame should be 0");
    }

    @Test
    void testSaveAndLoad() throws IOException {
        // Set a value
        config.setEnableTimeBasedControl(false);

        // Save it
        config.save();

        // Create a new config instance to test loading
        ModConfig loadedConfig = new ModConfig(tempDir);
        loadedConfig.load();

        // Verify the loaded value
        assertFalse(loadedConfig.isEnableTimeBasedControl(),
            "Loaded config should have saved value (false)");
    }

    @Test
    void testSaveCreatesConfigDirectory() throws IOException {
        Path subDir = tempDir.resolve("nested").resolve("config");
        ModConfig nestedConfig = new ModConfig(subDir);

        nestedConfig.save();

        assertTrue(Files.exists(subDir),
            "Save should create parent directories");
        assertTrue(Files.exists(subDir.resolve("better-firefly-bushes.properties")),
            "Save should create config file in nested directory");
    }

    @Test
    void testLoadFromExistingFile() throws IOException {
        Path configFile = tempDir.resolve("better-firefly-bushes.properties");
        Properties props = new Properties();
        props.setProperty("enableTimeBasedControl", "false");
        props.store(Files.newOutputStream(configFile), "Test config");

        config.load();

        assertFalse(config.isEnableTimeBasedControl(),
            "Should load false from existing file");
    }

    @Test
    void testSaveWritesCorrectFormat() throws IOException {
        config.setEnableTimeBasedControl(false);
        config.save();

        Path configFile = tempDir.resolve("better-firefly-bushes.properties");
        Properties props = new Properties();
        props.load(Files.newInputStream(configFile));

        assertEquals("false", props.getProperty("enableTimeBasedControl"),
            "Saved file should contain correct property value");
    }

    @Test
    void testLoadHandlesIOException() throws IOException {
        Path configFile = tempDir.resolve("better-firefly-bushes.properties");
        Files.createDirectory(configFile);

        config.load();

        assertTrue(config.isEnableTimeBasedControl(),
            "Should use default value when IOException occurs during load");
    }

    @Test
    void testSaveHandlesIOException() throws IOException {
        Path invalidDir = tempDir.resolve("invalid-file");
        Files.createFile(invalidDir);

        ModConfig invalidConfig = new ModConfig(invalidDir);

        assertDoesNotThrow(invalidConfig::save,
            "Save should handle IOException gracefully without throwing");
    }
}
