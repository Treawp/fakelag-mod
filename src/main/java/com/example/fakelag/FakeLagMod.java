package com.example.fakelag;

import com.example.fakelag.config.FakeLagConfig;
import com.example.fakelag.core.PacketDelayQueue;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeLagMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("fakelag");
    private static FakeLagConfig config;
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(FakeLagConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(FakeLagConfig.class).getConfig();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.fakelag.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "category.fakelag"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                config.enabled = !config.enabled;
                if (config.enabled && config.mode == FakeLagConfig.Mode.PULSE) {
                    PacketDelayQueue.startPulse(config.pulseHoldMs, config.pulseReleaseMs);
                    LOGGER.info("[FakeLag] PULSE ON hold:{}ms release:{}ms",
                        config.pulseHoldMs, config.pulseReleaseMs);
                } else if (!config.enabled) {
                    PacketDelayQueue.stopPulse();
                    LOGGER.info("[FakeLag] OFF");
                } else {
                    LOGGER.info("[FakeLag] DELAY ON out:{}ms in:{}ms",
                        config.outboundDelayMs, config.inboundDelayMs);
                }
            }
        });

        LOGGER.info("[FakeLag] Loaded!");
    }

    public static boolean isActive() {
        return config != null && config.enabled;
    }

    public static FakeLagConfig getConfig() {
        return config;
    }
          }
