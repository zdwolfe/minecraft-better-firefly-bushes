package com.betterfireflybushes;

import com.betterfireflybushes.animation.AnimationController;
import com.betterfireflybushes.config.ModConfig;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class BetterFireflyBushesMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(BetterFireflyBushesMod.class);

    @Getter private static ModConfig config;
    @Getter private static AnimationController animationController;

    @Override
    public void onInitialize() {
        LOGGER.debug("Initializing");

        final Path configDir = FabricLoader.getInstance().getConfigDir();
        config = new ModConfig(configDir);
        config.load();
        animationController = new AnimationController(config);

        LOGGER.debug("Initialized");
    }

}
