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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.handler.InsteonChannelHandler;
import org.openhab.binding.insteon.internal.handler.feature.CommandHandler;
import org.openhab.binding.insteon.internal.handler.feature.MessageDispatcher;
import org.openhab.binding.insteon.internal.handler.feature.MessageHandler;
import org.openhab.binding.insteon.internal.handler.feature.PollHandler;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.ParameterParser;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DeviceFeature represents a certain feature (trait) of a given Insteon device, e.g. something
 * operating under a given InsteonAddress that can be manipulated (relay) or read (sensor).
 *
 * The DeviceFeature does the processing of incoming messages, and handles commands for the
 * particular feature it represents.
 *
 * It uses four mechanisms for that:
 *
 * 1) MessageDispatcher: makes high level decisions about an incoming message and then runs the
 * 2) MessageHandler: further processes the message, updates state etc
 * 3) CommandHandler: translates commands from the openhab bus into an Insteon message.
 * 4) PollHandler: creates an Insteon message to query the DeviceFeature
 *
 * Lastly, InsteonChannelHandler can register with the DeviceFeature to get notifications when
 * the state of a feature has changed. In practice, a InsteonChannelHandler corresponds to an
 * openHAB item.
 *
 * The character of a DeviceFeature is thus given by a set of message and command handlers.
 * A FeatureTemplate captures exactly that: it says what set of handlers make up a DeviceFeature.
 *
 * DeviceFeatures are added to a new device by referencing a FeatureTemplate (defined in device_features.xml)
 * from the Device definition file (device_types.xml).
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class DeviceFeature {
    public enum QueryStatus {
        NEVER_QUERIED,
        QUERY_CREATED,
        QUERY_QUEUED,
        QUERY_PENDING,
        QUERY_ANSWERED,
        NOT_POLLABLE
    }

    public enum StateChangeType {
        ALWAYS,
        CHANGED
    };

    private static final Logger logger = LoggerFactory.getLogger(DeviceFeature.class);

    private String name;
    private String type;
    private InsteonDevice device = new InsteonDevice();
    private QueryStatus queryStatus = QueryStatus.NOT_POLLABLE;
    private State state = UnDefType.NULL;
    private @Nullable Double lastMsgValue;
    private @Nullable Msg lastQueryMsg;

    private MessageHandler defaultMsgHandler = MessageHandler.makeDefaultHandler(this);
    private CommandHandler defaultCommandHandler = CommandHandler.makeDefaultHandler(this);
    private @Nullable PollHandler pollHandler;
    private @Nullable MessageDispatcher dispatcher;
    private @Nullable DeviceFeature groupFeature;

    private Map<String, String> parameters = new HashMap<>();
    private Map<Integer, MessageHandler> msgHandlers = new HashMap<>();
    private Map<Class<? extends Command>, CommandHandler> commandHandlers = new HashMap<>();
    private Map<ChannelUID, InsteonChannelHandler> channelHandlers = new HashMap<>();
    private List<DeviceFeature> connectedFeatures = new ArrayList<>();

    /**
     * Constructor
     *
     * @param type feature type
     */
    public DeviceFeature(String type) {
        this.name = type; // use feature type as name by default
        this.type = type;
    }

    // various simple getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public InsteonDevice getDevice() {
        return device;
    }

    public Map<String, String> getParameters() {
        synchronized (parameters) {
            return parameters;
        }
    }

    public @Nullable String getParameter(String key) {
        synchronized (parameters) {
            return parameters.get(key);
        }
    }

    public boolean hasParameter(String key) {
        synchronized (parameters) {
            return parameters.containsKey(key);
        }
    }

    public boolean getParameterAsBoolean(String key, boolean defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), Boolean.class, defaultValue);
    }

    public int getParameterAsInteger(String key, int defaultValue) {
        return ParameterParser.getParameterAsOrDefault(getParameter(key), Integer.class, defaultValue);
    }

    public synchronized @Nullable Double getLastMsgValue() {
        return lastMsgValue;
    }

    public double getLastMsgValueAsDouble(double defaultValue) {
        Double lastMsgValue = getLastMsgValue();
        return lastMsgValue == null ? defaultValue : lastMsgValue.doubleValue();
    }

    public int getLastMsgValueAsInteger(int defaultValue) {
        Double lastMsgValue = getLastMsgValue();
        return lastMsgValue == null ? defaultValue : lastMsgValue.intValue();
    }

    public synchronized @Nullable Msg getLastQueryMessage() {
        return lastQueryMsg;
    }

    public int getLastQueryCommand() {
        Msg lastQueryMsg = getLastQueryMessage();
        return lastQueryMsg == null ? -1 : lastQueryMsg.getIntOrDefault("command1", -1);
    }

    public int getLastQueryCommand2() {
        Msg lastQueryMsg = getLastQueryMessage();
        return lastQueryMsg == null ? -1 : lastQueryMsg.getIntOrDefault("command2", -1);
    }

    public synchronized QueryStatus getQueryStatus() {
        return queryStatus;
    }

    public synchronized State getState() {
        return state;
    }

    public boolean isGroupFeature() {
        return !connectedFeatures.isEmpty();
    }

    public boolean isPartOfGroupFeature() {
        return groupFeature != null;
    }

    public boolean isControllerFeature() {
        String linkType = getParameter("link");
        return "both".equals(linkType) || "controller".equals(linkType);
    }

    public boolean isResponderFeature() {
        String linkType = getParameter("link");
        return "both".equals(linkType) || "responder".equals(linkType);
    }

    public boolean isEventFeature() {
        return getParameterAsBoolean("event", false);
    }

    public boolean isHiddenFeature() {
        return getParameterAsBoolean("hidden", false);
    }

    public boolean isStatusFeature() {
        return getParameterAsBoolean("status", false);
    }

    public int getGroup() {
        return getParameterAsInteger("group", 1);
    }

    public MessageHandler getDefaultMsgHandler() {
        return defaultMsgHandler;
    }

    public @Nullable MessageHandler getMsgHandler(int key) {
        synchronized (msgHandlers) {
            return msgHandlers.get(key);
        }
    }

    public MessageHandler getOrDefaultMsgHandler(int key) {
        synchronized (msgHandlers) {
            return msgHandlers.getOrDefault(key, defaultMsgHandler);
        }
    }

    public @Nullable CommandHandler getCommandHandler(Class<? extends Command> key) {
        synchronized (commandHandlers) {
            return commandHandlers.get(key);
        }
    }

    public @Nullable PollHandler getPollHandler() {
        return pollHandler;
    }

    public List<DeviceFeature> getConnectedFeatures() {
        return connectedFeatures;
    }

    public @Nullable DeviceFeature getGroupFeature() {
        return groupFeature;
    }

    // various simple setters
    public void setDevice(InsteonDevice device) {
        this.device = device;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessageDispatcher(@Nullable MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void setPollHandler(@Nullable PollHandler pollHandler) {
        this.pollHandler = pollHandler;
    }

    public void setDefaultCommandHandler(CommandHandler defaultCommandHandler) {
        this.defaultCommandHandler = defaultCommandHandler;
    }

    public void setDefaultMsgHandler(MessageHandler defaultMsgHandler) {
        this.defaultMsgHandler = defaultMsgHandler;
    }

    public void setGroupFeature(DeviceFeature groupFeature) {
        this.groupFeature = groupFeature;
    }

    public synchronized void setLastMsgValue(double lastMsgValue) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} set last message value to: {}", device.getAddress(), name, lastMsgValue);
        }
        this.lastMsgValue = lastMsgValue;
    }

    public synchronized void setLastQueryMessage(@Nullable Msg lastQueryMsg) {
        this.lastQueryMsg = lastQueryMsg;
    }

    public synchronized void setState(State state) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} set state to: {}", device.getAddress(), name, state);
        }
        this.state = state;
    }

    public synchronized void setQueryStatus(QueryStatus queryStatus) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} set query status to: {}", device.getAddress(), name, queryStatus);
        }
        this.queryStatus = queryStatus;
    }

    public void initializeQueryStatus() {
        PollHandler pollHandler = this.pollHandler;
        // set query status to never queried if feature pollable,
        // otherwise to not pollable if not already in that state
        if (pollHandler != null && pollHandler.makeMsg() != null) {
            setQueryStatus(QueryStatus.NEVER_QUERIED);
        } else if (queryStatus != QueryStatus.NOT_POLLABLE) {
            setQueryStatus(QueryStatus.NOT_POLLABLE);
        }
    }

    public void addParameters(Map<String, String> params) {
        synchronized (parameters) {
            parameters.putAll(params);
        }
    }

    /**
     * Adds a connected feature such that this DeviceFeature can
     * act as a feature group
     *
     * @param feature the device feature connected to this feature
     */
    public void addConnectedFeature(DeviceFeature feature) {
        connectedFeatures.add(feature);
    }

    /**
     * Adds a channel handler to this feature
     *
     * @param channelUID the channel uid to add
     * @param handler the channel handler to add
     */
    public void addChannelHandler(ChannelUID channelUID, InsteonChannelHandler handler) {
        synchronized (channelHandlers) {
            if (!channelHandlers.containsKey(channelUID)) {
                channelHandlers.put(channelUID, handler);
            }
        }
    }

    /**
     * Removes a channel handler from this feature
     *
     * @param channelUID the channel uid to remove
     */
    public void removeChannelHandler(ChannelUID channelUID) {
        synchronized (channelHandlers) {
            channelHandlers.remove(channelUID);
        }
    }

    /**
     * Returns a channel handler based on channel uid
     *
     * @param channelUID the channel uid to check
     * @return channel handler if defined, otherwise null
     */
    public @Nullable InsteonChannelHandler getChannelHandler(ChannelUID channelUID) {
        synchronized (channelHandlers) {
            return channelHandlers.get(channelUID);
        }
    }

    /**
     * Returns list of channel handlers for this feature
     *
     * @return list of channel handlers
     */
    public List<InsteonChannelHandler> getChannelHandlers() {
        synchronized (channelHandlers) {
            return new ArrayList<>(channelHandlers.values());
        }
    }

    /**
     * Checks if this feature has a defined channel handler for a specific channel
     *
     * @param channelUID the channel uid to check
     * @return true if a handler is defined
     */
    public boolean hasChannelHandler(ChannelUID channelUID) {
        return getChannelHandler(channelUID) != null;
    }

    /**
     * Checks if this feature or any its connected features has a defined channel handler
     *
     * @return true if at least one handler is defined
     */
    public boolean hasChannelHandlers() {
        return !getChannelHandlers().isEmpty()
                || getConnectedFeatures().stream().anyMatch(DeviceFeature::hasChannelHandlers);
    }

    /**
     * Checks if this feature or any its connected features has a responder feature
     *
     * @return true if at least one feature is a responder
     */
    public boolean hasResponderFeatures() {
        return isResponderFeature() || getConnectedFeatures().stream().anyMatch(DeviceFeature::hasResponderFeatures);
    }

    /**
     * Checks if a message is a successful response queried by this feature
     *
     * @param msg the message to check
     * @return true if my direct ack
     */
    public boolean isMyDirectAck(Msg msg) {
        return msg.isAckOfDirect() && !msg.isReplayed() && isQueryPending();
    }

    /**
     * Checks if a message is a response queried by this feature
     *
     * @param msg the message to check
     * @return true if my direct ack or nack
     */
    public boolean isMyDirectAckOrNack(Msg msg) {
        return msg.isAckOrNackOfDirect() && !msg.isReplayed() && isQueryPending();
    }

    /**
     * Checks if a message is a successful im reply queried by this feature
     *
     * @param msg the message to check
     * @return true if my im reply ack
     */
    public boolean isMyIMReplyAck(Msg msg) {
        return msg.isReplyAck() && !msg.isInsteonMessage() && !msg.isX10() && isQueryPending();
    }

    /**
     * Checks if a message is an im reply queried by this feature
     *
     * @param msg the message to check
     * @return true if my im reply
     */
    public boolean isMyIMReply(Msg msg) {
        return msg.isReply() && !msg.isInsteonMessage() && !msg.isX10() && isQueryPending();
    }

    /**
     * Checks if this feature in query pending status
     *
     * @return true if query pending status
     */
    private boolean isQueryPending() {
        return getQueryStatus() == QueryStatus.QUERY_PENDING;
    }

    /**
     * Called when message is incoming. Dispatches message according to message dispatcher
     *
     * @param msg the message to dispatch
     * @return true if dispatch successful
     */
    public boolean handleMessage(Msg msg) {
        MessageDispatcher dispatcher = this.dispatcher;
        if (dispatcher == null) {
            logger.warn("{}:{} no dispatcher for msg {}", device.getAddress(), name, msg);
            return false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} handling message using dispatcher {}", device.getAddress(), name,
                    dispatcher.getClass().getSimpleName());
        }
        return dispatcher.dispatch(msg);
    }

    /**
     * Called when a related device command triggers for this device feature
     *
     * @param config the channel config of the item which sends the command
     * @param cmd the command to be exectued
     */
    public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
        handleCommand(null, config, cmd);
    }

    /**
     * Called when an openhab command arrives for this device feature
     *
     * @param channelUID the channel uid of the item which sends the command
     * @param config the channel config of the item which sends the command
     * @param cmd the command to be exectued
     */
    public void handleCommand(@Nullable ChannelUID channelUID, InsteonChannelConfiguration config, Command cmd) {
        Class<? extends Command> cmdClass = cmd.getClass();
        CommandHandler cmdHandler = commandHandlers.getOrDefault(cmdClass, defaultCommandHandler);
        if (!cmdHandler.canHandle(cmd)) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}:{} command {}:{} cannot be handled by {}", device.getAddress(), name,
                        cmdClass.getSimpleName(), cmd, cmdHandler.getClass().getSimpleName());
            }
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} handling command {}:{} using handler {}", device.getAddress(), name,
                    cmdClass.getSimpleName(), cmd, cmdHandler.getClass().getSimpleName());
        }
        cmdHandler.handleCommand(channelUID, config, cmd);
    }

    /**
     * Make a poll message using the configured poll message handler
     *
     * @return the poll message
     */
    public @Nullable Msg makePollMsg() {
        PollHandler pollHandler = this.pollHandler;
        if (pollHandler == null) {
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} making poll msg using handler {}", device.getAddress(), name,
                    pollHandler.getClass().getSimpleName());
        }
        return pollHandler.makeMsg();
    }

    /**
     * Sends request message to device
     *
     * @param msg request message to send
     */
    public void sendRequest(Msg msg) {
        device.enqueueRequest(msg, this);
    }

    /**
     * Publishes a state to all channel handlers for this feature
     *
     * @param state the state to publish
     * @param changeType the state change type
     */
    public void publishState(State state, StateChangeType changeType) {
        State oldState = getState();
        if (oldState.equals(state) && changeType != StateChangeType.ALWAYS) {
            return;
        }
        getChannelHandlers().forEach(handler -> handler.updateState(state));
        setState(state);
    }

    /**
     * Triggers an event to all channel handlers for this feature
     *
     * @param event the event name to trigger
     */
    public void triggerEvent(String event) {
        if (!isEventFeature()) {
            logger.warn("{}:{} not configured to handle triggered event", device.getAddress(), name);
            return;
        }
        getChannelHandlers().forEach(handler -> handler.triggerEvent(event));
    }

    /**
     * Triggers a poll at this feature, group feature or device level,
     * in order of precedence depending on pollability
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void triggerPoll(long delay) {
        // trigger feature poll if pollable
        if (doPoll(delay) != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} triggered poll on this feature", device.getAddress(), name);
            }
            return;
        }
        // trigger group feature poll if defined and pollable, as fallback
        DeviceFeature groupFeature = getGroupFeature();
        if (groupFeature != null && groupFeature.doPoll(delay) != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} triggered poll on group feature {}", device.getAddress(), name,
                        groupFeature.getName());
            }
            return;
        }
        // trigger device poll limiting to responder features, as last option
        device.doPollResponders(delay);
    }

    /**
     * Executes the polling of this feature
     *
     * @param delay scheduling delay (in milliseconds)
     * @return poll message
     */
    public @Nullable Msg doPoll(long delay) {
        Msg msg = makePollMsg();
        if (msg != null) {
            device.enqueueDelayedRequest(msg, this, delay);
        }
        return msg;
    }

    /**
     * Adjusts related devices from a specific channel handler
     *
     * @param channelUID channel uid
     * @param cmd the command to adjust to
     */
    public void adjustRelatedDevices(ChannelUID channelUID, Command cmd) {
        InsteonChannelHandler handler = getChannelHandler(channelUID);
        if (handler != null) {
            handler.adjustRelatedDevices(cmd);
        }
    }

    /**
     * Polls related devices from all channel handlers
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(long delay) {
        getChannelHandlers().forEach(handler -> handler.pollRelatedDevices(delay));
    }

    /**
     * Polls related devices from a specific channel handler
     *
     * @param channelUID channel uid
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(ChannelUID channelUID, long delay) {
        InsteonChannelHandler handler = getChannelHandler(channelUID);
        if (handler != null) {
            handler.pollRelatedDevices(delay);
        }
    }

    /**
     * Updates channel config for all channel handlers
     */
    public void updateChannelConfigs() {
        getChannelHandlers().forEach(InsteonChannelHandler::updateChannelConfig);
    }

    /**
     * Adds a message handler to this device feature
     *
     * @param cmd1 The insteon cmd1 of the incoming message for which the handler should be used
     * @param handler the handler to invoke
     */
    public void addMessageHandler(int cmd1, MessageHandler handler) {
        synchronized (msgHandlers) {
            msgHandlers.put(cmd1, handler);
        }
    }

    /**
     * Adds a command handler to this device feature
     *
     * @param classRef the command class reference for which this handler is invoked
     * @param handler the handler to call
     */
    public void addCommandHandler(Class<? extends Command> classRef, CommandHandler handler) {
        synchronized (commandHandlers) {
            commandHandlers.put(classRef, handler);
        }
    }

    /**
     * Returns this device feature information as a string
     */
    @Override
    public String toString() {
        String s = name + "->" + type;
        if (!parameters.isEmpty()) {
            s += parameters;
        }
        s += "(" + channelHandlers.size() + ":" + commandHandlers.size() + ":" + msgHandlers.size() + ")";
        return s;
    }

    /**
     * Factory method for creating DeviceFeature
     *
     * @param type the device feature type
     * @return the newly created DeviceFeature, or null if requested DeviceFeature does not exist.
     */
    public static @Nullable DeviceFeature makeDeviceFeature(String type) {
        FeatureTemplate template = FeatureTemplateLoader.instance().getTemplate(type);
        if (template == null) {
            logger.warn("unknown feature type: {}", type);
            return null;
        }
        return template.build();
    }
}
