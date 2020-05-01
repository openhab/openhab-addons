/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
import org.openhab.binding.heos.internal.HeosChannelManager;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscoveryListener;
import org.openhab.binding.heos.internal.handler.HeosChannelHandler;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.HeosPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosSystemHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosBridgeHandler extends BaseBridgeHandler implements HeosEventListener {
    private final Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

    private static final int HEOS_PORT = 1255;

    private List<String> heosPlaylists = new ArrayList<>();
    private List<HeosPlayerDiscoveryListener> playerDiscoveryList = new ArrayList<>();
    private Map<String, String> selectedPlayer = new HashMap<>();
    private List<String[]> selectedPlayerList = new ArrayList<>();
    private HeosChannelManager channelManager = new HeosChannelManager(this);
    private HeosChannelHandlerFactory channelHandlerFactory;

    private Map<String, HeosGroupHandler> groupHandlerMap = new HashMap<>();
    private Map<String, String> hashToGidMap = new HashMap<>();

    private ScheduledFuture<?> poolExecuter;

    private HeosSystem heos;
    private HeosFacade api;

    private int heartbeatPulse = 0;

    private boolean isRegisteredForChangeEvents = false;
    private boolean bridgeIsConnected = false;
    private boolean loggedIn = false;
    private boolean connectionDelay = false;
    private boolean bridgeHandlerdisposalOngoing = false;

    public HeosBridgeHandler(Bridge thing, HeosSystem heos, HeosFacade api) {
        super(thing);
        this.heos = heos;
        this.api = api;
        channelHandlerFactory = new HeosChannelHandlerFactory(this, api);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        ChannelTypeUID channelTypeUID = null; // Needed to detect the player channels on the bridge
        Channel channel = this.getThing().getChannel(channelUID.getId());
        if (channel != null) {
            channelTypeUID = channel.getChannelTypeUID();
        } else {
            logger.debug("No valid channel found");
            return;
        }
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID, channelTypeUID);
        if (channelHandler != null) {
            channelHandler.handleCommand(command, this, channelUID);
        }
    }

    @Override
    public synchronized void initialize() {
        scheduledStartUp();
    }

    private void scheduledStartUp() {
        poolExecuter = scheduler.schedule(() -> {
            connectBridge();
            bridgeHandlerdisposalOngoing = false;
            heos.startEventListener();
            heos.startHeartBeat(heartbeatPulse);
            logger.debug("HEOS System heart beat started. Pulse time is {}s", heartbeatPulse);
            // gets all available player and groups to ensure that the system knows
            // about the conjunction between the groupMemberHash and the GID
            triggerPlayerDiscovery();
            if (thing.getConfiguration().containsKey(USERNAME) && thing.getConfiguration().containsKey(PASSWORD)) {
                logger.debug("Logging in to HEOS account.");
                String name = thing.getConfiguration().get(USERNAME).toString();
                String password = thing.getConfiguration().get(PASSWORD).toString();
                api.logIn(name, password);
                updateState(CH_ID_REBOOT, OnOffType.OFF);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Can't log in. Username or password not set.");
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        " Username or password not set or incorrect. Please Log-In to enable all HEOS features");
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void connectBridge() {
        if (bridgeIsConnected) {
            return;
        }
        loggedIn = false;

        logger.debug("Initialize Bridge '{}' with IP '{}'", thing.getConfiguration().get(NAME),
                thing.getConfiguration().get(HOST));
        heartbeatPulse = Integer.valueOf(thing.getConfiguration().get(HEARTBEAT).toString());
        heos.setConnectionIP(thing.getConfiguration().get(HOST).toString());
        heos.setConnectionPort(HEOS_PORT);
        bridgeIsConnected = heos.establishConnection(connectionDelay); // the connectionDelay gives the HEOS time to
                                                                       // recover after a restart
        while (!bridgeIsConnected) {
            ScheduledFuture<?> reConnect = scheduler.schedule(() -> {
                heos.closeConnection();
                bridgeIsConnected = heos.establishConnection(connectionDelay);
                logger.debug("Could not initialize connection to HEOS system");
            }, 5, TimeUnit.SECONDS);
            while (!reConnect.isDone()) {
            }
        }
        if (!isRegisteredForChangeEvents) {
            api.registerforChangeEvents(this);
            isRegisteredForChangeEvents = true;
        }
        connectionDelay = false; // sets default to false again
    }

    @Override
    public void dispose() {
        bridgeHandlerdisposalOngoing = true; // Flag to prevent the handler from being updated during disposal
        api.unregisterforChangeEvents(this);
        logger.debug("HEOS bridge removed from change notifications");
        isRegisteredForChangeEvents = false;
        loggedIn = false;
        logger.debug("Dispose bridge '{}'", thing.getConfiguration().get(NAME));
        heos.closeConnection();
        bridgeIsConnected = false;
        poolExecuter.cancel(true); // Prevents doubled execution if OpenHab doubles
                                   // initialization of the bridge
    }

    /**
     * Manages adding the player channel to the bridge
     */
    @Override
    public synchronized void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        addPlayerChannel(childThing);
        logger.debug("Initialize child handler for: {}.", childThing.getUID().getId());
    }

    /**
     * Manages the removal of the player or group channels from the bridge.
     */
    @Override
    public synchronized void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.debug("Interrupted Exception - Message: {}", e.getMessage());
        }
        if (bridgeHandlerdisposalOngoing) { // Checks if bridgeHandler is going to disposed (by stopping the binding or
                                            // openHAB for example) and prevents it from being updated which stops the
                                            // disposal process.
            return;
        } else if (HeosPlayerHandler.class.equals(childHandler.getClass())) {
            String channelIdentifyer = "P" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingelChannel(channelIdentifyer));
        } else {
            String channelIdentifyer = "G" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingelChannel(channelIdentifyer));
            // removes the handler from the groupMemberMap that handler is no longer called
            // if group is getting online
            removeGroupHandlerInformation((HeosGroupHandler) childHandler);
        }
        logger.debug("Dispose child handler for: {}.", childThing.getUID().getId());
    }

    public void resetPlayerList(ChannelUID channelUID) {
        selectedPlayerList.forEach(element -> updateState(element[1], OnOffType.OFF));
        selectedPlayerList.clear();
        updateState(channelUID, OnOffType.OFF);
    }

    /**
     * Sets the HEOS Thing offline
     *
     * @param uid the uid of the Thing which shell set offline
     */
    public void setGroupOffline(String hashValue) {
        groupHandlerMap.forEach((hash, handler) -> {
            if (hash.equals(hashValue)) {
                handler.setStatusOffline();
            }
        });
    }

    /**
     * Sets the HEOS Thing offline
     *
     * @param uid the uid of the Thing which shell set offline
     */
    @SuppressWarnings("null")
    public void setThingStatusOffline(ThingUID uid) {
        if (getThingByUID(uid) != null) {
            HeosThingBaseHandler childHandler = (HeosThingBaseHandler) getThingByUID(uid).getHandler();
            childHandler.setStatusOffline();
        }
    }

    /**
     * Sets the HEOS Thing online. Also updates the link between
     * the groubMemberHash value with the actual gid of this group
     *
     * @param uid the uid of the Thing which shell set online
     */
    public void setGroupOnline(HeosGroup group, ThingUID uid) {
        hashToGidMap.put(group.getGroupMemberHash(), group.getGid());
        groupHandlerMap.forEach((hash, handler) -> {
            if (hash.equals(group.getGroupMemberHash())) {
                handler.setStatusOnline();
                addPlayerChannel(handler.getThing());
            }
        });
    }

    public void addGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.put(handler.getGroupMemberHash(), handler);
    }

    public void removeGroupHandlerInformation(HeosGroupHandler handler) {
        if (groupHandlerMap.containsKey(handler.getGroupMemberHash())) {
            groupHandlerMap.remove(handler.getGroupMemberHash());
        }
    }

    public String getActualGID(String groupHash) {
        return hashToGidMap.get(groupHash);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        // Do nothing
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        // Do nothing
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (EVENTTYPE_EVENT.equals(event)) {
            if (PLAYERS_CHANGED.equals(command)) {
                triggerPlayerDiscovery();
            } else if (GROUPS_CHANGED.equals(command)) {
                triggerPlayerDiscovery();
            } else if (CONNECTION_LOST.equals(command)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                bridgeIsConnected = false;
                logger.debug("Heos Bridge OFFLINE");
            } else if (CONNECTION_RESTORED.equals(command)) {
                connectionDelay = true;
                initialize();
            }
        }
        if (EVENTTYPE_SYSTEM.equals(event)) {
            if (SING_IN.equals(command)) {
                if (SUCCESS.equals(result)) {
                    if (!loggedIn) {
                        loggedIn = true;
                        addPlaylists();
                    }
                }
            } else if (USER_CHANGED.equals(command)) {
                if (!loggedIn) {
                    loggedIn = true;
                    addPlaylists();
                }
            }
        }
    }

    public void addPlaylists() {
        if (loggedIn) {
            heosPlaylists.clear();
            heosPlaylists = heos.getPlaylists();
        }
    }

    /**
     * Create a channel for the childThing. Depending if it is a HEOS Group
     * or a player an identification prefix is added
     *
     * @param childThing the thing the channel is created for
     */
    @SuppressWarnings("null")
    private void addPlayerChannel(Thing childThing) {
        String channelIdentifyer = "";
        String pid = "";
        if (HeosPlayerHandler.class.equals(childThing.getHandler().getClass())) {
            channelIdentifyer = "P" + childThing.getUID().getId();
            pid = childThing.getConfiguration().get(PROP_PID).toString();
        } else if (HeosGroupHandler.class.equals(childThing.getHandler().getClass())) {
            channelIdentifyer = "G" + childThing.getUID().getId();
            HeosGroupHandler handler = (HeosGroupHandler) childThing.getHandler();
            pid = handler.getGroupID();
        }
        Map<String, String> properties = new HashMap<>(2);
        String playerName = childThing.getLabel().toString();
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelIdentifyer);
        properties.put(NAME, playerName);
        properties.put(PID, pid);

        Channel channel = ChannelBuilder.create(channelUID, "Switch").withLabel(playerName).withType(CH_TYPE_PLAYER)
                .withProperties(properties).build();
        updateThingChannels(channelManager.addSingleChannel(channel));
    }

    private void updateThingChannels(List<Channel> channelList) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    public Map<String, HeosPlayer> getNewPlayer() {
        return heos.getAllPlayer();
    }

    public Map<String, HeosGroup> getNewGroups() {
        return heos.getGroups();
    }

    public Map<String, HeosGroup> getRemovedGroups() {
        return heos.getGroupsRemoved();
    }

    public Map<String, HeosPlayer> getRemovedPlayer() {
        return heos.getPlayerRemoved();
    }

    /**
     * The list with the currently selected player
     *
     * @return a HashMap which the currently selected player
     */
    public Map<String, String> getSelectedPlayer() {
        selectedPlayer.clear();
        for (int i = 0; i < selectedPlayerList.size(); i++) {
            selectedPlayer.put(selectedPlayerList.get(i)[0], selectedPlayerList.get(i)[1]);
        }
        return selectedPlayer;
    }

    public void setSelectedPlayer(Map<String, String> selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    public List<String[]> getSelectedPlayerList() {
        return selectedPlayerList;
    }

    public void setSelectedPlayerList(List<String[]> selectedPlayerList) {
        this.selectedPlayerList = selectedPlayerList;
    }

    public List<String> getHeosPlaylists() {
        return heosPlaylists;
    }

    public HeosChannelHandlerFactory getChannelHandlerFactory() {
        return channelHandlerFactory;
    }

    public void setChannelHandlerFactory(HeosChannelHandlerFactory channelHandlerFactory) {
        this.channelHandlerFactory = channelHandlerFactory;
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
        playerDiscoveryList.forEach(element -> element.playerChanged());
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public boolean isBridgeConnected() {
        return bridgeIsConnected;
    }
}
