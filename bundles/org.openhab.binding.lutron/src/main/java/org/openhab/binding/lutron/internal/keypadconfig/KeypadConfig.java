/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lutron.internal.keypadconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for keypad configuration definition classes
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public abstract class KeypadConfig {
    private final Logger logger = LoggerFactory.getLogger(KeypadConfig.class);

    protected final Map<String, List<KeypadComponent>> modelData = new HashMap<>();

    public abstract boolean isCCI(int id);

    public abstract boolean isButton(int id);

    public abstract boolean isLed(int id);

    /**
     * Get a list of all {@link KeypadComponent}s for the specified keypad model
     *
     * @param model The keypad model for which to return components.
     * @return List of components. Will be empty if no components match.
     */
    public List<KeypadComponent> getComponents(String model) {
        return getComponents(model, null);
    }

    /**
     * Get a list of {@link KeypadComponent}s of the specified type for the specified keypad model
     *
     * @param model The keypad model for which to return components.
     * @param type The component type to include, or null for all components.
     * @return List of components. Will be empty if no components match.
     */
    public List<KeypadComponent> getComponents(String model, @Nullable ComponentType type) {
        List<KeypadComponent> filteredList = new LinkedList<>();
        List<KeypadComponent> cList = modelData.get(model);
        if (cList == null) {
            logger.debug("Keypad components lookup using invalid keypad model: {}", model);
            return filteredList;
        } else if (type == null) {
            return cList;
        } else {
            for (KeypadComponent i : cList) {
                if (i.type() == type) {
                    filteredList.add(i);
                }
            }
            return filteredList;
        }
    }

    /**
     * Get a list of all component IDs for the specified keypad model
     *
     * @param model The keypad model for which to return component IDs.
     * @return List of component IDs. Will be empty if no components match.
     */
    public @Nullable List<Integer> getComponentIds(String model) {
        return getComponentIds(model, null);
    }

    /**
     * Get a list of component IDs of the specified type for the specified keypad model
     *
     * @param model The keypad model for which to return component IDs.
     * @param type The component type to include, or null for all components.
     * @return List of component IDs. Will be empty if no components match.
     */
    public List<Integer> getComponentIds(String model, @Nullable ComponentType type) {
        List<Integer> idList = new LinkedList<>();
        List<KeypadComponent> cList = modelData.get(model);
        if (cList == null) {
            logger.debug("Keypad component IDs lookup using invalid keypad model: {}", model);
        } else {
            for (KeypadComponent i : cList) {
                if (type == null || i.type() == type) {
                    idList.add(i.id());
                }
            }
        }
        return idList;
    }

    /**
     * Determine keypad model from list of button component IDs
     *
     * @param buttonIds List of button component IDs for a keypad. Must be in ascending order.
     * @return String containing the keypad model, or null if no models match.
     */
    public @Nullable String determineModelFromComponentIds(List<Integer> buttonIds) {
        for (String k : modelData.keySet()) {
            List<Integer> modelButtonIds = getComponentIds(k, ComponentType.BUTTON);
            Collections.sort(modelButtonIds); // make sure button IDs are in ascending order for comparison
            if (modelButtonIds.equals(buttonIds)) {
                return k;
            }
        }
        return null;
    }

    /**
     * Utility routine to concatenate multiple lists of {@link KeypadComponent}s
     *
     * @param lists Lists to concatenate
     * @return Concatenated list
     */
    @SafeVarargs
    protected static List<KeypadComponent> combinedList(final List<KeypadComponent>... lists) {
        List<KeypadComponent> newlist = new LinkedList<>();
        for (List<KeypadComponent> list : lists) {
            newlist.addAll(list);
        }
        return newlist;
    }
}
