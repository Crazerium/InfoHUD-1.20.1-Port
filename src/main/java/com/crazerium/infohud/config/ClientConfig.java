package com.crazerium.infohud.config;

import com.crazerium.infohud.client.layout.HudAnchor;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue X;
    public static final ForgeConfigSpec.IntValue Y;
    public static final ForgeConfigSpec.DoubleValue SCALE;
    public static final ForgeConfigSpec.EnumValue<HudAnchor> ANCHOR;

    public static final ForgeConfigSpec.BooleanValue SHOW_ICONS;
    public static final ForgeConfigSpec.BooleanValue TEXT_SHADOW;

    public static final ForgeConfigSpec.BooleanValue SHOW_PLAYER_FPS;
    public static final ForgeConfigSpec.BooleanValue SHOW_PING;
    public static final ForgeConfigSpec.BooleanValue SHOW_TPS;
    public static final ForgeConfigSpec.BooleanValue SHOW_MEMORY;
    public static final ForgeConfigSpec.BooleanValue SHOW_SERVER_MEMORY;
    public static final ForgeConfigSpec.BooleanValue SHOW_POSITION;
    public static final ForgeConfigSpec.BooleanValue SHOW_FACING;
    public static final ForgeConfigSpec.BooleanValue SHOW_DIMENSION;
    public static final ForgeConfigSpec.BooleanValue SHOW_BIOME;
    public static final ForgeConfigSpec.BooleanValue SHOW_LIGHT;
    public static final ForgeConfigSpec.BooleanValue SHOW_WORLD_TIME;
    public static final ForgeConfigSpec.BooleanValue SHOW_PLAY_TIME;
    public static final ForgeConfigSpec.BooleanValue SHOW_SESSION;
    public static final ForgeConfigSpec.BooleanValue SHOW_JUMPS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("layout");

        X = builder.defineInRange("x", 4, 0, 10000);
        Y = builder.defineInRange("y", 4, 0, 10000);

        SCALE = builder.defineInRange(
                "scale",
                1.0D,
                0.5D,
                3.0D
        );

        ANCHOR = builder.defineEnum(
                "anchor",
                HudAnchor.CUSTOM
        );

        SHOW_ICONS = builder.define(
                "show_icons",
                true
        );

        TEXT_SHADOW = builder.define(
                "text_shadow",
                true
        );

        builder.pop();
        builder.push("lines");

        SHOW_PLAYER_FPS = builder.define(
                "show_player_fps",
                true
        );

        SHOW_PING = builder.define(
                "show_ping",
                true
        );

        SHOW_TPS = builder.define(
                "show_tps",
                true
        );

        SHOW_MEMORY = builder.define(
                "show_memory",
                true
        );

        SHOW_SERVER_MEMORY = builder.define(
                "show_server_memory",
                true
        );

        SHOW_POSITION = builder.define(
                "show_position",
                true
        );

        SHOW_FACING = builder.define(
                "show_facing",
                true
        );

        SHOW_DIMENSION = builder.define(
                "show_dimension",
                true
        );

        SHOW_BIOME = builder.define(
                "show_biome",
                true
        );

        SHOW_LIGHT = builder.define(
                "show_light",
                true
        );

        SHOW_WORLD_TIME = builder.define(
                "show_world_time",
                true
        );

        SHOW_PLAY_TIME = builder.define(
                "show_play_time",
                true
        );

        SHOW_SESSION = builder.define(
                "show_session",
                true
        );

        SHOW_JUMPS = builder.define(
                "show_jumps",
                true
        );

        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}