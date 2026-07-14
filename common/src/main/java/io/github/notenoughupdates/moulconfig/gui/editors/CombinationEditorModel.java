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

import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class CombinationEditorModel {
    private final ProcessedOption option;
    private final ConfigEditorCombinationsProvider<?, ?> provider;
    private final List combinations;

    CombinationEditorModel(ProcessedOption option, ConfigEditorCombinationsProvider<?, ?> provider) {
        this.option = option;
        this.provider = provider;
        Object value = option.get();
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Combinations option must contain a list: " + option.getDebugDeclarationLocation());
        }
        combinations = (List) value;
    }

    int getCombinationCount() {
        return combinations.size();
    }

    List<Object> getEntries(int combinationIndex) {
        List<Object> entries = new ArrayList<>();
        entries.addAll(provider.getEntriesInternal(combinations.get(combinationIndex)));
        return entries;
    }

    List<Object> getAvailableChoices(@Nullable Integer combinationIndex) {
        List<Object> available = new ArrayList<>();
        Object combination = getCombinationOrNull(combinationIndex);
        if (combinationIndex != null && combination == null) {
            return available;
        }
        for (Object choice : provider.getChoicesInternal()) {
            boolean canAdd = combination == null
                ? provider.canCreateCombinationInternal(choice)
                : provider.isChoiceAvailableInternal(combination, choice);
            if (canAdd) {
                available.add(choice);
            }
        }
        return available;
    }

    boolean hasAvailableChoices(@Nullable Integer combinationIndex) {
        Object combination = getCombinationOrNull(combinationIndex);
        if (combinationIndex != null && combination == null) {
            return false;
        }
        for (Object choice : provider.getChoicesInternal()) {
            if (combination == null && provider.canCreateCombinationInternal(choice)) {
                return true;
            }
            if (combination != null && provider.isChoiceAvailableInternal(combination, choice)) {
                return true;
            }
        }
        return false;
    }

    boolean addChoice(@Nullable Integer combinationIndex, Object choice) {
        Object availableChoice = findAvailableChoice(combinationIndex, choice);
        if (availableChoice == null) {
            return false;
        }
        if (combinationIndex == null) {
            combinations.add(0, provider.createCombinationInternal(availableChoice));
        } else {
            provider.addChoiceInternal(combinations.get(combinationIndex), availableChoice);
        }
        notifyChanged();
        return true;
    }

    boolean removeChoice(int combinationIndex, int choiceIndex) {
        if (combinationIndex < 0 || combinationIndex >= combinations.size()) {
            return false;
        }
        Object combination = combinations.get(combinationIndex);
        List<?> entries = provider.getEntriesInternal(combination);
        if (choiceIndex < 0 || choiceIndex >= entries.size()) {
            return false;
        }
        provider.removeChoiceInternal(combination, choiceIndex);
        if (provider.hasNoEntriesInternal(combination)) {
            combinations.remove(combinationIndex);
        }
        notifyChanged();
        return true;
    }

    StructuredText getChoiceLabel(Object choice) {
        return provider.getChoiceLabelInternal(choice);
    }

    StructuredText getSelectedLabel(Object choice) {
        return provider.getSelectedLabelInternal(choice);
    }

    String getSearchText() {
        StringBuilder searchText = new StringBuilder("combination combinations condition conditions");
        for (Object choice : provider.getChoicesInternal()) {
            searchText.append(' ').append(provider.getChoiceLabelInternal(choice).getText());
        }
        return searchText.toString().toLowerCase(Locale.ROOT);
    }

    private void notifyChanged() {
        provider.onChanged();
        option.explicitNotifyChange();
    }

    private @Nullable Object findAvailableChoice(@Nullable Integer combinationIndex, Object requestedChoice) {
        Object requestedId = provider.getChoiceIdInternal(requestedChoice);
        for (Object availableChoice : getAvailableChoices(combinationIndex)) {
            if (Objects.equals(requestedId, provider.getChoiceIdInternal(availableChoice))) {
                return availableChoice;
            }
        }
        return null;
    }

    private @Nullable Object getCombinationOrNull(@Nullable Integer combinationIndex) {
        if (combinationIndex == null) {
            return null;
        }
        if (combinationIndex < 0 || combinationIndex >= combinations.size()) {
            return null;
        }
        return combinations.get(combinationIndex);
    }
}
