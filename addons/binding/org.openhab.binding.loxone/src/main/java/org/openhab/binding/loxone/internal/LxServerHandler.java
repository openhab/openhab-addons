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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.openhab.binding.loxone.internal.core.LxErrorCode;
import org.openhab.binding.loxone.internal.core.LxServerEvent;
import org.openhab.binding.loxone.internal.core.LxServerEvent.EventType;
import org.openhab.binding.loxone.internal.core.LxUuid;
import org.openhab.binding.loxone.internal.core.LxWsClient;
import org.openhab.binding.loxone.internal.core.LxWsStateUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Lock threadLock = new ReentrantLock();
    private final BlockingQueue<LxServerEvent> queue = new LinkedBlockingQueue<>();
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
            try {
                if (monitorThread == null) {
                    monitorThread = new LxServerThread();
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
        threadLock.lock();
        try {
            if (monitorThread != null) {
                LxServerEvent event = new LxServerEvent(EventType.CLIENT_CLOSING, LxErrorCode.OK, null);
                try {
                    queue.put(event);
                } catch (InterruptedException e) {
                    monitorThread.interrupt();
                }
                try {
                    monitorThread.join(5000);
                } catch (InterruptedException e) {
                    logger.warn("[{}] Waiting for thread termination interrupted.", debugId);
                }
                monitorThread = null;
            } else {
                logger.debug("[{}] Thing dispose - no thread", debugId);
            }
        } finally {
            threadLock.unlock();
        }
        socketClient = null;
        dynamicStateDescriptionProvider.removeAllDescriptions();
        controls.clear();
        channels.clear();
        states.clear();
        queue.clear();
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
    public boolean sendEvent(LxServerEvent event) {
        try {
            queue.put(event);
            return true;
        } catch (InterruptedException e) {
            logger.debug("[{}] Interrupted queue operation", debugId);
            return false;
        }
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

    /**
     * Parses received configuration, updates thing properties and creates appropriate room, category and control
     * objects. Creates channels and registers channel state descriptions in the provider.
     * This call does not purge previous configuration, but updates existing objects and removes objects which don't
     * exist anymore in the Miniserver.
     *
     * @param config json with configuration received from the Miniserver
     */
    private void updateConfig(LxConfig config) {
        logger.debug("[{}] Updating configuration from Miniserver", debugId);

        if (config.msInfo != null) {
            logger.trace("[{}] updating global config", debugId);
            Thing thing = getThing();
            thing.setProperty(MINISERVER_PROPERTY_MINISERVER_NAME, buildName(config.msInfo.msName));
            thing.setProperty(MINISERVER_PROPERTY_PROJECT_NAME, buildName(config.msInfo.projectName));
            thing.setProperty(MINISERVER_PROPERTY_CLOUD_ADDRESS, buildName(config.msInfo.remoteUrl));
            thing.setProperty(MINISERVER_PROPERTY_PHYSICAL_LOCATION, buildName(config.msInfo.location));
            thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, buildName(config.msInfo.swVersion));
            thing.setProperty(Thing.PROPERTY_SERIAL_NUMBER, buildName(config.msInfo.serialNr));
            thing.setProperty(Thing.PROPERTY_MAC_ADDRESS, buildName(config.msInfo.macAddress));
        } else {
            logger.warn("[{}] missing global configuration msInfo on Loxone", debugId);
        }

        if (config.controls != null) {
            logger.trace("[{}] creating controls in handler.", debugId);
            config.controls.values().forEach(ctrl -> addControlStructures(ctrl));
        }

        // merge control channels and update thing
        List<Channel> channels = new ArrayList<>();
        controls.values().forEach(control -> channels.addAll(control.getChannels()));
        addThingChannels(channels, true);
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
     * Thread that performs and supervises communication with the Miniserver.
     * <p>
     * It will try to maintain the connection as long as possible, handling errors and interruptions. There are two
     * reasons when this thread will terminate and stop connecting to the Miniserver:
     * when it receives close command from supervisor (thing handler) or when Miniserver locks out user due to too
     * many unsuccessful login attempts.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxServerThread extends Thread {
        private boolean running = true;
        // initial delay to initiate connection
        private int waitTime = firstConDelay;

        @Override
        public void run() {
            logger.debug("[{}] Thread starting", debugId);
            try {
                boolean connected = false;
                while (running) {
                    while (!connected) {
                        LxServerEvent wsMsg;
                        do {
                            wsMsg = queue.poll(waitTime, TimeUnit.SECONDS);
                            if (wsMsg != null) {
                                processMessage(wsMsg);
                            }
                        } while (wsMsg != null);
                        logger.debug("[{}] Server connecting to websocket", debugId);
                        connected = socketClient.connect();
                        if (!connected) {
                            waitTime = connectErrDelay;
                        }
                    }
                    while (connected) {
                        LxServerEvent wsMsg = queue.take();
                        connected = processMessage(wsMsg);
                    }
                }
            } catch (InterruptedException e) {
                logger.debug("[{}] Server thread interrupted, terminating", debugId);
            }
            logger.debug("[{}] Thread ending", debugId);
            socketClient.disconnect();
        }

        private boolean processMessage(LxServerEvent wsMsg) {
            EventType event = wsMsg.getEvent();
            logger.trace("[{}] Server received event: {}", debugId, event);
            switch (event) {
                case RECEIVED_CONFIG:
                    LxConfig config = (LxConfig) wsMsg.getObject();
                    if (config != null) {
                        updateConfig(config);
                    } else {
                        logger.debug("[{}] Server failed processing received configuration", debugId);
                    }
                    break;
                case STATE_UPDATE:
                    LxWsStateUpdateEvent update = (LxWsStateUpdateEvent) wsMsg.getObject();
                    Map<LxUuid, LxControlState> perStateUuid = states.get(update.getUuid());
                    if (perStateUuid != null) {
                        perStateUuid.forEach((controlUuid, state) -> {
                            state.setStateValue(update.getUpdateValue());
                        });
                    }
                    break;
                case SERVER_ONLINE:
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case SERVER_OFFLINE:
                    LxErrorCode reason = wsMsg.getOfflineReason();
                    String details = null;
                    if (wsMsg.getObject() instanceof String) {
                        details = (String) wsMsg.getObject();
                    }
                    logger.debug("[{}] Websocket goes OFFLINE, reason {} : {}.", debugId, reason, details);
                    processOfflineReason(reason, details);
                    return false;
                case CLIENT_CLOSING:
                    running = false;
                    return false;
                default:
                    logger.debug("[{}] Received unknown request {}", debugId, wsMsg.getEvent().name());
                    break;
            }
            return true;
        }

        private void processOfflineReason(LxErrorCode reason, String details) {
            switch (reason) {
                case TOO_MANY_FAILED_LOGIN_ATTEMPTS:
                    // assume credentials are wrong, do not re-attempt connections
                    // close thread and expect a new LxServer object will have to be re-created
                    // with corrected configuration
                    running = false;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Too many failed login attempts - stopped trying");
                    break;
                case USER_UNAUTHORIZED:
                    waitTime = userErrorDelay;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            details != null ? details : "User authentication error (invalid user name or password)");
                    break;
                case USER_AUTHENTICATION_TIMEOUT:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "User authentication timeout");
                    break;
                case COMMUNICATION_ERROR:
                    waitTime = comErrorDelay;
                    String text = "Error communicating with Miniserver";
                    if (details != null) {
                        text += " (" + details + ")";
                    }
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, text);
                    break;
                case INTERNAL_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            details != null ? "Internal error (" + details + ")" : "Internal error");
                    break;
                case WEBSOCKET_IDLE_TIMEOUT:
                    logger.warn("Idle timeout from Loxone Miniserver - adjust keepalive settings");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Timeout due to no activity");
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
        }
    }
}
