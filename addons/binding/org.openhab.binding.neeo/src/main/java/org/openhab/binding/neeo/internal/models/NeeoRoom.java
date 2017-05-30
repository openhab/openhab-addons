/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an Neeo Room (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoRoom {

    /** The room name */
    private final String name;

    /** The room key */
    private final String key;

    /** The devices in teh room */
    private final NeeoDevices devices;

    /** The scenarios in the room */
    private final NeeoScenarios scenarios;

    /** The recipes in the room */
    private final NeeoRecipes recipes;

    /**
     * Instantiates a new neeo room.
     *
     * @param name the name
     * @param hasController the has controller
     * @param key the key
     * @param scenarios the scenarios
     * @param devices the devices
     * @param recipes the recipes
     */
    public NeeoRoom(String name, boolean hasController, String key, NeeoScenarios scenarios, NeeoDevices devices,
            NeeoRecipes recipes) {
        this.name = name;
        this.key = key;
        this.scenarios = scenarios;
        this.devices = devices;
        this.recipes = recipes;
    }

    /**
     * Gets the room name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the room key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the recipes in the room
     *
     * @return the recipes
     */
    public NeeoRecipes getRecipes() {
        return recipes;
    }

    /**
     * Gets the devices in the room
     *
     * @return the devices
     */
    public NeeoDevices getDevices() {
        return devices;
    }

    /**
     * Gets the scenarios in the room
     *
     * @return the scenarios
     */
    public NeeoScenarios getScenarios() {
        return scenarios;
    }

    @Override
    public String toString() {
        return "NeeoRoom [name=" + name + ", key=" + key + ", scenarios=" + scenarios + ", devices=" + devices
                + ", recipes=" + recipes + "]";
    }

}
