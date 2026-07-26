package com.crazerium.infohud.client;

import com.crazerium.infohud.InfoHUD;
import com.crazerium.infohud.client.screen.InfoHudConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = InfoHUD.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientForgeEvents {

    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        while (ClientModEvents.TOGGLE_HUD.consumeClick()) {
            InfoHudOverlay.toggleVisible();
        }

        while (ClientModEvents.OPEN_SETTINGS.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();

            minecraft.setScreen(
                    new InfoHudConfigScreen(minecraft.screen)
            );
        }
    }
}