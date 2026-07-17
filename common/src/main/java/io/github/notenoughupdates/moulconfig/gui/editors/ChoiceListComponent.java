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
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent;
import io.github.notenoughupdates.moulconfig.gui.MouseEvent;
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent;
import io.github.notenoughupdates.moulconfig.observer.GetSetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

final class ChoiceListComponent<T> extends GuiComponent {
    private static final int ROW_HEIGHT = 12;
    private static final int BORDER_WIDTH = 1;
    private static final int TEXT_LEFT = 3;
    private static final int TEXT_TOP = 2;
    private static final int TEXT_RIGHT_INSET = 8;
    private static final int SEARCH_MARGIN = 3;
    private static final int SEARCH_HEIGHT = 14;
    private static final int LIST_TOP = SEARCH_MARGIN + SEARCH_HEIGHT + SEARCH_MARGIN;
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
    private final ToIntFunction<T> textColor;
    private final Consumer<T> selected;
    private final Runnable dismissed;
    private final TextFieldComponent searchField;
    private List<T> filteredChoices;
    private String search = "";
    private boolean focusSearch = true;
    private int scrollOffset;

    ChoiceListComponent(
        List<T> choices,
        int visibleWidth,
        int visibleHeight,
        Function<T, StructuredText> label,
        ToIntFunction<T> textColor,
        Consumer<T> selected,
        Runnable dismissed
    ) {
        this.choices = choices;
        this.visibleWidth = visibleWidth;
        this.visibleHeight = visibleHeight;
        this.label = label;
        this.textColor = textColor;
        this.selected = selected;
        this.dismissed = dismissed;
        this.filteredChoices = new ArrayList<>(choices);
        this.searchField = new TextFieldComponent(
            new GetSetter<String>() {
                @Override
                public String get() {
                    return search;
                }

                @Override
                public void set(String newValue) {
                    search = newValue;
                    refreshChoices();
                }
            },
            Math.max(1, visibleWidth - SEARCH_MARGIN * 2),
            () -> true,
            "Search...",
            IMinecraft.INSTANCE.getDefaultFontRenderer(),
            Collections.singleton('§')
        );
    }

    ChoiceListComponent(
        List<T> choices,
        int visibleWidth,
        int visibleHeight,
        Function<T, StructuredText> label,
        Consumer<T> selected,
        Runnable dismissed
    ) {
        this(choices, visibleWidth, visibleHeight, label, ignored -> TEXT_COLOR, selected, dismissed);
    }

    static int contentHeight(int choiceCount) {
        return LIST_TOP + choiceCount * ROW_HEIGHT + BORDER_WIDTH;
    }

    static <T> List<T> filterChoices(
        List<T> choices,
        Function<T, StructuredText> label,
        String query
    ) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) return new ArrayList<>(choices);
        return choices.stream()
            .filter(choice -> label.apply(choice).getText().toLowerCase(Locale.ROOT).contains(normalizedQuery))
            .collect(Collectors.toList());
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
        if (context.isHovered() && context.getMouseY() >= LIST_TOP && mouseEvent instanceof MouseEvent.Scroll) {
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
        GuiImmediateContext searchContext = context.translated(
            SEARCH_MARGIN,
            SEARCH_MARGIN,
            Math.max(1, context.getWidth() - SEARCH_MARGIN * 2),
            SEARCH_HEIGHT
        );
        if (searchField.mouseEvent(mouseEvent, searchContext)) return true;
        int contentY = context.getMouseY() + scrollOffset - LIST_TOP;
        if (contentY >= 0 && contentY < filteredChoices.size() * ROW_HEIGHT) {
            int selectedIndex = contentY / ROW_HEIGHT;
            selected.accept(filteredChoices.get(selectedIndex));
            refreshChoices();
        }
        return true;
    }

    @Override
    public boolean keyboardEvent(KeyboardEvent event, GuiImmediateContext context) {
        return searchField.keyboardEvent(
            event,
            context.translated(
                SEARCH_MARGIN,
                SEARCH_MARGIN,
                Math.max(1, context.getWidth() - SEARCH_MARGIN * 2),
                SEARCH_HEIGHT
            )
        );
    }

    @Override
    public <R> R foldChildren(R initial, BiFunction<GuiComponent, R, R> visitor) {
        return visitor.apply(searchField, initial);
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
        if (focusSearch) {
            searchField.requestFocus();
            focusSearch = false;
        }
        render.pushMatrix();
        render.translate(SEARCH_MARGIN, SEARCH_MARGIN);
        searchField.render(context.translated(
            SEARCH_MARGIN,
            SEARCH_MARGIN,
            Math.max(1, context.getWidth() - SEARCH_MARGIN * 2),
            SEARCH_HEIGHT
        ));
        render.popMatrix();
        render.pushScissor(BORDER_WIDTH, LIST_TOP, context.getWidth() - BORDER_WIDTH, context.getHeight() - BORDER_WIDTH);
        render.pushMatrix();
        render.translate(0, LIST_TOP - scrollOffset);
        IFontRenderer font = render.getMinecraft().getDefaultFontRenderer();
        for (int index = 0; index < filteredChoices.size(); index++) {
            int rowTop = index * ROW_HEIGHT;
            int mouseContentY = context.getMouseY() + scrollOffset - LIST_TOP;
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
                label.apply(filteredChoices.get(index)),
                font,
                TEXT_LEFT,
                rowTop + TEXT_TOP,
                false,
                context.getWidth() - TEXT_RIGHT_INSET,
                textColor.applyAsInt(filteredChoices.get(index))
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
        return Math.max(0, filteredChoices.size() * ROW_HEIGHT - listViewportHeight(height));
    }

    private void drawScrollbar(GuiImmediateContext context) {
        int contentHeight = filteredChoices.size() * ROW_HEIGHT;
        int viewportHeight = listViewportHeight(context.getHeight());
        if (contentHeight <= viewportHeight || viewportHeight <= 0) {
            return;
        }
        int trackTop = LIST_TOP + SCROLLBAR_EDGE_INSET;
        int trackHeight = viewportHeight - SCROLLBAR_EDGE_INSET * 2;
        int thumbHeight = Math.max(MIN_THUMB_HEIGHT, trackHeight * viewportHeight / contentHeight);
        int maxScroll = contentHeight - viewportHeight;
        int thumbTop = trackTop
            + (int) ((trackHeight - thumbHeight) * scrollOffset / (float) maxScroll);
        int scrollbarX = context.getWidth() - SCROLLBAR_RIGHT_INSET;
        context.getRenderContext().drawColoredRect(
            scrollbarX,
            trackTop,
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

    private int listViewportHeight(int height) {
        return Math.max(0, height - LIST_TOP - BORDER_WIDTH);
    }

    private void refreshChoices() {
        filteredChoices = filterChoices(choices, label, search);
        scrollOffset = 0;
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
