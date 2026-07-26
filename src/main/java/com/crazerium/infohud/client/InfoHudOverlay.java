package com.crazerium.infohud.client;

import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InfoHudOverlay {
    private static boolean visible = true;

    private static ClientLevel sessionLevel;
    private static long sessionStartedAtNanos;

    private InfoHudOverlay() {
    }

    public static final IGuiOverlay OVERLAY =
            (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

                Minecraft minecraft = Minecraft.getInstance();

                if (!visible
                        || minecraft.player == null
                        || minecraft.level == null
                        || minecraft.options.hideGui) {
                    return;
                }

                updateSession(minecraft.level);

                BlockPos position = minecraft.player.blockPosition();

                int fps = minecraft.getFps();
                int ping = getPing(minecraft);
                int lightLevel = minecraft.level.getMaxLocalRawBrightness(position);

                Runtime runtime = Runtime.getRuntime();

                long usedMemory = toMiB(
                        runtime.totalMemory() - runtime.freeMemory()
                );

                long maxMemory = toMiB(runtime.maxMemory());

                int memoryPercent = maxMemory == 0
                        ? 0
                        : (int) Math.round(usedMemory * 100.0D / maxMemory);

                String biome = minecraft.level
                        .getBiome(position)
                        .unwrapKey()
                        .map(key -> humanize(key.location()))
                        .orElse("Unknown");

                String dimension = humanize(
                        minecraft.level.dimension().location()
                );

                long worldTime = minecraft.level.getDayTime();

                List<HudLine> lines = new ArrayList<>();

                addLine(
                        lines,
                        ClientConfig.SHOW_PLAYER_FPS.get(),
                        Items.EXPERIENCE_BOTTLE,
                        playerLine(
                                minecraft.player.getGameProfile().getName(),
                                fps
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_PING.get(),
                        Items.GRAY_CONCRETE,
                        valueLine(
                                "Ping: ",
                                ping >= 0 ? ping + " ms" : "N/A",
                                ping >= 0
                                        ? lowIsGoodColor(ping, 100, 200)
                                        : ChatFormatting.GRAY
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_MEMORY.get(),
                        Items.CHEST,
                        valueLine(
                                "Memory: ",
                                usedMemory + "/" + maxMemory
                                        + " MB (" + memoryPercent + "%)",
                                lowIsGoodColor(memoryPercent, 69, 84)
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_POSITION.get(),
                        Items.COMPASS,
                        valueLine(
                                "Pos: ",
                                position.getX()
                                        + " "
                                        + position.getY()
                                        + " "
                                        + position.getZ(),
                                ChatFormatting.AQUA
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_FACING.get(),
                        Items.RECOVERY_COMPASS,
                        valueLine(
                                "Facing: ",
                                formatDirection(minecraft.player.getDirection()),
                                ChatFormatting.YELLOW
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_DIMENSION.get(),
                        Items.ENDER_EYE,
                        valueLine(
                                "Dimension: ",
                                dimension,
                                ChatFormatting.GOLD
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_BIOME.get(),
                        Items.GRASS_BLOCK,
                        valueLine(
                                "Biome: ",
                                biome,
                                ChatFormatting.GREEN
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_LIGHT.get(),
                        Items.TORCH,
                        valueLine(
                                "Light: ",
                                Integer.toString(lightLevel),
                                highIsGoodColor(lightLevel, 8, 4)
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_WORLD_TIME.get(),
                        Items.CLOCK,
                        valueLine(
                                "World Time: ",
                                formatWorldTime(worldTime),
                                ChatFormatting.GOLD
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_SESSION.get(),
                        Items.SPYGLASS,
                        valueLine(
                                "Session: ",
                                formatSessionTime(),
                                ChatFormatting.LIGHT_PURPLE
                        )
                );

                boolean showIcons = ClientConfig.SHOW_ICONS.get();
                boolean textShadow = ClientConfig.TEXT_SHADOW.get();

                float scale = ClientConfig.SCALE.get().floatValue();

                int lineHeight = showIcons ? 16 : 10;
                int textX = showIcons ? 20 : 0;
                int textY = showIcons ? 4 : 0;

                guiGraphics.pose().pushPose();

                guiGraphics.pose().translate(
                        ClientConfig.X.get(),
                        ClientConfig.Y.get(),
                        0.0D
                );

                guiGraphics.pose().scale(
                        scale,
                        scale,
                        1.0F
                );

                int currentY = 0;

                for (HudLine hudLine : lines) {
                    if (showIcons) {
                        guiGraphics.renderItem(
                                hudLine.icon(),
                                0,
                                currentY
                        );
                    }

                    guiGraphics.drawString(
                            minecraft.font,
                            hudLine.text(),
                            textX,
                            currentY + textY,
                            0xFFFFFFFF,
                            textShadow
                    );

                    currentY += lineHeight;
                }

                guiGraphics.pose().popPose();
            };

    public static void toggleVisible() {
        visible = !visible;
    }

    public static boolean isVisible() {
        return visible;
    }

    private static void updateSession(ClientLevel level) {
        if (sessionLevel != level) {
            sessionLevel = level;
            sessionStartedAtNanos = System.nanoTime();
        }
    }

    private static int getPing(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return -1;
        }

        PlayerInfo playerInfo = minecraft.getConnection()
                .getPlayerInfo(minecraft.player.getUUID());

        return playerInfo != null
                ? playerInfo.getLatency()
                : -1;
    }

    private static void addLine(
            List<HudLine> lines,
            boolean enabled,
            Item item,
            Component text
    ) {
        if (enabled) {
            lines.add(line(item, text));
        }
    }

    private static HudLine line(Item item, Component text) {
        return new HudLine(
                new ItemStack(item),
                text
        );
    }

    private static MutableComponent playerLine(
            String playerName,
            int fps
    ) {
        return Component.empty()
                .append(
                        Component.literal(playerName)
                                .withStyle(ChatFormatting.GOLD)
                )
                .append(
                        Component.literal(" FPS: ")
                                .withStyle(ChatFormatting.WHITE)
                )
                .append(
                        Component.literal(Integer.toString(fps))
                                .withStyle(highIsGoodColor(fps, 60, 30))
                );
    }

    private static MutableComponent valueLine(
            String label,
            String value,
            ChatFormatting valueColor
    ) {
        return Component.empty()
                .append(
                        Component.literal(label)
                                .withStyle(ChatFormatting.WHITE)
                )
                .append(
                        Component.literal(value)
                                .withStyle(valueColor)
                );
    }

    private static ChatFormatting highIsGoodColor(
            int value,
            int good,
            int warning
    ) {
        if (value >= good) {
            return ChatFormatting.GREEN;
        }

        if (value >= warning) {
            return ChatFormatting.YELLOW;
        }

        return ChatFormatting.RED;
    }

    private static ChatFormatting lowIsGoodColor(
            int value,
            int good,
            int warning
    ) {
        if (value <= good) {
            return ChatFormatting.GREEN;
        }

        if (value <= warning) {
            return ChatFormatting.YELLOW;
        }

        return ChatFormatting.RED;
    }

    private static String formatDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> "North -Z";
            case SOUTH -> "South +Z";
            case WEST -> "West -X";
            case EAST -> "East +X";
            case UP -> "Up +Y";
            case DOWN -> "Down -Y";
        };
    }

    private static String formatWorldTime(long dayTime) {
        long timeOfDay = Math.floorMod(
                dayTime + 6000L,
                24000L
        );

        int hours = (int) (timeOfDay / 1000L);

        int minutes = (int) (
                (timeOfDay % 1000L) * 60L / 1000L
        );

        long day = Math.floorDiv(dayTime, 24000L) + 1L;

        return String.format(
                Locale.ROOT,
                "%02d:%02d Day %d",
                hours,
                minutes,
                day
        );
    }

    private static String formatSessionTime() {
        if (sessionStartedAtNanos == 0L) {
            return "00m 00s";
        }

        long totalSeconds = (
                System.nanoTime() - sessionStartedAtNanos
        ) / 1_000_000_000L;

        long hours = totalSeconds / 3600L;
        long minutes = totalSeconds % 3600L / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return String.format(
                    Locale.ROOT,
                    "%dh %02dm %02ds",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                Locale.ROOT,
                "%02dm %02ds",
                minutes,
                seconds
        );
    }

    private static String humanize(ResourceLocation location) {
        String path = location.getPath()
                .replace('/', ' ')
                .replace('_', ' ');

        StringBuilder result = new StringBuilder();

        for (String part : path.split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(part.charAt(0))
            );

            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }

        return result.isEmpty()
                ? location.toString()
                : result.toString();
    }

    private static long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }

    private record HudLine(
            ItemStack icon,
            Component text
    ) {
    }
}