/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.plex.internal.handler;

import static org.openhab.binding.plex.internal.PlexBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plex.internal.PlexBindingConstants;
import org.openhab.binding.plex.internal.config.PlexServerConfiguration;
import org.openhab.binding.plex.internal.dto.MediaContainer;
import org.openhab.binding.plex.internal.dto.MediaContainer.MediaType;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlexServerHandler} is responsible for creating the
 * Bridge Thing for a PLEX Server.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexServerHandler extends BaseBridgeHandler implements PlexUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(PlexServerHandler.class);

    // Maintain mapping of handler and players
    private final Map<String, PlexPlayerHandler> playerHandlers = new ConcurrentHashMap<>();

    private PlexServerConfiguration config = new PlexServerConfiguration();
    // private @Nullable PlexServerHandler bridge;
    private PlexApiConnector plexAPIConnector;

    private @Nullable ScheduledFuture<?> pollingJob;

    private volatile boolean isRunning = false;

    public PlexServerHandler(Bridge bridge) {
        super(bridge);
        plexAPIConnector = new PlexApiConnector(scheduler);
        logger.debug("Initializing server handler");
    }

    public PlexApiConnector getPlexAPIConnector() {
        return plexAPIConnector;
    }

    /**
     * Initialize the Bridge set the config paramaters for the PLEX Server and
     * start the refresh Job.
     */
    @Override
    public void initialize() {
        config = getConfigAs(PlexServerConfiguration.class);
        if (!EMPTY.equals(config.host)) { // Check if a hostname is set
            plexAPIConnector.setParameters(config);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Host must be specified, check configuration");
            return;
        }
        if (!plexAPIConnector.hasToken()) {
            // No token is set by config, let's see if we can fetch one from username/password
            logger.debug("Token is not set, trying to fetch one");
            if ((EMPTY.equals(config.getUsername()) || EMPTY.equals(config.getPassword()))) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Username, password and Token is not set, unable to connect to PLEX without. ");
                return;
            } else {
                try {
                    plexAPIConnector.getToken();
                } catch (ConfigurationException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                    return;
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return;
                }
            }
        }
        logger.debug("Fetch API with config, {}", config.toString());
        if (!plexAPIConnector.getApi()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to fetch API, token may be wrong?");
            return;
        }
        isRunning = true;
        onUpdate(); // Start the session refresh
        scheduler.execute(() -> { // Start the web socket
            synchronized (this) {
                if (isRunning) {
                    PlexApiConnector localSockets = plexAPIConnector = new PlexApiConnector(scheduler);
                    localSockets.setParameters(config);
                    localSockets.registerListener(this);
                    localSockets.connect();
                }
            }
        });
    }

    /**
     * Not currently used, this is a read-only binding.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command '{}' for {}", command, channelUID);
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("PLEX is offline, ignoring command {} for channel {}", command, channelUID);
            return;
        }
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("No such channel for UID {}", channelUID);
            return;
        }
    }

    /**
     * Gets a list of all the players currently being used w/ a status of local. This
     * is used for discovery only.
     *
     * @return
     */
    public List<String> getAvailablePlayers() {
        List<String> availablePlayers = new ArrayList<String>();
        MediaContainer sessionData = plexAPIConnector.getSessionData();
        if (sessionData != null) {
            if (sessionData.getSize() > 0) {
                for (MediaType tmpMeta : sessionData.getMediaTypes()) {
                    if (playerHandlers.get(tmpMeta.getPlayer().getMachineIdentifier()) == null) {
                        if (tmpMeta.getPlayer().getLocal().equals("1")) {
                            availablePlayers.add(tmpMeta.getPlayer().getMachineIdentifier());
                        }
                    }
                }
            }
        }
        return availablePlayers;
    }

    /**
     * Called when a new player thing has been added. We add it to the hash map so we can
     * keep track of things.
     */
    @Override
    public synchronized void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        String playerID = (String) childThing.getConfiguration().get(CONFIG_PLAYER_ID);
        playerHandlers.put(playerID, (PlexPlayerHandler) childHandler);
        logger.warn("Bridge: Monitor handler was initialized for {} with id {}", childThing.getUID(), playerID);
    }

    /**
     * Called when a player has been removed from the system.
     */
    @Override
    public synchronized void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        String playerID = (String) childThing.getConfiguration().get(CONFIG_PLAYER_ID);
        playerHandlers.remove(playerID);
        logger.warn("Bridge: Monitor handler was disposed for {} with id {}", childThing.getUID(), playerID);
    }

    /**
     * Basically a callback method for the websocket handling
     */
    @Override
    public void onItemStatusUpdate(String sessionKey, String state) {
        try {
            for (Map.Entry<String, PlexPlayerHandler> entry : playerHandlers.entrySet()) {
                if (entry.getValue().getSessionKey().equals(sessionKey)) {
                    entry.getValue().updateStateChannel(state);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed setting item status : {}", e.getMessage());
        }
    }

    /**
     * Clears the foundInSession field for the configured players, then it sets the
     * data for the machineIds that are found in the session data set. This allows
     * us to determine if a device is on or off.
     *
     * @param sessionData The MediaContainer object that is pulled from the XML result of
     *            a call to the session data on PLEX.
     */
    @SuppressWarnings("null")
    private void refreshStates(MediaContainer sessionData) {
        int playerCount = 0;
        int playerActiveCount = 0;
        Iterator<PlexPlayerHandler> valueIterator = playerHandlers.values().iterator();
        while (valueIterator.hasNext()) {
            playerCount++;
            valueIterator.next().setFoundInSession(false);
        }
        if (sessionData != null) {
            if (sessionData.getSize() > 0) { // Cover condition where nothing is playing
                for (MediaContainer.MediaType tmpMeta : sessionData.getMediaTypes()) { // Roll through mediaType objects
                                                                                       // looking for machineID
                    if (playerHandlers.get(tmpMeta.getPlayer().getMachineIdentifier()) != null) { // if we have a player
                                                                                                  // configured, update
                                                                                                  // it
                        tmpMeta.setArt(plexAPIConnector.getURL(tmpMeta.getArt()));
                        if (tmpMeta.getType().equals("episode")) {
                            tmpMeta.setThumb(plexAPIConnector.getURL(tmpMeta.getGrandparentThumb()));
                            tmpMeta.setTitle(tmpMeta.getGrandparentTitle() + " : " + tmpMeta.getTitle());
                        } else if (tmpMeta.getType().equals("track")) {
                            tmpMeta.setThumb(plexAPIConnector.getURL(tmpMeta.getThumb()));
                            tmpMeta.setTitle(tmpMeta.getGrandparentTitle() + " - " + tmpMeta.getParentTitle() + " - "
                                    + tmpMeta.getTitle());
                        } else {
                            tmpMeta.setThumb(plexAPIConnector.getURL(tmpMeta.getThumb()));
                        }
                        playerHandlers.get(tmpMeta.getPlayer().getMachineIdentifier()).refreshSessionData(tmpMeta);
                        playerActiveCount++;
                    }
                }
            }
        }
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_SERVER_COUNT),
                new StringType(String.valueOf(playerCount)));
        updateState(new ChannelUID(getThing().getUID(), PlexBindingConstants.CHANNEL_SERVER_COUNTACTIVE),
                new StringType(String.valueOf(playerActiveCount)));
    }

    /**
     * Refresh all the configured players
     */
    private void refreshAllPlayers() {
        Iterator<PlexPlayerHandler> valueIterator = playerHandlers.values().iterator();
        while (valueIterator.hasNext()) {
            valueIterator.next().updateChannels();
        }
    }

    /**
     * This is called to start the refresh job and also to reset that refresh job when a config change is done.
     */
    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            int pollingInterval = ((BigDecimal) getConfig().get(CONFIG_REFRESH_RATE)).intValue();
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, pollingInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * The refresh job, pulls the session data and then calls refreshAllPlayers which will have them send
     * out their current status.
     */
    private Runnable pollingRunnable = () -> {
        try {
            MediaContainer plexSessionData = plexAPIConnector.getSessionData();
            if (plexSessionData != null) {
                refreshStates(plexSessionData);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Plex is not returning valid session data");
            }
            refreshAllPlayers();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String
                    .format("An exception occurred while polling the PLEX Server: '%s'", e.getMessage()).toString());
        }
    };

    @Override
    public void dispose() {
        logger.debug("Disposing PLEX Bridge Handler.");
        isRunning = false;
        plexAPIConnector.dispose();
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }
}
