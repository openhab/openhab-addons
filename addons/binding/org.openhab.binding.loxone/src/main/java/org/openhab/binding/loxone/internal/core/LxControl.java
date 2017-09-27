/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * @author Pawel Pieczul
 *
 */
@NonNullByDefault
public abstract class LxControl {
    private String name = "unknown";
    private String typeName = "unknown";
    private @Nullable LxContainer room;
    private @Nullable LxCategory category;
    private Map<String, LxControlState> states = new HashMap<>();

    LxUuid uuid;
    LxWsClient socketClient;
    @SuppressWarnings("null")
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
    LxControl(LxWsClient client, LxUuid uuid, LxJsonControl json, @Nullable LxContainer room,
            @Nullable LxCategory category) {
        logger.trace("Creating new LxControl: {}", json.type);
        socketClient = client;
        this.uuid = uuid;
        if (json.type != null) {
            @SuppressWarnings("null")
            @NonNull
            String type = json.type.toLowerCase();
            this.typeName = type;
        }
        update(json, room, category);
    }

    /**
     * Obtain control's type name (e.g. switch, rollershutter) by which Miniserver recognizes it
     *
     * @return
     *         name of the control type
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Gets state object of given name, if exists
     *
     * @param name
     *            name of state object
     * @return
     *         state object
     */
    public @Nullable LxControlState getState(String name) {
        if (states.containsKey(name)) {
            return states.get(name);
        }
        return null;
    }

    /**
     * Gets value of a state object of given name, if exists
     *
     * @param name
     *            name of state object
     * @return
     *         state object's value
     */
    public @Nullable Double getStateValue(String name) {
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
     *         state object's textvalue
     */
    public @Nullable String getStateTextValue(String name) {
        LxControlState state = getState(name);
        if (state != null) {
            return state.getTextValue();
        }
        return null;
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
        LxContainer localRoom = room;
        if (localRoom != null) {
            localRoom.removeControl(this);
        }
        LxCategory localCat = category;
        if (localCat != null) {
            localCat.removeControl(this);
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
    public @Nullable LxContainer getRoom() {
        return room;
    }

    /**
     * Obtain category of this control
     *
     * @return
     *         Control's category or null if no category
     */
    public @Nullable LxCategory getCategory() {
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
    public boolean equals(@Nullable Object object) {
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
    void update(LxJsonControl json, @Nullable LxContainer room, @Nullable LxCategory category) {
        logger.trace("Updating LxControl: {}", json.type);

        String jsonName = json.name;
        if (jsonName != null) {
            this.name = jsonName;
        }
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
                @SuppressWarnings("null")
                @NonNull
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
                    @SuppressWarnings("null")
                    @NonNull
                    String name = jsonState.getKey().toLowerCase();
                    if (states.containsKey(name)) {
                        logger.trace("Existing state for LxControl {}: {}", json.type, name);
                        @SuppressWarnings("null")
                        @NonNull
                        LxControlState state = states.get(name);
                        state.getUuid().setUpdate(true);
                    } else {
                        logger.trace("New state for LxControl {}: {}", json.type, name);
                        LxControlState state = new LxControlState(id, name, this);
                        states.put(name, state);
                    }
                }
            }
        }
    }

    /**
     * Create a {@link LxControl} object for a control received from the Miniserver
     *
     * @param client
     *            websocket client to facilitate communication with Miniserver
     * @param uuid
     *            UUID of the control to be created
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            Room that this control belongs to
     * @param category
     *            Category that this control belongs to
     * @return
     *         created control object or null if error
     */
    static @Nullable LxControl createControl(LxWsClient client, LxUuid uuid, LxJsonControl json,
            @Nullable LxContainer room, @Nullable LxCategory category) {
        if (json.type == null || json.name == null) {
            return null;
        }
        LxControl ctrl = null;

        @SuppressWarnings("null")
        @NonNull
        String type = json.type.toLowerCase();

        if (LxControlSwitch.accepts(type)) {
            ctrl = new LxControlSwitch(client, uuid, json, room, category);
        } else if (LxControlPushbutton.accepts(type)) {
            ctrl = new LxControlPushbutton(client, uuid, json, room, category);
        } else if (LxControlTimedSwitch.accepts(type)) {
            ctrl = new LxControlTimedSwitch(client, uuid, json, room, category);
        } else if (LxControlDimmer.accepts(type)) {
            ctrl = new LxControlDimmer(client, uuid, json, room, category);
        } else if (LxControlJalousie.accepts(type)) {
            ctrl = new LxControlJalousie(client, uuid, json, room, category);
        } else if (LxControlTextState.accepts(type)) {
            ctrl = new LxControlTextState(client, uuid, json, room, category);
        } else if (json.details != null) {
            if (LxControlInfoOnlyDigital.accepts(type) && json.details.text != null) {
                ctrl = new LxControlInfoOnlyDigital(client, uuid, json, room, category);
            } else if (LxControlInfoOnlyAnalog.accepts(type)) {
                ctrl = new LxControlInfoOnlyAnalog(client, uuid, json, room, category);
            } else if (LxControlLightController.accepts(type)) {
                ctrl = new LxControlLightController(client, uuid, json, room, category);
            } else if (LxControlRadio.accepts(type)) {
                ctrl = new LxControlRadio(client, uuid, json, room, category);
            }
        }
        return ctrl;
    }
}
