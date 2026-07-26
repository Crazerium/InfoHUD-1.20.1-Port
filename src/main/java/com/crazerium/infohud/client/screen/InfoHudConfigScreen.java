package com.crazerium.infohud.client.screen;

import com.crazerium.infohud.client.InfoHudOverlay;
import com.crazerium.infohud.client.layout.HudAnchor;
import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Locale;

public final class InfoHudConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 170;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;

    private static final int PRESET_WIDTH = 104;
    private static final int PRESET_GAP = 4;

    private final Screen parent;

    private int xRowY;
    private int yRowY;
    private int scaleRowY;

    public InfoHudConfigScreen(Screen parent) {
        super(Component.translatable("HUD Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        addPresetButtons(centerX, 46);

        this.xRowY = 78;
        this.yRowY = 104;
        this.scaleRowY = 130;

        addAdjustmentButtons(
                centerX,
                this.xRowY,
                () -> changeX(-5),
                () -> changeX(5)
        );

        addAdjustmentButtons(
                centerX,
                this.yRowY,
                () -> changeY(-5),
                () -> changeY(5)
        );

        addAdjustmentButtons(
                centerX,
                this.scaleRowY,
                () -> changeScale(-0.1D),
                () -> changeScale(0.1D)
        );

        int leftX = centerX - BUTTON_WIDTH - 5;
        int rightX = centerX + 5;
        int startY = 164;

        addToggle(
                leftX,
                startY,
                "Show icons",
                ClientConfig.SHOW_ICONS
        );

        addToggle(
                rightX,
                startY,
                "Text shadow",
                ClientConfig.TEXT_SHADOW
        );

        addToggle(
                leftX,
                startY + ROW_SPACING,
                "Show player and FPS",
                ClientConfig.SHOW_PLAYER_FPS
        );

        addToggle(
                rightX,
                startY + ROW_SPACING,
                "Show ping",
                ClientConfig.SHOW_PING
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 2,
                "Show TPS and MSPT",
                ClientConfig.SHOW_TPS
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 2,
                "Show client RAM",
                ClientConfig.SHOW_MEMORY
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 3,
                "Show server RAM",
                ClientConfig.SHOW_SERVER_MEMORY
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 3,
                "Show position",
                ClientConfig.SHOW_POSITION
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 4,
                "Show facing",
                ClientConfig.SHOW_FACING
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 4,
                "Show dimension",
                ClientConfig.SHOW_DIMENSION
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 5,
                "Show biome",
                ClientConfig.SHOW_BIOME
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 5,
                "Show light",
                ClientConfig.SHOW_LIGHT
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 6,
                "Show world time",
                ClientConfig.SHOW_WORLD_TIME
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 6,
                "Show play time",
                ClientConfig.SHOW_PLAY_TIME
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 7,
                "Show session",
                ClientConfig.SHOW_SESSION
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 7,
                "Show jumps",
                ClientConfig.SHOW_JUMPS
        );

        int bottomY = Math.min(
                startY + ROW_SPACING * 8 + 10,
                this.height - 28
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Reset"),
                                button -> resetSettings()
                        )
                        .bounds(
                                centerX - BUTTON_WIDTH - 5,
                                bottomY,
                                BUTTON_WIDTH,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Done"),
                                button -> saveAndClose()
                        )
                        .bounds(
                                centerX + 5,
                                bottomY,
                                BUTTON_WIDTH,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    private void addPresetButtons(
            int centerX,
            int y
    ) {
        HudAnchor[] anchors = {
                HudAnchor.TOP_LEFT,
                HudAnchor.TOP_CENTER,
                HudAnchor.TOP_RIGHT,
                HudAnchor.BOTTOM_LEFT,
                HudAnchor.BOTTOM_RIGHT
        };

        String[] names = {
                "Top left",
                "Top center",
                "Top right",
                "Bottom left",
                "Bottom right"
        };

        int totalWidth =
                PRESET_WIDTH * anchors.length
                        + PRESET_GAP * (anchors.length - 1);

        int startX = centerX - totalWidth / 2;

        for (int index = 0; index < anchors.length; index++) {
            HudAnchor anchor = anchors[index];
            String name = names[index];

            this.addRenderableWidget(
                    Button.builder(
                                    Component.translatable(name),
                                    button -> {
                                        ClientConfig.ANCHOR.set(anchor);
                                    }
                            )
                            .bounds(
                                    startX
                                            + index
                                            * (PRESET_WIDTH + PRESET_GAP),
                                    y,
                                    PRESET_WIDTH,
                                    BUTTON_HEIGHT
                            )
                            .build()
            );
        }
    }

    private void addAdjustmentButtons(
            int centerX,
            int y,
            Runnable decrease,
            Runnable increase
    ) {
        this.addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> decrease.run()
                        )
                        .bounds(
                                centerX - 115,
                                y,
                                40,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> increase.run()
                        )
                        .bounds(
                                centerX + 75,
                                y,
                                40,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    private void addToggle(
            int x,
            int y,
            String name,
            ForgeConfigSpec.BooleanValue configValue
    ) {
        this.addRenderableWidget(
                Button.builder(
                                toggleLabel(
                                        name,
                                        configValue.get()
                                ),
                                button -> {
                                    boolean newValue =
                                            !configValue.get();

                                    configValue.set(newValue);

                                    button.setMessage(
                                            toggleLabel(
                                                    name,
                                                    newValue
                                            )
                                    );
                                }
                        )
                        .bounds(
                                x,
                                y,
                                BUTTON_WIDTH,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    private static Component toggleLabel(
            String name,
            boolean enabled
    ) {
        return Component.translatable(name)
                .append(": ")
                .append(
                        Component.translatable(
                                enabled ? "On" : "Off"
                        )
                );
    }

    private static void changeX(int amount) {
        InfoHudOverlay.switchToCustomPosition();

        int value =
                ClientConfig.X.get() + amount;

        ClientConfig.X.set(
                Math.max(
                        0,
                        Math.min(10000, value)
                )
        );
    }

    private static void changeY(int amount) {
        InfoHudOverlay.switchToCustomPosition();

        int value =
                ClientConfig.Y.get() + amount;

        ClientConfig.Y.set(
                Math.max(
                        0,
                        Math.min(10000, value)
                )
        );
    }

    private static void changeScale(double amount) {
        double value =
                ClientConfig.SCALE.get() + amount;

        value = Math.max(
                0.5D,
                Math.min(3.0D, value)
        );

        value =
                Math.round(value * 10.0D) / 10.0D;

        ClientConfig.SCALE.set(value);
    }

    private void resetSettings() {
        ClientConfig.X.set(4);
        ClientConfig.Y.set(4);
        ClientConfig.SCALE.set(1.0D);
        ClientConfig.ANCHOR.set(HudAnchor.TOP_LEFT);

        ClientConfig.SHOW_ICONS.set(true);
        ClientConfig.TEXT_SHADOW.set(true);

        ClientConfig.SHOW_PLAYER_FPS.set(true);
        ClientConfig.SHOW_PING.set(true);
        ClientConfig.SHOW_TPS.set(true);
        ClientConfig.SHOW_MEMORY.set(true);
        ClientConfig.SHOW_SERVER_MEMORY.set(true);
        ClientConfig.SHOW_POSITION.set(true);
        ClientConfig.SHOW_FACING.set(true);
        ClientConfig.SHOW_DIMENSION.set(true);
        ClientConfig.SHOW_BIOME.set(true);
        ClientConfig.SHOW_LIGHT.set(true);
        ClientConfig.SHOW_WORLD_TIME.set(true);
        ClientConfig.SHOW_PLAY_TIME.set(true);
        ClientConfig.SHOW_SESSION.set(true);
        ClientConfig.SHOW_JUMPS.set(true);

        if (this.minecraft != null) {
            this.minecraft.setScreen(
                    new InfoHudConfigScreen(this.parent)
            );
        }
    }

    private void saveAndClose() {
        if (ClientConfig.SPEC.isLoaded()) {
            ClientConfig.SPEC.save();
        }

        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static String anchorName(
            HudAnchor anchor
    ) {
        return switch (anchor) {
            case CUSTOM -> "Custom";
            case TOP_LEFT -> "Top left";
            case TOP_CENTER -> "Top center";
            case TOP_RIGHT -> "Top right";
            case BOTTOM_LEFT -> "Bottom left";
            case BOTTOM_RIGHT -> "Bottom right";
        };
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                10,
                0xFFFFFFFF
        );

        Component presetText =
                Component.translatable("Position preset")
                        .append(": ")
                        .append(
                                Component.translatable(
                                        anchorName(
                                                ClientConfig.ANCHOR.get()
                                        )
                                )
                        );

        guiGraphics.drawCenteredString(
                this.font,
                presetText,
                this.width / 2,
                28,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Position X")
                        .append(
                                ": " + ClientConfig.X.get()
                        ),
                this.width / 2,
                this.xRowY + 6,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Position Y")
                        .append(
                                ": " + ClientConfig.Y.get()
                        ),
                this.width / 2,
                this.yRowY + 6,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Scale")
                        .append(
                                ": " + String.format(
                                        Locale.ROOT,
                                        "%.1f",
                                        ClientConfig.SCALE.get()
                                )
                        ),
                this.width / 2,
                this.scaleRowY + 6,
                0xFFFFFFFF
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }
}