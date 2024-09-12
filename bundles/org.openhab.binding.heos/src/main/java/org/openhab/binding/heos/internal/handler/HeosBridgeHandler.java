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
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.handler.FutureUtil.cancel;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
import org.openhab.binding.heos.internal.HeosChannelManager;
import org.openhab.binding.heos.internal.action.HeosActions;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.configuration.BridgeConfiguration;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscoveryListener;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.dto.HeosError;
import org.openhab.binding.heos.internal.json.dto.HeosEvent;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosMediaEventListener;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johannes Einig - Initial contribution
 * @author Martin van Wingerden - change handling of stop/pause depending on playing item type
 */
@NonNullByDefault
public class HeosBridgeHandler extends BaseBridgeHandler implements HeosEventListener {
    private final Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

    private static final int HEOS_PORT = 1255;

    private final Set<HeosMediaEventListener> heosMediaEventListeners = new CopyOnWriteArraySet<>();
    private final List<HeosPlayerDiscoveryListener> playerDiscoveryList = new CopyOnWriteArrayList<>();
    private final HeosChannelManager channelManager = new HeosChannelManager(this);
    private final HeosChannelHandlerFactory channelHandlerFactory;

    private final Map<String, HeosGroupHandler> groupHandlerMap = new ConcurrentHashMap<>();
    private final Map<String, String> hashToGidMap = new ConcurrentHashMap<>();

    private List<String[]> selectedPlayerList = new CopyOnWriteArrayList<>();

    private @Nullable Future<?> startupFuture;
    private final List<Future<?>> childHandlerInitializedFutures = new CopyOnWriteArrayList<>();

    private final HeosSystem heosSystem;
    private @Nullable HeosFacade apiConnection;

    private boolean loggedIn = false;
    private boolean bridgeHandlerDisposalOngoing = false;

    private @NonNullByDefault({}) BridgeConfiguration configuration;

    private int failureCount;

    public HeosBridgeHandler(Bridge bridge, HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        super(bridge);
        heosSystem = new HeosSystem(scheduler);
        channelHandlerFactory = new HeosChannelHandlerFactory(this, heosDynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        @Nullable
        Channel channel = this.getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("No valid channel found");
            return;
        }

        @Nullable
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        @Nullable
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID, this, channelTypeUID);
        if (channelHandler != null) {
            try {
                channelHandler.handleBridgeCommand(command, thing.getUID());
                failureCount = 0;
                updateStatus(ONLINE);
            } catch (IOException | ReadException e) {
                logger.debug("Failed to handle bridge command", e);
                failureCount++;

                if (failureCount > FAILURE_COUNT_LIMIT) {
                    updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Failed to handle command: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public synchronized void initialize() {
        configuration = thing.getConfiguration().as(BridgeConfiguration.class);
        cancel(startupFuture);
        startupFuture = scheduler.submit(this::delayedInitialize);
    }

    private void delayedInitialize() {
        @Nullable
        HeosFacade connection = null;
        try {
            logger.debug("Running scheduledStartUp job");

            connection = connectBridge();
            updateStatus(ThingStatus.ONLINE);
            updateState(CH_ID_REBOOT, OnOffType.OFF);

            logger.debug("HEOS System heart beat started. Pulse time is {}s", configuration.heartbeat);
            // gets all available player and groups to ensure that the system knows
            // about the conjunction between the groupMemberHash and the GID
            triggerPlayerDiscovery();
            @Nullable
            String username = configuration.username;
            @Nullable
            String password = configuration.password;
            if (username != null && !"".equals(username) && password != null && !"".equals(password)) {
                login(connection, username, password);
            } else {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Can't log in. Username or password not set.");
            }

            fetchPlayersAndGroups();
        } catch (Telnet.ReadException | IOException | RuntimeException e) {
            logger.debug("Error occurred while connecting", e);
            if (connection != null) {
                connection.closeConnection();
            }
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Errors occurred: " + e.getMessage());
            cancel(startupFuture, false);
            startupFuture = scheduler.schedule(this::delayedInitialize, 30, TimeUnit.SECONDS);
        }
    }

    private void fetchPlayersAndGroups() {
        try {
            @Nullable
            Player[] onlinePlayers = getApiConnection().getPlayers().payload;
            @Nullable
            Group[] onlineGroups = getApiConnection().getGroups().payload;

            if (onlinePlayers != null && onlineGroups != null) {
                updatePlayerStatus(onlinePlayers, onlineGroups);
            }
        } catch (ReadException | IOException e) {
            logger.debug("Failed updating online state of groups/players", e);
        }
    }

    private void updatePlayerStatus(@Nullable Player[] onlinePlayers, @Nullable Group[] onlineGroups) {
        if (onlinePlayers == null || onlineGroups == null) {
            return;
        }
        Set<String> players = Stream.of(onlinePlayers).map(p -> Objects.toString(p.playerId))
                .collect(Collectors.toSet());
        Set<String> groups = Stream.of(onlineGroups).map(p -> p.id).collect(Collectors.toSet());

        for (Thing thing : getThing().getThings()) {
            try {
                @Nullable
                ThingHandler handler = thing.getHandler();
                if (handler instanceof HeosThingBaseHandler heosHandler) {
                    Set<String> target = handler instanceof HeosPlayerHandler ? players : groups;
                    String id = heosHandler.getId();

                    if (target.contains(id)) {
                        heosHandler.setStatusOnline();
                    } else {
                        heosHandler.setStatusOffline();
                    }
                }
            } catch (HeosNotFoundException e) {
                logger.debug("SKipping handler which reported not found", e);
            }
        }
    }

    private HeosFacade connectBridge() throws IOException, Telnet.ReadException {
        loggedIn = false;

        logger.debug("Initialize Bridge '{}' with IP '{}'", thing.getProperties().get(PROP_NAME),
                configuration.ipAddress);
        bridgeHandlerDisposalOngoing = false;
        HeosFacade connection = heosSystem.establishConnection(configuration.ipAddress, HEOS_PORT,
                configuration.heartbeat);
        connection.registerForChangeEvents(this);

        apiConnection = connection;

        return connection;
    }

    private void login(HeosFacade connection, String username, String password) throws IOException, ReadException {
        logger.debug("Logging in to HEOS account.");
        HeosResponseObject<Void> response = connection.logIn(username, password);

        if (response.result) {
            logger.debug("successfully logged-in, event is fired to handle post-login behaviour");
            return;
        }

        @Nullable
        HeosError error = response.getError();
        logger.debug("Failed to login: {}", error);
        updateStatus(ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                error != null ? error.code.toString() : "Failed to login, no error was returned.");
    }

    @Override
    public void dispose() {
        bridgeHandlerDisposalOngoing = true; // Flag to prevent the handler from being updated during disposal

        cancel(startupFuture);
        for (Future<?> future : childHandlerInitializedFutures) {
            cancel(future);
        }

        @Nullable
        HeosFacade localApiConnection = apiConnection;
        if (localApiConnection == null) {
            logger.debug("Not disposing bridge because of missing apiConnection");
            return;
        }

        localApiConnection.unregisterForChangeEvents(this);
        logger.debug("HEOS bridge removed from change notifications");

        logger.debug("Dispose bridge '{}'", thing.getProperties().get(PROP_NAME));
        localApiConnection.closeConnection();
    }

    /**
     * Manages the removal of the player or group channels from the bridge.
     */
    @Override
    public synchronized void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Disposing child handler for: {}.", childThing.getUID().getId());
        if (bridgeHandlerDisposalOngoing) { // Checks if bridgeHandler is going to disposed (by stopping the binding or
            // openHAB for example) and prevents it from being updated which stops the
            // disposal process.
        } else if (childHandler instanceof HeosPlayerHandler) {
            String channelIdentifier = "P" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingleChannel(channelIdentifier));
        } else if (childHandler instanceof HeosGroupHandler groupHandler) {
            String channelIdentifier = "G" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingleChannel(channelIdentifier));
            // removes the handler from the groupMemberMap that handler is no longer called
            // if group is getting online
            removeGroupHandlerInformation(groupHandler);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("Initialized child handler for: {}.", childThing.getUID().getId());
        childHandlerInitializedFutures.add(scheduler.submit(() -> addPlayerChannel(childThing, null)));
    }

    void resetPlayerList(ChannelUID channelUID) {
        selectedPlayerList.forEach(element -> updateState(element[1], OnOffType.OFF));
        selectedPlayerList.clear();
        updateState(channelUID, OnOffType.OFF);
    }

    /**
     * Sets the HEOS Thing offline
     */
    @SuppressWarnings("null")
    public void setGroupOffline(String groupMemberHash) {
        HeosGroupHandler groupHandler = groupHandlerMap.get(groupMemberHash);
        if (groupHandler != null) {
            groupHandler.setStatusOffline();
        }
        hashToGidMap.remove(groupMemberHash);
    }

    /**
     * Sets the HEOS Thing online. Also updates the link between
     * the groupMemberHash value with the actual gid of this group
     */
    public void setGroupOnline(String groupMemberHash, String groupId) {
        hashToGidMap.put(groupMemberHash, groupId);
        Optional.ofNullable(groupHandlerMap.get(groupMemberHash)).ifPresent(handler -> {
            handler.setStatusOnline();
            addPlayerChannel(handler.getThing(), groupId);
        });
    }

    /**
     * Create a channel for the childThing. Depending if it is a HEOS Group
     * or a player an identification prefix is added
     *
     * @param childThing the thing the channel is created for
     * @param groupId
     */
    private void addPlayerChannel(Thing childThing, @Nullable String groupId) {
        try {
            String channelIdentifier = "";
            String pid = "";
            @Nullable
            ThingHandler handler = childThing.getHandler();
            if (handler instanceof HeosPlayerHandler playerHandler) {
                channelIdentifier = "P" + childThing.getUID().getId();
                pid = playerHandler.getId();
            } else if (handler instanceof HeosGroupHandler groupHandler) {
                channelIdentifier = "G" + childThing.getUID().getId();
                if (groupId == null) {
                    pid = groupHandler.getId();
                } else {
                    pid = groupId;
                }
            }
            Map<String, String> properties = new HashMap<>();
            @Nullable
            String playerName = childThing.getLabel();
            playerName = playerName == null ? pid : playerName;
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelIdentifier);
            properties.put(PROP_NAME, playerName);
            properties.put(PID, pid);

            Channel channel = ChannelBuilder.create(channelUID, "Switch").withLabel(playerName).withType(CH_TYPE_PLAYER)
                    .withProperties(properties).build();
            updateThingChannels(channelManager.addSingleChannel(channel));
        } catch (HeosNotFoundException e) {
            logger.debug("Group is not yet initialized fully");
        }
    }

    public void addGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.put(handler.getGroupMemberHash(), handler);
    }

    private void removeGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.remove(handler.getGroupMemberHash());
    }

    public @Nullable String getActualGID(String groupHash) {
        return hashToGidMap.get(groupHash);
    }

    @Override
    public void playerStateChangeEvent(HeosEventObject eventObject) {
        // do nothing
    }

    @Override
    public void playerStateChangeEvent(HeosResponseObject<?> responseObject) {
        // do nothing
    }

    @Override
    public void playerMediaChangeEvent(String pid, Media media) {
        heosMediaEventListeners.forEach(element -> element.playerMediaChangeEvent(pid, media));
    }

    @Override
    public void bridgeChangeEvent(String event, boolean success, Object command) {
        if (EVENT_TYPE_EVENT.equals(event)) {
            if (HeosEvent.PLAYERS_CHANGED.equals(command) || HeosEvent.GROUPS_CHANGED.equals(command)) {
                fetchPlayersAndGroups();
                triggerPlayerDiscovery();
            } else if (EVENT_STREAM_TIMEOUT.equals(command)) {
                logger.debug("HEOS Bridge events timed-out might be nothing, trying to reconnect");
            } else if (CONNECTION_LOST.equals(command)) {
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                logger.debug("HEOS Bridge OFFLINE");
            } else if (CONNECTION_RESTORED.equals(command)) {
                initialize();
            }
        }
        if (EVENT_TYPE_SYSTEM.equals(event) && HeosEvent.USER_CHANGED == command) {
            if (success && !loggedIn) {
                loggedIn = true;
            }
        }
    }

    private synchronized void updateThingChannels(List<Channel> channelList) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    public Player[] getPlayers() throws IOException, ReadException {
        HeosResponseObject<Player[]> response = getApiConnection().getPlayers();
        @Nullable
        Player[] players = response.payload;
        if (players == null) {
            throw new IOException("Received no valid payload");
        }
        return players;
    }

    public Group[] getGroups() throws IOException, ReadException {
        HeosResponseObject<Group[]> response = getApiConnection().getGroups();
        @Nullable
        Group[] groups = response.payload;
        if (groups == null) {
            throw new IOException("Received no valid payload");
        }
        return groups;
    }

    /**
     * The list with the currently selected player
     *
     * @return a HashMap which the currently selected player
     */
    public Map<String, String> getSelectedPlayer() {
        return selectedPlayerList.stream().collect(Collectors.toMap(a -> a[0], a -> a[1], (a, b) -> a));
    }

    public List<String[]> getSelectedPlayerList() {
        return selectedPlayerList;
    }

    public void setSelectedPlayerList(List<String[]> selectedPlayerList) {
        this.selectedPlayerList = selectedPlayerList;
    }

    public HeosChannelHandlerFactory getChannelHandlerFactory() {
        return channelHandlerFactory;
    }

    /**
     * Register an {@link HeosPlayerDiscoveryListener} to get informed
     * if the amount of groups or players have changed
     *
     * @param listener the implementing class
     */
    public void registerPlayerDiscoverListener(HeosPlayerDiscoveryListener listener) {
        playerDiscoveryList.add(listener);
    }

    private void triggerPlayerDiscovery() {
        playerDiscoveryList.forEach(HeosPlayerDiscoveryListener::playerChanged);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isBridgeConnected() {
        @Nullable
        HeosFacade connection = apiConnection;
        return connection != null && connection.isConnected();
    }

    public HeosFacade getApiConnection() throws HeosNotConnectedException {
        @Nullable
        HeosFacade localApiConnection = apiConnection;
        if (localApiConnection != null) {
            return localApiConnection;
        } else {
            throw new HeosNotConnectedException();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(HeosActions.class);
    }

    public void registerMediaEventListener(HeosMediaEventListener heosMediaEventListener) {
        heosMediaEventListeners.add(heosMediaEventListener);
    }
}
