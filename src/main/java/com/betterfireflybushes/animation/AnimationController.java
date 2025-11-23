package com.betterfireflybushes.animation;

import com.betterfireflybushes.config.ModConfig;
import net.minecraft.world.World;

public class AnimationController {

    private final ModConfig config;

    public AnimationController(ModConfig config) {
        this.config = config;
    }

    public boolean shouldAnimate(World world) {
        if (!config.isEnableTimeBasedControl()) {
            return true;
        }

        long worldTime = world.getTimeOfDay();
        long normalizedTime = worldTime % 24000;
        int startTime = config.getAnimationStartTime();
        int endTime = config.getAnimationEndTime();

        boolean inWindow;
        if (startTime <= endTime) {
            // Day: start=12000, end=23000
            inWindow = normalizedTime >= startTime && normalizedTime < endTime;
        } else {
            // Wrap around midnight: start=23000, end=1000
            inWindow = normalizedTime >= startTime || normalizedTime < endTime;
        }

        return inWindow;
    }
}
