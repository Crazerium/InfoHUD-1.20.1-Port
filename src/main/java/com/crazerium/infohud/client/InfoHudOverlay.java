package com.crazerium.infohud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InfoHudOverlay {

    private static final int X = 4;
    private static final int Y = 4;
    private static final int LINE_HEIGHT = 10;

    private InfoHudOverlay() {}

    public static final IGuiOverlay OVERLAY =
            (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.player == null
                        || minecraft.level == null
                        || minecraft.options.hideGui) {
                    return;
                }

                BlockPos position = minecraft.player.blockPosition();

                int ping = -1;

                if (minecraft.getConnection() != null) {
                    PlayerInfo playerInfo = minecraft.getConnection()
                            .getPlayerInfo(minecraft.player.getUUID());

                    if (playerInfo != null) {
                        ping = playerInfo.getLatency();
                    }
                }

                String biome = minecraft.level
                        .getBiome(position)
                        .unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("unknown");

                String dimension = minecraft.level
                        .dimension()
                        .location()
                        .toString();

                List<String> lines = new ArrayList<>();

                lines.add("InfoHUD 1.20.1");
                lines.add("FPS: " + minecraft.getFps());
                lines.add("Ping: " + (ping >= 0 ? ping + " ms" : "N/A"));

                lines.add(String.format(
                        Locale.ROOT,
                        "XYZ: %.1f / %.1f / %.1f",
                        minecraft.player.getX(),
                        minecraft.player.getY(),
                        minecraft.player.getZ()
                ));

                lines.add("Direction: " + minecraft.player.getDirection().getName());
                lines.add("Dimension: " + dimension);
                lines.add("Biome: " + biome);
                lines.add("World Time: " + minecraft.level.getDayTime());

                int currentY = Y;

                for (int index = 0; index < lines.size(); index++) {
                    int color = index == 0 ? 0xFFFFAA00 : 0xFFFFFFFF;

                    guiGraphics.drawString(
                            minecraft.font,
                            lines.get(index),
                            X,
                            currentY,
                            color,
                            true
                    );

                    currentY += LINE_HEIGHT;
                }
            };
}