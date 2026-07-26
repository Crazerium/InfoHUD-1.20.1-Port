package com.crazerium.infohud.client;

import com.crazerium.infohud.client.data.ClientServerData;
import com.crazerium.infohud.client.layout.HudAnchor;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfoHudOverlay {

    private static boolean visible = true;
    private static int lastResolvedX = 4;
    private static int lastResolvedY = 4;
    private static ClientLevel sessionLevel;
    private static long sessionStartedAtNanos;
    private static final Pattern PERSONAL_SPACE_TEAM_PATTERN =
            Pattern.compile(
                    "(?:team|ps_team)[_:/-]*([0-9a-fA-F-]{32,36})",
                    Pattern.CASE_INSENSITIVE
            );

    private InfoHudOverlay() {
    }

    public static final IGuiOverlay OVERLAY =
            (
                    forgeGui,
                    guiGraphics,
                    partialTick,
                    screenWidth,
                    screenHeight
            ) -> {
                Minecraft minecraft =
                        Minecraft.getInstance();

                if (
                        !visible
                                || minecraft.player == null
                                || minecraft.level == null
                                || minecraft.options.hideGui
                ) {
                    return;
                }

                updateSession(minecraft.level);

                BlockPos position =
                        minecraft.player.blockPosition();

                int fps = minecraft.getFps();
                int ping = getPing(minecraft);

                int lightLevel =
                        minecraft.level
                                .getMaxLocalRawBrightness(
                                        position
                                );

                Runtime runtime =
                        Runtime.getRuntime();

                long clientUsedMemory = toMiB(
                        runtime.totalMemory()
                                - runtime.freeMemory()
                );

                long clientMaxMemory = toMiB(
                        runtime.maxMemory()
                );

                int clientMemoryPercent =
                        percentage(
                                clientUsedMemory,
                                clientMaxMemory
                        );

                boolean serverDataAvailable =
                        ClientServerData.hasFreshData();

                long serverUsedMemory =
                        ClientServerData
                                .getServerUsedMemoryMiB();

                long serverMaxMemory =
                        ClientServerData
                                .getServerMaxMemoryMiB();

                int serverMemoryPercent =
                        percentage(
                                serverUsedMemory,
                                serverMaxMemory
                        );

                String biome =
                        minecraft.level
                                .getBiome(position)
                                .unwrapKey()
                                .map(
                                        key -> humanize(
                                                key.location()
                                        )
                                )
                                .orElse("Unknown");

                String dimension = formatDimension(
                        minecraft.level.dimension().location(),
                        minecraft.player.getGameProfile().getName()
                );

                long worldTime =
                        minecraft.level.getDayTime();

                List<HudLine> lines =
                        new ArrayList<>();

                addLine(
                        lines,
                        ClientConfig
                                .SHOW_PLAYER_FPS
                                .get(),
                        Items.EXPERIENCE_BOTTLE,
                        playerLine(
                                minecraft.player
                                        .getGameProfile()
                                        .getName(),
                                fps
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_PING.get(),
                        Items.GRAY_CONCRETE,
                        valueLine(
                                "Ping: ",
                                ping >= 0
                                        ? ping + " ms"
                                        : "N/A",
                                ping >= 0
                                        ? lowIsGoodColor(
                                        ping,
                                        100,
                                        200
                                )
                                        : ChatFormatting.GRAY
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_TPS.get(),
                        Items.REPEATER,
                        serverDataAvailable
                                ? performanceLine(
                                ClientServerData.getTps(),
                                ClientServerData.getMspt()
                        )
                                : valueLine(
                                "TPS: ",
                                "N/A",
                                ChatFormatting.GRAY
                        )
                );

                addLine(
                        lines,
                        ClientConfig.SHOW_MEMORY.get(),
                        Items.CHEST,
                        memoryLine(
                                "Client RAM: ",
                                clientUsedMemory,
                                clientMaxMemory,
                                clientMemoryPercent
                        )
                );

                addLine(
                        lines,
                        ClientConfig
                                .SHOW_SERVER_MEMORY
                                .get(),
                        Items.ENDER_CHEST,
                        serverDataAvailable
                                ? memoryLine(
                                "Server RAM: ",
                                serverUsedMemory,
                                serverMaxMemory,
                                serverMemoryPercent
                        )
                                : valueLine(
                                "Server RAM: ",
                                "N/A",
                                ChatFormatting.GRAY
                        )
                );

                addLine(
                        lines,
                        ClientConfig
                                .SHOW_POSITION
                                .get(),
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
                                formatDirection(
                                        minecraft.player
                                                .getDirection()
                                ),
                                ChatFormatting.YELLOW
                        )
                );

                addLine(
                        lines,
                        ClientConfig
                                .SHOW_DIMENSION
                                .get(),
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
                                Integer.toString(
                                        lightLevel
                                ),
                                highIsGoodColor(
                                        lightLevel,
                                        8,
                                        4
                                )
                        )
                );

                addLine(
                        lines,
                        ClientConfig
                                .SHOW_WORLD_TIME
                                .get(),
                        Items.CLOCK,
                        valueLine(
                                "World Time: ",
                                formatWorldTime(
                                        worldTime
                                ),
                                ChatFormatting.GOLD
                        )
                );

                addLine(
                        lines,
                        ClientConfig
                                .SHOW_PLAY_TIME
                                .get(),
                        Items.NAME_TAG,
                        serverDataAvailable
                                ? valueLine(
                                "Play Time: ",
                                formatTicks(
                                        ClientServerData
                                                .getPlayTimeTicks()
                                ),
                                ChatFormatting.YELLOW
                        )
                                : valueLine(
                                "Play Time: ",
                                "N/A",
                                ChatFormatting.GRAY
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

                addLine(
                        lines,
                        ClientConfig.SHOW_JUMPS.get(),
                        Items.RABBIT_FOOT,
                        serverDataAvailable
                                ? valueLine(
                                "Jumps: ",
                                Integer.toString(
                                        ClientServerData
                                                .getJumps()
                                ),
                                ChatFormatting.GOLD
                        )
                                : valueLine(
                                "Jumps: ",
                                "N/A",
                                ChatFormatting.GRAY
                        )
                );

                renderLines(
                        minecraft,
                        guiGraphics,
                        lines,
                        screenWidth,
                        screenHeight
                );
            };

    private static String formatDimension(
            ResourceLocation location,
            String playerName
    ) {
        String namespace = location.getNamespace();
        String path = location.getPath();

        String fullId = (
                namespace + ":" + path
        ).toLowerCase(Locale.ROOT);

        boolean isPersonalSpace =
                fullId.contains("personalspace")
                        || fullId.contains("personal_space")
                        || fullId.contains("personal-space");

        if (!isPersonalSpace) {
            return humanize(location);
        }

        Matcher teamMatcher =
                PERSONAL_SPACE_TEAM_PATTERN.matcher(fullId);

        if (teamMatcher.find()) {
            String teamUuid = teamMatcher
                    .group(1)
                    .replace("-", "");

            return "ps_" + teamUuid;
        }

        return "ps_" + playerName;
    }

    public static void toggleVisible() {
        visible = !visible;
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void switchToCustomPosition() {
        if (ClientConfig.ANCHOR.get() == HudAnchor.CUSTOM) {
            return;
        }

        ClientConfig.X.set(lastResolvedX);
        ClientConfig.Y.set(lastResolvedY);
        ClientConfig.ANCHOR.set(HudAnchor.CUSTOM);
    }

    private static void renderLines(
            Minecraft minecraft,
            net.minecraft.client.gui.GuiGraphics guiGraphics,
            List<HudLine> lines,
            int screenWidth,
            int screenHeight
    ) {
        if (lines.isEmpty()) {
            return;
        }

        boolean showIcons = ClientConfig.SHOW_ICONS.get();
        boolean textShadow = ClientConfig.TEXT_SHADOW.get();

        float scale = ClientConfig.SCALE.get().floatValue();

        int lineHeight = showIcons ? 16 : 10;
        int textX = showIcons ? 20 : 0;
        int textY = showIcons ? 4 : 0;

        int unscaledWidth = 0;

        for (HudLine hudLine : lines) {
            int lineWidth =
                    textX + minecraft.font.width(hudLine.text());

            if (showIcons) {
                lineWidth = Math.max(lineWidth, 16);
            }

            unscaledWidth = Math.max(
                    unscaledWidth,
                    lineWidth
            );
        }

        int lastLineHeight = showIcons
                ? 16
                : minecraft.font.lineHeight;

        int unscaledHeight =
                (lines.size() - 1) * lineHeight
                        + lastLineHeight;

        int scaledWidth = (int) Math.ceil(
                unscaledWidth * scale
        );

        int scaledHeight = (int) Math.ceil(
                unscaledHeight * scale
        );

        int margin = 4;

        int resolvedX;
        int resolvedY;

        HudAnchor anchor = ClientConfig.ANCHOR.get();

        switch (anchor) {
            case TOP_LEFT -> {
                resolvedX = margin;
                resolvedY = margin;
            }

            case TOP_CENTER -> {
                resolvedX =
                        (screenWidth - scaledWidth) / 2;

                resolvedY = margin;
            }

            case TOP_RIGHT -> {
                resolvedX =
                        screenWidth - scaledWidth - margin;

                resolvedY = margin;
            }

            case BOTTOM_LEFT -> {
                resolvedX = margin;

                resolvedY =
                        screenHeight - scaledHeight - margin;
            }

            case BOTTOM_RIGHT -> {
                resolvedX =
                        screenWidth - scaledWidth - margin;

                resolvedY =
                        screenHeight - scaledHeight - margin;
            }

            case CUSTOM -> {
                resolvedX = ClientConfig.X.get();
                resolvedY = ClientConfig.Y.get();
            }

            default -> {
                resolvedX = margin;
                resolvedY = margin;
            }
        }

        resolvedX = clamp(
                resolvedX,
                0,
                Math.max(0, screenWidth - scaledWidth)
        );

        resolvedY = clamp(
                resolvedY,
                0,
                Math.max(0, screenHeight - scaledHeight)
        );

        lastResolvedX = resolvedX;
        lastResolvedY = resolvedY;

        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(
                resolvedX,
                resolvedY,
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
    }

    private static int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    private static void updateSession(
            ClientLevel level
    ) {
        if (sessionLevel != level) {
            sessionLevel = level;

            sessionStartedAtNanos =
                    System.nanoTime();
        }
    }

    private static int getPing(
            Minecraft minecraft
    ) {
        if (
                minecraft.player == null
                        || minecraft.getConnection()
                        == null
        ) {
            return -1;
        }

        PlayerInfo playerInfo =
                minecraft.getConnection()
                        .getPlayerInfo(
                                minecraft.player
                                        .getUUID()
                        );

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
            lines.add(
                    line(item, text)
            );
        }
    }

    private static HudLine line(
            Item item,
            Component text
    ) {
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
                        Component.literal(
                                        playerName
                                )
                                .withStyle(
                                        ChatFormatting.GOLD
                                )
                )
                .append(
                        Component.literal(" FPS: ")
                                .withStyle(
                                        ChatFormatting.WHITE
                                )
                )
                .append(
                        Component.literal(
                                        Integer.toString(fps)
                                )
                                .withStyle(
                                        highIsGoodColor(
                                                fps,
                                                60,
                                                30
                                        )
                                )
                );
    }

    private static MutableComponent performanceLine(
            double tps,
            double mspt
    ) {
        return Component.empty()
                .append(
                        Component.literal("TPS: ")
                                .withStyle(
                                        ChatFormatting.WHITE
                                )
                )
                .append(
                        Component.literal(
                                        String.format(
                                                Locale.ROOT,
                                                "%.1f",
                                                tps
                                        )
                                )
                                .withStyle(
                                        tpsColor(tps)
                                )
                )
                .append(
                        Component.literal(" MSPT: ")
                                .withStyle(
                                        ChatFormatting.WHITE
                                )
                )
                .append(
                        Component.literal(
                                        String.format(
                                                Locale.ROOT,
                                                "%.2f",
                                                mspt
                                        )
                                )
                                .withStyle(
                                        msptColor(mspt)
                                )
                );
    }

    private static MutableComponent memoryLine(
            String label,
            long usedMemory,
            long maxMemory,
            int percent
    ) {
        return valueLine(
                label,
                usedMemory
                        + "/"
                        + maxMemory
                        + " MB ("
                        + percent
                        + "%)",
                lowIsGoodColor(
                        percent,
                        69,
                        84
                )
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
                                .withStyle(
                                        ChatFormatting.WHITE
                                )
                )
                .append(
                        Component.literal(value)
                                .withStyle(valueColor)
                );
    }

    private static ChatFormatting tpsColor(
            double tps
    ) {
        if (tps >= 19.0D) {
            return ChatFormatting.GREEN;
        }

        if (tps >= 15.0D) {
            return ChatFormatting.YELLOW;
        }

        return ChatFormatting.RED;
    }

    private static ChatFormatting msptColor(
            double mspt
    ) {
        if (mspt <= 45.0D) {
            return ChatFormatting.GREEN;
        }

        if (mspt <= 50.0D) {
            return ChatFormatting.YELLOW;
        }

        return ChatFormatting.RED;
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

    private static String formatDirection(
            Direction direction
    ) {
        return switch (direction) {
            case NORTH -> "North -Z";
            case SOUTH -> "South +Z";
            case WEST -> "West -X";
            case EAST -> "East +X";
            case UP -> "Up +Y";
            case DOWN -> "Down -Y";
        };
    }

    private static String formatWorldTime(
            long dayTime
    ) {
        long timeOfDay = Math.floorMod(
                dayTime + 6000L,
                24000L
        );

        int hours = (int) (
                timeOfDay / 1000L
        );

        int minutes = (int) (
                (timeOfDay % 1000L)
                        * 60L
                        / 1000L
        );

        long day = Math.floorDiv(
                dayTime,
                24000L
        ) + 1L;

        return String.format(
                Locale.ROOT,
                "%02d:%02d Day %d",
                hours,
                minutes,
                day
        );
    }

    private static String formatTicks(
            int ticks
    ) {
        long totalSeconds =
                Math.max(0, ticks) / 20L;

        return formatDuration(
                totalSeconds
        );
    }

    private static String formatSessionTime() {
        if (sessionStartedAtNanos == 0L) {
            return "00m 00s";
        }

        long totalSeconds = (
                System.nanoTime()
                        - sessionStartedAtNanos
        ) / 1_000_000_000L;

        return formatDuration(
                totalSeconds
        );
    }

    private static String formatDuration(
            long totalSeconds
    ) {
        long hours =
                totalSeconds / 3600L;

        long minutes =
                totalSeconds % 3600L / 60L;

        long seconds =
                totalSeconds % 60L;

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

    private static String humanize(
            ResourceLocation location
    ) {
        String path = location
                .getPath()
                .replace('/', ' ')
                .replace('_', ' ');

        StringBuilder result =
                new StringBuilder();

        for (
                String part
                : path.split("\\s+")
        ) {
            if (part.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(
                            part.charAt(0)
                    )
            );

            if (part.length() > 1) {
                result.append(
                        part.substring(1)
                );
            }
        }

        return result.isEmpty()
                ? location.toString()
                : result.toString();
    }

    private static int percentage(
            long used,
            long max
    ) {
        if (max <= 0L) {
            return 0;
        }

        return (int) Math.round(
                used * 100.0D / max
        );
    }

    private static long toMiB(
            long bytes
    ) {
        return bytes / 1024L / 1024L;
    }

    private record HudLine(
            ItemStack icon,
            Component text
    ) {
    }
}