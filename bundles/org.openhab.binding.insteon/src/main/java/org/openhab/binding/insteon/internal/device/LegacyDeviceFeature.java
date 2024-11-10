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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonLegacyChannelConfiguration;
import org.openhab.binding.insteon.internal.device.feature.LegacyCommandHandler;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureListener;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureListener.StateChangeType;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureTemplate;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureTemplateLoader;
import org.openhab.binding.insteon.internal.device.feature.LegacyMessageDispatcher;
import org.openhab.binding.insteon.internal.device.feature.LegacyMessageHandler;
import org.openhab.binding.insteon.internal.device.feature.LegacyPollHandler;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
 * Lastly, DeviceFeatureListeners can register with the DeviceFeature to get notifications when
 * the state of a feature has changed. In practice, a DeviceFeatureListener corresponds to an
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
public class LegacyDeviceFeature {
    public enum QueryStatus {
        NEVER_QUERIED,
        QUERY_PENDING,
        QUERY_ANSWERED
    }

    private final Logger logger = LoggerFactory.getLogger(LegacyDeviceFeature.class);

    private LegacyDevice device = new LegacyDevice();
    private String name = "INVALID_FEATURE_NAME";
    private boolean isStatus = false;
    private int directAckTimeout = 6000;
    private QueryStatus queryStatus = QueryStatus.NEVER_QUERIED;

    private LegacyMessageHandler defaultMsgHandler = new LegacyMessageHandler.DefaultMsgHandler(this);
    private LegacyCommandHandler defaultCommandHandler = new LegacyCommandHandler.WarnCommandHandler(this);
    private @Nullable LegacyPollHandler pollHandler = null;
    private @Nullable LegacyMessageDispatcher dispatcher = null;

    private Map<Integer, LegacyMessageHandler> msgHandlers = new HashMap<>();
    private Map<Class<? extends Command>, LegacyCommandHandler> commandHandlers = new HashMap<>();
    private List<LegacyFeatureListener> listeners = new ArrayList<>();
    private List<LegacyDeviceFeature> connectedFeatures = new ArrayList<>();

    /**
     * Constructor
     *
     * @param device Insteon device to which this feature belongs
     * @param name descriptive name for that feature
     */
    public LegacyDeviceFeature(LegacyDevice device, String name) {
        this.name = name;
        setDevice(device);
    }

    /**
     * Constructor
     *
     * @param name descriptive name of the feature
     */
    public LegacyDeviceFeature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized QueryStatus getQueryStatus() {
        return queryStatus;
    }

    public LegacyDevice getDevice() {
        return device;
    }

    public boolean isFeatureGroup() {
        return !connectedFeatures.isEmpty();
    }

    public boolean isStatusFeature() {
        return isStatus;
    }

    public int getDirectAckTimeout() {
        return directAckTimeout;
    }

    public LegacyMessageHandler getDefaultMsgHandler() {
        return defaultMsgHandler;
    }

    public Map<Integer, LegacyMessageHandler> getMsgHandlers() {
        return this.msgHandlers;
    }

    public List<LegacyDeviceFeature> getConnectedFeatures() {
        return connectedFeatures;
    }

    public void setStatusFeature(boolean isStatus) {
        this.isStatus = isStatus;
    }

    public void setPollHandler(LegacyPollHandler pollHandler) {
        this.pollHandler = pollHandler;
    }

    public void setDevice(LegacyDevice device) {
        this.device = device;
    }

    public void setMessageDispatcher(LegacyMessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void setDefaultCommandHandler(LegacyCommandHandler defaultCommandHandler) {
        this.defaultCommandHandler = defaultCommandHandler;
    }

    public void setDefaultMsgHandler(LegacyMessageHandler defaultMsgHandler) {
        this.defaultMsgHandler = defaultMsgHandler;
    }

    public synchronized void setQueryStatus(QueryStatus queryStatus) {
        logger.trace("{} set query status to: {}", name, queryStatus);
        this.queryStatus = queryStatus;
    }

    public void setTimeout(@Nullable String timeout) {
        if (timeout != null && !timeout.isEmpty()) {
            try {
                directAckTimeout = Integer.parseInt(timeout);
                logger.trace("ack timeout set to {}", directAckTimeout);
            } catch (NumberFormatException e) {
                logger.warn("invalid number for timeout: {}", timeout);
            }
        }
    }

    /**
     * Add a listener (item) to a device feature
     *
     * @param listener the listener
     */
    public void addListener(LegacyFeatureListener listener) {
        synchronized (listeners) {
            for (LegacyFeatureListener l : listeners) {
                if (l.getItemName().equals(listener.getItemName())) {
                    return;
                }
            }
            listeners.add(listener);
        }
    }

    /**
     * Adds a connected feature such that this DeviceFeature can
     * act as a feature group
     *
     * @param feature the device feature related to this feature
     */
    public void addConnectedFeature(LegacyDeviceFeature feature) {
        connectedFeatures.add(feature);
    }

    public boolean hasListeners() {
        if (!listeners.isEmpty()) {
            return true;
        }
        for (LegacyDeviceFeature feature : connectedFeatures) {
            if (feature.hasListeners()) {
                return true;
            }
        }
        return false;
    }

    /**
     * removes a DeviceFeatureListener from this feature
     *
     * @param itemName name of the item to remove as listener
     * @return true if a listener was removed
     */
    public boolean removeListener(String itemName) {
        boolean listenerRemoved = false;
        synchronized (listeners) {
            for (Iterator<LegacyFeatureListener> it = listeners.iterator(); it.hasNext();) {
                LegacyFeatureListener listener = it.next();
                if (listener.getItemName().equals(itemName)) {
                    it.remove();
                    listenerRemoved = true;
                }
            }
        }
        return listenerRemoved;
    }

    public boolean isReferencedByItem(String itemName) {
        synchronized (listeners) {
            for (LegacyFeatureListener listener : listeners) {
                if (listener.getItemName().equals(itemName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called when message is incoming. Dispatches message according to message dispatcher
     *
     * @param msg The message to dispatch
     * @return true if dispatch successful
     */
    public boolean handleMessage(Msg msg) {
        LegacyMessageDispatcher dispatcher = this.dispatcher;
        if (dispatcher == null) {
            logger.warn("{} no dispatcher for msg {}", name, msg);
            return false;
        }
        return dispatcher.dispatch(msg);
    }

    /**
     * Called when an openhab command arrives for this device feature
     *
     * @param config the binding config of the item which sends the command
     * @param cmd the command to be exectued
     */
    public void handleCommand(InsteonLegacyChannelConfiguration config, Command cmd) {
        Class<? extends Command> key = cmd.getClass();
        LegacyCommandHandler handler = commandHandlers.containsKey(key) ? commandHandlers.get(key)
                : defaultCommandHandler;
        if (handler != null) {
            logger.trace("{} uses {} to handle command {} for {}", getName(), handler.getClass().getSimpleName(),
                    key.getSimpleName(), getDevice().getAddress());
            handler.handleCommand(config, cmd, getDevice());
        }
    }

    /**
     * Make a poll message using the configured poll message handler
     *
     * @return the poll message
     */
    public @Nullable Msg makePollMsg() {
        LegacyPollHandler pollHandler = this.pollHandler;
        if (pollHandler == null) {
            return null;
        }
        logger.trace("{} making poll msg for {} using handler {}", getName(), getDevice().getAddress(),
                pollHandler.getClass().getSimpleName());
        return pollHandler.makeMsg(device);
    }

    /**
     * Publish new state to all device feature listeners, but give them
     * additional dataKey and dataValue information so they can decide
     * whether to publish the data to the bus.
     *
     * @param newState state to be published
     * @param changeType what kind of changes to publish
     * @param dataKey the key on which to filter
     * @param dataValue the value that must be matched
     */
    public void publish(State newState, StateChangeType changeType, String dataKey, String dataValue) {
        logger.debug("{}:{} publishing: {}", this.getDevice().getAddress(), getName(), newState);
        synchronized (listeners) {
            for (LegacyFeatureListener listener : listeners) {
                listener.stateChanged(newState, changeType, dataKey, dataValue);
            }
        }
    }

    /**
     * Publish new state to all device feature listeners
     *
     * @param newState state to be published
     * @param changeType what kind of changes to publish
     */
    public void publish(State newState, StateChangeType changeType) {
        logger.debug("{}:{} publishing: {}", this.getDevice().getAddress(), getName(), newState);
        synchronized (listeners) {
            for (LegacyFeatureListener listener : listeners) {
                listener.stateChanged(newState, changeType);
            }
        }
    }

    /**
     * Poll all device feature listeners for related devices
     */
    public void pollRelatedDevices() {
        synchronized (listeners) {
            for (LegacyFeatureListener listener : listeners) {
                listener.pollRelatedDevices();
            }
        }
    }

    /**
     * Adds a message handler to this device feature.
     *
     * @param cm1 The insteon cmd1 of the incoming message for which the handler should be used
     * @param handler the handler to invoke
     */
    public void addMessageHandler(int cm1, LegacyMessageHandler handler) {
        synchronized (msgHandlers) {
            msgHandlers.put(cm1, handler);
        }
    }

    /**
     * Adds a command handler to this device feature
     *
     * @param command the command for which this handler is invoked
     * @param handler the handler to call
     */
    public void addCommandHandler(Class<? extends Command> command, LegacyCommandHandler handler) {
        synchronized (commandHandlers) {
            commandHandlers.put(command, handler);
        }
    }

    /**
     * Turn DeviceFeature into String
     */
    @Override
    public String toString() {
        return name + "(" + listeners.size() + ":" + commandHandlers.size() + ":" + msgHandlers.size() + ")";
    }

    /**
     * Factory method for creating DeviceFeatures.
     *
     * @param name The name of the device feature to create.
     * @return The newly created DeviceFeature, or null if requested DeviceFeature does not exist.
     */
    public static @Nullable LegacyDeviceFeature makeDeviceFeature(String name) {
        LegacyFeatureTemplate template = LegacyFeatureTemplateLoader.instance().getTemplate(name);
        return template != null ? template.build() : null;
    }
}
