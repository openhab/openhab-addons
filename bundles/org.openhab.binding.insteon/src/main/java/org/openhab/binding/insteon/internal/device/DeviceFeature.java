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
package org.openhab.binding.insteon.internal.device;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.feature.CommandHandler;
import org.openhab.binding.insteon.internal.device.feature.FeatureListener;
import org.openhab.binding.insteon.internal.device.feature.FeatureTemplate;
import org.openhab.binding.insteon.internal.device.feature.FeatureTemplateRegistry;
import org.openhab.binding.insteon.internal.device.feature.MessageDispatcher;
import org.openhab.binding.insteon.internal.device.feature.MessageHandler;
import org.openhab.binding.insteon.internal.device.feature.PollHandler;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.ParameterParser;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
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
 * the state of a feature is updated. In practice, a InsteonChannelHandler corresponds to an
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
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class DeviceFeature {
    public static enum QueryStatus {
        NEVER_QUERIED,
        QUERY_SCHEDULED,
        QUERY_QUEUED,
        QUERY_SENT,
        QUERY_ACKED,
        QUERY_ANSWERED,
        NOT_POLLABLE
    }

    private final Logger logger = LoggerFactory.getLogger(DeviceFeature.class);

    private String name;
    private String type;
    private Device device;
    private QueryStatus queryStatus = QueryStatus.NOT_POLLABLE;
    private State state = UnDefType.NULL;
    private @Nullable Double lastMsgValue;
    private @Nullable Msg queryMsg;

    private MessageHandler defaultMsgHandler = MessageHandler.makeDefaultHandler(this);
    private CommandHandler defaultCommandHandler = CommandHandler.makeDefaultHandler(this);
    private @Nullable PollHandler pollHandler;
    private @Nullable MessageDispatcher dispatcher;
    private @Nullable DeviceFeature groupFeature;

    private Map<String, String> parameters = new HashMap<>();
    private Map<String, MessageHandler> msgHandlers = new HashMap<>();
    private Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private List<DeviceFeature> connectedFeatures = new ArrayList<>();
    private Set<FeatureListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Constructor
     *
     * @param name feature name
     * @param type feature type
     * @param device feature device
     */
    public DeviceFeature(String name, String type, Device device) {
        this.name = name;
        this.type = type;
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Device getDevice() {
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
        return Optional.ofNullable(getLastMsgValue()).map(Double::doubleValue).orElse(defaultValue);
    }

    public int getLastMsgValueAsInteger(int defaultValue) {
        return Optional.ofNullable(getLastMsgValue()).map(Double::intValue).orElse(defaultValue);
    }

    public synchronized @Nullable Msg getQueryMessage() {
        return queryMsg;
    }

    public int getQueryCommand() {
        Msg queryMsg = getQueryMessage();
        if (queryMsg != null) {
            try {
                return queryMsg.getInt("command1");
            } catch (FieldException e) {
                logger.warn("{}:{} error parsing msg {}", device.getAddress(), name, queryMsg, e);
            }
        }
        return -1;
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

    public boolean isControllerOrResponderFeature() {
        return isControllerFeature() || isResponderFeature();
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

    public int getComponentId() {
        int componentId = 0;
        if (device instanceof InsteonDevice insteonDevice) {
            // use feature group as component id if device has more than one controller or responder feature,
            // othewise use the component id of the link db first record
            if (insteonDevice.getControllerOrResponderFeatures().size() > 1) {
                componentId = getGroup();
            } else {
                componentId = insteonDevice.getLinkDB().getFirstRecordComponentId();
            }
        }
        return componentId;
    }

    public MessageHandler getDefaultMsgHandler() {
        return defaultMsgHandler;
    }

    public @Nullable MessageHandler getMsgHandler(int command, int group) {
        synchronized (msgHandlers) {
            return msgHandlers.get(MessageHandler.generateId(command, group));
        }
    }

    public MessageHandler getOrDefaultMsgHandler(int command, int group) {
        synchronized (msgHandlers) {
            return msgHandlers.getOrDefault(MessageHandler.generateId(command, group), defaultMsgHandler);
        }
    }

    public MessageHandler getOrDefaultMsgHandler(int command) {
        return getOrDefaultMsgHandler(command, -1);
    }

    public CommandHandler getOrDefaultCommandHandler(String key) {
        synchronized (commandHandlers) {
            return commandHandlers.getOrDefault(key, defaultCommandHandler);
        }
    }

    public @Nullable MessageDispatcher getMsgDispatcher() {
        return dispatcher;
    }

    public @Nullable PollHandler getPollHandler() {
        return pollHandler;
    }

    public boolean isPollable() {
        PollHandler pollHandler = getPollHandler();
        return pollHandler != null && pollHandler.makeMsg() != null;
    }

    public @Nullable DeviceFeature getGroupFeature() {
        return groupFeature;
    }

    public List<DeviceFeature> getConnectedFeatures() {
        synchronized (connectedFeatures) {
            return connectedFeatures;
        }
    }

    public boolean hasControllerFeatures() {
        return isControllerFeature() || getConnectedFeatures().stream().anyMatch(DeviceFeature::hasControllerFeatures);
    }

    public boolean hasResponderFeatures() {
        return isResponderFeature() || getConnectedFeatures().stream().anyMatch(DeviceFeature::hasResponderFeatures);
    }

    public boolean hasListeners() {
        return !listeners.isEmpty() || getConnectedFeatures().stream().anyMatch(DeviceFeature::hasListeners);
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
        logger.trace("{}:{} setting last message value to: {}", device.getAddress(), name, lastMsgValue);
        this.lastMsgValue = lastMsgValue;
    }

    public synchronized void setQueryMessage(@Nullable Msg queryMsg) {
        this.queryMsg = queryMsg;
    }

    public synchronized void setQueryStatus(QueryStatus queryStatus) {
        logger.trace("{}:{} setting query status to: {}", device.getAddress(), name, queryStatus);
        this.queryStatus = queryStatus;
    }

    public synchronized void setState(State state) {
        logger.trace("{}:{} setting state to: {}", device.getAddress(), name, state);
        this.state = state;
    }

    public void initializeQueryStatus() {
        // set query status to never queried if feature pollable,
        // otherwise to not pollable if not already in that state
        if (isPollable()) {
            setQueryStatus(QueryStatus.NEVER_QUERIED);
        } else if (queryStatus != QueryStatus.NOT_POLLABLE) {
            setQueryStatus(QueryStatus.NOT_POLLABLE);
        }
    }

    public void addParameters(Map<String, String> params) {
        synchronized (parameters) {
            parameters.putAll(params);
        }
        // reset message handler map ids if new group parameter added
        if (params.containsKey(PARAMETER_GROUP)) {
            resetMessageHandlerIds();
        }
    }

    public void addMessageHandler(String key, MessageHandler handler) {
        synchronized (msgHandlers) {
            if (msgHandlers.putIfAbsent(key, handler) != null) {
                logger.warn("{}: ignoring duplicate message handler: {}->{}", type, key, handler);
            }
        }
    }

    public void addCommandHandler(String key, CommandHandler handler) {
        synchronized (commandHandlers) {
            if (commandHandlers.putIfAbsent(key, handler) != null) {
                logger.warn("{}: ignoring duplicate command handler: {}->{}", type, key, handler);
            }
        }
    }

    private void resetMessageHandlerIds() {
        synchronized (msgHandlers) {
            if (!msgHandlers.isEmpty()) {
                Map<String, MessageHandler> handlers = msgHandlers.values().stream()
                        .collect(Collectors.toMap(MessageHandler::getId, Function.identity()));
                msgHandlers.clear();
                msgHandlers.putAll(handlers);
            }
        }
    }

    public void addConnectedFeature(DeviceFeature feature) {
        synchronized (connectedFeatures) {
            connectedFeatures.add(feature);
        }
    }

    public void registerListener(FeatureListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(FeatureListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns if a message is a successful response queried by this feature
     *
     * @param msg the message to check
     * @return true if my direct ack
     */
    public boolean isMyDirectAck(Msg msg) {
        return msg.isAckOfDirect() && !msg.isReplayed() && getQueryStatus() == QueryStatus.QUERY_ACKED;
    }

    /**
     * Returns if a message is a failed response queried by this feature
     *
     * @param msg the message to check
     * @return true if my direct nack
     */
    public boolean isMyDirectNack(Msg msg) {
        if (msg.isNackOfDirect() && !msg.isReplayed() && getQueryStatus() == QueryStatus.QUERY_ACKED) {
            if (logger.isDebugEnabled()) {
                try {
                    int cmd2 = msg.getInt("command2");
                    if (cmd2 == 0xFF) {
                        logger.debug("got a sender device id not in responder database failed command msg: {}", msg);
                    } else if (cmd2 == 0xFE) {
                        logger.debug("got a no load detected failed command msg: {}", msg);
                    } else if (cmd2 == 0xFD) {
                        logger.debug("got an incorrect checksum failed command msg: {}", msg);
                    } else if (cmd2 == 0xFC) {
                        logger.debug("got a database search timeout failed command msg: {}", msg);
                    } else if (cmd2 == 0xFB) {
                        logger.debug("got an illegal value failed command msg: {}", msg);
                    } else {
                        logger.debug("got an unknown failed command msg: {}", msg);
                    }
                } catch (FieldException e) {
                    logger.warn("{}:{} error parsing msg {}", device.getAddress(), name, msg, e);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns if a message is a response queried by this feature
     *
     * @param msg the message to check
     * @return true if my direct ack or nack
     */
    public boolean isMyDirectAckOrNack(Msg msg) {
        return isMyDirectAck(msg) || isMyDirectNack(msg);
    }

    /**
     * Returns if a message is a reply to a query sent by this feature
     *
     * @param msg the message to check
     * @return true if my reply
     */
    public boolean isMyReply(Msg msg) {
        Msg queryMsg = getQueryMessage();
        return queryMsg != null && msg.isReplyOf(queryMsg) && getQueryStatus() == QueryStatus.QUERY_SENT;
    }

    /**
     * Handles message according to message dispatcher
     *
     * @param msg the message to dispatch
     * @return true if dispatch successful
     */
    public boolean handleMessage(Msg msg) {
        MessageDispatcher dispatcher = getMsgDispatcher();
        if (dispatcher == null) {
            logger.warn("{}:{} no dispatcher for msg {}", device.getAddress(), name, msg);
            return false;
        }
        logger.trace("{}:{} handling message using dispatcher {}", device.getAddress(), name,
                dispatcher.getClass().getSimpleName());
        return dispatcher.dispatch(msg);
    }

    /**
     * Handles command for this device feature
     *
     * @param cmd the command to be executed
     */
    public void handleCommand(Command cmd) {
        handleCommand(new InsteonChannelConfiguration(), cmd);
    }

    /**
     * Handles command for this device feature
     *
     * @param config the channel config of the item which sends the command
     * @param cmd the command to be executed
     */
    public void handleCommand(InsteonChannelConfiguration config, Command cmd) {
        String cmdType = cmd.getClass().getSimpleName();
        CommandHandler cmdHandler = getOrDefaultCommandHandler(cmdType);
        if (!cmdHandler.canHandle(cmd)) {
            logger.debug("{}:{} command {}:{} cannot be handled by {}", device.getAddress(), name, cmdType, cmd,
                    cmdHandler.getClass().getSimpleName());
            return;
        }
        logger.trace("{}:{} handling command {}:{} using handler {}", device.getAddress(), name, cmdType, cmd,
                cmdHandler.getClass().getSimpleName());
        cmdHandler.handleCommand(config, cmd);
    }

    /**
     * Makes a poll message using the configured poll message handler
     *
     * @return the poll message
     */
    public @Nullable Msg makePollMsg() {
        PollHandler pollHandler = getPollHandler();
        if (pollHandler == null) {
            return null;
        }
        logger.trace("{}:{} making poll msg using handler {}", device.getAddress(), name,
                pollHandler.getClass().getSimpleName());
        return pollHandler.makeMsg();
    }

    /**
     * Sends request message to device
     *
     * @param msg request message to send
     */
    public void sendRequest(Msg msg) {
        device.sendMessage(msg, this, 0L);
    }

    /**
     * Updates the state for this feature
     *
     * @param state the state to update
     */
    public void updateState(State state) {
        setState(state);
        listeners.forEach(listener -> listener.stateUpdated(state));
    }

    /**
     * Triggers an event this feature
     *
     * @param event the event name to trigger
     */
    public void triggerEvent(String event) {
        if (!isEventFeature()) {
            logger.warn("{}:{} not configured to handle triggered event", device.getAddress(), name);
            return;
        }
        listeners.forEach(listener -> listener.eventTriggered(event));
    }

    /**
     * Triggers a poll at this feature, group feature or device level,
     * in order of precedence depending on pollability
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void triggerPoll(long delay) {
        // determine poll delay for this feature if not provided
        if (delay == -1) {
            delay = getPollDelay();
        }
        // trigger feature poll if pollable
        if (doPoll(delay) != null) {
            logger.trace("{}:{} triggered poll on this feature", device.getAddress(), name);
            return;
        }
        // trigger group feature poll if defined and pollable, as fallback
        DeviceFeature groupFeature = getGroupFeature();
        if (groupFeature != null && groupFeature.doPoll(delay) != null) {
            logger.trace("{}:{} triggered poll on group feature {}", device.getAddress(), name, groupFeature.getName());
            return;
        }
        // trigger device poll limiting to responder features, otherwise
        if (device instanceof InsteonDevice insteonDevice) {
            insteonDevice.pollResponders(delay);
        }
    }

    /**
     * Returns the poll delay for this feature
     *
     * @return the poll delay based on device ramp rate if supported and available, otherwise 0
     */
    private long getPollDelay() {
        if (RampRate.supportsFeatureType(type) && device instanceof InsteonDevice insteonDevice) {
            State state = insteonDevice.getFeatureState(FEATURE_RAMP_RATE);
            RampRate rampRate;
            if (state instanceof QuantityType<?> rampTime) {
                rampTime = Objects.requireNonNullElse(rampTime.toInvertibleUnit(Units.SECOND), rampTime);
                rampRate = RampRate.fromTime(rampTime.doubleValue());
            } else {
                rampRate = RampRate.DEFAULT;
            }
            return rampRate.getTimeInMilliseconds();
        }
        return 0L;
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
            device.sendMessage(msg, this, delay);
        }
        return msg;
    }

    /**
     * Polls related devices to this feature
     *
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(long delay) {
        if (device instanceof InsteonDevice insteonDevice) {
            insteonDevice.pollRelatedDevices(getGroup(), delay);
        }
    }

    /**
     * Polls related devices to a broadcast group
     *
     * @param group broadcast group
     * @param delay scheduling delay (in milliseconds)
     */
    public void pollRelatedDevices(int group, long delay) {
        InsteonModem modem = device instanceof InsteonModem insteonModem ? insteonModem
                : device instanceof InsteonDevice insteonDevice ? insteonDevice.getModem() : null;
        if (modem != null) {
            modem.pollRelatedDevices(group, delay);
        }
    }

    /**
     * Adjusts related devices to this feature
     *
     * @param config the channel config
     * @param cmd the command to adjust to
     */
    public void adjustRelatedDevices(InsteonChannelConfiguration config, Command cmd) {
        if (device instanceof InsteonDevice insteonDevice) {
            insteonDevice.adjustRelatedDevices(getGroup(), config, cmd);
        }
    }

    /**
     * Returns broadcast group for this feature
     *
     * @param config the channel config
     * @return the broadcast group if found, otherwise -1
     */
    public int getBroadcastGroup(InsteonChannelConfiguration config) {
        if (device instanceof InsteonDevice insteonDevice) {
            return insteonDevice.getBroadcastGroup(this);
        } else if (device instanceof InsteonModem) {
            return config.getGroup();
        }
        return -1;
    }

    @Override
    public String toString() {
        String s = name + "->" + type;
        if (!parameters.isEmpty()) {
            s += parameters;
        }
        s += "(" + commandHandlers.size() + ":" + msgHandlers.size() + ":" + listeners.size() + ")";
        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceFeature other = (DeviceFeature) obj;
        return name.equals(other.name) && type.equals(other.type) && device.equals(other.device);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + device.hashCode();
        return result;
    }

    /**
     * Factory method for creating DeviceFeature
     *
     * @param device the feature device
     * @param name the feature name
     * @param type the feature type
     * @param parameters the feature parameters
     * @return the newly created DeviceFeature, or null if requested feature type does not exist.
     */
    public static @Nullable DeviceFeature makeDeviceFeature(Device device, String name, String type,
            Map<String, String> parameters) {
        FeatureTemplate template = FeatureTemplateRegistry.getInstance().getTemplate(type);
        if (template == null) {
            return null;
        }

        DeviceFeature feature = template.build(name, device);
        feature.addParameters(parameters);
        feature.initializeQueryStatus();

        return feature;
    }
}
