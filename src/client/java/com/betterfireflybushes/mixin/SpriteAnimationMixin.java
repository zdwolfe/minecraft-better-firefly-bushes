package com.betterfireflybushes.mixin;

import com.betterfireflybushes.BetterFireflyBushesMod;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

/**
 * controls firefly bush sprite animation
 */
@Mixin(Sprite.class)
public abstract class SpriteAnimationMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpriteAnimationMixin.class);

    private static boolean hasLoggedMixinActive = false;
    private static long lastLogTime = 0;

    @Shadow public abstract Identifier getAtlasId();

    @Inject(method = "createAnimation", at = @At("RETURN"), cancellable = true)
    private void onCreateAnimation(CallbackInfoReturnable<Sprite.TickableAnimation> cir) {
        if (!hasLoggedMixinActive) {
            hasLoggedMixinActive = true;
        }

        Sprite.TickableAnimation original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        Identifier atlasId = getAtlasId();
        Sprite sprite = (Sprite) (Object) this;
        String spriteId = sprite.toString();

        LOGGER.debug("Animation for sprite in atlas: {}, sprite: {}", atlasId, spriteId);
        if (isFireflyBush(spriteId)) {

            // Wrap the animation
            Sprite.TickableAnimation wrapped = new Sprite.TickableAnimation() {
                private boolean wasAnimating = true;
                private Field frameIndexField = null;
                private boolean triedReflection = false;

                private void setFrame(int targetFrame) {
                    if (!triedReflection) {
                        triedReflection = true;
                        try {
                            for (Field field : original.getClass().getDeclaredFields()) {
                                field.setAccessible(true);
                                Object fieldValue = field.get(original);

                                String valueClass = fieldValue != null ? fieldValue.getClass().getName() : "null";
                                LOGGER.debug("  Field '{}' type='{}' value class='{}'",
                                    field.getName(), field.getType().getName(), valueClass);

                                // Check if field TYPE is class_7768 (SpriteContents), not the value's class
                                if (fieldValue != null && field.getType().getName().contains("class_7768")) {
                                    LOGGER.debug("Found SpriteContents field: {}", field.getName());

                                    Class<?> spriteContentsClass = fieldValue.getClass();
                                    LOGGER.debug("SpriteContents fields:");
                                    for (Field scField : spriteContentsClass.getDeclaredFields()) {
                                        LOGGER.debug("  Field: {} (type: {})", scField.getName(), scField.getType().getName());
                                    }

                                    // find field that looks like frame index
                                    for (Field scField : spriteContentsClass.getDeclaredFields()) {
                                        if (scField.getType() == int.class) {
                                            scField.setAccessible(true);
                                            frameIndexField = scField;
                                            LOGGER.info("Using field '{}' as frame index", scField.getName());
                                            break;
                                        }
                                    }

                                    break;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Could not access frame index field via reflection", e);
                        }
                    }

                    if (frameIndexField != null) {
                        try {
                            for (Field field : original.getClass().getDeclaredFields()) {
                                field.setAccessible(true);
                                Object fieldValue = field.get(original);
                                if (fieldValue != null && field.getType().getName().contains("class_7768")) {
                                    frameIndexField.setInt(fieldValue, targetFrame);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Failed to set frame", e);
                        }
                    }
                }

                @Override
                public void tick(GpuTexture texture) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ClientWorld world = client.world;

                    if (world != null && BetterFireflyBushesMod.getAnimationController() != null) {
                        boolean shouldAnimate = BetterFireflyBushesMod.getAnimationController().shouldAnimate(world);
                        long currentTime = world.getTime();
                        long timeOfDay = currentTime % 24000;

                        // Log periodically
                        if (currentTime - lastLogTime > 100) {
                            LOGGER.debug("Firefly animation tick - Time: {}, TimeOfDay: {}, ShouldAnimate: {}",
                                currentTime, timeOfDay, shouldAnimate);
                            lastLogTime = currentTime;
                        }

                        if (shouldAnimate) {
                            original.tick(texture);
                            wasAnimating = true;
                        } else {
                            // furing daytime force frame to configured frozen frame
                            // still tick to keep animation state fresh
                            original.tick(texture);
                            int frozenFrame = BetterFireflyBushesMod.getConfig().getFrozenFrame();
                            setFrame(frozenFrame);
                            if (wasAnimating) {
                                LOGGER.debug("Transitioning to day - forcing frame to {}", frozenFrame);
                                wasAnimating = false;
                            }
                        }
                    } else {
                        // passthrough
                        original.tick(texture);
                    }
                }

                @Override
                public void close() {
                    original.close();
                }
            };

            cir.setReturnValue(wrapped);
        }
    }

    private static boolean isFireflyBush(String spriteId) {
        return spriteId != null && spriteId.contains("firefly_bush");
    }
}
