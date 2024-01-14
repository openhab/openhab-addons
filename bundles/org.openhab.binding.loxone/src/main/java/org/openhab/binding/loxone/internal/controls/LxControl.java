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
package org.openhab.binding.loxone.internal.controls;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxConfig;
import org.openhab.binding.loxone.internal.types.LxContainer;
import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

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
public class LxControl {

    /**
     * This class contains static configuration of the control and is used to make the fields transparent to the child
     * classes that implement specific controls.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    public static class LxControlConfig {
        private final LxServerHandlerApi thingHandler;
        private final LxContainer room;
        private final LxCategory category;

        LxControlConfig(LxControlConfig config) {
            this(config.thingHandler, config.room, config.category);
        }

        public LxControlConfig(LxServerHandlerApi thingHandler, LxContainer room, LxCategory category) {
            this.room = room;
            this.category = category;
            this.thingHandler = thingHandler;
        }
    }

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
         * @param uuid UUID of the control object to be created
         * @return a newly created control object
         */
        abstract LxControl create(LxUuid uuid);

        /**
         * Return a type name for this control.
         *
         * @return type name (as used on the Miniserver)
         */
        abstract String getType();
    }

    /**
     * This class describes additional parameters of a control received from the Miniserver and is used during JSON
     * deserialization.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    class LxControlDetails {
        Double min;
        Double max;
        Double step;
        String format;
        String actualFormat;
        String totalFormat;
        Boolean increaseOnly;
        String allOff;
        String url;
        String urlHd;
        Map<String, String> outputs;
        Boolean presenceConnected;
        Integer connectedInputs;
        Boolean hasVaporizer;
        Boolean hasDoorSensor;
    }

    /**
     * A callback that should be implemented by child classes to process received commands. This callback can be
     * provided for each channel created by the controls.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    @FunctionalInterface
    interface CommandCallback {
        abstract void handleCommand(Command cmd) throws IOException;
    }

    /**
     * A callback that should be implemented by child classes to return current channel state. This callback can be
     * provided for each channel created by the controls.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    @FunctionalInterface
    interface StateCallback {
        abstract State getChannelState();
    }

    /**
     * A set of callbacks registered per each channel by the child classes.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class Callbacks {
        private CommandCallback commandCallback;
        private StateCallback stateCallback;

        private Callbacks(CommandCallback cC, StateCallback sC) {
            commandCallback = cC;
            stateCallback = sC;
        }
    }

    /*
     * Parameters parsed from the JSON configuration file during deserialization
     */
    LxUuid uuid;
    LxControlDetails details;
    private String name;
    private LxUuid roomUuid;
    private Boolean isSecured;
    private LxUuid categoryUuid;
    private Map<LxUuid, LxControl> subControls;
    private final Map<String, LxState> states;

    /*
     * Parameters set when finalizing {@link LxConfig} object setup. They will be null right after constructing object.
     */
    transient String defaultChannelLabel;
    private transient LxControlConfig config;

    /*
     * Parameters set when object is connected to the openHAB by the binding handler
     */
    final transient Set<String> tags = new HashSet<>();
    private final transient List<Channel> channels = new ArrayList<>();
    private final transient Map<ChannelUID, Callbacks> callbacks = new HashMap<>();

    private final transient Logger logger;
    private int numberOfChannels = 0;

    /*
     * JSON deserialization routine, called during parsing configuration by the GSON library
     */
    public static final JsonDeserializer<LxControl> DESERIALIZER = new JsonDeserializer<>() {
        @Override
        public LxControl deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject parent = json.getAsJsonObject();
            String controlName = LxConfig.deserializeString(parent, "name");
            String controlType = LxConfig.deserializeString(parent, "type");
            LxUuid uuid = LxConfig.deserializeObject(parent, "uuidAction", LxUuid.class, context);
            if (controlName == null || controlType == null || uuid == null) {
                throw new JsonParseException("Control name/type/uuid is null.");
            }
            LxControl control = LxControlFactory.createControl(uuid, controlType);
            if (control == null) {
                return null;
            }
            control.name = controlName;
            control.isSecured = LxConfig.deserializeObject(parent, "isSecured", Boolean.class, context);
            control.roomUuid = LxConfig.deserializeObject(parent, "room", LxUuid.class, context);
            control.categoryUuid = LxConfig.deserializeObject(parent, "cat", LxUuid.class, context);
            control.details = LxConfig.deserializeObject(parent, "details", LxControlDetails.class, context);
            control.subControls = LxConfig.deserializeObject(parent, "subControls",
                    new TypeToken<Map<LxUuid, LxControl>>() {
                    }.getType(), context);

            JsonObject states = parent.getAsJsonObject("states");
            if (states != null) {
                states.entrySet().forEach(entry -> {
                    // temperature state of intelligent home controller object is the only
                    // one that has state represented as an array, as this is not implemented
                    // yet, we will skip this state
                    JsonElement element = entry.getValue();
                    if (element != null && !(element instanceof JsonArray)) {
                        String value = element.getAsString();
                        if (value != null) {
                            String name = entry.getKey().toLowerCase();
                            control.states.put(name, new LxState(new LxUuid(value), name, control));
                        }
                    }
                });
            }
            return control;
        }
    };

    LxControl(LxUuid uuid) {
        logger = LoggerFactory.getLogger(LxControl.class);
        this.uuid = uuid;
        states = new HashMap<>();
    }

    /**
     * A method that executes commands by the control. It delegates command execution to a registered callback method.
     *
     * @param channelId channel Id for the command
     * @param command value of the command to perform
     * @throws IOException in case of communication error with the Miniserver
     */
    public final void handleCommand(ChannelUID channelId, Command command) throws IOException {
        Callbacks c = callbacks.get(channelId);
        if (c != null && c.commandCallback != null) {
            c.commandCallback.handleCommand(command);
        } else {
            logger.debug("Control UUID={} has no command handler", getUuid());
        }
    }

    /**
     * Provides actual state value for the specified channel. It delegates execution to a registered callback method.
     *
     * @param channelId channel ID to get state for
     * @return state if the channel value or null if no value available
     */
    public final State getChannelState(ChannelUID channelId) {
        Callbacks c = callbacks.get(channelId);
        if (c != null && c.stateCallback != null) {
            try {
                return c.stateCallback.getChannelState();
            } catch (NumberFormatException e) {
                return UnDefType.UNDEF;
            }
        }
        return null;
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
     * Get control's and its subcontrols' channels
     *
     * @return channels
     */
    public List<Channel> getChannelsWithSubcontrols() {
        final List<Channel> list = new ArrayList<>(channels);
        subControls.values().forEach(c -> list.addAll(c.getChannelsWithSubcontrols()));
        return list;
    }

    /**
     * Get control's Miniserver states
     *
     * @return control's Miniserver states
     */
    public Map<String, LxState> getStates() {
        return states;
    }

    /**
     * Gets information is password is required to operate on this control object
     *
     * @return true is control is secured
     */
    public Boolean isSecured() {
        return isSecured != null && isSecured;
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
     * Initialize Miniserver's control in runtime. Each class that implements {@link LxControl} should override this
     * method and call it as a first step in the overridden implementation. Then it should add all runtime data, like
     * channels and any fields that derive their value from the parsed JSON configuration.
     * Before this method is called during configuration parsing, the control object must not be used.
     *
     * @param configToSet control's configuration
     */
    public void initialize(LxControlConfig configToSet) {
        logger.debug("Initializing LxControl: {}", uuid);

        if (config != null) {
            logger.error("Error, attempt to initialize control that is already initialized: {}", uuid);
            return;
        }
        config = configToSet;

        if (subControls == null) {
            subControls = new HashMap<>();
        } else {
            subControls.values().removeIf(Objects::isNull);
        }

        if (config.room != null) {
            config.room.addControl(this);
        }

        if (config.category != null) {
            config.category.addControl(this);
        }

        String label = getLabel();
        if (label == null) {
            // Each control on a Miniserver must have a name defined, but in case this is a subject
            // of some malicious data attack, we'll prevent null pointer exception
            label = "Undefined name";
        }
        String roomName = config.room != null ? config.room.getName() : null;
        if (roomName != null) {
            label = roomName + " / " + label;
        }
        defaultChannelLabel = label;

        // Propagate to all subcontrols of this control object
        subControls.values().forEach(c -> c.initialize(config));
    }

    /**
     * This method will be called from {@link LxState}, when Miniserver state value is updated.
     * By default it will query all channels of the control and update their state accordingly.
     * This method will not handle channel state descriptions, as they must be prepared individually.
     * It can be overridden in child class to handle particular states differently.
     *
     * @param state changed Miniserver state or null if not specified (any/all)
     */
    public void onStateChange(LxState state) {
        if (config == null) {
            logger.error("Attempt to change state with not finalized configuration!: {}", state.getUuid());
        } else {
            channels.forEach(channel -> {
                ChannelUID channelId = channel.getUID();
                State channelState = getChannelState(channelId);
                if (channelState != null) {
                    config.thingHandler.setChannelState(channelId, channelState);
                }
            });
        }
    }

    /**
     * Gets room UUID after it was deserialized by GSON
     *
     * @return room UUID
     */
    public LxUuid getRoomUuid() {
        return roomUuid;
    }

    /**
     * Gets category UUID after it was deserialized by GSON
     *
     * @return category UUID
     */
    public LxUuid getCategoryUuid() {
        return categoryUuid;
    }

    /**
     * Gets a GSON object for reuse
     *
     * @return GSON object
     */
    Gson getGson() {
        if (config == null) {
            logger.error("Attempt to get GSON from not finalized configuration!");
            return null;
        }
        return config.thingHandler.getGson();
    }

    /**
     * Adds a new control in the framework. Called when a control is dynamically created based on some control's state
     * changes from the Miniserver.
     *
     * @param control a new control to be created
     */
    static void addControl(LxControl control) {
        control.config.thingHandler.addControl(control);
    }

    /**
     * Removes a control from the framework. Called when a control is dynamically deleted based on some control's state
     * changes from the Miniserver.
     *
     * @param control a control to be removed
     */
    static void removeControl(LxControl control) {
        control.config.thingHandler.removeControl(control);
        control.dispose();
    }

    /**
     * Gets control's configuration
     *
     * @return configuration
     */
    LxControlConfig getConfig() {
        return config;
    }

    /**
     * Get control's room.
     *
     * @return control's room object
     */
    LxContainer getRoom() {
        return config.room;
    }

    /**
     * Get control's category.
     *
     * @return control's category object
     */
    LxCategory getCategory() {
        return config.category;
    }

    /**
     * Changes the channel state in the framework.
     *
     * @param id channel ID
     * @param state new state value
     */
    void setChannelState(ChannelUID id, State state) {
        if (config == null) {
            logger.error("Attempt to set channel state with not finalized configuration!: {}", id);
        } else {
            if (state != null) {
                config.thingHandler.setChannelState(id, state);
            }
        }
    }

    /**
     * Returns control label that will be used for building channel name. This allows for customizing the label per
     * control by overriding this method, but keeping {@link LxControl#getName()} intact.
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
        LxState state = states.get(name);
        if (state != null) {
            Object value = state.getStateValue();
            if (value instanceof Double) {
                return (Double) value;
            }
        }
        return null;
    }

    /**
     * Gets value of a state object of given name, if exists, and converts it to decimal type value.
     *
     * @param name state name
     * @return state value
     */
    State getStateDecimalValue(String name) {
        Double value = getStateDoubleValue(name);
        if (value != null) {
            return new DecimalType(value);
        }
        return null;
    }

    /**
     * Gets value of a state object of given name, if exists, and converts it to percent type value.
     * Assumes the state value is between 0.0-100.0 which corresponds directly to 0-100 percent.
     *
     * @param name state name
     * @return state value
     */
    State getStatePercentValue(String name) {
        Double value = getStateDoubleValue(name);
        if (value == null) {
            return null;
        }
        if (value >= 0.0 && value <= 100.0) {
            return new PercentType(value.intValue());
        }
        return UnDefType.UNDEF;
    }

    /**
     * Gets text value of a state object of given name, if exists
     *
     * @param name name of state object
     * @return state object's text value
     */
    String getStateTextValue(String name) {
        LxState state = states.get(name);
        if (state != null) {
            Object value = state.getStateValue();
            if (value instanceof String str) {
                return str;
            }
        }
        return null;
    }

    /**
     * Gets text value of a state object of given name, if exists and converts it to string type
     *
     * @param name name of state object
     * @return state object's text value
     */
    State getStateStringValue(String name) {
        String value = getStateTextValue(name);
        if (value != null) {
            return new StringType(value);
        }
        return null;
    }

    /**
     * Gets double value of a state object of given name, if exists and converts it to switch type
     *
     * @param name name of state object
     * @return state object's text value
     */
    State getStateOnOffValue(String name) {
        Double value = getStateDoubleValue(name);
        if (value != null) {
            if (value == 1.0) {
                return OnOffType.ON;
            }
            return OnOffType.OFF;
        }
        return null;
    }

    /**
     * Create a new channel and add it to the control. Channel ID is assigned automatically in the order of calls to
     * this method, see (@link LxControl#getChannelId}.
     *
     * @param itemType item type for the channel
     * @param typeId channel type ID for the channel
     * @param channelLabel channel label
     * @param channelDescription channel description
     * @param tags tags for the channel or null if no tags needed
     * @param commandCallback {@link LxControl} child class method that will be called when command is received
     * @param stateCallback {@link LxControl} child class method that will be called to get state value
     * @return channel ID of the added channel (can be used to later set state description to it)
     */
    ChannelUID addChannel(String itemType, ChannelTypeUID typeId, String channelLabel, String channelDescription,
            Set<String> tags, CommandCallback commandCallback, StateCallback stateCallback) {
        if (channelLabel == null || channelDescription == null) {
            logger.error("Attempt to add channel with not finalized configuration!: {}", channelLabel);
            return null;
        }
        ChannelUID channelId = getChannelId(numberOfChannels++);
        ChannelBuilder builder = ChannelBuilder.create(channelId, itemType).withType(typeId).withLabel(channelLabel)
                .withDescription(channelDescription + " : " + channelLabel);
        if (tags != null) {
            builder.withDefaultTags(tags);
        }
        channels.add(builder.build());
        if (commandCallback != null || stateCallback != null) {
            callbacks.put(channelId, new Callbacks(commandCallback, stateCallback));
        }
        return channelId;
    }

    /**
     * Adds a new {@link StateDescriptionFragment} for a channel that has multiple options to select from or a custom
     * format string.
     *
     * @param channelId channel ID to add the description for
     * @param descriptionFragment channel state description fragment
     */
    void addChannelStateDescriptionFragment(ChannelUID channelId, StateDescriptionFragment descriptionFragment) {
        if (config == null) {
            logger.error("Attempt to set channel state description with not finalized configuration!: {}", channelId);
        } else {
            if (channelId != null && descriptionFragment != null) {
                config.thingHandler.setChannelStateDescription(channelId, descriptionFragment.toStateDescription());
            }
        }
    }

    /**
     * Sends an action command to the Miniserver using active socket connection
     *
     * @param action string with action command
     * @throws IOException when communication error with Miniserver occurs
     */
    void sendAction(String action) throws IOException {
        if (config == null) {
            logger.error("Attempt to send command with not finalized configuration!: {}", action);
        } else {
            config.thingHandler.sendAction(uuid, action);
        }
    }

    /**
     * Remove all channels from the control. This method is used by child classes that may decide to stop exposing any
     * channels, for example by {@link LxControlMood}, which is based on {@link LxControlSwitch}, but sometime does not
     * expose anything to the user.
     */
    void removeAllChannels() {
        channels.clear();
        callbacks.clear();
    }

    /**
     * Call when control is no more needed - unlink it from containers
     */
    private void dispose() {
        if (config.room != null) {
            config.room.removeControl(this);
        }
        if (config.category != null) {
            config.category.removeControl(this);
        }
        subControls.values().forEach(control -> control.dispose());
    }

    /**
     * Build channel ID for the control, based on control's UUID, thing's UUID and index of the channel for the control
     *
     * @param index index of a channel within control (0 for primary channel) all indexes greater than 0 will have
     *            -index added to the channel ID
     * @return channel ID for the control and index
     */
    private ChannelUID getChannelId(int index) {
        if (config == null) {
            logger.error("Attempt to get control's channel ID with not finalized configuration!: {}", index);
            return null;
        }
        String controlId = uuid.toString();
        if (index > 0) {
            controlId += "-" + index;
        }
        return new ChannelUID(config.thingHandler.getThingId(), controlId);
    }
}
