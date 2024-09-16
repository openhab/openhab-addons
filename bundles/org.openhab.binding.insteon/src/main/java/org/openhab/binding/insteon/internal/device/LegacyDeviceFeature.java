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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonLegacyChannelConfiguration;
import org.openhab.binding.insteon.internal.device.feature.LegacyCommandHandler;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureListener;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureListener.StateChangeType;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureTemplate;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureTemplateLoader;
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureTemplateLoader.ParsingException;
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
 */
@NonNullByDefault
public class LegacyDeviceFeature {
    public enum QueryStatus {
        NEVER_QUERIED,
        QUERY_PENDING,
        QUERY_ANSWERED
    }

    private static final Logger logger = LoggerFactory.getLogger(LegacyDeviceFeature.class);

    private static Map<String, LegacyFeatureTemplate> features = new HashMap<>();

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

    // various simple getters
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
        return (connectedFeatures);
    }

    // various simple setters
    public void setStatusFeature(boolean f) {
        isStatus = f;
    }

    public void setPollHandler(LegacyPollHandler h) {
        pollHandler = h;
    }

    public void setDevice(LegacyDevice d) {
        device = d;
    }

    public void setMessageDispatcher(LegacyMessageDispatcher md) {
        dispatcher = md;
    }

    public void setDefaultCommandHandler(LegacyCommandHandler ch) {
        defaultCommandHandler = ch;
    }

    public void setDefaultMsgHandler(LegacyMessageHandler mh) {
        defaultMsgHandler = mh;
    }

    public synchronized void setQueryStatus(QueryStatus status) {
        logger.trace("{} set query status to: {}", name, status);
        queryStatus = status;
    }

    public void setTimeout(@Nullable String s) {
        if (s != null && !s.isEmpty()) {
            try {
                directAckTimeout = Integer.parseInt(s);
                logger.trace("ack timeout set to {}", directAckTimeout);
            } catch (NumberFormatException e) {
                logger.warn("invalid number for timeout: {}", s);
            }
        }
    }

    /**
     * Add a listener (item) to a device feature
     *
     * @param l the listener
     */
    public void addListener(LegacyFeatureListener l) {
        synchronized (listeners) {
            for (LegacyFeatureListener m : listeners) {
                if (m.getItemName().equals(l.getItemName())) {
                    return;
                }
            }
            listeners.add(l);
        }
    }

    /**
     * Adds a connected feature such that this DeviceFeature can
     * act as a feature group
     *
     * @param f the device feature related to this feature
     */
    public void addConnectedFeature(LegacyDeviceFeature f) {
        connectedFeatures.add(f);
    }

    public boolean hasListeners() {
        if (!listeners.isEmpty()) {
            return true;
        }
        for (LegacyDeviceFeature f : connectedFeatures) {
            if (f.hasListeners()) {
                return true;
            }
        }
        return false;
    }

    /**
     * removes a DeviceFeatureListener from this feature
     *
     * @param aItemName name of the item to remove as listener
     * @return true if a listener was removed
     */
    public boolean removeListener(String aItemName) {
        boolean listenerRemoved = false;
        synchronized (listeners) {
            for (Iterator<LegacyFeatureListener> it = listeners.iterator(); it.hasNext();) {
                LegacyFeatureListener fl = it.next();
                if (fl.getItemName().equals(aItemName)) {
                    it.remove();
                    listenerRemoved = true;
                }
            }
        }
        return listenerRemoved;
    }

    public boolean isReferencedByItem(String aItemName) {
        synchronized (listeners) {
            for (LegacyFeatureListener fl : listeners) {
                if (fl.getItemName().equals(aItemName)) {
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
     * @param c the binding config of the item which sends the command
     * @param cmd the command to be exectued
     */
    public void handleCommand(InsteonLegacyChannelConfiguration c, Command cmd) {
        Class<? extends Command> key = cmd.getClass();
        LegacyCommandHandler h = commandHandlers.containsKey(key) ? commandHandlers.get(key) : defaultCommandHandler;
        if (h != null) {
            logger.trace("{} uses {} to handle command {} for {}", getName(), h.getClass().getSimpleName(),
                    key.getSimpleName(), getDevice().getAddress());
            h.handleCommand(c, cmd, getDevice());
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
     * @param c the command for which this handler is invoked
     * @param handler the handler to call
     */
    public void addCommandHandler(Class<? extends Command> c, LegacyCommandHandler handler) {
        synchronized (commandHandlers) {
            commandHandlers.put(c, handler);
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
     * @param s The name of the device feature to create.
     * @return The newly created DeviceFeature, or null if requested DeviceFeature does not exist.
     */
    @Nullable
    public static LegacyDeviceFeature makeDeviceFeature(String s) {
        LegacyDeviceFeature f = null;
        synchronized (features) {
            LegacyFeatureTemplate ft = features.get(s);
            if (ft != null) {
                f = ft.build();
            } else {
                logger.warn("unimplemented feature requested: {}", s);
            }
        }
        return f;
    }

    /**
     * Reads the features templates from an input stream and puts them in global map
     *
     * @param input the input stream from which to read the feature templates
     */
    public static void readFeatureTemplates(InputStream input) {
        try {
            List<LegacyFeatureTemplate> featureTemplates = LegacyFeatureTemplateLoader.readTemplates(input);
            synchronized (features) {
                for (LegacyFeatureTemplate f : featureTemplates) {
                    features.put(f.getName(), f);
                }
            }
        } catch (IOException e) {
            logger.warn("IOException while reading device features", e);
        } catch (ParsingException e) {
            logger.warn("Parsing exception while reading device features", e);
        }
    }

    /**
     * Reads the feature templates from a file and adds them to a global map
     *
     * @param file name of the file to read from
     */
    public static void readFeatureTemplates(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            readFeatureTemplates(fis);
        } catch (FileNotFoundException e) {
            logger.warn("cannot read feature templates from file {} ", file, e);
        }
    }

    /**
     * static initializer
     */
    static {
        // read features from xml file and store them in a map
        InputStream input = LegacyDeviceFeature.class.getResourceAsStream("/legacy-device-features.xml");
        Objects.requireNonNull(input);
        readFeatureTemplates(input);
    }
}
