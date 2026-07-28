package com.crazerium.infohud.client.screen;

import com.crazerium.infohud.client.InfoHudOverlay;
import com.crazerium.infohud.client.layout.HudAnchor;
import com.crazerium.infohud.client.layout.HudElement;
import com.crazerium.infohud.client.layout.HudLayoutManager;
import com.crazerium.infohud.client.layout.HudPosition;
import com.crazerium.infohud.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private final Map<HudElement, HudPosition> workingPositions = new EnumMap<>(HudElement.class);
    private List<InfoHudOverlay.HudLine> lines = new ArrayList<>();
    private HudElement draggingElement;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean positionsLoaded;

    public HudEditorScreen(Screen parent) {
        super(Component.translatable("InfoHUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Minecraft minecraft = Minecraft.getInstance();
        this.lines = InfoHudOverlay.collectHudLines(minecraft);
        if (!this.positionsLoaded) {
            loadCurrentPositions();
            this.positionsLoaded = true;
        }

        int margin = 6;
        int gap = 3;
        int presetCount = 5;
        int available = Math.max(150, this.width - margin * 2 - gap * (presetCount - 1));
        int presetWidth = Math.max(28, Math.min(72, available / presetCount));
        int totalWidth = presetWidth * presetCount + gap * (presetCount - 1);
        int startX = (this.width - totalWidth) / 2;
        int presetY = 24;

        addPresetButton(startX, presetY, presetWidth, "↖", HudAnchor.TOP_LEFT);
        addPresetButton(startX + (presetWidth + gap), presetY, presetWidth, "↑", HudAnchor.TOP_CENTER);
        addPresetButton(startX + (presetWidth + gap) * 2, presetY, presetWidth, "↗", HudAnchor.TOP_RIGHT);
        addPresetButton(startX + (presetWidth + gap) * 3, presetY, presetWidth, "↙", HudAnchor.BOTTOM_LEFT);
        addPresetButton(startX + (presetWidth + gap) * 4, presetY, presetWidth, "↘", HudAnchor.BOTTOM_RIGHT);

        int bottomY = this.height - 24;
        int actionWidth = Math.max(62, Math.min(100, (this.width - 32) / 3));
        int actionGap = 4;
        int actionTotal = actionWidth * 3 + actionGap * 2;
        int actionX = (this.width - actionTotal) / 2;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Save"),
                                button -> saveAndClose()
                        )
                        .bounds(actionX, bottomY, actionWidth, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Reset layout"),
                                button -> resetWorkingLayout()
                        )
                        .bounds(actionX + actionWidth + actionGap, bottomY, actionWidth, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("Cancel"),
                                button -> cancelAndClose()
                        )
                        .bounds(actionX + (actionWidth + actionGap) * 2, bottomY, actionWidth, 20)
                        .build()
        );
    }

    private void addPresetButton(
            int x,
            int y,
            int width,
            String label,
            HudAnchor anchor
    ) {
        this.addRenderableWidget(
                Button.builder(
                                Component.literal(label),
                                button -> applyPreset(anchor)
                        )
                        .bounds(x, y, width, 18)
                        .build()
        );
    }

    private void loadCurrentPositions() {
        Minecraft minecraft = Minecraft.getInstance();
        float scale = ClientConfig.SCALE.get().floatValue();
        this.workingPositions.clear();
        for (InfoHudOverlay.HudLine line : this.lines) {
            InfoHudOverlay.ElementMetrics metrics = InfoHudOverlay.measure(minecraft, line, scale);
            this.workingPositions.put(
                    line.element(),
                    HudLayoutManager.getPosition(
                            line.element(),
                            this.width,
                            this.height,
                            metrics.width(),
                            metrics.height()
                    )
            );
        }
    }

    private void resetWorkingLayout() {
        Minecraft minecraft = Minecraft.getInstance();
        float scale = ClientConfig.SCALE.get().floatValue();
        this.workingPositions.clear();
        for (InfoHudOverlay.HudLine line : this.lines) {
            InfoHudOverlay.ElementMetrics metrics = InfoHudOverlay.measure(minecraft, line, scale);
            this.workingPositions.put(
                    line.element(),
                    HudLayoutManager.defaultPosition(
                            line.element(),
                            this.width,
                            this.height,
                            metrics.width(),
                            metrics.height()
                    )
            );
        }
    }

    private void resetElement(InfoHudOverlay.HudLine line) {
        Minecraft minecraft = Minecraft.getInstance();
        float scale = ClientConfig.SCALE.get().floatValue();
        InfoHudOverlay.ElementMetrics metrics = InfoHudOverlay.measure(minecraft, line, scale);
        this.workingPositions.put(
                line.element(),
                HudLayoutManager.defaultPosition(
                        line.element(),
                        this.width,
                        this.height,
                        metrics.width(),
                        metrics.height()
                )
        );
    }

    private void applyPreset(HudAnchor anchor) {
        if (this.lines.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float scale = ClientConfig.SCALE.get().floatValue();
        List<LayoutEntry> entries = new ArrayList<>();
        int maximumHeight = 1;

        for (InfoHudOverlay.HudLine line : this.lines) {
            InfoHudOverlay.ElementMetrics metrics = InfoHudOverlay.measure(minecraft, line, scale);
            entries.add(new LayoutEntry(line, metrics));
            maximumHeight = Math.max(maximumHeight, metrics.height());
        }

        int topSafe = 46;
        int bottomSafe = 30;
        int margin = 6;
        int rowGap = 3;
        int columnGap = 10;
        int rowHeight = maximumHeight + rowGap;
        int availableHeight = Math.max(rowHeight, this.height - topSafe - bottomSafe);
        int rowsPerColumn = Math.max(1, availableHeight / rowHeight);
        int columns = (entries.size() + rowsPerColumn - 1) / rowsPerColumn;
        int[] columnWidths = new int[columns];

        for (int index = 0; index < entries.size(); index++) {
            int column = index / rowsPerColumn;
            columnWidths[column] = Math.max(columnWidths[column], entries.get(index).metrics().width());
        }

        int groupWidth = 0;
        for (int width : columnWidths) {
            groupWidth += width;
        }
        groupWidth += Math.max(0, columns - 1) * columnGap;

        int maximumRows = Math.min(rowsPerColumn, entries.size());
        int groupHeight = maximumRows * rowHeight - rowGap;

        int groupX = switch (anchor) {
            case TOP_CENTER -> (this.width - groupWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> this.width - groupWidth - margin;
            default -> margin;
        };

        int groupY = switch (anchor) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> this.height - bottomSafe - groupHeight;
            default -> topSafe;
        };

        groupX = Math.max(0, groupX);
        groupY = Math.max(0, groupY);

        int[] columnOffsets = new int[columns];
        int offset = 0;
        for (int column = 0; column < columns; column++) {
            columnOffsets[column] = offset;
            offset += columnWidths[column] + columnGap;
        }

        for (int index = 0; index < entries.size(); index++) {
            LayoutEntry entry = entries.get(index);
            int column = index / rowsPerColumn;
            int row = index % rowsPerColumn;
            int x = groupX + columnOffsets[column];
            int y = groupY + row * rowHeight;
            this.workingPositions.put(
                    entry.line().element(),
                    HudLayoutManager.fromPixels(
                            x,
                            y,
                            this.width,
                            this.height,
                            entry.metrics().width(),
                            entry.metrics().height()
                    )
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        for (int index = this.lines.size() - 1; index >= 0; index--) {
            InfoHudOverlay.HudLine line = this.lines.get(index);
            ElementBox box = getBox(line);
            if (!box.contains(mouseX, mouseY)) {
                continue;
            }

            if (button == 0) {
                this.draggingElement = line.element();
                this.dragOffsetX = (int) Math.round(mouseX) - box.x();
                this.dragOffsetY = (int) Math.round(mouseY) - box.y();
                return true;
            }

            if (button == 1) {
                resetElement(line);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (button == 0 && this.draggingElement != null) {
            InfoHudOverlay.HudLine line = findLine(this.draggingElement);
            if (line == null) {
                return true;
            }

            Minecraft minecraft = Minecraft.getInstance();
            float scale = ClientConfig.SCALE.get().floatValue();
            InfoHudOverlay.ElementMetrics metrics = InfoHudOverlay.measure(minecraft, line, scale);
            int x = (int) Math.round(mouseX) - this.dragOffsetX;
            int y = (int) Math.round(mouseY) - this.dragOffsetY;
            this.workingPositions.put(
                    line.element(),
                    HudLayoutManager.fromPixels(
                            x,
                            y,
                            this.width,
                            this.height,
                            metrics.width(),
                            metrics.height()
                    )
            );
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingElement != null) {
            this.draggingElement = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private InfoHudOverlay.HudLine findLine(HudElement element) {
        for (InfoHudOverlay.HudLine line : this.lines) {
            if (line.element() == element) {
                return line;
            }
        }
        return null;
    }

    private ElementBox getBox(InfoHudOverlay.HudLine line) {
        Minecraft minecraft = Minecraft.getInstance();
        float scale = ClientConfig.SCALE.get().floatValue();
        InfoHudOverlay.ElementMetrics metrics = InfoHudOverlay.measure(minecraft, line, scale);
        HudPosition position = this.workingPositions.getOrDefault(
                line.element(),
                HudLayoutManager.defaultPosition(
                        line.element(),
                        this.width,
                        this.height,
                        metrics.width(),
                        metrics.height()
                )
        );
        HudLayoutManager.PixelPosition pixels = HudLayoutManager.toPixels(
                position,
                this.width,
                this.height,
                metrics.width(),
                metrics.height()
        );
        return new ElementBox(pixels.x(), pixels.y(), metrics.width(), metrics.height());
    }

    private void saveAndClose() {
        HudLayoutManager.saveProfile(this.workingPositions);
        if (ClientConfig.SPEC.isLoaded()) {
            ClientConfig.SPEC.save();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void cancelAndClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void onClose() {
        cancelAndClose();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(guiGraphics);
        guiGraphics.fill(0, 0, this.width, 45, 0xB0000000);
        guiGraphics.fill(0, this.height - 29, this.width, this.height, 0xB0000000);

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Left-click to drag, right-click to reset an element"),
                this.width / 2,
                6,
                0xFFFFFF55
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("Layout profile for GUI")
                        .copy()
                        .append(": ")
                        .append(Integer.toString(HudLayoutManager.currentGuiScale()))
                        .append("x"),
                this.width / 2,
                14,
                0xFFB8C7E0
        );

        Minecraft minecraft = Minecraft.getInstance();
        float scale = ClientConfig.SCALE.get().floatValue();

        for (InfoHudOverlay.HudLine line : this.lines) {
            ElementBox box = getBox(line);
            boolean hovered = box.contains(mouseX, mouseY);
            boolean dragging = line.element() == this.draggingElement;
            int border = dragging ? 0xFFFFFF55 : hovered ? 0xFF70E8FF : 0x705A6A82;
            int background = dragging ? 0x504B4B00 : hovered ? 0x50305A66 : 0x28000000;

            guiGraphics.fill(
                    box.x() - 2,
                    box.y() - 2,
                    box.x() + box.width() + 2,
                    box.y() + box.height() + 2,
                    background
            );
            drawOutline(
                    guiGraphics,
                    box.x() - 2,
                    box.y() - 2,
                    box.width() + 4,
                    box.height() + 4,
                    border
            );

            InfoHudOverlay.renderHudLine(
                    guiGraphics,
                    minecraft,
                    line,
                    box.x(),
                    box.y(),
                    scale
            );

            if (hovered || dragging) {
                int labelY = Math.max(0, box.y() - 12);
                guiGraphics.drawString(
                        this.font,
                        line.element().displayName(),
                        box.x(),
                        labelY,
                        0xFFFFFFFF,
                        true
                );
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private static void drawOutline(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private record LayoutEntry(
            InfoHudOverlay.HudLine line,
            InfoHudOverlay.ElementMetrics metrics
    ) {
    }

    private record ElementBox(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.x
                    && mouseX < this.x + this.width
                    && mouseY >= this.y
                    && mouseY < this.y + this.height;
        }
    }
}