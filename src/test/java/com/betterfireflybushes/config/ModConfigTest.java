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

    @Test
    void testFrozenFrameValidRange() {
        config.setFrozenFrame(0);
        assertEquals(0, config.getFrozenFrame(),
            "frozenFrame should accept 0");

        config.setFrozenFrame(500);
        assertEquals(500, config.getFrozenFrame(),
            "frozenFrame should accept 500");

        config.setFrozenFrame(1000);
        assertEquals(1000, config.getFrozenFrame(),
            "frozenFrame should accept 1000");
    }

    @Test
    void testFrozenFrameClampsBelowMinimum() {
        config.setFrozenFrame(-1);
        assertEquals(0, config.getFrozenFrame(),
            "frozenFrame should clamp -1 to 0");

        config.setFrozenFrame(-100);
        assertEquals(0, config.getFrozenFrame(),
            "frozenFrame should clamp -100 to 0");
    }

    @Test
    void testFrozenFrameClampsAboveMaximum() {
        config.setFrozenFrame(1001);
        assertEquals(1000, config.getFrozenFrame(),
            "frozenFrame should clamp 1001 to 1000");

        config.setFrozenFrame(5000);
        assertEquals(1000, config.getFrozenFrame(),
            "frozenFrame should clamp 5000 to 1000");

        config.setFrozenFrame(Integer.MAX_VALUE);
        assertEquals(1000, config.getFrozenFrame(),
            "frozenFrame should clamp Integer.MAX_VALUE to 1000");
    }

    @Test
    void testFrozenFramePersistsClampedValue() throws IOException {
        config.setFrozenFrame(2000);
        assertEquals(1000, config.getFrozenFrame(),
            "frozenFrame should be clamped to 1000");

        config.save();
        ModConfig loadedConfig = new ModConfig(tempDir);
        loadedConfig.load();

        assertEquals(1000, loadedConfig.getFrozenFrame(),
            "Clamped frozenFrame value should be persisted");
    }

    @Test
    void testFrozenFrameLoadClampsBadConfigValues() throws IOException {
        Path configFile = tempDir.resolve("better-firefly-bushes.properties");
        Properties props = new Properties();
        props.setProperty("frozenFrame", "5000");
        props.store(Files.newOutputStream(configFile), "Test config");

        config.load();

        assertEquals(1000, config.getFrozenFrame(),
            "frozenFrame should clamp value loaded from config file");
    }

    @Test
    void testFrozenFrameLoadClampsNegativeConfigValues() throws IOException {
        Path configFile = tempDir.resolve("better-firefly-bushes.properties");
        Properties props = new Properties();
        props.setProperty("frozenFrame", "-50");
        props.store(Files.newOutputStream(configFile), "Test config");

        config.load();

        assertEquals(0, config.getFrozenFrame(),
            "frozenFrame should clamp negative value loaded from config file to 0");
    }
}
