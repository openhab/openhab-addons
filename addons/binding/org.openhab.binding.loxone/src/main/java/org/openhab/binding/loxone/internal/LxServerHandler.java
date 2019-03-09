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
package org.openhab.binding.loxone.internal;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.controls.LxControl;
import org.openhab.binding.loxone.internal.controls.LxControlState;
import org.openhab.binding.loxone.internal.core.LxConfig;
import org.openhab.binding.loxone.internal.core.LxConfig.LxServerInfo;
import org.openhab.binding.loxone.internal.core.LxErrorCode;
import org.openhab.binding.loxone.internal.core.LxUuid;
import org.openhab.binding.loxone.internal.core.LxWsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Representation of a Loxone Miniserver. It is an openHAB {@link Thing}, which is used to communicate with
 * objects (controls) configured in the Miniserver over {@link Channels}.
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class LxServerHandler extends BaseThingHandler implements LxServerHandlerApi {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MINISERVER);
    private final Logger logger = LoggerFactory.getLogger(LxServerHandler.class);

    private LxDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private LxWsClient socketClient;

    private int firstConDelay = 1;
    private int connectErrDelay = 10;
    private int userErrorDelay = 60;
    private int comErrorDelay = 30;

    // initial delay to initiate connection
    private int reconnectDelay = firstConDelay;

    // Data structures
    private final Map<LxUuid, LxControl> controls = new HashMap<>();
    private final Map<ChannelUID, LxControl> channels = new HashMap<>();

    // Map of state UUID to a map of control UUID and state objects
    // State with a unique UUID can be configured in many controls and each control can even have a different name of
    // the state. It must be ensured that updates received for this state UUID are passed to all controls that have this
    // state UUID configured.
    private Map<LxUuid, Map<LxUuid, LxControlState>> states = new HashMap<>();

    // Services
    private int debugId = 0;
    private Thread monitorThread;
    private final Gson gson;
    private final Lock threadLock = new ReentrantLock();
    private final Condition connectDelay = threadLock.newCondition();
    private final Condition sessionActive = threadLock.newCondition();
    private static AtomicInteger staticDebugId = new AtomicInteger(1);

    /**
     * Create {@link LxServerHandler} object
     *
     * @param thing    Thing object that creates the handler
     * @param provider state description provider service
     */
    public LxServerHandler(Thing thing, LxDynamicStateDescriptionProvider provider) {
        super(thing);
        if (provider != null) {
            dynamicStateDescriptionProvider = provider;
        } else {
            logger.warn("Dynamic state description provider is null");
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LxUuid.class, LxUuid.DESERIALIZER);
        builder.registerTypeAdapter(LxControl.class, LxControl.DESERIALIZER);
        gson = builder.create();
    }

    /*
     * Methods from BaseThingHandler
     */

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannelState(channelUID);
            return;
        }
        try {
            LxControl control = channels.get(channelUID);
            if (control != null) {
                control.handleCommand(channelUID, command);
            } else {
                logger.error("[{}] Received command {} for unknown control.", debugId, command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("[{}] Channel linked: {}", debugId, channelUID.getAsString());
        updateChannelState(channelUID);
    }

    @Override
    public void initialize() {
        debugId = staticDebugId.getAndIncrement();
        logger.trace("[{}] Initializing thing instance", debugId);
        LxBindingConfiguration cfg = getConfig().as(LxBindingConfiguration.class);
        if (cfg.firstConDelay >= 0 && firstConDelay != cfg.firstConDelay) {
            logger.debug("[{}] Changing firstConDelay to {}", debugId, cfg.firstConDelay);
            firstConDelay = cfg.firstConDelay;
        }
        if (cfg.connectErrDelay >= 0 && connectErrDelay != cfg.connectErrDelay) {
            logger.debug("[{}] Changing connectErrDelay to {}", debugId, cfg.connectErrDelay);
            connectErrDelay = cfg.connectErrDelay;
        }
        if (cfg.userErrorDelay >= 0 && userErrorDelay != cfg.userErrorDelay) {
            logger.debug("[{}] Changing userErrorDelay to {}", debugId, cfg.userErrorDelay);
            userErrorDelay = cfg.userErrorDelay;
        }
        if (cfg.comErrorDelay >= 0 && comErrorDelay != cfg.comErrorDelay) {
            logger.debug("[{}] Changing comErrorDelay to {}", debugId, cfg.comErrorDelay);
            comErrorDelay = cfg.comErrorDelay;
        }
        try {
            socketClient = new LxWsClient(debugId, this, cfg);
            threadLock.lock();
            if (debugId > 1) {
                reconnectDelay = 0;
            }
            try {
                if (monitorThread == null) {
                    monitorThread = new LxServerThread(debugId);
                    monitorThread.start();
                }
            } finally {
                threadLock.unlock();
            }
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
        }
    }

    @Override
    public void dispose() {
        logger.debug("[{}] Disposing of thing", debugId);
        Thread thread;
        threadLock.lock();
        try {
            thread = monitorThread;
            if (monitorThread != null) {
                monitorThread.interrupt();
                monitorThread = null;
            }
            clearConfiguration();
        } finally {
            threadLock.unlock();
        }
        if (thread != null) {
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                logger.warn("[{}] Waiting for thread termination interrupted.", debugId);
            }
        }
        logger.debug("[{}] Disposing of thing ended", debugId);
    }

    /*
     * Methods from LoxoneMiniserverHandlerApi
     */

    @Override
    public void setChannelState(ChannelUID channelId, State state) {
        updateState(channelId, state);
    }

    @Override
    public void setChannelStateDescription(ChannelUID channelId, StateDescription description) {
        logger.debug("[{}] State description update for channel {}", debugId, channelId);
        dynamicStateDescriptionProvider.setDescription(channelId, description);
    }

    @Override
    public void addControl(LxControl control) {
        addControlStructures(control);
        addThingChannels(control.getChannels(), false);
    }

    @Override
    public void removeControl(LxControl control) {
        logger.debug("[{}] Removing control: {}", debugId, control.getName());
        control.getSubControls().values().forEach(subControl -> removeControl(subControl));
        LxUuid controlUuid = control.getUuid();
        control.getStates().values().forEach(state -> {
            LxUuid stateUuid = state.getUuid();
            Map<LxUuid, LxControlState> perUuid = states.get(stateUuid);
            if (perUuid != null) {
                perUuid.remove(controlUuid);
                if (perUuid.isEmpty()) {
                    states.remove(stateUuid);
                }
            }
        });

        ThingBuilder builder = editThing();
        control.getChannels().forEach(channel -> {
            ChannelUID id = channel.getUID();
            builder.withoutChannel(id);
            channels.remove(id);
            dynamicStateDescriptionProvider.removeDescription(id);
        });
        updateThing(builder.build());
        controls.remove(controlUuid);
    }

    @Override
    public void sendAction(LxUuid id, String operation) throws IOException {
        socketClient.sendAction(id, operation);
    }

    @Override
    public ThingUID getThingId() {
        return getThing().getUID();
    }

    @Override
    public String getSetting(String name) {
        Object value = getConfig().get(name);
        return (value instanceof String) ? (String) value : null;
    }

    @Override
    public void setSettings(Map<String, String> properties) {
        Configuration config = getConfig();
        properties.forEach((name, value) -> config.put(name, value));
        updateConfiguration(config);
    }

    @Override
    public void setMiniserverConfig(String message, LxWsClient.LxResponseCfgApi cfgApi) {
        logger.debug("[{}] Setting configuration from Miniserver", debugId);
        clearConfiguration();

        LxConfig config = gson.fromJson(message, LxConfig.class);
        if (config.msInfo == null) {
            logger.warn("[{}] missing global configuration msInfo on Loxone", debugId);
            config.msInfo = config.new LxServerInfo();
        }

        LxServerInfo info = config.msInfo;
        if (cfgApi != null) {
            // documentation claims values received in CFG API structure are more important than those from the config
            // JSON and should be used to determine the right software version running on Miniserver
            if (cfgApi.version != null) {
                info.swVersion = cfgApi.version;
            }
            if (cfgApi.snr != null) {
                info.macAddress = cfgApi.snr;
            }
        }
        config.finalize(this);

        logger.trace("[{}] setting global config", debugId);
        Thing thing = getThing();
        thing.setProperty(MINISERVER_PROPERTY_MINISERVER_NAME, buildName(info.msName));
        thing.setProperty(MINISERVER_PROPERTY_PROJECT_NAME, buildName(info.projectName));
        thing.setProperty(MINISERVER_PROPERTY_CLOUD_ADDRESS, buildName(info.remoteUrl));
        thing.setProperty(MINISERVER_PROPERTY_PHYSICAL_LOCATION, buildName(info.location));
        thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, buildName(info.swVersion));
        thing.setProperty(Thing.PROPERTY_SERIAL_NUMBER, buildName(info.serialNr));
        thing.setProperty(Thing.PROPERTY_MAC_ADDRESS, buildName(info.macAddress));

        if (config.controls != null) {
            logger.trace("[{}] creating controls in handler.", debugId);
            config.controls.values().forEach(ctrl -> addControlStructures(ctrl));
        } else {
            logger.warn("[{}] no controls received in Miniserver configuration.", debugId);
        }

        // merge control channels and update thing
        List<Channel> channels = new ArrayList<>();
        controls.values().forEach(control -> channels.addAll(control.getChannels()));
        addThingChannels(channels, true);
    }

    @Override
    public void setOffline(LxErrorCode code, String reason) {
        ThingStatus status = getThing().getStatus();
        if (status == ThingStatus.OFFLINE) {
            logger.debug("[{}] received offline request with code {}, but thing already offline.", debugId, code);
            return;
        }
        switch (code) {
            case TOO_MANY_FAILED_LOGIN_ATTEMPTS:
                // assume credentials are wrong, do not re-attempt connections any time soon
                // expect a new instance will have to be initialized with corrected configuration
                setReconnectDelay(60 * 60 * 24 * 7);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Too many failed login attempts - stopped trying");
                break;
            case USER_UNAUTHORIZED:
                setReconnectDelay(userErrorDelay);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        reason != null ? reason : "User authentication error (invalid user name or password)");
                break;
            case USER_AUTHENTICATION_TIMEOUT:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "User authentication timeout");
                break;
            case COMMUNICATION_ERROR:
                setReconnectDelay(comErrorDelay);
                String text = "Error communicating with Miniserver";
                if (reason != null) {
                    text += " (" + reason + ")";
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, text);
                break;
            case INTERNAL_ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        reason != null ? "Internal error (" + reason + ")" : "Internal error");
                break;
            case WEBSOCKET_IDLE_TIMEOUT:
                logger.warn("Idle timeout from Loxone Miniserver - adjust keepalive settings");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Timeout due to no activity");
                break;
            case ERROR_CODE_MISSING:
                logger.warn("No error code available from the Miniserver");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unknown reason - error code missing");
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown reason");
                break;
        }
        threadLock.lock();
        try {
            sessionActive.signalAll();
        } finally {
            threadLock.unlock();
        }
    }

    @Override
    public void setOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void updateStateValue(LxUuid uuid, Object value) {
        Map<LxUuid, LxControlState> perStateUuid = states.get(uuid);
        if (perStateUuid != null) {
            perStateUuid.forEach((controlUuid, state) -> {
                state.setStateValue(value);
            });
        }
    }

    /**
     * Sets value for the delay before websocket connect attempt
     *
     * @param delay number of seconds to wait
     */
    private void setReconnectDelay(int delay) {
        threadLock.lock();
        try {
            reconnectDelay = delay;
        } finally {
            threadLock.unlock();
        }
    }

    /**
     * Remove all channel-related configuration.
     */
    private void clearConfiguration() {
        controls.clear();
        channels.clear();
        states.clear();
        dynamicStateDescriptionProvider.removeAllDescriptions();
    }

    /**
     * Adds channels to the thing, to make them available to the framework and user.
     * This method will sort the channels according to their label.
     * It is expected that input list contains no duplicate channel IDs.
     *
     * @param newChannels
     *                        a list of channels to add to the thing
     * @param purge
     *                        if true, old channels will be removed, otherwise merged
     */
    private void addThingChannels(List<Channel> newChannels, boolean purge) {
        List<Channel> channels = newChannels;
        if (!purge) {
            channels.addAll(getThing().getChannels());
        }
        channels.sort((c1, c2) -> {
            String label = c1.getLabel();
            return label == null ? 1 : label.compareTo(c2.getLabel());
        });
        ThingBuilder builder = editThing();
        builder.withChannels(channels);
        updateThing(builder.build());
    }

    /**
     * Add a new control, its states, subcontrols and channels to the handler structures.
     * Handler maintains maps of all controls (main controls + subcontrols), all channels for all controls and all
     * states to match received openHAB commands and state updates from the Miniserver. States also contain links to
     * possibly multiple control objects, as many controls can share the same state with the same state uuid.
     *
     * @param control a created control object to be added
     */
    private void addControlStructures(LxControl control) {
        LxUuid uuid = control.getUuid();
        logger.debug("[{}] Adding control to handler: {}, {}", debugId, uuid, control.getName());
        control.getStates().values().forEach(state -> {
            Map<LxUuid, LxControlState> perUuid = states.get(state.getUuid());
            if (perUuid == null) {
                perUuid = new HashMap<>();
                states.put(state.getUuid(), perUuid);
            }
            perUuid.put(uuid, state);
        });
        controls.put(control.getUuid(), control);
        control.getChannels().forEach(channel -> channels.put(channel.getUID(), control));
        control.getSubControls().values().forEach(subControl -> addControlStructures(subControl));
    }

    /**
     * Updates an actual state of a channel.
     * Determines control for the channel and retrieves the state from the control.
     *
     * @param channelId channel ID to update its state
     */
    private void updateChannelState(ChannelUID channelId) {
        LxControl control = channels.get(channelId);
        if (control != null) {
            State state = control.getChannelState(channelId);
            if (state != null) {
                updateState(channelId, state);
            }
        } else {
            logger.error("[{}] Received state update request for unknown control (channelId={}).", debugId, channelId);
        }
    }

    /**
     * Check and convert null string to empty string.
     *
     * @param name string to check
     * @return string guaranteed to be not null
     */
    private String buildName(String name) {
        if (name == null) {
            return "";
        }
        return name;
    }

    /**
     * Thread that maintains connection to the Miniserver.
     * It will periodically attempt to connect and if failed, wait a configured amount of time.
     * If connection succeeds, it will sleep until the session is terminated. Then it will wait and try to reconnect
     * again.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxServerThread extends Thread {
        private int debugId = 0;

        LxServerThread(int id) {
            debugId = id;
        }

        @Override
        public void run() {
            logger.debug("[{}] Thread starting", debugId);
            threadLock.lock();
            try {
                while (!isInterrupted()) {
                    try {
                        if (reconnectDelay > 0) {
                            logger.debug("[{}] Delaying connect request by {} seconds.", debugId, reconnectDelay);
                            connectDelay.await(reconnectDelay, TimeUnit.SECONDS);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                    logger.debug("[{}] Server connecting to websocket", debugId);
                    if (!socketClient.connect()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Failed to connect to Miniserver's WebSocket");
                        reconnectDelay = connectErrDelay;
                    } else {
                        logger.debug("[{}] Sleeping indefinitely waiting for need to reconnect.", debugId);
                        try {
                            sessionActive.await();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                logger.debug("[{}] Stopping reconnect attempts permanently", debugId);
                socketClient.disconnect(LxErrorCode.OK, "Thing handler going down.");
                socketClient = null;
            } finally {
                threadLock.unlock();
            }
            logger.debug("[{}] Thread ending", debugId);
        }
    }
}
