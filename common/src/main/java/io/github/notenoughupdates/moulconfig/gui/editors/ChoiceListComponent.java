/*
 * Copyright (C) 2026 SoftConfig contributors
 *
 * This file is part of SoftConfig.
 *
 * SoftConfig is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * SoftConfig is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SoftConfig. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package io.github.notenoughupdates.moulconfig.gui.editors;

import io.github.notenoughupdates.moulconfig.common.IFontRenderer;
import io.github.notenoughupdates.moulconfig.common.IMinecraft;
import io.github.notenoughupdates.moulconfig.common.RenderContext;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.GuiComponent;
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext;
import io.github.notenoughupdates.moulconfig.gui.MouseEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

final class ChoiceListComponent<T> extends GuiComponent {
    private static final int ROW_HEIGHT = 12;
    private static final int BORDER_WIDTH = 1;
    private static final int TEXT_LEFT = 3;
    private static final int TEXT_TOP = 2;
    private static final int TEXT_RIGHT_INSET = 8;
    private static final int SCROLL_STEP = 12;
    private static final int SCROLLBAR_EDGE_INSET = 1;
    private static final int SCROLLBAR_RIGHT_INSET = 3;
    private static final int SCROLLBAR_WIDTH = 2;
    private static final int MIN_THUMB_HEIGHT = 8;
    private static final int MIN_OVERLAY_SIZE = 16;
    private static final int BACKGROUND_COLOR = 0xFF201F26;
    private static final int OUTLINE_COLOR = 0xFF404047;
    private static final int HOVER_COLOR = 0xFF303039;
    private static final int TEXT_COLOR = 0xFFA0A0A0;
    private static final int SCROLLBAR_TRACK_COLOR = 0x44FFFFFF;
    private static final int SCROLLBAR_THUMB_COLOR = 0xAAAAAAAA;

    private final List<T> choices;
    private final int visibleWidth;
    private final int visibleHeight;
    private final Function<T, StructuredText> label;
    private final Consumer<T> selected;
    private final Runnable dismissed;
    private int scrollOffset;

    ChoiceListComponent(
        List<T> choices,
        int visibleWidth,
        int visibleHeight,
        Function<T, StructuredText> label,
        Consumer<T> selected,
        Runnable dismissed
    ) {
        this.choices = choices;
        this.visibleWidth = visibleWidth;
        this.visibleHeight = visibleHeight;
        this.label = label;
        this.selected = selected;
        this.dismissed = dismissed;
    }

    static int contentHeight(int choiceCount) {
        return choiceCount * ROW_HEIGHT + BORDER_WIDTH * 2;
    }

    static <T> Placement measure(
        List<T> choices,
        Function<T, StructuredText> label,
        int anchorX,
        int anchorY,
        int minimumWidth,
        int maxVisibleChoices,
        int horizontalInset,
        int screenMargin
    ) {
        IMinecraft minecraft = IMinecraft.INSTANCE;
        return measure(
            choices,
            label,
            minecraft.getDefaultFontRenderer()::getStringWidth,
            anchorX,
            anchorY,
            minecraft.getScaledWidth(),
            minecraft.getScaledHeight(),
            minimumWidth,
            maxVisibleChoices,
            horizontalInset,
            screenMargin
        );
    }

    static <T> Placement measure(
        List<T> choices,
        Function<T, StructuredText> label,
        ToIntFunction<StructuredText> textWidth,
        int anchorX,
        int anchorY,
        int screenWidth,
        int screenHeight,
        int minimumWidth,
        int maxVisibleChoices,
        int horizontalInset,
        int screenMargin
    ) {
        int widestLabel = choices.stream()
            .map(label)
            .mapToInt(textWidth)
            .max()
            .orElse(0);
        int availableWidth = Math.max(MIN_OVERLAY_SIZE, screenWidth - screenMargin * 2);
        int visibleWidth = Math.min(Math.max(minimumWidth, widestLabel + horizontalInset), availableWidth);
        int availableHeight = Math.max(MIN_OVERLAY_SIZE, screenHeight - screenMargin * 2);
        int desiredHeight = contentHeight(Math.min(choices.size(), maxVisibleChoices));
        int visibleHeight = Math.min(desiredHeight, availableHeight);
        int x = clampToScreen(anchorX, visibleWidth, screenWidth, screenMargin);
        int y = clampToScreen(anchorY, visibleHeight, screenHeight, screenMargin);
        return new Placement(x, y, visibleWidth, visibleHeight);
    }

    private static int clampToScreen(int position, int size, int screenSize, int margin) {
        return Math.max(margin, Math.min(position, screenSize - size - margin));
    }

    @Override
    public int getWidth() {
        return visibleWidth;
    }

    @Override
    public int getHeight() {
        return visibleHeight;
    }

    @Override
    public boolean mouseEvent(MouseEvent mouseEvent, GuiImmediateContext context) {
        clampScroll(context.getHeight());
        if (context.isHovered() && mouseEvent instanceof MouseEvent.Scroll) {
            MouseEvent.Scroll scroll = (MouseEvent.Scroll) mouseEvent;
            int target = (int) (scrollOffset - scroll.getDWheel() * SCROLL_STEP);
            scrollOffset = Math.max(0, Math.min(target, maxScrollOffset(context.getHeight())));
            return true;
        }
        if (!(mouseEvent instanceof MouseEvent.Click) || !((MouseEvent.Click) mouseEvent).getMouseState()) {
            return false;
        }
        if (!context.isHovered()) {
            dismissed.run();
            return true;
        }
        int contentY = context.getMouseY() + scrollOffset - BORDER_WIDTH;
        if (contentY >= 0 && contentY < choices.size() * ROW_HEIGHT) {
            int selectedIndex = contentY / ROW_HEIGHT;
            selected.accept(choices.get(selectedIndex));
        }
        return true;
    }

    @Override
    public void render(GuiImmediateContext context) {
        clampScroll(context.getHeight());
        RenderContext render = context.getRenderContext();
        render.drawColoredRect(0, 0, context.getWidth(), context.getHeight(), OUTLINE_COLOR);
        render.drawColoredRect(
            BORDER_WIDTH,
            BORDER_WIDTH,
            context.getWidth() - BORDER_WIDTH,
            context.getHeight() - BORDER_WIDTH,
            BACKGROUND_COLOR
        );
        render.pushScissor(BORDER_WIDTH, BORDER_WIDTH, context.getWidth() - BORDER_WIDTH, context.getHeight() - BORDER_WIDTH);
        render.pushMatrix();
        render.translate(0, -scrollOffset);
        IFontRenderer font = render.getMinecraft().getDefaultFontRenderer();
        for (int index = 0; index < choices.size(); index++) {
            int rowTop = BORDER_WIDTH + index * ROW_HEIGHT;
            int mouseContentY = context.getMouseY() + scrollOffset;
            if (context.getMouseX() >= 0 && context.getMouseX() < context.getWidth()
                && mouseContentY >= rowTop && mouseContentY < rowTop + ROW_HEIGHT) {
                render.drawColoredRect(
                    BORDER_WIDTH,
                    rowTop,
                    context.getWidth() - BORDER_WIDTH,
                    rowTop + ROW_HEIGHT,
                    HOVER_COLOR
                );
            }
            render.drawStringScaledMaxWidth(
                label.apply(choices.get(index)),
                font,
                TEXT_LEFT,
                rowTop + TEXT_TOP,
                false,
                context.getWidth() - TEXT_RIGHT_INSET,
                TEXT_COLOR
            );
        }
        render.popMatrix();
        render.popScissor();
        drawScrollbar(context);
    }

    private void clampScroll(int height) {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset(height)));
    }

    private int maxScrollOffset(int height) {
        return Math.max(0, contentHeight(choices.size()) - height);
    }

    private void drawScrollbar(GuiImmediateContext context) {
        int contentHeight = contentHeight(choices.size());
        if (contentHeight <= context.getHeight()) {
            return;
        }
        int trackHeight = context.getHeight() - SCROLLBAR_EDGE_INSET * 2;
        int thumbHeight = Math.max(MIN_THUMB_HEIGHT, trackHeight * context.getHeight() / contentHeight);
        int maxScroll = contentHeight - context.getHeight();
        int thumbTop = SCROLLBAR_EDGE_INSET
            + (int) ((trackHeight - thumbHeight) * scrollOffset / (float) maxScroll);
        int scrollbarX = context.getWidth() - SCROLLBAR_RIGHT_INSET;
        context.getRenderContext().drawColoredRect(
            scrollbarX,
            SCROLLBAR_EDGE_INSET,
            scrollbarX + SCROLLBAR_WIDTH,
            context.getHeight() - SCROLLBAR_EDGE_INSET,
            SCROLLBAR_TRACK_COLOR
        );
        context.getRenderContext().drawColoredRect(
            scrollbarX,
            thumbTop,
            scrollbarX + SCROLLBAR_WIDTH,
            thumbTop + thumbHeight,
            SCROLLBAR_THUMB_COLOR
        );
    }

    static final class Placement {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        Placement(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        int getWidth() {
            return width;
        }

        int getHeight() {
            return height;
        }
    }
}
