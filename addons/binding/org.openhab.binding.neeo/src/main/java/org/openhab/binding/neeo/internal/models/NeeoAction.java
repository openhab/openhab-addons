/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import com.google.gson.annotations.SerializedName;

/**
 * The model representing an forward actions result (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 *
 */
public class NeeoAction {
    /** The action - can be null */
    private final String action;

    /** The action parameter - generally null */
    @SerializedName("actionparameter")
    private final String actionParameter;

    /** The recipe name - only valid on launch of recipe */
    private final String recipe;

    /** The device name - usually filled */
    private final String device;

    /** The room name - usually filled */
    private final String room;

    /**
     * Constructs the action from the given parameters
     *
     * @param action a possibly null, possibly empty action
     * @param actionParameter a possibly null, possibly empty actionparameter
     * @param recipe a possibly null, possibly empty recipe name
     * @param device a possibly null, possibly empty device name
     * @param room a possibly null, possibly empty room name
     */
    public NeeoAction(String action, String actionParameter, String recipe, String device, String room) {
        this.action = action;
        this.actionParameter = actionParameter;
        this.recipe = recipe;
        this.device = device;
        this.room = room;
    }

    /**
     * Returns the action
     *
     * @return a possibly null, possibly empty action
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the action parameter
     *
     * @return a possibly null, possibly empty action parameter
     */
    public String getActionParameter() {
        return actionParameter;
    }

    /**
     * Returns the recipe name
     *
     * @return a possibly null, possibly empty recipe name
     */
    public String getRecipe() {
        return recipe;
    }

    /**
     * Returns the device name
     *
     * @return a possibly null, possibly empty device name
     */
    public String getDevice() {
        return device;
    }

    /**
     * Returns the room name
     *
     * @return a possibly null, possibly room name
     */
    public String getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "NeeoAction [action=" + action + ", actionParameter=" + actionParameter + ", recipe=" + recipe
                + ", device=" + device + ", room=" + room + "]";
    }

}
