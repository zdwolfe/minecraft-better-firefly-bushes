package com.betterfireflybushes.mixin;

import com.betterfireflybushes.BetterFireflyBushesMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(SpriteContents.Animator.class)
public abstract class SpriteAnimatorMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpriteAnimatorMixin.class);

    @Shadow
    @Final
    private int frame;
    
    private boolean wasAnimating = true;
    private boolean initialized = false;
    private SpriteContents cachedContents = null;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickHead(CallbackInfo ci) {
        // check if this is a firefly bush
        if (!initialized) {
            initialized = true;
            cachedContents = getSpriteContents();
            if (cachedContents != null) {
                boolean isFirefly = isFireflyBush(cachedContents);
                LOGGER.debug("SpriteAnimatorMixin initialized - isFireflyBush: {}", isFirefly);
            }
        }

        if (cachedContents != null && isFireflyBush(cachedContents)) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;

            if (world != null && BetterFireflyBushesMod.getAnimationController() != null) {
                boolean shouldAnimate = BetterFireflyBushesMod.getAnimationController().shouldAnimate(world);

                if (!shouldAnimate) {
                    // prevent animation
                    ci.cancel();

                    // set frame to frozen frame if it's time
                    int frozenFrame = BetterFireflyBushesMod.getConfig().getFrozenFrame();
                    int frameCount = getFrameCount();
                    int clampedFrame = Math.min(frozenFrame, frameCount - 1);

                    if (clampedFrame != frozenFrame) {
                        LOGGER.warn("frozenFrame {} exceeds available frames ({}), clamping to {}",
                            frozenFrame, frameCount, clampedFrame);
                    }

                    setFrame(clampedFrame);

                    if (wasAnimating) {
                        LOGGER.debug("Firefly animation paused at frame {}", clampedFrame);
                        wasAnimating = false;
                    }
                } else {
                    if (!wasAnimating) {
                        LOGGER.debug("Firefly animation resumed");
                    }
                    wasAnimating = true;
                }
            }
        }
    }

    private SpriteContents getSpriteContents() {
        try {
            Class<?> animatorClass = SpriteContents.Animator.class;

            Field animationField = null;
            for (Field field : animatorClass.getDeclaredFields()) {
                // match by type name - handles both obfuscated (class_7764$class_5790) and deobfuscated names
                String typeName = field.getType().getName();
                if (typeName.contains("SpriteContents$Animation") ||
                    (typeName.contains("class_7764$") && !typeName.equals("net.minecraft.class_7764"))) {
                    animationField = field;
                    animationField.setAccessible(true);
                    break;
                }
            }

            if (animationField != null) {
                Object animationObj = animationField.get(this);
                if (animationObj != null) {
                    // find the SpriteContents in Animation
                    for (Field field : animationObj.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        String typeName = field.getType().getName();
                        if (field.getType() == SpriteContents.class || typeName.equals("net.minecraft.class_7764")) {
                            return (SpriteContents) field.get(animationObj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not access Animator fields via reflection", e);
        }

        return null;
    }

    private boolean isFireflyBush(SpriteContents contents) {
        String contentsStr = contents.toString();
        return contentsStr != null && contentsStr.contains("firefly_bush");
    }

    private int getFrameCount() {
        try {
            Field animationField = null;
            for (Field field : SpriteContents.Animator.class.getDeclaredFields()) {
                String typeName = field.getType().getName();
                if (typeName.contains("SpriteContents$Animation") ||
                    (typeName.contains("class_7764$") && !typeName.equals("net.minecraft.class_7764"))) {
                    animationField = field;
                    animationField.setAccessible(true);
                    break;
                }
            }

            if (animationField != null) {
                Object animationObj = animationField.get(this);
                if (animationObj != null) {
                    // Look for the frames List field in Animation
                    for (Field field : animationObj.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = field.get(animationObj);
                        if (value instanceof java.util.List) {
                            int count = ((java.util.List<?>) value).size();
                            LOGGER.debug("Found frame count: {}", count);
                            return count;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not determine frame count", e);
        }
        return 10; // arbitrary fallback
    }

    private void setFrame(int targetFrame) {
        try {
            Field frameField = null;
            Field[] fields = SpriteContents.Animator.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == int.class) {
                    frameField = field;
                    break;
                }
            }

            if (frameField != null) {
                frameField.setAccessible(true);
                frameField.setInt(this, targetFrame);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set frame", e);
        }
    }
}
