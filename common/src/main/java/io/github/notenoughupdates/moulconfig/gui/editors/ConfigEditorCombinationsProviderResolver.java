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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

final class ConfigEditorCombinationsProviderResolver {
    private ConfigEditorCombinationsProviderResolver() {
    }

    static ConfigEditorCombinationsProvider<?, ?> resolve(
        Class<? extends ConfigEditorCombinationsProvider<?, ?>> providerClass
    ) {
        try {
            Object singleton = findSingleton(providerClass);
            if (singleton != null) {
                return ConfigEditorCombinationsProvider.class.cast(singleton);
            }
            Constructor<?> constructor = providerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return ConfigEditorCombinationsProvider.class.cast(constructor.newInstance());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException exception) {
            throw new IllegalArgumentException("Cannot create combinations provider " + providerClass.getName(), exception);
        }
    }

    private static Object findSingleton(Class<?> providerClass) throws IllegalAccessException {
        Field instanceField;
        try {
            instanceField = providerClass.getDeclaredField("INSTANCE");
        } catch (NoSuchFieldException exception) {
            return null;
        }
        if (!Modifier.isStatic(instanceField.getModifiers())) {
            return null;
        }
        instanceField.setAccessible(true);
        Object instance = instanceField.get(null);
        return providerClass.isInstance(instance) ? instance : null;
    }
}
