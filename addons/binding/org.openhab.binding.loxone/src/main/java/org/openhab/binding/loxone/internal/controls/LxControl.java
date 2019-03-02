/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;
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
         * @param handlerApi thing handler object representing the Miniserver
         * @param uuid       UUID of this control
         * @param json       JSON describing the control as received from the Miniserver
         * @param room       Room that this control belongs to
         * @param category   Category that this control belongs to
         * @return a newly created control object
         */
        abstract LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category);

        /**
         * Return a type name for this control.
         *
         * @return type name (as used on the Miniserver)
         */
        abstract String getType();
    }

    final LxServerHandlerApi handlerApi;
    final LxUuid uuid;
    final ChannelUID defaultChannelId;
    final String defaultChannelLabel;
    final Set<String> tags = new HashSet<>();
    final List<Channel> channels = new ArrayList<>();

    private LxContainer room;
    private LxCategory category;
    private String name;

    private final ThingUID thingId;
    private final Logger logger = LoggerFactory.getLogger(LxControl.class);
    private final Map<LxUuid, LxControl> subControls = new HashMap<>();
    private final Map<String, LxControlState> states = new HashMap<>();

    /**
     * Create a Miniserver's control object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       UUID of this control
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       Room that this control belongs to
     * @param category   Category that this control belongs to
     */
    LxControl(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        logger.trace("Creating new LxControl: {}", json.type);
        this.uuid = uuid;
        this.handlerApi = handlerApi;
        thingId = handlerApi.getThingId();
        defaultChannelId = getChannelId(0);

        update(json, room, category);

        String label = getLabel();
        if (label == null) {
            // Each control on a Miniserver must have a name defined, but in case this is a subject
            // of some malicious data attack, we'll prevent null pointer exception
            label = "Undefined name";
        }
        String roomName = room != null ? room.getName() : null;
        if (roomName != null) {
            label = roomName + " / " + label;
        }
        defaultChannelLabel = label;
    }

    /**
     * A method that executes commands by the control.
     *
     * @param channelId channel Id for the command
     * @param command   value of the command to perform
     * @throws IOException in case of communication error with the Miniserver
     */
    public abstract void handleCommand(ChannelUID channelId, Command command) throws IOException;

    /**
     * Provides actual state value for the specified channel
     *
     * @param channelId channel ID to get state for
     * @return state if the channel value or null if no value available
     */
    public abstract State getChannelState(ChannelUID channelId);

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
        subControls.values().forEach(control -> control.dispose());
    }

    /**
     * Obtain control's name
     *
     * @return Human readable name of control
     */
    public String getName() {
        return name;
    }

    /**
     * Get control's UUID as defined on the Miniserver
     *
     * @return UUID of the control
     */
    public LxUuid getUuid() {
        return uuid;
    }

    /**
     * Get subcontrols of this control
     *
     * @return subcontrols of the control
     */
    public Map<LxUuid, LxControl> getSubControls() {
        return subControls;
    }

    /**
     * Get control's channels
     *
     * @return channels
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * Get control's Miniserver states
     *
     * @return control's Miniserver states
     */
    public Map<String, LxControlState> getStates() {
        return states;
    }

    /**
     * Compare UUID's of two controls -
     *
     * @param object Object to compare with
     * @return true if UUID of two objects are equal
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
        return Objects.equals(c.uuid, uuid);
    }

    /**
     * Hash code of the control is equal to its UUID's hash code
     */
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * Update Miniserver's control in runtime.
     *
     * @param json     JSON describing the control as received from the Miniserver
     * @param room     New room that this control belongs to
     * @param category New category that this control belongs to
     */
    public void update(LxJsonControl json, LxContainer room, LxCategory category) {
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

    /**
     * Get control's room.
     *
     * @return control's room object
     */
    LxContainer getRoom() {
        return room;
    }

    /**
     * Get control's category.
     *
     * @return control's category object
     */
    LxCategory getCategory() {
        return category;
    }

    /**
     * This method will be called from {@link LxControlState}, when Miniserver state value is updated.
     * By default it will query all channels of the control and update their state accordingly.
     * This method will not handle channel state descriptions, as they must be prepared individually.
     * It can be overridden in child class to handle particular states differently.
     *
     * @param state changed Miniserver state or null if not specified (any/all)
     */
    void onStateChange(LxControlState state) {
        channels.forEach(channel -> {
            ChannelUID channelId = channel.getUID();
            State channelState = getChannelState(channelId);
            if (channelState != null) {
                handlerApi.setChannelState(channelId, channelState);
            }
        });
    }

    /**
     * Returns control label that will be used for building channel name. This allows for customizing the label per
     * control.
     *
     * @return control channel label
     */
    String getLabel() {
        return name;
    }

    /**
     * Gets value of a state object of given name, if exists
     *
     * @param name name of state object
     * @return state object's value
     */
    Double getStateDoubleValue(String name) {
        LxControlState state = states.get(name);
        if (state != null) {
            Object value = state.getStateValue();
            if (value instanceof Double) {
                return (Double) value;
            }
        }
        return null;
    }

    /**
     * Gets text value of a state object of given name, if exists
     *
     * @param name name of state object
     * @return state object's text value
     */
    String getStateTextValue(String name) {
        LxControlState state = states.get(name);
        if (state != null) {
            Object value = state.getStateValue();
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    /**
     * Build channel ID for the control, based on control's UUID, thing's UUID and index of the channel for the control
     *
     * @param index index of a channel within control (0 for primary channel) all indexes greater than 0 will have
     *                  -index added to the channel ID
     * @return channel ID for the control and index
     */
    ChannelUID getChannelId(int index) {
        String controlId = uuid.toString();
        if (index > 0) {
            controlId += "-" + index;
        }
        return new ChannelUID(thingId, controlId);
    }

    /**
     * Create a new channel and add it to the control.
     *
     * @param itemType           item type for the channel
     * @param typeId             channel type ID for the channel
     * @param channelId          channel ID
     * @param channelLabel       channel label
     * @param channelDescription channel description
     * @param tags               tags for the channel or null if no tags needed
     */
    void addChannel(String itemType, ChannelTypeUID typeId, ChannelUID channelId, String channelLabel,
            String channelDescription, Set<String> tags) {
        ChannelBuilder builder = ChannelBuilder.create(channelId, itemType).withType(typeId).withLabel(channelLabel)
                .withDescription(channelDescription + " : " + channelLabel);
        if (tags != null) {
            builder.withDefaultTags(tags);
        }
        channels.add(builder.build());
    }

    /**
     * Adds a new {@link StateDescription} for a channel that has multiple options to select from or a custom format
     * string.
     *
     * @param channelId   channel ID to add the description for
     * @param description channel state description
     */
    void addChannelStateDescription(ChannelUID channelId, StateDescription description) {
        handlerApi.setChannelStateDescription(channelId, description);
    }

    /**
     * Sends an action command to the Miniserver using active socket connection
     *
     * @param action string with action command
     * @throws IOException when communication error with Miniserver occurs
     */
    void sendAction(String action) throws IOException {
        handlerApi.sendAction(uuid, action);
    }
}
