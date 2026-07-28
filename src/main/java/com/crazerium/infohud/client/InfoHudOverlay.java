package com.crazerium.infohud.client;

import com.crazerium.infohud.client.data.ClientServerData;
import com.crazerium.infohud.client.layout.HudElement;
import com.crazerium.infohud.client.layout.HudLayoutManager;
import com.crazerium.infohud.client.layout.HudPosition;
import com.crazerium.infohud.client.screen.HudEditorScreen;
import com.crazerium.infohud.client.screen.InfoHudConfigScreen;
import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfoHudOverlay {
    private static boolean visible = true;
    private static ClientLevel sessionLevel;
    private static long sessionStartedAtNanos;
    private static final Pattern PERSONAL_SPACE_TEAM_PATTERN = Pattern.compile(
            "(?:team|ps_team)[_:/-]*([0-9a-fA-F-]{32,36})",
            Pattern.CASE_INSENSITIVE
    );

    private InfoHudOverlay() {
    }

    public static final IGuiOverlay OVERLAY = (
            forgeGui,
            guiGraphics,
            partialTick,
            screenWidth,
            screenHeight
    ) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (
                !visible
                        || minecraft.player == null
                        || minecraft.level == null
                        || minecraft.options.hideGui
                        || minecraft.screen instanceof InfoHudConfigScreen
                        || minecraft.screen instanceof HudEditorScreen
        ) {
            return;
        }

        List<HudLine> lines = collectHudLines(minecraft);
        float scale = ClientConfig.SCALE.get().floatValue();
        for (HudLine line : lines) {
            ElementMetrics metrics = measure(minecraft, line, scale);
            HudPosition position = HudLayoutManager.getPosition(
                    line.element(),
                    screenWidth,
                    screenHeight,
                    metrics.width(),
                    metrics.height()
            );
            HudLayoutManager.PixelPosition pixels = HudLayoutManager.toPixels(
                    position,
                    screenWidth,
                    screenHeight,
                    metrics.width(),
                    metrics.height()
            );
            renderHudLine(guiGraphics, minecraft, line, pixels.x(), pixels.y(), scale);
        }
    };

    public static List<HudLine> collectHudLines(Minecraft minecraft) {
        List<HudLine> lines = new ArrayList<>();
        if (minecraft.player == null || minecraft.level == null) {
            return lines;
        }

        updateSession(minecraft.level);

        BlockPos position = minecraft.player.blockPosition();
        int fps = minecraft.getFps();
        int ping = getPing(minecraft);
        int lightLevel = minecraft.level.getMaxLocalRawBrightness(position);

        Runtime runtime = Runtime.getRuntime();
        long clientUsedMemory = toMiB(runtime.totalMemory() - runtime.freeMemory());
        long clientMaxMemory = toMiB(runtime.maxMemory());
        int clientMemoryPercent = percentage(clientUsedMemory, clientMaxMemory);

        boolean serverDataAvailable = ClientServerData.hasFreshData();
        long serverUsedMemory = ClientServerData.getServerUsedMemoryMiB();
        long serverMaxMemory = ClientServerData.getServerMaxMemoryMiB();
        int serverMemoryPercent = percentage(serverUsedMemory, serverMaxMemory);

        String biome = minecraft.level
                .getBiome(position)
                .unwrapKey()
                .map(key -> humanize(key.location()))
                .orElse("Unknown");

        String dimension = formatDimension(
                minecraft.level.dimension().location(),
                minecraft.player.getGameProfile().getName()
        );

        long worldTime = minecraft.level.getDayTime();

        addLine(
                lines,
                HudElement.PLAYER_FPS,
                Items.EXPERIENCE_BOTTLE,
                playerLine(minecraft.player.getGameProfile().getName(), fps)
        );

        addLine(
                lines,
                HudElement.PING,
                Items.GRAY_CONCRETE,
                valueLine(
                        "Ping: ",
                        ping >= 0 ? ping + " ms" : "N/A",
                        ping >= 0 ? lowIsGoodColor(ping, 100, 200) : ChatFormatting.GRAY
                )
        );

        addLine(
                lines,
                HudElement.TPS,
                Items.REPEATER,
                serverDataAvailable
                        ? performanceLine(ClientServerData.getTps(), ClientServerData.getMspt())
                        : valueLine("TPS: ", "N/A", ChatFormatting.GRAY)
        );

        addLine(
                lines,
                HudElement.CLIENT_MEMORY,
                Items.CHEST,
                memoryLine("Client RAM: ", clientUsedMemory, clientMaxMemory, clientMemoryPercent)
        );

        addLine(
                lines,
                HudElement.SERVER_MEMORY,
                Items.ENDER_CHEST,
                serverDataAvailable
                        ? memoryLine("Server RAM: ", serverUsedMemory, serverMaxMemory, serverMemoryPercent)
                        : valueLine("Server RAM: ", "N/A", ChatFormatting.GRAY)
        );

        addLine(
                lines,
                HudElement.POSITION,
                Items.COMPASS,
                valueLine(
                        "Pos: ",
                        position.getX() + " " + position.getY() + " " + position.getZ(),
                        ChatFormatting.AQUA
                )
        );

        addLine(
                lines,
                HudElement.FACING,
                Items.RECOVERY_COMPASS,
                valueLine(
                        "Facing: ",
                        formatDirection(minecraft.player.getDirection()),
                        ChatFormatting.YELLOW
                )
        );

        addLine(
                lines,
                HudElement.DIMENSION,
                Items.ENDER_EYE,
                valueLine("Dimension: ", dimension, ChatFormatting.GOLD)
        );

        addLine(
                lines,
                HudElement.BIOME,
                Items.GRASS_BLOCK,
                valueLine("Biome: ", biome, ChatFormatting.GREEN)
        );

        addLine(
                lines,
                HudElement.LIGHT,
                Items.TORCH,
                valueLine(
                        "Light: ",
                        Integer.toString(lightLevel),
                        highIsGoodColor(lightLevel, 8, 4)
                )
        );

        addLine(
                lines,
                HudElement.WORLD_TIME,
                Items.CLOCK,
                valueLine("World Time: ", formatWorldTime(worldTime), ChatFormatting.GOLD)
        );

        addLine(
                lines,
                HudElement.PLAY_TIME,
                Items.NAME_TAG,
                serverDataAvailable
                        ? valueLine(
                        "Play Time: ",
                        formatTicks(ClientServerData.getPlayTimeTicks()),
                        ChatFormatting.YELLOW
                )
                        : valueLine("Play Time: ", "N/A", ChatFormatting.GRAY)
        );

        addLine(
                lines,
                HudElement.SESSION,
                Items.SPYGLASS,
                valueLine("Session: ", formatSessionTime(), ChatFormatting.LIGHT_PURPLE)
        );

        addLine(
                lines,
                HudElement.JUMPS,
                Items.RABBIT_FOOT,
                serverDataAvailable
                        ? valueLine(
                        "Jumps: ",
                        Integer.toString(ClientServerData.getJumps()),
                        ChatFormatting.GOLD
                )
                        : valueLine("Jumps: ", "N/A", ChatFormatting.GRAY)
        );

        return lines;
    }

    public static Map<HudElement, HudLayoutManager.ElementSize> collectElementSizes(
            Minecraft minecraft,
            List<HudLine> lines,
            float scale
    ) {
        EnumMap<HudElement, HudLayoutManager.ElementSize> result = new EnumMap<>(HudElement.class);
        for (HudLine line : lines) {
            ElementMetrics metrics = measure(minecraft, line, scale);
            result.put(
                    line.element(),
                    new HudLayoutManager.ElementSize(metrics.width(), metrics.height())
            );
        }
        return result;
    }

    public static ElementMetrics measure(Minecraft minecraft, HudLine line, float scale) {
        boolean showIcons = ClientConfig.SHOW_ICONS.get();
        int textX = showIcons ? 20 : 0;
        int unscaledWidth = textX + minecraft.font.width(line.text());
        if (showIcons) {
            unscaledWidth = Math.max(unscaledWidth, 16);
        }
        int unscaledHeight = showIcons ? 16 : minecraft.font.lineHeight;
        return new ElementMetrics(
                Math.max(1, (int) Math.ceil(unscaledWidth * scale)),
                Math.max(1, (int) Math.ceil(unscaledHeight * scale)),
                unscaledWidth,
                unscaledHeight
        );
    }

    public static void renderHudLine(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            HudLine line,
            int x,
            int y,
            float scale
    ) {
        boolean showIcons = ClientConfig.SHOW_ICONS.get();
        boolean textShadow = ClientConfig.TEXT_SHADOW.get();
        int textX = showIcons ? 20 : 0;
        int textY = showIcons ? 4 : 0;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0D);
        guiGraphics.pose().scale(scale, scale, 1.0F);

        if (showIcons) {
            guiGraphics.renderItem(line.icon(), 0, 0);
        }

        guiGraphics.drawString(
                minecraft.font,
                line.text(),
                textX,
                textY,
                0xFFFFFFFF,
                textShadow
        );

        guiGraphics.pose().popPose();
    }

    public static void toggleVisible() {
        visible = !visible;
    }

    public static boolean isVisible() {
        return visible;
    }

    private static void addLine(
            List<HudLine> lines,
            HudElement element,
            Item item,
            Component text
    ) {
        if (element.isEnabled()) {
            lines.add(new HudLine(element, new ItemStack(item), text));
        }
    }

    private static String formatDimension(ResourceLocation location, String playerName) {
        String namespace = location.getNamespace();
        String path = location.getPath();
        String fullId = (namespace + ":" + path).toLowerCase(Locale.ROOT);
        boolean isPersonalSpace = fullId.contains("personalspace")
                || fullId.contains("personal_space")
                || fullId.contains("personal-space");

        if (!isPersonalSpace) {
            return humanize(location);
        }

        Matcher teamMatcher = PERSONAL_SPACE_TEAM_PATTERN.matcher(fullId);
        if (teamMatcher.find()) {
            String teamUuid = teamMatcher.group(1).replace("-", "");
            return "ps_" + teamUuid;
        }

        return "ps_" + playerName;
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

        PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return playerInfo != null ? playerInfo.getLatency() : -1;
    }

    private static MutableComponent playerLine(String playerName, int fps) {
        return Component.empty()
                .append(Component.literal(playerName).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" FPS: ").withStyle(ChatFormatting.WHITE))
                .append(
                        Component.literal(Integer.toString(fps))
                                .withStyle(highIsGoodColor(fps, 60, 30))
                );
    }

    private static MutableComponent performanceLine(double tps, double mspt) {
        return Component.empty()
                .append(Component.literal("TPS: ").withStyle(ChatFormatting.WHITE))
                .append(
                        Component.literal(String.format(Locale.ROOT, "%.1f", tps))
                                .withStyle(tpsColor(tps))
                )
                .append(Component.literal(" MSPT: ").withStyle(ChatFormatting.WHITE))
                .append(
                        Component.literal(String.format(Locale.ROOT, "%.2f", mspt))
                                .withStyle(msptColor(mspt))
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
                usedMemory + "/" + maxMemory + " MB (" + percent + "%)",
                lowIsGoodColor(percent, 69, 84)
        );
    }

    private static MutableComponent valueLine(
            String label,
            String value,
            ChatFormatting valueColor
    ) {
        return Component.empty()
                .append(Component.literal(label).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(value).withStyle(valueColor));
    }

    private static ChatFormatting tpsColor(double tps) {
        if (tps >= 19.0D) {
            return ChatFormatting.GREEN;
        }
        if (tps >= 15.0D) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

    private static ChatFormatting msptColor(double mspt) {
        if (mspt <= 45.0D) {
            return ChatFormatting.GREEN;
        }
        if (mspt <= 50.0D) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

    private static ChatFormatting highIsGoodColor(int value, int good, int warning) {
        if (value >= good) {
            return ChatFormatting.GREEN;
        }
        if (value >= warning) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.RED;
    }

    private static ChatFormatting lowIsGoodColor(int value, int good, int warning) {
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
        long timeOfDay = Math.floorMod(dayTime + 6000L, 24000L);
        int hours = (int) (timeOfDay / 1000L);
        int minutes = (int) ((timeOfDay % 1000L) * 60L / 1000L);
        long day = Math.floorDiv(dayTime, 24000L) + 1L;
        return String.format(Locale.ROOT, "%02d:%02d Day %d", hours, minutes, day);
    }

    private static String formatTicks(int ticks) {
        long totalSeconds = Math.max(0, ticks) / 20L;
        return formatDuration(totalSeconds);
    }

    private static String formatSessionTime() {
        if (sessionStartedAtNanos == 0L) {
            return "00m 00s";
        }
        long totalSeconds = (System.nanoTime() - sessionStartedAtNanos) / 1_000_000_000L;
        return formatDuration(totalSeconds);
    }

    private static String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = totalSeconds % 3600L / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format(Locale.ROOT, "%dh %02dm %02ds", hours, minutes, seconds);
        }
        return String.format(Locale.ROOT, "%02dm %02ds", minutes, seconds);
    }

    private static String humanize(ResourceLocation location) {
        String path = location.getPath().replace('/', ' ').replace('_', ' ');
        StringBuilder result = new StringBuilder();
        for (String part : path.split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return result.isEmpty() ? location.toString() : result.toString();
    }

    private static int percentage(long used, long max) {
        if (max <= 0L) {
            return 0;
        }
        return (int) Math.round(used * 100.0D / max);
    }

    private static long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }

    public record HudLine(HudElement element, ItemStack icon, Component text) {
    }

    public record ElementMetrics(
            int width,
            int height,
            int unscaledWidth,
            int unscaledHeight
    ) {
    }
}