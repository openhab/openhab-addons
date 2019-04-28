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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
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

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
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
import org.openhab.binding.loxone.internal.types.LxConfig;
import org.openhab.binding.loxone.internal.types.LxConfig.LxServerInfo;
import org.openhab.binding.loxone.internal.types.LxErrorCode;
import org.openhab.binding.loxone.internal.types.LxResponse;
import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxUuid;
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

    private static final String SOCKET_URL = "/ws/rfc6455";
    private static final String CMD_CFG_API = "jdev/cfg/api";

    private static final Gson GSON;

    private InetAddress host;
    private int port;

    private int firstConDelay = 1;
    private int connectErrDelay = 10;
    private int userErrorDelay = 60;
    private int comErrorDelay = 30;
    private long keepAlivePeriod = 240; // 4 minutes, server timeout is 5 minutes

    // initial delay to initiate connection
    private int reconnectDelay = firstConDelay;

    // Map of state UUID to a map of control UUID and state objects
    // State with a unique UUID can be configured in many controls and each control can even have a different name of
    // the state. It must be ensured that updates received for this state UUID are passed to all controls that have this
    // state UUID configured.
    private Map<LxUuid, Map<LxUuid, LxState>> states = new HashMap<>();

    private LxWebSocket socket;
    private WebSocketClient wsClient;

    private int debugId = 0;
    private Thread monitorThread;
    private final Lock threadLock = new ReentrantLock();
    private final Condition connectDelay = threadLock.newCondition();
    private final Condition sessionActive = threadLock.newCondition();

    // Data structures
    private final Map<LxUuid, LxControl> controls = new HashMap<>();
    private final Map<ChannelUID, LxControl> channels = new HashMap<>();

    private LxDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final Logger logger = LoggerFactory.getLogger(LxServerHandler.class);
    private static AtomicInteger staticDebugId = new AtomicInteger(1);

    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(LxBindingConstants.THING_TYPE_MINISERVER);

    private QueuedThreadPool jettyThreadPool;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LxUuid.class, LxUuid.DESERIALIZER);
        builder.registerTypeAdapter(LxControl.class, LxControl.DESERIALIZER);
        GSON = builder.create();
    }

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
            setOffline(LxErrorCode.COMMUNICATION_ERROR, e.getMessage());
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
        try {
            this.host = InetAddress.getByName(cfg.host);
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
            return;
        }
        this.port = cfg.port;
        if (cfg.keepAlivePeriod > 0 && cfg.keepAlivePeriod != keepAlivePeriod) {
            logger.debug("[{}] Changing keepAlivePeriod to {}", debugId, cfg.keepAlivePeriod);
            keepAlivePeriod = cfg.keepAlivePeriod;
        }
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

        jettyThreadPool = new QueuedThreadPool();
        jettyThreadPool.setName(LxServerHandler.class.getSimpleName() + "-" + debugId);
        jettyThreadPool.setDaemon(true);

        socket = new LxWebSocket(debugId, this, cfg, host);
        wsClient = new WebSocketClient();
        wsClient.setExecutor(jettyThreadPool);

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
    }

    /*
     * Public methods that are called by {@link LxControl} child classes
     */

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.loxone.internal.LxServerHandlerApi#sendAction(org.openhab.binding.loxone.internal.types.
     * LxUuid, java.lang.String)
     */
    @Override
    public void sendAction(LxUuid id, String operation) throws IOException {
        socket.sendAction(id, operation);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.loxone.internal.LxServerHandlerApi#addControl(org.openhab.binding.loxone.internal.controls.
     * LxControl)
     */
    @Override
    public void addControl(LxControl control) {
        addControlStructures(control);
        addThingChannels(control.getChannelsWithSubcontrols(), false);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.loxone.internal.LxServerHandlerApi#removeControl(org.openhab.binding.loxone.internal.controls
     * .LxControl)
     */
    @Override
    public void removeControl(LxControl control) {
        logger.debug("[{}] Removing control: {}", debugId, control.getName());
        control.getSubControls().values().forEach(subControl -> removeControl(subControl));
        LxUuid controlUuid = control.getUuid();
        control.getStates().values().forEach(state -> {
            LxUuid stateUuid = state.getUuid();
            Map<LxUuid, LxState> perUuid = states.get(stateUuid);
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
            dynamicStateDescriptionProvider.removeDescription(id);
            channels.remove(id);
        });
        updateThing(builder.build());
        controls.remove(controlUuid);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.loxone.internal.LxServerHandlerApi#setChannelState(org.eclipse.smarthome.core.thing.
     * ChannelUID, org.eclipse.smarthome.core.types.State)
     */
    @Override
    public void setChannelState(ChannelUID channelId, State state) {
        updateState(channelId, state);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.loxone.internal.LxServerHandlerApi#setChannelStateDescription(org.eclipse.smarthome.core.
     * thing.ChannelUID, org.eclipse.smarthome.core.types.StateDescription)
     */
    @Override
    public void setChannelStateDescription(ChannelUID channelId, StateDescription description) {
        logger.debug("[{}] State description update for channel {}", debugId, channelId);
        dynamicStateDescriptionProvider.setDescription(channelId, description);
    }

    /*
     * Public methods called by {@link LxWsSecurity} child classes.
     */

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.loxone.internal.LxServerHandlerApi#getSetting(java.lang.String)
     */
    @Override
    public String getSetting(String name) {
        Object value = getConfig().get(name);
        return (value instanceof String) ? (String) value : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.loxone.internal.LxServerHandlerApi#setSettings(java.util.Map)
     */
    @Override
    public void setSettings(Map<String, String> properties) {
        Configuration config = getConfig();
        properties.forEach((name, value) -> config.put(name, value));
        updateConfiguration(config);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.loxone.internal.LxServerHandlerApi#getGson()
     */
    @Override
    public Gson getGson() {
        return GSON;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.loxone.internal.LxServerHandlerApi#getThingId()
     */
    @Override
    public ThingUID getThingId() {
        return getThing().getUID();
    }

    /*
     * Methods called by {@link LxWebSocket} class.
     */

    /**
     * Dispose of all objects created from the Miniserver configuration.
     */
    void clearConfiguration() {
        controls.clear();
        channels.clear();
        states.clear();
        dynamicStateDescriptionProvider.removeAllDescriptions();
    }

    /**
     * Sets a new configuration received from the Miniserver and creates all required channels.
     *
     * @param config Miniserver's configuration
     */
    void setMiniserverConfig(LxConfig config) {
        logger.debug("[{}] Setting configuration from Miniserver", debugId);

        if (config.msInfo == null) {
            logger.warn("[{}] missing global configuration msInfo on Loxone", debugId);
            config.msInfo = config.new LxServerInfo();
        }
        Thing thing = getThing();
        LxServerInfo info = config.msInfo;
        thing.setProperty(LxBindingConstants.MINISERVER_PROPERTY_MINISERVER_NAME, buildName(info.msName));
        thing.setProperty(LxBindingConstants.MINISERVER_PROPERTY_PROJECT_NAME, buildName(info.projectName));
        thing.setProperty(LxBindingConstants.MINISERVER_PROPERTY_CLOUD_ADDRESS, buildName(info.remoteUrl));
        thing.setProperty(LxBindingConstants.MINISERVER_PROPERTY_PHYSICAL_LOCATION, buildName(info.location));
        thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, buildName(info.swVersion));
        thing.setProperty(Thing.PROPERTY_SERIAL_NUMBER, buildName(info.serialNr));
        thing.setProperty(Thing.PROPERTY_MAC_ADDRESS, buildName(info.macAddress));

        List<Channel> list = new ArrayList<>();
        if (config.controls != null) {
            logger.trace("[{}] creating control structures.", debugId);
            config.controls.values().forEach(ctrl -> {
                addControlStructures(ctrl);
                list.addAll(ctrl.getChannelsWithSubcontrols());
            });
        } else {
            logger.warn("[{}] no controls received in Miniserver configuration.", debugId);
        }
        addThingChannels(list, true);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update to the new value of a state received from Miniserver. This method will go through all instances of this
     * state UUID and update their value, which will trigger corresponding control state update method in each control
     * that has this state.
     *
     * @param uuid  Miniserver's state UUID
     * @param value a new value for this state
     */
    void updateStateValue(LxUuid uuid, Object value) {
        Map<LxUuid, LxState> perStateUuid = states.get(uuid);
        if (perStateUuid != null) {
            perStateUuid.forEach((controlUuid, state) -> {
                state.setStateValue(value);
            });
        }
    }

    /**
     * Set thing status to offline and start attempts to establish a new connection to the Miniserver after a delay
     * depending of the reason for going offline.
     *
     * @param code   error code
     * @param reason reason for going offline
     */
    void setOffline(LxErrorCode code, String reason) {
        switch (code) {
            case TOO_MANY_FAILED_LOGIN_ATTEMPTS:
                // assume credentials are wrong, do not re-attempt connections any time soon
                // expect a new instance will have to be initialized with corrected configuration
                setReconnectDelay(60 * 60 * 24 * 7);
                updateStatusToOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Too many failed login attempts - stopped trying");
                break;
            case USER_UNAUTHORIZED:
                setReconnectDelay(userErrorDelay);
                updateStatusToOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        reason != null ? reason : "User authentication error (invalid user name or password)");
                break;
            case USER_AUTHENTICATION_TIMEOUT:
                updateStatusToOffline(ThingStatusDetail.COMMUNICATION_ERROR, "User authentication timeout");
                break;
            case COMMUNICATION_ERROR:
                setReconnectDelay(comErrorDelay);
                String text = "Error communicating with Miniserver";
                if (reason != null) {
                    text += " (" + reason + ")";
                }
                updateStatusToOffline(ThingStatusDetail.COMMUNICATION_ERROR, text);
                break;
            case INTERNAL_ERROR:
                updateStatusToOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        reason != null ? "Internal error (" + reason + ")" : "Internal error");
                break;
            case WEBSOCKET_IDLE_TIMEOUT:
                logger.warn("Idle timeout from Loxone Miniserver - adjust keepalive settings");
                updateStatusToOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Timeout due to no activity");
                break;
            case ERROR_CODE_MISSING:
                logger.warn("No error code available from the Miniserver");
                updateStatusToOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Unknown reason - error code missing");
                break;
            default:
                updateStatusToOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Unknown reason");
                break;
        }
        threadLock.lock();
        try {
            sessionActive.signalAll();
        } finally {
            threadLock.unlock();
        }

    }

    /**
     * Add a new control, its states, subcontrols and channels to the handler structures.
     * Handler maintains maps of all controls (main controls + subcontrols), all channels for all controls and all
     * states to match received openHAB commands and state updates from the Miniserver. States also contain links to
     * possibly multiple control objects, as many controls can share the same state with the same state uuid.
     * To create channels, {@link LxServerHandler#addThingChannels} method should be called separately. This allows
     * creation of all channels for all controls with a single thing update.
     *
     * @param control a created control object to be added
     */
    private void addControlStructures(LxControl control) {
        LxUuid uuid = control.getUuid();
        logger.debug("[{}] Adding control to handler: {}, {}", debugId, uuid, control.getName());
        control.getStates().values().forEach(state -> {
            Map<LxUuid, LxState> perUuid = states.get(state.getUuid());
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
     * Adds channels to the thing, to make them available to the framework and user.
     * This method will sort the channels according to their label.
     * It is expected that input list contains no duplicate channel IDs.
     *
     * @param newChannels a list of channels to add to the thing
     * @param purge       if true, old channels will be removed, otherwise merged
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
     * Connect the websocket.
     * Attempts to connect to the websocket on a remote Miniserver. If a connection is established, a
     * {@link LxWebSocket#onConnect} method will be called from a parallel websocket thread.
     *
     * @return true if connection request initiated correctly, false if not
     */
    private boolean connect() {
        logger.debug("[{}] connect() websocket", debugId);
        /*
         * Try to read CfgApi structure from the miniserver. It contains serial number and firmware version. If it can't
         * be read this is not a fatal issue, we will assume most recent version running.
         */
        String message = socket.httpGet(CMD_CFG_API);
        if (message != null) {
            LxResponse resp = socket.getResponse(message);
            if (resp != null) {
                socket.setFwVersion(GSON.fromJson(resp.getValueAsString(), LxResponse.LxResponseCfgApi.class).version);
            }
        } else {
            logger.debug("[{}] Http get failed for API config request.", debugId);
        }

        try {
            wsClient.start();
            // Following the PR github.com/eclipse/smarthome/pull/6636
            // without this zero timeout, jetty will wait 30 seconds for stopping the client to eventually fail
            // with the timeout it is immediate and all threads end correctly
            jettyThreadPool.setStopTimeout(0);
            URI target = new URI("ws://" + host.getHostAddress() + ":" + port + SOCKET_URL);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("remotecontrol");
            socket.startResponseTimeout();
            logger.debug("[{}] Connecting to server : {} ", debugId, target);
            wsClient.connect(socket, target, request);
            return true;
        } catch (Exception e) {
            logger.debug("[{}] Error starting websocket client: {}", debugId, e.getMessage());
            try {
                wsClient.stop();
            } catch (Exception e2) {
                logger.debug("[{}] Error stopping websocket client: {}", debugId, e2.getMessage());
            }
            return false;
        }
    }

    /*
     * Private methods
     */

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
     * Disconnect websocket session - initiated from this end.
     *
     * @param code   error code for disconnecting the websocket
     * @param reason reason for disconnecting the websocket
     */
    private void disconnect(LxErrorCode code, String reason) {
        logger.debug("[{}] disconnect the websocket: {}, {}", debugId, code, reason);
        socket.disconnect(code, reason);
        try {
            logger.debug("[{}] client stop", debugId);
            wsClient.stop();
            logger.debug("[{}] client stopped", debugId);
        } catch (Exception e) {
            logger.debug("[{}] Exception disconnecting the websocket: ", e.getMessage());
        }
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
                    if (!connect()) {
                        updateStatusToOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                "Failed to connect to Miniserver's WebSocket");
                        reconnectDelay = connectErrDelay;
                    } else {
                        try {
                            logger.debug("[{}] Sleeping for {} seconds.", debugId, keepAlivePeriod);
                            while (!sessionActive.await(keepAlivePeriod, TimeUnit.SECONDS)) {
                                socket.sendKeepAlive();
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                logger.debug("[{}] Stopping reconnect attempts permanently", debugId);
                disconnect(LxErrorCode.OK, "Thing is going down.");
            } finally {
                threadLock.unlock();
            }
            logger.debug("[{}] Thread ending", debugId);
        }
    }

    /**
     * Updates the thing status to offline, if it is not already offline. This will preserve he first reason of going
     * offline in case there were multiple reasons.
     *
     * @param code   error code
     * @param reason reason for going offline
     */
    private void updateStatusToOffline(ThingStatusDetail code, String reason) {
        ThingStatus status = getThing().getStatus();
        if (status == ThingStatus.OFFLINE) {
            logger.debug("[{}] received offline request with code {}, but thing already offline.", debugId, code);
        } else {
            updateStatus(ThingStatus.OFFLINE, code, reason);
        }
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

}
