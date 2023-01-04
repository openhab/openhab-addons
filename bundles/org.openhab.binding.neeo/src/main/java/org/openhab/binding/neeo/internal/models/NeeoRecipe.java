/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a Neeo Recipe (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoRecipe {

    /** Recipe type for launching the recipe */
    public static final String LAUNCH = "launch";

    /** Recipe type for powering off the recipe */
    public static final String POWEROFF = "poweroff";

    /** The recipe key */
    @Nullable
    private String key;

    /** The type of recipe (generally launch/poweroff) */
    @Nullable
    private String type;

    /** The name of the recipe */
    @Nullable
    private String name;

    /** Whether the recipe is enabled */
    private boolean enabled;

    /** ?? whether the recipe is dirty ?? */
    private boolean dirty;

    // May be used in the future...
    // private NeeoStep[] steps;
    // private NeeoCondition[] conditions;
    // private NeeoTrigger trigger;

    /** The associated room key */
    @Nullable
    private String roomKey;

    /** The associated room name. */
    @Nullable
    private String roomName;

    /** The scenario key recipe is linked to */
    @Nullable
    private String scenarioKey;

    /** Whether the recipe is hidden or not */
    private boolean isHiddenRecipe;

    /** ?? whether this is a custom recipe ?? */
    private boolean isCustom;

    /**
     * Gets the recipe key
     *
     * @return the key
     */
    @Nullable
    public String getKey() {
        return key;
    }

    /**
     * Gets the recipe type
     *
     * @return the type
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Gets the recipe name
     *
     * @return the name
     */
    @Nullable
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
    @Nullable
    public String getRoomKey() {
        return roomKey;
    }

    /**
     * Gets the associated room name.
     *
     * @return the room name
     */
    @Nullable
    public String getRoomName() {
        return roomName;
    }

    /**
     * Gets the associated scenario key.
     *
     * @return the scenario key
     */
    @Nullable
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
