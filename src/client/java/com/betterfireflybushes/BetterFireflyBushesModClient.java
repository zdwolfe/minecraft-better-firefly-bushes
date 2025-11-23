package com.betterfireflybushes;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterFireflyBushesModClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BetterFireflyBushesModClient.class);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing client");
        LOGGER.info("Client initialized");
    }
}
