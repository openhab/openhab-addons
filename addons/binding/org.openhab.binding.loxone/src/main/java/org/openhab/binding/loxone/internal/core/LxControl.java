/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * A control of Loxone Miniserver.
 * <p>
 * It represents a control object on the Miniserver. Controls can represent an input, functional block or an output of
 * the Miniserver, that is marked as visible in the Loxone UI. Controls can belong to a {@link LxContainer} room and a
 * {@link LxCategory} category.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public abstract class LxControl {

    /**
     * This class is used to instantiate a particular control object by the {@link LxControlFactory}
     * 
     * @author Pawel Pieczul - initial contribution
     *
     */
    abstract static class LxControlInstance {
        /**
         * Creates an instance of a particular control class.
         *
         * @param client
         *            websocket client to facilitate communication with Miniserver
         * @param uuid
         *            UUID of this control
         * @param json
         *            JSON describing the control as received from the Miniserver
         * @param room
         *            Room that this control belongs to
         * @param category
         *            Category that this control belongs to
         * @return
         *         a newly created control object
         */
        abstract LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category);

        /**
         * Return a type name for this control.
         *
         * @return
         *         type name (as used on the Miniserver)
         */
        abstract String getType();
    }

    private String name;
    private LxContainer room;
    private LxCategory category;
    private Map<String, LxControlState> states = new HashMap<>();

    LxUuid uuid;
    LxWsClient socketClient;
    Logger logger = LoggerFactory.getLogger(LxControl.class);
    Map<LxUuid, LxControl> subControls = new HashMap<>();

    /**
     * Create a Miniserver's control object.
     *
     * @param client
     *            websocket client to facilitate communication with Miniserver
     * @param uuid
     *            UUID of this control
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            Room that this control belongs to
     * @param category
     *            Category that this control belongs to
     */
    LxControl(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        logger.trace("Creating new LxControl: {}", json.type);
        socketClient = client;
        this.uuid = uuid;
        update(json, room, category);
    }

    /**
     * Gets value of a state object of given name, if exists
     *
     * @param name
     *            name of state object
     * @return
     *         state object's value
     */
    Double getStateValue(String name) {
        LxControlState state = getState(name);
        if (state != null) {
            return state.getValue();
        }
        return null;
    }

    /**
     * Gets text value of a state object of given name, if exists
     *
     * @param name
     *            name of state object
     * @return
     *         state object's text value
     */
    String getStateTextValue(String name) {
        LxControlState state = getState(name);
        if (state != null) {
            return state.getTextValue();
        }
        return null;
    }

    /**
     * Adds a listener for a particular state, if the state exists for the control.
     *
     * @param stateName
     *            name of the state to add listener for
     * @param listener
     *            listener to listen for state changes
     * @return
     *         state that listener was added to or null if no such state
     */
    public LxControlState addStateListener(String stateName, LxControlStateListener listener) {
        LxControlState state = getState(stateName);
        if (state != null) {
            state.addListener(listener);
        } else {
            logger.debug("Attempt to add listener for not existing state {}", stateName);
        }
        return state;
    }

    public Map<LxUuid, LxControl> getSubControls() {
        return subControls;
    }

    public Map<String, LxControlState> getStates() {
        return states;
    }

    /**
     * Call when control is no more needed - unlink it from containers
     */
    public void dispose() {
        if (room != null) {
            room.removeControl(this);
        }
        if (category != null) {
            category.removeControl(this);
        }
        for (LxControl control : subControls.values()) {
            control.dispose();
        }
    }

    /**
     * Obtain control's name
     *
     * @return
     *         Human readable name of control
     */
    public String getName() {
        return name;
    }

    /**
     * Obtain UUID of this control
     *
     * @return
     *         UUID
     */
    public LxUuid getUuid() {
        return uuid;
    }

    /**
     * Obtain room that this control belongs to
     *
     * @return
     *         Control's room or null if no room
     */
    public LxContainer getRoom() {
        return room;
    }

    /**
     * Obtain category of this control
     *
     * @return
     *         Control's category or null if no category
     */
    public LxCategory getCategory() {
        return category;
    }

    /**
     * Compare UUID's of two controls -
     *
     * @param object
     *            Object to compare with
     * @return
     *         true if UUID of two objects are equal
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (object.getClass() != getClass()) {
            return false;
        }
        LxControl c = (LxControl) object;
        return Objects.equals(c.getUuid(), getUuid());
    }

    /**
     * Hash code of the control is equal to its UUID's hash code
     */
    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    /**
     * Update Miniserver's control in runtime.
     *
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            New room that this control belongs to
     * @param category
     *            New category that this control belongs to
     */
    void update(LxJsonControl json, LxContainer room, LxCategory category) {
        logger.trace("Updating LxControl: {}", json.type);

        this.name = json.name;
        this.room = room;
        this.category = category;
        uuid.setUpdate(true);
        if (room != null) {
            room.addOrUpdateControl(this);
        }
        if (category != null) {
            category.addOrUpdateControl(this);
        }

        // retrieve all states from the configuration
        if (json.states != null) {
            logger.trace("Reading states for LxControl: {}", json.type);

            for (Map.Entry<String, JsonElement> jsonState : json.states.entrySet()) {
                JsonElement element = jsonState.getValue();
                if (element instanceof JsonArray) {
                    // temperature state of intelligent home controller object is the only
                    // one that has state represented as an array, as this is not implemented
                    // yet, we will skip this state
                    continue;
                }
                String value = element.getAsString();
                if (value != null) {
                    LxUuid id = new LxUuid(value);
                    String name = jsonState.getKey().toLowerCase();
                    LxControlState state = states.get(name);
                    if (state == null) {
                        logger.trace("New state for LxControl {}: {}", json.type, name);
                        state = new LxControlState(id, name, this);
                    } else {
                        logger.trace("Existing state for LxControl {}: {}", json.type, name);
                        state.getUuid().setUpdate(true);
                        state.setName(name);
                    }
                    states.put(name, state);
                }
            }
        }
    }

    private LxControlState getState(String name) {
        return states.get(name);
    }
}
