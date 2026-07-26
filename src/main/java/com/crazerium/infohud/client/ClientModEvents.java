package com.crazerium.infohud.client;

import com.crazerium.infohud.InfoHUD;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = InfoHUD.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientModEvents {
    private static final String TOGGLE_HUD_NAME = "Toggle InfoHUD";
    private static final String INFOHUD_CATEGORY = "InfoHUD";

    public static final KeyMapping TOGGLE_HUD = new KeyMapping(
            TOGGLE_HUD_NAME,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            INFOHUD_CATEGORY
    );

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("main_hud", InfoHudOverlay.OVERLAY);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_HUD);
    }
}