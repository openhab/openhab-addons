/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.loxone.core.LxJsonApp3.LxJsonControl;
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
public abstract class LxControl {
    private String name;
    private String typeName = null;
    private LxContainer room;
    private LxCategory category;
    private Map<String, LxControlState> states = new HashMap<String, LxControlState>();
    Logger logger = LoggerFactory.getLogger(LxControl.class);

    LxUuid uuid;
    LxWsClient socketClient;
    Map<LxUuid, LxControl> subControls = new HashMap<LxUuid, LxControl>();

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
        socketClient = client;
        this.uuid = uuid;
        if (json.type != null) {
            this.typeName = json.type.toLowerCase();
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
    public LxControlState getState(String name) {
        if (states.containsKey(name)) {
            return states.get(name);
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
                        state = new LxControlState(id, name, this);
                    } else {
                        state.getUuid().setUpdate(true);
                        state.setName(name);
                    }
                    states.put(name, state);
                }
            }
        }
    }

    static LxControl createControl(LxWsClient client, LxUuid id, LxJsonControl json, LxContainer room,
            LxCategory category) {
        if (json == null || json.type == null || json.name == null) {
            return null;
        }

        LxControl ctrl = null;
        String type = json.type.toLowerCase();

        if (LxControlSwitch.accepts(type)) {
            ctrl = new LxControlSwitch(client, id, json, room, category);

        } else if (LxControlPushbutton.accepts(type)) {
            ctrl = new LxControlPushbutton(client, id, json, room, category);

        } else if (LxControlJalousie.accepts(type)) {
            ctrl = new LxControlJalousie(client, id, json, room, category);
        } else if (LxControlTextState.accepts(type)) {
            ctrl = new LxControlTextState(client, id, json, room, category);

        } else if (json.details != null) {

            if (LxControlInfoOnlyDigital.accepts(type) && json.details.text != null) {
                ctrl = new LxControlInfoOnlyDigital(client, id, json, room, category);

            } else if (LxControlInfoOnlyAnalog.accepts(type)) {
                ctrl = new LxControlInfoOnlyAnalog(client, id, json, room, category);

            } else if (LxControlLightController.accepts(type)) {
                ctrl = new LxControlLightController(client, id, json, room, category);

            } else if (LxControlRadio.accepts(type)) {
                ctrl = new LxControlRadio(client, id, json, room, category);
            }
        }
        return ctrl;
    }
}
