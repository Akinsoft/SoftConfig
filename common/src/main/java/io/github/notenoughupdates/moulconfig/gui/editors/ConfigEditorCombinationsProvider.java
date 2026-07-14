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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class ConfigEditorCombinationsProvider<C, E> {
    private final Class<C> combinationType;
    private final Class<E> choiceType;

    protected ConfigEditorCombinationsProvider(Class<C> combinationType, Class<E> choiceType) {
        this.combinationType = Objects.requireNonNull(combinationType, "combinationType");
        this.choiceType = Objects.requireNonNull(choiceType, "choiceType");
    }

    public abstract List<? extends E> getChoices();

    public abstract List<E> getEntries(C combination);

    public abstract C createCombination(E firstChoice);

    public abstract StructuredText getChoiceLabel(E choice);

    public abstract Object getChoiceId(E choice);

    public StructuredText getSelectedLabel(E choice) {
        return getChoiceLabel(choice);
    }

    public @Nullable Object getChoiceGroup(E choice) {
        return null;
    }

    public E copyChoice(E choice) {
        return choice;
    }

    public boolean canCreateCombination(E choice) {
        return true;
    }

    public boolean isChoiceAvailable(C combination, E choice) {
        Object choiceId = getChoiceId(choice);
        Object choiceGroup = getChoiceGroup(choice);
        for (E entry : getEntries(combination)) {
            if (Objects.equals(choiceId, getChoiceId(entry))) {
                return false;
            }
            if (choiceGroup != null && Objects.equals(choiceGroup, getChoiceGroup(entry))) {
                return false;
            }
        }
        return true;
    }

    public void onChanged() {
    }

    final List<?> getChoicesInternal() {
        return getChoices();
    }

    final List<?> getEntriesInternal(Object combination) {
        return getEntries(combinationType.cast(combination));
    }

    final Object createCombinationInternal(Object choice) {
        E typedChoice = choiceType.cast(choice);
        return createCombination(copyChoice(typedChoice));
    }

    final void addChoiceInternal(Object combination, Object choice) {
        E typedChoice = choiceType.cast(choice);
        getEntries(combinationType.cast(combination)).add(copyChoice(typedChoice));
    }

    final void removeChoiceInternal(Object combination, int choiceIndex) {
        getEntries(combinationType.cast(combination)).remove(choiceIndex);
    }

    final boolean hasNoEntriesInternal(Object combination) {
        return getEntries(combinationType.cast(combination)).isEmpty();
    }

    final boolean canCreateCombinationInternal(Object choice) {
        return canCreateCombination(choiceType.cast(choice));
    }

    final boolean isChoiceAvailableInternal(Object combination, Object choice) {
        return isChoiceAvailable(combinationType.cast(combination), choiceType.cast(choice));
    }

    final StructuredText getChoiceLabelInternal(Object choice) {
        return getChoiceLabel(choiceType.cast(choice));
    }

    final StructuredText getSelectedLabelInternal(Object choice) {
        return getSelectedLabel(choiceType.cast(choice));
    }

    final Object getChoiceIdInternal(Object choice) {
        return getChoiceId(choiceType.cast(choice));
    }
}
