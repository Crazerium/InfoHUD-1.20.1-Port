package com.crazerium.infohud.client.screen;

import com.crazerium.infohud.client.layout.HudElement;
import com.crazerium.infohud.client.layout.HudLayoutManager;
import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Locale;

public final class InfoHudConfigScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_HEIGHT = 23;

    private final Screen parent;
    private int page;
    private int pageCount = 1;
    private int toggleStartY;
    private int toggleBottomY;
    private int columns;
    private int rowsPerPage;

    public InfoHudConfigScreen(Screen parent) {
        super(Component.translatable("HUD Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        buildWidgets();
    }

    private void buildWidgets() {
        int margin = 8;
        int gap = 4;
        int centerX = this.width / 2;
        int wideButtonWidth = Math.max(140, Math.min(260, this.width - margin * 2));

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Open layout editor"),
                                button -> openEditor()
                        )
                        .bounds(
                                centerX - wideButtonWidth / 2,
                                25,
                                wideButtonWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        int scaleControlWidth = Math.max(26, Math.min(40, this.width / 8));
        int scaleLeftX = Math.max(margin, centerX - 88);
        int scaleRightX = Math.min(
                this.width - margin - scaleControlWidth,
                centerX + 88 - scaleControlWidth
        );
        this.addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> changeScale(-0.1D)
                        )
                        .bounds(
                                scaleLeftX,
                                49,
                                scaleControlWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> changeScale(0.1D)
                        )
                        .bounds(
                                scaleRightX,
                                49,
                                scaleControlWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        int optionWidth = Math.max(90, Math.min(170, (this.width - margin * 2 - gap) / 2));
        int optionsTotal = optionWidth * 2 + gap;
        int optionsX = centerX - optionsTotal / 2;

        addToggle(
                optionsX,
                73,
                optionWidth,
                "Show icons",
                ClientConfig.SHOW_ICONS
        );

        addToggle(
                optionsX + optionWidth + gap,
                73,
                optionWidth,
                "Text shadow",
                ClientConfig.TEXT_SHADOW
        );

        this.toggleStartY = 101;
        this.toggleBottomY = this.height - 31;
        this.columns = this.width >= 330 ? 2 : 1;
        this.rowsPerPage = Math.max(
                1,
                (this.toggleBottomY - this.toggleStartY) / ROW_HEIGHT
        );

        int itemsPerPage = Math.max(1, this.rowsPerPage * this.columns);
        this.pageCount = Math.max(
                1,
                (HudElement.values().length + itemsPerPage - 1) / itemsPerPage
        );
        this.page = Math.max(0, Math.min(this.pageCount - 1, this.page));

        int toggleWidth = this.columns == 2
                ? Math.max(100, (this.width - margin * 2 - gap) / 2)
                : Math.max(140, this.width - margin * 2);
        int toggleAreaWidth = toggleWidth * this.columns + gap * (this.columns - 1);
        int toggleAreaX = centerX - toggleAreaWidth / 2;
        int startIndex = this.page * itemsPerPage;
        int endIndex = Math.min(HudElement.values().length, startIndex + itemsPerPage);

        for (int index = startIndex; index < endIndex; index++) {
            int localIndex = index - startIndex;
            int column = localIndex % this.columns;
            int row = localIndex / this.columns;
            HudElement element = HudElement.values()[index];
            addElementToggle(
                    toggleAreaX + column * (toggleWidth + gap),
                    this.toggleStartY + row * ROW_HEIGHT,
                    toggleWidth,
                    element
            );
        }

        int bottomY = this.height - 24;
        int smallWidth = Math.max(28, Math.min(48, this.width / 9));
        int actionWidth = Math.max(
                44,
                Math.min(
                        100,
                        (this.width - margin * 2 - smallWidth * 2 - gap * 3) / 2
                )
        );
        int totalWidth = smallWidth + gap + actionWidth + gap + actionWidth + gap + smallWidth;
        int bottomX = centerX - totalWidth / 2;

        Button previous = Button.builder(
                        Component.literal("<"),
                        button -> changePage(-1)
                )
                .bounds(bottomX, bottomY, smallWidth, BUTTON_HEIGHT)
                .build();
        previous.active = this.page > 0;
        this.addRenderableWidget(previous);

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Reset"),
                                button -> resetSettings()
                        )
                        .bounds(
                                bottomX + smallWidth + gap,
                                bottomY,
                                actionWidth,
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
                                bottomX + smallWidth + gap + actionWidth + gap,
                                bottomY,
                                actionWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );

        Button next = Button.builder(
                        Component.literal(">"),
                        button -> changePage(1)
                )
                .bounds(
                        bottomX + smallWidth + gap + actionWidth + gap + actionWidth + gap,
                        bottomY,
                        smallWidth,
                        BUTTON_HEIGHT
                )
                .build();
        next.active = this.page < this.pageCount - 1;
        this.addRenderableWidget(next);
    }

    private void addElementToggle(int x, int y, int width, HudElement element) {
        this.addRenderableWidget(
                Button.builder(
                                toggleLabel(element.displayName(), element.isEnabled()),
                                button -> {
                                    boolean enabled = !element.isEnabled();
                                    element.configValue().set(enabled);
                                    button.setMessage(toggleLabel(element.displayName(), enabled));
                                }
                        )
                        .bounds(x, y, width, BUTTON_HEIGHT)
                        .build()
        );
    }

    private void addToggle(
            int x,
            int y,
            int width,
            String translationKey,
            ForgeConfigSpec.BooleanValue value
    ) {
        this.addRenderableWidget(
                Button.builder(
                                toggleLabel(Component.translatable(translationKey), value.get()),
                                button -> {
                                    boolean enabled = !value.get();
                                    value.set(enabled);
                                    button.setMessage(
                                            toggleLabel(Component.translatable(translationKey), enabled)
                                    );
                                }
                        )
                        .bounds(x, y, width, BUTTON_HEIGHT)
                        .build()
        );
    }

    private static Component toggleLabel(Component name, boolean enabled) {
        return name.copy()
                .append(": ")
                .append(Component.translatable(enabled ? "On" : "Off"));
    }

    private void openEditor() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new HudEditorScreen(this));
        }
    }

    private void changePage(int direction) {
        int nextPage = Math.max(0, Math.min(this.pageCount - 1, this.page + direction));
        if (nextPage == this.page) {
            return;
        }
        this.page = nextPage;
        this.clearWidgets();
        buildWidgets();
    }

    private void changeScale(double amount) {
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

        for (HudElement element : HudElement.values()) {
            element.configValue().set(true);
        }

        HudLayoutManager.resetAll();
        this.page = 0;
        this.clearWidgets();
        buildWidgets();
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
        guiGraphics.fill(0, 0, this.width, 96, 0x70000000);
        guiGraphics.fill(0, this.toggleStartY - 5, this.width, this.toggleBottomY, 0x35000000);
        guiGraphics.fill(0, this.height - 29, this.width, this.height, 0x70000000);

        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                8,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Scale")
                        .copy()
                        .append(": ")
                        .append(String.format(Locale.ROOT, "%.1f", ClientConfig.SCALE.get())),
                this.width / 2,
                55,
                0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("GUI profile")
                        .copy()
                        .append(": ")
                        .append(Integer.toString(HudLayoutManager.currentGuiScale()))
                        .append("x"),
                this.width / 2,
                89,
                0xFFB8C7E0
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Page")
                        .copy()
                        .append(": ")
                        .append(Integer.toString(this.page + 1))
                        .append(" / ")
                        .append(Integer.toString(this.pageCount)),
                this.width / 2,
                this.height - 34,
                0xFFB8C7E0
        );

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}