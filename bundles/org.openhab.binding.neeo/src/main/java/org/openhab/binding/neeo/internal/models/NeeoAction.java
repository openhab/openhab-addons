/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * The model representing a forward actions result (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 *
 */
@NonNullByDefault
public class NeeoAction {
    /** The action - can be null */
    @Nullable
    private String action;

    /** The action parameter - generally null */
    @Nullable
    @SerializedName("actionparameter")
    private String actionParameter;

    /** The recipe name - only valid on launch of recipe */
    @Nullable
    private String recipe;

    /** The device name - usually filled */
    @Nullable
    private String device;

    /** The room name - usually filled */
    @Nullable
    private String room;

    /**
     * Returns the action
     *
     * @return a possibly null, possibly empty action
     */
    @Nullable
    public String getAction() {
        return action;
    }

    /**
     * Returns the action parameter
     *
     * @return a possibly null, possibly empty action parameter
     */
    @Nullable
    public String getActionParameter() {
        return actionParameter;
    }

    /**
     * Returns the recipe name
     *
     * @return a possibly null, possibly empty recipe name
     */
    @Nullable
    public String getRecipe() {
        return recipe;
    }

    /**
     * Returns the device name
     *
     * @return a possibly null, possibly empty device name
     */
    @Nullable
    public String getDevice() {
        return device;
    }

    /**
     * Returns the room name
     *
     * @return a possibly null, possibly room name
     */
    @Nullable
    public String getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "NeeoAction [action=" + action + ", actionParameter=" + actionParameter + ", recipe=" + recipe
                + ", device=" + device + ", room=" + room + "]";
    }
}
