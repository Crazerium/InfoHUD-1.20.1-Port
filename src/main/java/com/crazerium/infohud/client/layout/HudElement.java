package com.crazerium.infohud.client.layout;

import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public enum HudElement {
    PLAYER_FPS("player_fps", "Show player and FPS", 0),
    PING("ping", "Show ping", 1),
    TPS("tps", "Show TPS and MSPT", 2),
    CLIENT_MEMORY("client_memory", "Show client RAM", 3),
    SERVER_MEMORY("server_memory", "Show server RAM", 4),
    POSITION("position", "Show position", 5),
    FACING("facing", "Show facing", 6),
    DIMENSION("dimension", "Show dimension", 7),
    BIOME("biome", "Show biome", 8),
    LIGHT("light", "Show light", 9),
    WORLD_TIME("world_time", "Show world time", 10),
    PLAY_TIME("play_time", "Show play time", 11),
    SESSION("session", "Show session", 12),
    JUMPS("jumps", "Show jumps", 13);

    private final String id;
    private final String translationKey;
    private final int order;

    HudElement(String id, String translationKey, int order) {
        this.id = id;
        this.translationKey = translationKey;
        this.order = order;
    }

    public String id() {
        return this.id;
    }

    public Component displayName() {
        return Component.translatable(this.translationKey);
    }

    public int order() {
        return this.order;
    }

    public ForgeConfigSpec.BooleanValue configValue() {
        return switch (this) {
            case PLAYER_FPS -> ClientConfig.SHOW_PLAYER_FPS;
            case PING -> ClientConfig.SHOW_PING;
            case TPS -> ClientConfig.SHOW_TPS;
            case CLIENT_MEMORY -> ClientConfig.SHOW_MEMORY;
            case SERVER_MEMORY -> ClientConfig.SHOW_SERVER_MEMORY;
            case POSITION -> ClientConfig.SHOW_POSITION;
            case FACING -> ClientConfig.SHOW_FACING;
            case DIMENSION -> ClientConfig.SHOW_DIMENSION;
            case BIOME -> ClientConfig.SHOW_BIOME;
            case LIGHT -> ClientConfig.SHOW_LIGHT;
            case WORLD_TIME -> ClientConfig.SHOW_WORLD_TIME;
            case PLAY_TIME -> ClientConfig.SHOW_PLAY_TIME;
            case SESSION -> ClientConfig.SHOW_SESSION;
            case JUMPS -> ClientConfig.SHOW_JUMPS;
        };
    }

    public boolean isEnabled() {
        return configValue().get();
    }
}