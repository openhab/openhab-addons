/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.api.HeosAPI;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscovery;
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

public class HeosBridgeHandler extends BaseBridgeHandler implements HeosEventListener, DiscoveryListener {

    private List<String> heosPlaylists = new ArrayList<String>();

    private HashMap<ThingUID, ThingHandler> handlerList = new HashMap<>();
    private HashMap<String, String> selectedPlayer = new HashMap<String, String>();
    private ArrayList<String[]> selectedPlayerList = new ArrayList<String[]>();
    private HashMap<ThingUID, ThingStatus> thingOnlineState = new HashMap<ThingUID, ThingStatus>();

    private ScheduledExecutorService initPhaseExecutor;
    private InitProcedure initPhaseRunnable = new InitProcedure();

    private HeosPlayerDiscovery playerDiscovery;
    private HeosSystem heos;
    private HeosAPI api;

    private int heartBeatPulse = 0;

    private boolean isRegisteredForChangeEvents = false;
    private boolean bridgeIsConnected = false;
    private boolean handleGroups = true;
    private boolean loggedIn = false;
    private boolean connectionDelay = false;
    private boolean bridgeHandlerdisposalOngoing = false;

    private Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

    public HeosBridgeHandler(Bridge thing, HeosSystem heos, HeosAPI api) {
        super(thing);
        this.heos = heos;
        this.api = api;
    }

    // @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        Channel channel = this.thing.getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("Channel {} not found", channelUID.toString());
            return;
        }
        if (channel.getChannelTypeUID().equals(CH_TYPE_PLAYER)) {
            if (command.toString().equals("ON")) {
                String[] selectedPlayerInfo = new String[2];
                selectedPlayerInfo[0] = channel.getProperties().get(PID);
                selectedPlayerInfo[1] = channelUID.getId();
                selectedPlayerList.add(selectedPlayerInfo);
            } else {
                if (!selectedPlayerList.isEmpty()) {
                    int indexPlayerChannel = -1;
                    for (int i = 0; i < selectedPlayerList.size(); i++) {
                        String localPID = selectedPlayerList.get(i)[0];
                        if (localPID == channel.getProperties().get(PID)) {
                            indexPlayerChannel = i;
                        }
                    }
                    selectedPlayerList.remove(indexPlayerChannel);
                }
            }
        }

        if (channel.getChannelTypeUID().equals(CH_TYPE_FAVORIT)) {
            if (command.toString().equals("ON")) {
                if (!selectedPlayerList.isEmpty()) {
                    for (int i = 0; i < selectedPlayerList.size(); i++) {
                        String pid = selectedPlayerList.get(i)[0];
                        String mid = channelUID.getId(); // the channel ID represents the MID of the favorite
                        api.playStation(pid, FAVORIT_SID, null, mid, null);
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                selectedPlayerList.clear();
            }
        }

        if (channelUID.getId().equals(CH_ID_PLAYLISTS)) {
            logger.debug("Start Playlist with {}", command.toString());
            if (!selectedPlayerList.isEmpty()) {
                for (int i = 0; i < selectedPlayerList.size(); i++) {
                    String pid = selectedPlayerList.get(i)[0];
                    String cid = heosPlaylists.get(Integer.valueOf(command.toString()));
                    api.addContainerToQueuePlayNow(pid, PLAYLISTS_SID, cid);
                }
            }
            resetPlayerList(CH_ID_PLAYLISTS);
        }

        if (channelUID.getId().equals(CH_ID_BUILDGROUP)) {
            if (command.toString().equals("ON")) {
                if (!selectedPlayerList.isEmpty()) {
                    String[] player = new String[selectedPlayerList.size()];
                    for (int i = 0; i < selectedPlayerList.size(); i++) {
                        player[i] = selectedPlayerList.get(i)[0];
                    }
                    api.groupPlayer(player);
                    resetPlayerList(CH_ID_BUILDGROUP);
                }
            }
        }
        if (channelUID.getId().equals(CH_ID_DYNGROUPSHAND)) {
            if (command.toString().equals("ON")) {
                handleGroups = true;
            } else {
                handleGroups = false;
            }
        }
        if (channelUID.getId().equals(CH_ID_REBOOT)) {
            if (command.toString().equals("ON")) {
                api.reboot();
                updateState(CH_ID_REBOOT, OnOffType.OFF);
            }
        }
        if (channelUID.getId().equals(CH_ID_RAW_COMMAND)) {
            heos.send(command.toString());
        }
        if (channelUID.getId().equals(CH_ID_PLAY_URL)) {
            if (!selectedPlayerList.isEmpty()) {
                for (int i = 0; i < selectedPlayerList.size(); i++) {
                    String pid = selectedPlayerList.get(i)[0];
                    String url = command.toString();
                    api.playURL(pid, url);
                }
            }
            resetPlayerList(CH_ID_PLAY_URL);
        }
    }

    public void resetPlayerList(@NonNull String channelUID) {
        for (int i = 0; i < selectedPlayerList.size(); i++) {
            updateState(selectedPlayerList.get(i)[1], OnOffType.OFF);
        }
        selectedPlayerList.clear();
        updateState(channelUID, OnOffType.OFF);
    }

    @Override
    public synchronized void initialize() {
        if (bridgeIsConnected) {
            return;
        }
        loggedIn = false;

        logger.info("Initit Brige '{}' with IP '{}'", thing.getConfiguration().get(NAME),
                thing.getConfiguration().get(HOST));

        heartBeatPulse = Integer.valueOf(thing.getConfiguration().get(HEART_BEAT).toString());
        heos.setConnectionIP(thing.getConfiguration().get(HOST).toString());
        heos.setConnectionPort(1255);
        bridgeIsConnected = heos.establishConnection(connectionDelay); // the connectionDelay gives the HEOS time to
                                                                       // recover after a restart
        while (!bridgeIsConnected) {
            heos.closeConnection();
            bridgeIsConnected = heos.establishConnection(connectionDelay);
            logger.warn("Could not initialize connection to HEOS system");
        }

        if (!isRegisteredForChangeEvents) {
            api.registerforChangeEvents(this);
            isRegisteredForChangeEvents = true;
        }

        scheduledStartUp();
        handleGroups = true;
        updateStatus(ThingStatus.ONLINE);
        logger.info("HEOS Bridge Online");
        connectionDelay = false; // sets default to false again
    }

    @Override
    public void dispose() {
        bridgeHandlerdisposalOngoing = true; // Flag to prevent the handler from being updated during disposal
        api.unregisterforChangeEvents(this);
        logger.info("HEOS bridge removed from change notifications");
        isRegisteredForChangeEvents = false;
        loggedIn = false;
        logger.info("Dispose Brige '{}'", thing.getConfiguration().get(NAME));
        heos.closeConnection();
        bridgeIsConnected = false;
        initPhaseExecutor.shutdownNow(); // Prevents doubled execution if OpenHab doubles initialization of the
                                         // bridge
        // updateStatus(ThingStatus.OFFLINE);
        super.dispose();
    }

    /**
     * Manages the adding of the childHandler to the handlerList and sets the Status
     * of the thing to ThingStatus.ONLINE.
     * Add also the player or group channel to the bridge.
     */

    @Override
    public synchronized void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        handlerList.put(childThing.getUID(), childHandler);
        thingOnlineState.put(childThing.getUID(), ThingStatus.ONLINE);
        this.addPlayerChannel(childThing);
        logger.info("Inizialize child handler for: {}.", childThing.getUID().getId());
    }

    /**
     * Manages the removal of the childHandler from the handlerList and sets the Status
     * of the thing to ThingsStatus.REMOVED
     * Removes also the channel of the player or group from the bridge.
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
        } else if (childThing.getConfiguration().get(TYPE).equals(PLAYER)) {
            String channelIdentifyer = "P" + childThing.getConfiguration().get(PID).toString();
            this.removeChannel(CH_TYPE_PLAYER, channelIdentifyer);
        } else {
            String channelIdentifyer = "G" + childThing.getConfiguration().get(GID).toString();
            this.removeChannel(CH_TYPE_PLAYER, channelIdentifyer);
        }

        handlerList.remove(childThing.getUID(), childHandler);
        thingOnlineState.put(childThing.getUID(), ThingStatus.REMOVED);
        logger.info("Dispose child handler for: {}.", childThing.getUID().getId());
        return;
    }

    public void thingStatusOffline(ThingUID uid) {
        thingOnlineState.put(uid, ThingStatus.OFFLINE);
    }

    public void thingStatusOnline(ThingUID uid) {
        thingOnlineState.put(uid, ThingStatus.ONLINE);
    }

    public void setHeosPlayerDiscovery(HeosPlayerDiscovery discover) {
        this.playerDiscovery = discover;
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        // Do nothing
    }

    @Override
    public void playerMediaChangeEvent(String pid, HashMap<String, String> info) {
        // Do nothing
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (event.equals(EVENT_EVENT)) {
            if (command.equals(PLAYERS_CHANGED)) {
                playerDiscovery.scanForNewPlayers();
            } else if (command.equals(GROUPS_CHANGED)) {
                playerDiscovery.scanForNewPlayers();
            } else if (command.equals(CONNECTION_LOST)) {
                updateStatus(ThingStatus.OFFLINE);
                bridgeIsConnected = false;
                logger.warn("Heos Bridge OFFLINE");
            } else if (command.equals(CONNECTION_RESTORED)) {
                connectionDelay = true;
                initialize();
            }
        }
        if (event.equals(EVENT_SYSTEM)) {
            if (command.equals(COM_SING_IN)) {
                if (result.equals(SUCCESS)) {
                    if (!loggedIn) {
                        loggedIn = true;
                        addFavorits();
                        addPlaylists();
                    }
                }
            } else if (command.equals(COM_USER_CHANGED)) {
                if (!loggedIn) {
                    loggedIn = true;
                    addFavorits();
                    addPlaylists();
                }
            }
        }
    }

    /**
     * If a thing is discovered, the method checks if the thing is already known
     * and the state is only temporary set to OFFLINE. If so initialize() of the
     * thing is called.
     */

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (handlerList.containsKey(result.getThingUID())) {
            if (thingOnlineState.containsKey(result.getThingUID())) {
                if (thingOnlineState.get(result.getThingUID()).equals(ThingStatus.OFFLINE)) {
                    handlerList.get(result.getThingUID()).initialize();
                }
            }
        }
    }

    /**
     * If handleGroups is activated, the HEOS group is not removed as things.
     * Only the status is set to offline and the information is stored within
     * the thingOnlineState HashMap as ThingStatus.OFFLINE.
     * If handleGroups is not active the thing is completely removed. The handler
     * is removed from the handler list via childHandlerDisposed()
     */

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        logger.info("Removing Thing: {}.", thingUID.getId());
        if (handlerList.get(thingUID) != null) {
            if (!handleGroups) {
                handlerList.get(thingUID).handleRemoval();
            } else {
                if (handlerList.get(thingUID).getClass().equals(HeosGroupHandler.class)) {
                    HeosGroupHandler handler = (HeosGroupHandler) handlerList.get(thingUID);
                    thingOnlineState.put(thingUID, ThingStatus.OFFLINE);
                    handler.setStatusOffline();
                } else if (handlerList.get(thingUID).getClass().equals(HeosPlayerHandler.class)) {
                    HeosPlayerHandler handler = (HeosPlayerHandler) handlerList.get(thingUID);
                    thingOnlineState.put(thingUID, ThingStatus.OFFLINE);
                    handler.setStatusOffline();
                }
            }
        }
    }

    @Override
    public @NonNull Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source, long timestamp,
            @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        return Collections.emptyList();
    }

    public void addPlaylists() {
        if (loggedIn) {
            heosPlaylists.clear();
            heosPlaylists = heos.getPlaylists();
        }
    }

    public void addFavorits() {
        if (loggedIn) {
            logger.info("Adding HEOS Favorite Channels");
            removeChannels(CH_TYPE_FAVORIT);
            logger.info("Old Favorite Channels removed");

            List<HashMap<String, String>> favList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> favorits = new HashMap<String, String>(4);
            favList = heos.getFavorits();
            int favCount = favList.size();
            ArrayList<Channel> favoritChannels = new ArrayList<Channel>(favCount);

            if (favCount != 0) {
                for (int i = 0; i < favCount; i++) {
                    for (String key : favList.get(i).keySet()) {
                        if (key.equals(MID)) {
                            favorits.put(key, favList.get(i).get(key));
                        }
                        if (key.equals(NAME)) {
                            favorits.put(key, favList.get(i).get(key));
                        }
                        if (key.equals("null")) {
                            return;
                        }
                    }
                    logger.info("Add Favorite Channel: {}", favorits.get(NAME));

                    favoritChannels.add(createFavoritChannel(favorits));
                }
            }
            addChannel(favoritChannels);
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

        if (childThing.getConfiguration().get(TYPE).equals(PLAYER)) {
            channelIdentifyer = "P" + childThing.getConfiguration().get(PID).toString();
            pid = childThing.getConfiguration().get(PID).toString();
        } else if (childThing.getConfiguration().get(TYPE).equals(GROUP)) {
            channelIdentifyer = "G" + childThing.getConfiguration().get(GID).toString();
            pid = childThing.getConfiguration().get(GID).toString();
        }

        @NonNull
        String playerName = childThing.getConfiguration().get(NAME).toString();

        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelIdentifyer);
        if (!hasChannel(channelUID)) {
            HashMap<String, String> properties = new HashMap<String, String>(2);
            properties.put(NAME, childThing.getConfiguration().get(NAME).toString());
            properties.put(PID, pid);

            Channel channel = ChannelBuilder.create(channelUID, "Switch").withLabel(playerName).withType(CH_TYPE_PLAYER)
                    .withProperties(properties).build();

            ArrayList<Channel> newChannelList = new ArrayList<>(1);
            newChannelList.add(channel);
            addChannel(newChannelList);
        }
    }

    private void addChannel(List<Channel> newChannelList) {
        List<Channel> existingChannelList = thing.getChannels();
        ArrayList<Channel> mutableChannelList = new ArrayList<Channel>();
        mutableChannelList.addAll(existingChannelList);
        mutableChannelList.addAll(newChannelList);

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(mutableChannelList);
        updateThing(thingBuilder.build());
    }

    private Channel createFavoritChannel(HashMap<String, String> properties) {
        String favoritName = properties.get(NAME);
        Channel channel = ChannelBuilder.create(new ChannelUID(this.getThing().getUID(), properties.get(MID)), "Switch")
                .withLabel(favoritName).withType(CH_TYPE_FAVORIT).withProperties(properties).build();

        return channel;
    }

    private void removeChannels(ChannelTypeUID channelType) {
        List<Channel> channelList = thing.getChannels();
        ArrayList<Channel> mutableChannelList = new ArrayList<Channel>();
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
        ArrayList<Channel> mutableChannelList = new ArrayList<Channel>();
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

    public HashMap<String, HeosPlayer> getNewPlayer() {
        return heos.getAllPlayer();
    }

    public HashMap<String, HeosGroup> getNewGroups() {
        return heos.getGroups();
    }

    public HashMap<String, HeosGroup> getRemovedGroups() {
        return heos.getGroupsRemoved();
    }

    public HashMap<String, HeosPlayer> getRemovedPlayer() {
        return heos.getPlayerRemoved();
    }

    /**
     * The list with the currently selected player
     *
     * @return a HashMap which the currently selected player
     */

    public HashMap<String, String> getSelectedPlayer() {
        selectedPlayer.clear();
        for (int i = 0; i < selectedPlayerList.size(); i++) {
            selectedPlayer.put(selectedPlayerList.get(i)[0], selectedPlayerList.get(i)[1]);
        }
        return selectedPlayer;
    }

    public void setSelectedPlayer(HashMap<String, String> selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    private void scheduledStartUp() {
        initPhaseExecutor = Executors.newScheduledThreadPool(1);
        initPhaseRunnable = new InitProcedure();
        initPhaseExecutor.schedule(this.initPhaseRunnable, 10, TimeUnit.SECONDS);
    }

    public class InitProcedure implements Runnable {
        @Override
        public void run() {
            bridgeHandlerdisposalOngoing = false;

            heos.startEventListener();
            heos.startHeartBeat(heartBeatPulse);
            logger.info("HEOS System heart beat startet. Pulse time is {}s", heartBeatPulse);
            updateState(CH_ID_DYNGROUPSHAND, OnOffType.ON); // activates dynamic group handling by default

            if (thing.getConfiguration().containsKey(USER_NAME) && thing.getConfiguration().containsKey(PASSWORD)) {
                logger.info("Logging in to HEOS account.");
                String name = thing.getConfiguration().get(USER_NAME).toString();
                String password = thing.getConfiguration().get(PASSWORD).toString();
                api.logIn(name, password);
            } else {
                logger.warn("Can not log in. Username and Password not set");
            }
        }
    }
}
