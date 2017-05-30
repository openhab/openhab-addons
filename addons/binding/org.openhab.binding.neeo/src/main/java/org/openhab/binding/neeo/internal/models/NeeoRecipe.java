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
 * The model representing an Neeo Recipe (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoRecipe {

    /** Recipe type for launching the recipe */
    public static final String LAUNCH = "launch";

    /** Recipe type for powering off the recipe */
    public static final String POWEROFF = "poweroff";

    /** The recipe key */
    private final String key;

    /** The type of recipe (generally launch/poweroff) */
    private final String type;

    /** The name of the recipe */
    private final String name;

    /** Whether the recipe is enabled */
    private final boolean enabled;

    /** ?? whether the recipe is dirty ?? */
    private final boolean dirty;

    // May be used in the future...
    // private final NeeoStep[] steps;
    // private final NeeoCondition[] conditions;
    // private final NeeoTrigger trigger;

    /** The associated room key */
    private final String roomKey;

    /** The associated room name. */
    private final String roomName;

    /** The scenario key recipe is linked to */
    private final String scenarioKey;

    /** Whether the recipe is hidden or not */
    private final boolean isHiddenRecipe;

    /** ?? whether this is a custom recipe ?? */
    private final boolean isCustom;

    /**
     * Instantiates a new neeo recipe.
     *
     * @param key the key
     * @param type the type
     * @param name the name
     * @param enabled the enabled
     * @param dirty the dirty
     * @param roomKey the room key
     * @param roomName the room name
     * @param scenarioKey the scenario key
     * @param isHiddenRecipe the is hidden recipe
     * @param isCustom the is custom
     */
    public NeeoRecipe(String key, String type, String name, boolean enabled, boolean dirty, String roomKey,
            String roomName, String scenarioKey, boolean isHiddenRecipe, boolean isCustom) {
        this.key = key;
        this.type = type;
        this.name = name;
        this.enabled = enabled;
        this.dirty = dirty;
        this.roomKey = roomKey;
        this.roomName = roomName;
        this.scenarioKey = scenarioKey;
        this.isHiddenRecipe = isHiddenRecipe;
        this.isCustom = isCustom;
    }

    /**
     * Gets the recipe key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the recipe type
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the recipe name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the recipe is enabled
     *
     * @return true, if is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if the recipe is dirty
     *
     * @return true, if is dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Gets the associated room key.
     *
     * @return the room key
     */
    public String getRoomKey() {
        return roomKey;
    }

    /**
     * Gets the associated room name.
     *
     * @return the room name
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Gets the associated scenario key.
     *
     * @return the scenario key
     */
    public String getScenarioKey() {
        return scenarioKey;
    }

    /**
     * Checks if the recipe is hidden
     *
     * @return true, if is hidden recipe
     */
    public boolean isHiddenRecipe() {
        return isHiddenRecipe;
    }

    /**
     * Checks if its a custom recipe
     *
     * @return true, if is custom
     */
    public boolean isCustom() {
        return isCustom;
    }

    @Override
    public String toString() {
        return "NeeoRecipe [key=" + key + ", type=" + type + ", name=" + name + ", enabled=" + enabled + ", dirty="
                + dirty + ", roomKey=" + roomKey + ", roomName=" + roomName + ", scenarioKey=" + scenarioKey
                + ", isHiddenRecipe=" + isHiddenRecipe + ", isCustom=" + isCustom + "]";
    }
}
