package com.crazerium.infohud.client.layout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HudLayoutManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("infohud-layout.json");
    private static LayoutData data;

    private HudLayoutManager() {
    }

    public static int currentGuiScale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() == null) {
            return 1;
        }
        return Math.max(1, Math.min(8, (int) Math.round(minecraft.getWindow().getGuiScale())));
    }

    public static HudPosition getPosition(
            HudElement element,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight
    ) {
        ensureLoaded();
        ProfileData profile = getProfile(false);
        if (profile != null) {
            PositionData stored = profile.elements.get(element.id());
            if (stored != null) {
                return new HudPosition(stored.x, stored.y).clamped();
            }
        }
        return defaultPosition(element, screenWidth, screenHeight, elementWidth, elementHeight);
    }

    public static Map<HudElement, HudPosition> snapshot(
            Map<HudElement, ElementSize> sizes,
            int screenWidth,
            int screenHeight
    ) {
        EnumMap<HudElement, HudPosition> result = new EnumMap<>(HudElement.class);
        for (HudElement element : HudElement.values()) {
            ElementSize size = sizes.getOrDefault(element, new ElementSize(1, 1));
            result.put(
                    element,
                    getPosition(element, screenWidth, screenHeight, size.width(), size.height())
            );
        }
        return result;
    }

    public static void saveProfile(Map<HudElement, HudPosition> positions) {
        ensureLoaded();
        ProfileData profile = getProfile(true);
        for (Map.Entry<HudElement, HudPosition> entry : positions.entrySet()) {
            HudPosition position = entry.getValue().clamped();
            profile.elements.put(
                    entry.getKey().id(),
                    new PositionData(position.x(), position.y())
            );
        }
        save();
    }

    public static void setPosition(HudElement element, HudPosition position) {
        ensureLoaded();
        ProfileData profile = getProfile(true);
        HudPosition clamped = position.clamped();
        profile.elements.put(element.id(), new PositionData(clamped.x(), clamped.y()));
    }

    public static void resetElement(HudElement element) {
        ensureLoaded();
        ProfileData profile = getProfile(false);
        if (profile != null) {
            profile.elements.remove(element.id());
        }
    }

    public static void resetCurrentProfile() {
        ensureLoaded();
        data.profiles.remove(profileKey());
        save();
    }

    public static void resetAll() {
        ensureLoaded();
        data.profiles.clear();
        save();
    }

    public static HudPosition defaultPosition(
            HudElement element,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight
    ) {
        int guiScale = currentGuiScale();
        int columns = guiScale >= 3 ? 2 : 1;
        int rows = (HudElement.values().length + columns - 1) / columns;
        int column = element.order() / rows;
        int row = element.order() % rows;
        int spacingY = Math.max(18, elementHeight + 2);
        int pixelX = columns == 1 ? 4 : 4 + column * Math.max(1, screenWidth / columns);
        int pixelY = 4 + row * spacingY;
        return fromPixels(pixelX, pixelY, screenWidth, screenHeight, elementWidth, elementHeight);
    }

    public static HudPosition fromPixels(
            int x,
            int y,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight
    ) {
        int maxX = Math.max(0, screenWidth - Math.max(1, elementWidth));
        int maxY = Math.max(0, screenHeight - Math.max(1, elementHeight));
        int clampedX = Math.max(0, Math.min(maxX, x));
        int clampedY = Math.max(0, Math.min(maxY, y));
        double normalizedX = maxX == 0 ? 0.0D : clampedX / (double) maxX;
        double normalizedY = maxY == 0 ? 0.0D : clampedY / (double) maxY;
        return new HudPosition(normalizedX, normalizedY);
    }

    public static PixelPosition toPixels(
            HudPosition position,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight
    ) {
        HudPosition clamped = position.clamped();
        int maxX = Math.max(0, screenWidth - Math.max(1, elementWidth));
        int maxY = Math.max(0, screenHeight - Math.max(1, elementHeight));
        return new PixelPosition(
                (int) Math.round(clamped.x() * maxX),
                (int) Math.round(clamped.y() * maxY)
        );
    }

    private static ProfileData getProfile(boolean create) {
        String key = profileKey();
        ProfileData profile = data.profiles.get(key);
        if (profile == null && create) {
            profile = new ProfileData();
            data.profiles.put(key, profile);
        }
        return profile;
    }

    private static String profileKey() {
        return "gui_scale_" + currentGuiScale();
    }

    private static void ensureLoaded() {
        if (data != null) {
            return;
        }
        data = new LayoutData();
        if (Files.notExists(PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            LayoutData loaded = GSON.fromJson(reader, LayoutData.class);
            if (loaded != null) {
                data = loaded;
            }
        } catch (Exception ignored) {
            data = new LayoutData();
        }
        if (data.profiles == null) {
            data.profiles = new HashMap<>();
        }
        for (ProfileData profile : data.profiles.values()) {
            if (profile.elements == null) {
                profile.elements = new LinkedHashMap<>();
            }
        }
    }

    private static void save() {
        try {
            Path parent = PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = PATH.resolveSibling(PATH.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(
                        temporary,
                        PATH,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException ignored) {
                Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
        }
    }

    public record ElementSize(int width, int height) {
    }

    public record PixelPosition(int x, int y) {
    }

    private static final class LayoutData {
        int version = 1;
        Map<String, ProfileData> profiles = new HashMap<>();
    }

    private static final class ProfileData {
        Map<String, PositionData> elements = new LinkedHashMap<>();
    }

    private static final class PositionData {
        double x;
        double y;

        PositionData() {
        }

        PositionData(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}