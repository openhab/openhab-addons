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
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
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

    private List<String> heosPlaylists = new ArrayList<>();
    private List<HeosPlayerDiscoveryListener> playerDiscoveryList = new ArrayList<>();
    private Map<String, String> selectedPlayer = new HashMap<>();
    private List<String[]> selectedPlayerList = new ArrayList<>();
    private HeosChannelHandlerFactory channelHandlerFactory;

    private ScheduledFuture<?> poolExecuter;

    private HeosSystem heos;
    private HeosFacade api;

    private int heartbeatPulse = 0;

    private boolean isRegisteredForChangeEvents = false;
    private boolean bridgeIsConnected = false;
    private boolean loggedIn = false;
    private boolean connectionDelay = false;
    private boolean bridgeHandlerdisposalOngoing = false;

    private final Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

    public HeosBridgeHandler(Bridge thing, HeosSystem heos, HeosFacade api) {
        super(thing);
        this.heos = heos;
        this.api = api;
        channelHandlerFactory = new HeosChannelHandlerFactory(this, api);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID);
        if (channelHandler != null) {
            channelHandler.handleCommand(command, this, channelUID);
            return;
        }
    }

    public void resetPlayerList(@NonNull ChannelUID channelUID) {
        selectedPlayerList.forEach(element -> updateState(element[1], OnOffType.OFF));
        selectedPlayerList.clear();
        updateState(channelUID, OnOffType.OFF);
    }

    @Override
    public synchronized void initialize() {
        if (bridgeIsConnected) {
            return;
        }
        loggedIn = false;

        logger.debug("Initialize Bridge '{}' with IP '{}'", thing.getConfiguration().get(NAME),
                thing.getConfiguration().get(HOST));
        heartbeatPulse = Integer.valueOf(thing.getConfiguration().get(HEARTBEAT).toString());
        heos.setConnectionIP(thing.getConfiguration().get(HOST).toString());
        heos.setConnectionPort(1255);
        bridgeIsConnected = heos.establishConnection(connectionDelay); // the connectionDelay gives the HEOS time to
                                                                       // recover after a restart
        while (!bridgeIsConnected) {
            heos.closeConnection();
            bridgeIsConnected = heos.establishConnection(connectionDelay);
            logger.debug("Could not initialize connection to HEOS system");
        }

        if (!isRegisteredForChangeEvents) {
            api.registerforChangeEvents(this);
            isRegisteredForChangeEvents = true;
        }

        scheduledStartUp();
        updateState(CH_ID_REBOOT, OnOffType.OFF);
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
        this.addPlayerChannel(childThing);
        logger.debug("Initzialize child handler for: {}.", childThing.getUID().getId());
    }

    /**
     * Manages the removal of the player or group channels from the bridge. *
     */
    @Override
    public synchronized void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.debug("Interrupted Exection - Message: {}", e.getMessage());
        }
        if (bridgeHandlerdisposalOngoing) { // Checks if bridgeHandler is going to disposed (by stopping the binding or
                                            // OpenHab for example) and prevents it from being updated which stops the
                                            // disposal process.
            return;
        } else if (HeosPlayerHandler.class.equals(childHandler.getClass())) {
            String channelIdentifyer = "P" + childThing.getConfiguration().get(PID).toString();
            this.removeChannel(CH_TYPE_PLAYER, channelIdentifyer);
        } else {
            String channelIdentifyer = "G" + childThing.getConfiguration().get(GID).toString();
            this.removeChannel(CH_TYPE_PLAYER, channelIdentifyer);
        }

        logger.debug("Dispose child handler for: {}.", childThing.getUID().getId());
        return;
    }

    /**
     * Sets the HEOS Thing offline
     *
     * @param uid the uid of the Thing which shell set offline
     */
    @SuppressWarnings("null")
    public void setThingStatusOffline(ThingUID uid) {
        if (this.getThingByUID(uid) != null) {
            HeosThingBaseHandler childHandler = (HeosThingBaseHandler) this.getThingByUID(uid).getHandler();
            childHandler.setStatusOffline();
        }
    }

    /**
     * Sets the HEOS Thing online
     *
     * @param uid the uid of the Thing which shell set offline
     */
    @SuppressWarnings("null")
    public void setThingStatusOnline(ThingUID uid) {
        if (this.getThingByUID(uid) != null) {
            HeosThingBaseHandler childHandler = (HeosThingBaseHandler) this.getThingByUID(uid).getHandler();
            childHandler.setStatusOnline();
        }
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
                        addFavorites();
                        addPlaylists();
                    }
                }
            } else if (USER_CHANGED.equals(command)) {
                if (!loggedIn) {
                    loggedIn = true;
                    addFavorites();
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

    public void addFavorites() {
        if (loggedIn) {
            logger.debug("Adding HEOS Favorite Channels");
            removeChannels(CH_TYPE_FAVORITE);
            logger.debug("Old Favorite Channels removed");

            List<Map<String, String>> favList = new ArrayList<>();
            Map<String, String> favorites = new HashMap<>(4);
            favList = heos.getFavorites();
            int favCount = favList.size();
            List<Channel> favoriteChannels = new ArrayList<>(favCount);

            if (favCount != 0) {
                for (int i = 0; i < favCount; i++) {
                    for (String key : favList.get(i).keySet()) {
                        if (key.equals(MID)) {
                            favorites.put(key, favList.get(i).get(key));
                        }
                        if (key.equals(NAME)) {
                            favorites.put(key, favList.get(i).get(key));
                        }
                        if (key.equals("null")) {
                            return;
                        }
                    }
                    logger.debug("Add Favorite Channel: {}", favorites.get(NAME));

                    favoriteChannels.add(createFavoriteChannel(favorites));
                }
            }
            addChannel(favoriteChannels);
        }
    }

    /**
     * Create a channel for the childThing. Depending if it is a HEOS Group
     * or a player an identification prefix is added
     *
     * @param childThing the thing the channel is created for
     */
    private void addPlayerChannel(Thing childThing) {
        String channelIdentifyer = "";
        String pid = "";
        if (HeosPlayerHandler.class.equals(childThing.getHandler().getClass())) {
            channelIdentifyer = "P" + childThing.getConfiguration().get(PID).toString();
            pid = childThing.getConfiguration().get(PID).toString();
        } else if (HeosGroupHandler.class.equals(childThing.getHandler().getClass())) {
            channelIdentifyer = "G" + childThing.getConfiguration().get(GID).toString();
            pid = childThing.getConfiguration().get(GID).toString();
        }

        @NonNull
        String playerName = childThing.getConfiguration().get(NAME).toString();

        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelIdentifyer);
        if (!hasChannel(channelUID)) {
            Map<String, String> properties = new HashMap<>(2);
            properties.put(NAME, childThing.getConfiguration().get(NAME).toString());
            properties.put(PID, pid);

            Channel channel = ChannelBuilder.create(channelUID, "Switch").withLabel(playerName).withType(CH_TYPE_PLAYER)
                    .withProperties(properties).build();

            List<Channel> newChannelList = new ArrayList<>(1);
            newChannelList.add(channel);
            addChannel(newChannelList);
        }
    }

    private void addChannel(List<Channel> newChannelList) {
        List<Channel> existingChannelList = thing.getChannels();
        List<Channel> mutableChannelList = new ArrayList<>();
        mutableChannelList.addAll(existingChannelList);
        mutableChannelList.addAll(newChannelList);
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(mutableChannelList);
        updateThing(thingBuilder.build());
    }

    private Channel createFavoriteChannel(Map<String, String> properties) {
        String favoriteName = properties.get(NAME);
        Channel channel = ChannelBuilder.create(new ChannelUID(this.getThing().getUID(), properties.get(MID)), "Switch")
                .withLabel(favoriteName).withType(CH_TYPE_FAVORITE).withProperties(properties).build();
        return channel;
    }

    private void removeChannels(ChannelTypeUID channelType) {
        List<Channel> channelList = thing.getChannels();
        List<Channel> mutableChannelList = new ArrayList<>();
        mutableChannelList.addAll(channelList);
        for (int i = 0; i < mutableChannelList.size(); i++) {
            if (mutableChannelList.get(i).getChannelTypeUID().equals(channelType)) {
                mutableChannelList.remove(i);
                i = 0;
            }
        }
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(mutableChannelList);
        updateThing(thingBuilder.build());
    }

    private void removeChannel(ChannelTypeUID channelType, String channelIdentifyer) {
        ChannelUID channelUID = new ChannelUID(this.thing.getUID(), channelIdentifyer);
        List<Channel> channelList = thing.getChannels();
        List<Channel> mutableChannelList = new ArrayList<>();
        mutableChannelList.addAll(channelList);
        for (int i = 0; i < mutableChannelList.size(); i++) {
            if (mutableChannelList.get(i).getUID().equals(channelUID)) {
                mutableChannelList.remove(i);
            }
        }
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(mutableChannelList);
        updateThing(thingBuilder.build());
    }

    private boolean hasChannel(ChannelUID channelUID) {
        List<Channel> channelList = thing.getChannels();
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getUID().equals(channelUID)) {
                return true;
            }
        }
        return false;
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

    /**
     * @return the selectedPlayerList
     */
    public List<String[]> getSelectedPlayerList() {
        return selectedPlayerList;
    }

    /**
     * @return the selectedPlayerList
     */
    public void setSelectedPlayerList(List<String[]> selectedPlayerList) {
        this.selectedPlayerList = selectedPlayerList;
    }

    /**
     * @return the heosPlaylists
     */
    public List<String> getHeosPlaylists() {
        return heosPlaylists;
    }

    /**
     * @return the channelHandlerFactory
     */
    public HeosChannelHandlerFactory getChannelHandlerFactory() {
        return channelHandlerFactory;
    }

    /**
     * @param channelHandlerFactory the channelHandlerFactory to set
     */
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

    private void scheduledStartUp() {
        poolExecuter = scheduler.schedule(() -> {
            bridgeHandlerdisposalOngoing = false;
            heos.startEventListener();
            heos.startHeartBeat(heartbeatPulse);
            logger.debug("HEOS System heart beat started. Pulse time is {}s", heartbeatPulse);
            if (thing.getConfiguration().containsKey(USERNAME) && thing.getConfiguration().containsKey(PASSWORD)) {
                logger.debug("Logging in to HEOS account.");
                String name = thing.getConfiguration().get(USERNAME).toString();
                String password = thing.getConfiguration().get(PASSWORD).toString();
                api.logIn(name, password);
            } else {
                logger.info("Can not log in. Username and Password not set");
            }
            updateStatus(ThingStatus.ONLINE);
            logger.debug("HEOS bridge Online");
        }, 10, TimeUnit.SECONDS);
    }
}
