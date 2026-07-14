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

import io.github.notenoughupdates.moulconfig.GuiTextures;
import io.github.notenoughupdates.moulconfig.common.RenderContext;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.GuiComponent;
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext;
import io.github.notenoughupdates.moulconfig.gui.MouseEvent;
import io.github.notenoughupdates.moulconfig.internal.ColourUtil;
import io.github.notenoughupdates.moulconfig.internal.LerpingInteger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

final class CombinationRowsComponent extends GuiComponent {
    static final int EDITOR_WIDTH = 100;
    private static final int ENTRY_HEIGHT = 12;
    private static final int ADD_X = 4;
    private static final int ADD_SIZE = 12;
    private static final int CARD_BOTTOM_PADDING = 3;
    private static final int CARD_GAP = 3;
    private static final int ENTRY_TEXT_X = 4;
    private static final int ENTRY_TEXT_Y = 2;
    private static final int ENTRY_TEXT_WIDTH = 77;
    private static final int DELETE_X = 86;
    private static final int DELETE_Y = 1;
    private static final int DELETE_SIZE = 10;
    private static final int DELETE_TINT_NORMAL = 255;
    private static final int DELETE_TINT_HOVERED = 0;
    private static final int DELETE_TINT_TIME = 3;
    private static final int DELETE_TINT_MIN_STEP = 2;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int BUTTON_TEXT_INSET = 4;

    private final CombinationEditorModel model;
    private final IntConsumer openChoices;
    private final IdentityHashMap<Object, LerpingInteger2> deleteAnimations = new IdentityHashMap<>();

    CombinationRowsComponent(CombinationEditorModel model, IntConsumer openChoices) {
        this.model = model;
        this.openChoices = openChoices;
    }

    static void drawButton(
        GuiImmediateContext context,
        int left,
        int top,
        int width,
        int height,
        StructuredText text
    ) {
        context.getRenderContext().drawDarkRect(left, top, width, height);
        context.getRenderContext().drawStringCenteredScaledMaxWidth(
            text,
            context.getRenderContext().getMinecraft().getDefaultFontRenderer(),
            left + width / 2F,
            top + height / 2F,
            false,
            width - BUTTON_TEXT_INSET,
            TEXT_COLOR
        );
    }

    @Override
    public int getWidth() {
        return EDITOR_WIDTH;
    }

    @Override
    public int getHeight() {
        return createLayout().height;
    }

    @Override
    public void render(GuiImmediateContext context) {
        CombinationsLayout layout = createLayout();
        Set<Object> activeEntries = Collections.newSetFromMap(new IdentityHashMap<>());
        for (CombinationLayout combination : layout.combinations) {
            context.getRenderContext().drawDarkRect(0, combination.top, EDITOR_WIDTH, combination.height);
            for (EntryLayout entry : combination.entries) {
                activeEntries.add(entry.value);
                renderEntry(context, entry);
            }
            if (combination.addButton != null) {
                Bounds bounds = combination.addButton;
                drawButton(context, bounds.left, bounds.top, bounds.width, bounds.height, StructuredText.of("+"));
            }
        }
        deleteAnimations.keySet().removeIf(entry -> !activeEntries.contains(entry));
    }

    @Override
    public boolean mouseEvent(MouseEvent mouseEvent, GuiImmediateContext context) {
        if (!(mouseEvent instanceof MouseEvent.Click)) {
            return false;
        }
        MouseEvent.Click click = (MouseEvent.Click) mouseEvent;
        if (!click.getMouseState() || click.getMouseButton() != 0) {
            return false;
        }
        CombinationsLayout layout = createLayout();
        for (CombinationLayout combination : layout.combinations) {
            for (int entryIndex = 0; entryIndex < combination.entries.size(); entryIndex++) {
                EntryLayout entry = combination.entries.get(entryIndex);
                if (entry.deleteBounds.contains(context.getMouseX(), context.getMouseY())) {
                    return model.removeChoice(combination.index, entryIndex);
                }
            }
            if (combination.addButton != null
                && combination.addButton.contains(context.getMouseX(), context.getMouseY())) {
                openChoices.accept(combination.index);
                return true;
            }
        }
        return false;
    }

    private void renderEntry(GuiImmediateContext context, EntryLayout entry) {
        RenderContext render = context.getRenderContext();
        render.drawStringScaledMaxWidth(
            model.getSelectedLabel(entry.value),
            render.getMinecraft().getDefaultFontRenderer(),
            ENTRY_TEXT_X,
            entry.top + ENTRY_TEXT_Y,
            false,
            ENTRY_TEXT_WIDTH,
            TEXT_COLOR
        );
        boolean isHovered = entry.deleteBounds.contains(context.getMouseX(), context.getMouseY());
        LerpingInteger2 animation = deleteAnimations.computeIfAbsent(
            entry.value,
            ignored -> new LerpingInteger2(DELETE_TINT_NORMAL, DELETE_TINT_TIME, DELETE_TINT_MIN_STEP)
        );
        animation.setTarget(isHovered ? DELETE_TINT_HOVERED : DELETE_TINT_NORMAL);
        int nonRedTint = animation.getValue();
        Bounds bounds = entry.deleteBounds;
        render.drawComplexTexture(
            GuiTextures.DELETE,
            bounds.left,
            bounds.top,
            bounds.width,
            bounds.height,
            draw -> draw.color(ColourUtil.packARGB(DELETE_TINT_NORMAL, DELETE_TINT_NORMAL, nonRedTint, nonRedTint))
        );
    }

    private CombinationsLayout createLayout() {
        int y = 0;
        List<CombinationLayout> combinations = new ArrayList<>();
        for (int combinationIndex = 0; combinationIndex < model.getCombinationCount(); combinationIndex++) {
            int top = y;
            List<EntryLayout> entries = new ArrayList<>();
            for (Object entry : model.getEntries(combinationIndex)) {
                Bounds deleteBounds = new Bounds(DELETE_X, y + DELETE_Y, DELETE_SIZE, DELETE_SIZE);
                entries.add(new EntryLayout(entry, y, deleteBounds));
                y += ENTRY_HEIGHT;
            }
            Bounds addButton = null;
            if (model.hasAvailableChoices(combinationIndex)) {
                addButton = new Bounds(ADD_X, y, ADD_SIZE, ADD_SIZE);
                y += ADD_SIZE;
            }
            y += CARD_BOTTOM_PADDING;
            int height = y - top;
            if (combinationIndex < model.getCombinationCount() - 1) {
                y += CARD_GAP;
            }
            combinations.add(new CombinationLayout(combinationIndex, top, height, entries, addButton));
        }
        return new CombinationsLayout(y, combinations);
    }

    private static final class CombinationsLayout {
        private final int height;
        private final List<CombinationLayout> combinations;

        private CombinationsLayout(int height, List<CombinationLayout> combinations) {
            this.height = height;
            this.combinations = combinations;
        }
    }

    private static final class CombinationLayout {
        private final int index;
        private final int top;
        private final int height;
        private final List<EntryLayout> entries;
        private final Bounds addButton;

        private CombinationLayout(int index, int top, int height, List<EntryLayout> entries, Bounds addButton) {
            this.index = index;
            this.top = top;
            this.height = height;
            this.entries = entries;
            this.addButton = addButton;
        }
    }

    private static final class EntryLayout {
        private final Object value;
        private final int top;
        private final Bounds deleteBounds;

        private EntryLayout(Object value, int top, Bounds deleteBounds) {
            this.value = value;
            this.top = top;
            this.deleteBounds = deleteBounds;
        }
    }

    private static final class Bounds {
        private final int left;
        private final int top;
        private final int width;
        private final int height;

        private Bounds(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }

        private boolean contains(int x, int y) {
            return x >= left && x < left + width && y >= top && y < top + height;
        }
    }
}
