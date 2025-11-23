package com.betterfireflybushes.config;

import com.betterfireflybushes.BetterFireflyBushesMod;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ModConfig config = BetterFireflyBushesMod.getConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Better Firefly Bushes Configuration"));

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Time-Based Control"), config.isEnableTimeBasedControl())
                .setDefaultValue(true)
                .setTooltip(Text.literal("When enabled, firefly bushes only animate during configured time window"))
                .setSaveConsumer(config::setEnableTimeBasedControl)
                .build());

            general.addEntry(entryBuilder.startIntField(Text.literal("Animation Start Time"), config.getAnimationStartTime())
                .setDefaultValue(12000)
                .setTooltip(Text.literal("Minecraft time when animations start (0-24000)"))
                .setSaveConsumer(config::setAnimationStartTime)
                .build());

            general.addEntry(entryBuilder.startIntField(Text.literal("Animation End Time"), config.getAnimationEndTime())
                .setDefaultValue(23000)
                .setTooltip(Text.literal("Minecraft time when animations end (0-24000)"))
                .setSaveConsumer(config::setAnimationEndTime)
                .build());

            general.addEntry(entryBuilder.startIntField(Text.literal("Frozen Frame"), config.getFrozenFrame())
                .setDefaultValue(0)
                .setTooltip(Text.literal("Frame to display when not animating (0-9)"))
                .setSaveConsumer(config::setFrozenFrame)
                .build());

            builder.setSavingRunnable(() -> {
                config.save();
                BetterFireflyBushesMod.LOGGER.info("Configuration saved from ModMenu");
            });

            return builder.build();
        };
    }
}
