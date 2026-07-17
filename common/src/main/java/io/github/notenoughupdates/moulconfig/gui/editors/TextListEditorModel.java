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

import io.github.notenoughupdates.moulconfig.ChromaColour;

import java.util.List;
import java.util.Locale;

final class TextListEditorModel {
    private final List<TextListEntry> entries;
    private final Runnable onChanged;

    @SuppressWarnings("unchecked")
    TextListEditorModel(Object value, Runnable onChanged) {
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Text list option must contain a mutable list");
        }
        List<?> list = (List<?>) value;
        if (!list.stream().allMatch(TextListEntry.class::isInstance)) {
            throw new IllegalArgumentException("Text list entries must be TextListEntry values");
        }
        entries = (List<TextListEntry>) list;
        this.onChanged = onChanged;
    }

    List<TextListEntry> getEntries() {
        return entries;
    }

    ChangeResult addEntry(String rawText) {
        String text = rawText.trim();
        if (text.isEmpty() || entries.stream().anyMatch(entry -> entry.getText().equalsIgnoreCase(text))) {
            return ChangeResult.UNCHANGED;
        }
        entries.add(new TextListEntry(text));
        changed();
        return ChangeResult.CHANGED;
    }

    ChangeResult moveEntry(int fromIndex, int toIndex) {
        if (!isValidIndex(fromIndex) || !isValidIndex(toIndex) || fromIndex == toIndex) {
            return ChangeResult.UNCHANGED;
        }
        entries.add(toIndex, entries.remove(fromIndex));
        changed();
        return ChangeResult.CHANGED;
    }

    ChangeResult removeEntry(int index) {
        if (!isValidIndex(index)) {
            return ChangeResult.UNCHANGED;
        }
        entries.remove(index);
        changed();
        return ChangeResult.CHANGED;
    }

    ChangeResult setText(TextListEntry entry, String rawText) {
        String text = rawText.trim();
        if (!entries.contains(entry) || text.isEmpty() || entries.stream().anyMatch(other ->
            other != entry && other.getText().equalsIgnoreCase(text))) {
            return ChangeResult.UNCHANGED;
        }
        if (entry.getText().equals(text)) {
            return ChangeResult.UNCHANGED;
        }
        entry.setText(text);
        changed();
        return ChangeResult.CHANGED;
    }

    void setColour(TextListEntry entry, ChromaColour colour) {
        if (!entries.contains(entry) || entry.getColour().equals(colour)) {
            return;
        }
        entry.setColour(colour);
        changed();
    }

    void toggleSound(TextListEntry entry) {
        if (!entries.contains(entry)) {
            return;
        }
        entry.setSoundEnabled(!entry.isSoundEnabled());
        changed();
    }

    void setSoundVolume(TextListEntry entry, float volumePercent) {
        float volume = Math.max(
            TextListEntry.MIN_SOUND_VOLUME_PERCENT,
            Math.min(TextListEntry.MAX_SOUND_VOLUME_PERCENT, volumePercent)
        );
        if (!entries.contains(entry) || Float.compare(entry.getSoundVolumePercent(), volume) == 0) {
            return;
        }
        entry.setSoundVolumePercent(volume);
        changed();
    }

    void setSound(TextListEntry entry, String sound) {
        if (!entries.contains(entry) || entry.getSound().equals(sound)) {
            return;
        }
        entry.setSound(sound);
        changed();
    }

    String searchText() {
        StringBuilder text = new StringBuilder();
        entries.forEach(entry -> text.append(' ').append(entry.getText()));
        return text.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < entries.size();
    }

    private void changed() {
        onChanged.run();
    }

    enum ChangeResult {
        CHANGED,
        UNCHANGED,
    }
}
