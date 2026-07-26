package com.crazerium.infohud.client.screen;

import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Locale;

public final class InfoHudConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;

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

        this.xRowY = 38;
        this.yRowY = 64;
        this.scaleRowY = 90;

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
        int startY = 125;

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
                "Show memory",
                ClientConfig.SHOW_MEMORY
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 2,
                "Show position",
                ClientConfig.SHOW_POSITION
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 3,
                "Show facing",
                ClientConfig.SHOW_FACING
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 3,
                "Show dimension",
                ClientConfig.SHOW_DIMENSION
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 4,
                "Show biome",
                ClientConfig.SHOW_BIOME
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 4,
                "Show light",
                ClientConfig.SHOW_LIGHT
        );

        addToggle(
                leftX,
                startY + ROW_SPACING * 5,
                "Show world time",
                ClientConfig.SHOW_WORLD_TIME
        );

        addToggle(
                rightX,
                startY + ROW_SPACING * 5,
                "Show session",
                ClientConfig.SHOW_SESSION
        );

        int bottomY = Math.min(
                startY + ROW_SPACING * 6 + 10,
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
                                toggleLabel(name, configValue.get()),
                                button -> {
                                    boolean newValue = !configValue.get();

                                    configValue.set(newValue);
                                    button.setMessage(
                                            toggleLabel(name, newValue)
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
        int value = ClientConfig.X.get() + amount;

        ClientConfig.X.set(
                Math.max(0, Math.min(10000, value))
        );
    }

    private static void changeY(int amount) {
        int value = ClientConfig.Y.get() + amount;

        ClientConfig.Y.set(
                Math.max(0, Math.min(10000, value))
        );
    }

    private static void changeScale(double amount) {
        double value = ClientConfig.SCALE.get() + amount;

        value = Math.max(0.5D, Math.min(3.0D, value));
        value = Math.round(value * 10.0D) / 10.0D;

        ClientConfig.SCALE.set(value);
    }

    private void resetSettings() {
        ClientConfig.X.set(4);
        ClientConfig.Y.set(4);
        ClientConfig.SCALE.set(1.0D);

        ClientConfig.SHOW_ICONS.set(true);
        ClientConfig.TEXT_SHADOW.set(true);

        ClientConfig.SHOW_PLAYER_FPS.set(true);
        ClientConfig.SHOW_PING.set(true);
        ClientConfig.SHOW_MEMORY.set(true);
        ClientConfig.SHOW_POSITION.set(true);
        ClientConfig.SHOW_FACING.set(true);
        ClientConfig.SHOW_DIMENSION.set(true);
        ClientConfig.SHOW_BIOME.set(true);
        ClientConfig.SHOW_LIGHT.set(true);
        ClientConfig.SHOW_WORLD_TIME.set(true);
        ClientConfig.SHOW_SESSION.set(true);

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
                15,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Position X")
                        .append(": " + ClientConfig.X.get()),
                this.width / 2,
                this.xRowY + 6,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Position Y")
                        .append(": " + ClientConfig.Y.get()),
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