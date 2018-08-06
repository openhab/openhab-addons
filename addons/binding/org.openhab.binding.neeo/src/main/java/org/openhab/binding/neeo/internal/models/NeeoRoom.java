/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing an Neeo Room (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoRoom {

    /** The room name */
    @Nullable
    private String name;

    /** The room key */
    @Nullable
    private String key;

    /** The devices in the room */
    @Nullable
    private NeeoDevices devices;

    /** The scenarios in the room */
    @Nullable
    private NeeoScenarios scenarios;

    /** The recipes in the room */
    @Nullable
    private NeeoRecipes recipes;

    /**
     * Gets the room name
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Gets the room key
     *
     * @return the key
     */
    @Nullable
    public String getKey() {
        return key;
    }

    /**
     * Gets the recipes in the room
     *
     * @return the recipes
     */
    public NeeoRecipes getRecipes() {
        final NeeoRecipes localRecipes = recipes;
        return localRecipes == null ? new NeeoRecipes(new NeeoRecipe[0]) : localRecipes;
    }

    /**
     * Gets the devices in the room
     *
     * @return the devices
     */
    public NeeoDevices getDevices() {
        final NeeoDevices localDevices = devices;
        return localDevices == null ? new NeeoDevices(new NeeoDevice[0]) : localDevices;
    }

    /**
     * Gets the scenarios in the room
     *
     * @return the scenarios
     */
    public NeeoScenarios getScenarios() {
        final NeeoScenarios localScenarios = scenarios;
        return localScenarios == null ? new NeeoScenarios(new NeeoScenario[0]) : localScenarios;
    }

    @Override
    public String toString() {
        return "NeeoRoom [name=" + name + ", key=" + key + ", scenarios=" + scenarios + ", devices=" + devices
                + ", recipes=" + recipes + "]";
    }

}
