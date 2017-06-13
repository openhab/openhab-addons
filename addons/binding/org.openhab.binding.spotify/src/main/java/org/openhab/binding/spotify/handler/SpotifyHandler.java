/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.handler;

import static org.openhab.binding.spotify.SpotifyBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.spotify.discovery.SpotifyDeviceDiscovery;
import org.openhab.binding.spotify.internal.SpotifyAuthService;
import org.openhab.binding.spotify.internal.SpotifyHandlerFactory;
import org.openhab.binding.spotify.internal.SpotifySession;
import org.openhab.binding.spotify.internal.SpotifySession.SpotifyWebAPIPlayerInfo;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyHandler} is the main class to manage Spotify WebAPI connection and update status of things.
 *
 * @author Andreas Stenlund - Initial contribution
 */
public class SpotifyHandler extends ConfigStatusBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(SpotifyHandler.class);

    @SuppressWarnings("rawtypes")
    private ServiceRegistration discoveryRegistration;

    private SpotifySession spotifySession = null;
    private SpotifyDeviceDiscovery discoveryService;
    private SpotifyAuthService authService = null;
    private SpotifyHandlerFactory handlerFactory = null;

    private Integer pollingInterval = 5;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    @SuppressWarnings("rawtypes")
    ScheduledFuture future = null;

    public SpotifyHandler(Bridge bridge, SpotifyHandlerFactory handlerFactory) {
        super(bridge);
        this.handlerFactory = handlerFactory;
    }

    public SpotifySession getSpotifySession() {
        return spotifySession;
    }

    public void setSpotifySession(SpotifySession session) {
        this.spotifySession = session;

        if (!spotifySession.scheduleAccessTokenRefresh()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No access token retrieved. Update configuration and/or perform the Spotify WebAPI Logon procedure at /connectspotify/");

            if (authService != null) {
                authService.authenticateSpotifyPlayer(this);
            }

        } else {
            updateStatus(ThingStatus.ONLINE);

            // Create the discovery service
            discoveryService = new SpotifyDeviceDiscovery(this);

            // And register it as an OSGi service
            discoveryRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                    new Hashtable<String, Object>());

            startPolling(pollingInterval);
        }

    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // no messages
        Collection<ConfigStatusMessage> configStatusMessages;
        configStatusMessages = Collections.emptyList();
        return configStatusMessages;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        String channel = channelUID.getId();

        switch (channel) {
            case CHANNEL_TRACKPLAY:
                if (command instanceof StringType) {
                    spotifySession.playTrack(((StringType) command).toString());
                }
                break;
            case CHANNEL_TRACKPLAYER:
                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        spotifySession.playActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PLAY);
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        spotifySession.pauseActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PAUSE);
                    }
                }
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        spotifySession.playActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PLAY);
                    } else if (command.equals(OnOffType.OFF)) {
                        spotifySession.pauseActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PAUSE);
                    }
                }
                if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        spotifySession.nextTrack();
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        spotifySession.previousTrack();
                    }

                }
                if (command instanceof StringType) {
                    String cmd = ((StringType) command).toString();
                    if (cmd.equalsIgnoreCase("play")) {
                        spotifySession.playActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PLAY);
                    } else if (cmd.equalsIgnoreCase("pause")) {
                        spotifySession.pauseActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PAUSE);
                    } else if (cmd.equalsIgnoreCase("next")) {
                        spotifySession.nextTrack();
                    } else if (cmd.equalsIgnoreCase("prev") || cmd.equalsIgnoreCase("previous")) {
                        spotifySession.previousTrack();
                    }

                }
                break;
            case CHANNEL_DEVICESHUFFLE:
                if (command instanceof OnOffType) {
                    spotifySession.setShuffleState(command.equals(OnOffType.OFF) ? "false" : "true");
                }
                break;
            case CHANNEL_DEVICEVOLUME:
                if (command instanceof DecimalType) {
                    PercentType volume = new PercentType(((DecimalType) command).intValue());
                    spotifySession.setVolume(volume.intValue());
                    setChannelValue(CHANNEL_DEVICEVOLUME, volume);
                } else if (command instanceof PercentType) {
                    PercentType volume = (PercentType) command;
                    spotifySession.setVolume(volume.intValue());
                    setChannelValue(CHANNEL_DEVICEVOLUME, volume);
                }
                break;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        if (future != null) {
            future.cancel(true);
        }

        if (spotifySession != null) {
            spotifySession.dispose();
        }

        if (discoveryService != null) {
            discoveryService.abortScan();
            discoveryRegistration.unregister();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Spotify bridge handler.");

        Configuration conf = getConfig();
        pollingInterval = ((java.math.BigDecimal) conf.get("refreshPeriod")).intValueExact();

        final String clientId = (String) conf.get("clientId");
        final String clientSecret = (String) conf.get("clientSecret");
        final String refreshToken = (String) conf.get("refreshToken");

        if (handlerFactory != null) {
            authService = handlerFactory.getSpotifyAuthService();
        }

        if (getConfig().get("clientId") != null) {
            setSpotifySession(SpotifySession.getInstance(this, clientId, clientSecret, refreshToken));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Spotify Web API - client parameters not set.");
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler thingHandler, Thing thing) {
        logger.debug("Initializing child {} : {} .", thingHandler.getClass().getName(), thing.getLabel());
        SpotifyDeviceHandler handler = (SpotifyDeviceHandler) thingHandler;
        handler.setController(this);

    }

    @Override
    public void childHandlerDisposed(ThingHandler thingHandler, Thing thing) {
        logger.debug("Disposing child {} : {} .", thingHandler.getClass().getName(), thing.getLabel());

    }

    /**
     * This method initiates a new thread for polling the available Spotify Connect devices and update the player
     * information
     *
     * @param intervall the number of seconds between update requests.
     */
    private void startPolling(int intervall) {
        // Cancel the previous thread
        if (future != null) {
            future.cancel(false);
        }

        // TODO: See if the creation of thread here can be replaced with existing threads & thread pools.
        Runnable task = () -> {
            try {
                logger.debug("Polling Spotify for status");
                updateDeviceStatus();

                // TODO: Determine if the player info query should be seprated from the device status query.
                updatePlayerInfo();

            } catch (Exception ex) {
                logger.error("Exception caught in anonymous scheduled runnable task. This task will now stop.", ex);
                // TODO: best practice to ignore the exception in this thread or be nice and let the thread end as it
                // doesn't work?
                throw (ex);
            }

        };
        future = executor.scheduleWithFixedDelay(task, 0, intervall, TimeUnit.SECONDS);

    }

    /**
     * Updates
     */
    private void updateDeviceStatus() {
        final HashMap<String, SpotifyDeviceHandler> allDevices = new HashMap<String, SpotifyDeviceHandler>();
        final List<SpotifyDeviceHandler> availableDevices = new ArrayList<SpotifyDeviceHandler>();

        final List<SpotifySession.SpotifyWebAPIDeviceList.Device> spotifyDevices = spotifySession.listDevices();

        List<Thing> bridgeThings = this.getThing().getThings();
        for (Thing thing : bridgeThings) {
            if (thing.getHandler() instanceof SpotifyDeviceHandler) {
                SpotifyDeviceHandler handler = (SpotifyDeviceHandler) thing.getHandler();

                String handlerDeviceId = handler.getDeviceId();
                allDevices.put(handlerDeviceId, handler);
            }
        }

        for (SpotifySession.SpotifyWebAPIDeviceList.Device device : spotifyDevices) {
            if (allDevices.keySet().contains(device.getId())) {
                SpotifyDeviceHandler handler = allDevices.get(device.getId());

                logger.debug("Spotify device is a thing: {} Device [ {} {}, {} ]",
                        handler.getThing().getUID().getAsString(), device.getId(), device.getName(), device.getType());

                // Device status is not always available from Spotify Web API so list of available devices
                // varies, keep track of the active ones
                availableDevices.add(handler);

                handler.setChannelValue(CHANNEL_DEVICENAME, new StringType(device.getName()));
                handler.setChannelValue(CHANNEL_DEVICETYPE, new StringType(device.getType()));
                if (device.getVolumePercent() != null) {
                    handler.setChannelValue(CHANNEL_DEVICEVOLUME, new PercentType(device.getVolumePercent()));
                }
                handler.setChannelValue(CHANNEL_DEVICEACTIVE, device.getIsActive() ? OnOffType.ON : OnOffType.OFF);
                handler.setChannelValue(CHANNEL_DEVICERESTRICTED,
                        device.getIsRestricted() ? OnOffType.ON : OnOffType.OFF);
            } else {
                // TODO: add new Thing? Decide on whether to add automatic discovery of devices not configured as things
                // here
                // and get rid of discovery implementation?
                logger.debug("Spotify device is not a thing: {} ({}, {})", device.getName(), device.getType(),
                        device.getId());
            }
        }

        // Change status of the device
        for (SpotifyDeviceHandler handler : allDevices.values()) {
            if (availableDevices.contains(handler)) {
                // If device is available, but OFFLINE - take ONLINE
                if (handler.getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                    logger.debug("Taking device {} ONLINE.", handler.getThing().getUID());
                    handler.changeStatus(ThingStatus.ONLINE);
                }
            } else {
                // If device is not available, but ONLINE - take OFFLINE
                if (handler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    logger.debug("Taking device {} OFFLINE.", handler.getThing().getUID());
                    handler.changeStatus(ThingStatus.OFFLINE);
                }
            }
        }
    }

    /**
     *
     */
    private void updatePlayerInfo() {
        SpotifyWebAPIPlayerInfo playerInfo = spotifySession.getPlayerInfo();

        setChannelValue(CHANNEL_TRACKPLAYER, playerInfo.getIsPlaying() ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        setChannelValue(CHANNEL_TRACKSHUFFLE, playerInfo.getShuffleState() ? OnOffType.ON : OnOffType.OFF);
        setChannelValue(CHANNEL_TRACKREPEAT, new StringType(playerInfo.getRepeatState()));

        Long progress = playerInfo.getProgressMs();
        Long duration = playerInfo.getItem().getDurationMs();
        setChannelValue(CHANNEL_PLAYED_TRACKPROGRESS_MS, new DecimalType(progress));
        setChannelValue(CHANNEL_PLAYED_TRACKDURATION_MS, new DecimalType(duration));

        SimpleDateFormat fmt = new SimpleDateFormat("m:ss");
        String progressFmt = fmt.format(new Date(progress));
        String durationFmt = fmt.format(new Date(duration));
        setChannelValue(CHANNEL_PLAYED_TRACKPROGRESS_FMT, new StringType(progressFmt));
        setChannelValue(CHANNEL_PLAYED_TRACKDURATION_FMT, new StringType(durationFmt));

        setChannelValue(CHANNEL_PLAYED_TRACKID, new StringType(playerInfo.getItem().getId()));
        setChannelValue(CHANNEL_PLAYED_TRACKHREF, new StringType(playerInfo.getItem().getHref()));
        setChannelValue(CHANNEL_PLAYED_TRACKURI, new StringType(playerInfo.getItem().getUri()));
        setChannelValue(CHANNEL_PLAYED_TRACKNAME, new StringType(playerInfo.getItem().getName()));
        setChannelValue(CHANNEL_PLAYED_TRACKTYPE, new StringType(playerInfo.getItem().getType()));
        setChannelValue(CHANNEL_PLAYED_TRACKNUMBER, new StringType(playerInfo.getItem().getTrackNumber().toString()));
        setChannelValue(CHANNEL_PLAYED_TRACKDISCNUMBER,
                new StringType(playerInfo.getItem().getDiscNumber().toString()));
        setChannelValue(CHANNEL_PLAYED_TRACKPOPULARITY, new DecimalType(playerInfo.getItem().getPopularity()));

        setChannelValue(CHANNEL_PLAYED_ALBUMID, new StringType(playerInfo.getItem().getAlbum().getId()));
        setChannelValue(CHANNEL_PLAYED_ALBUMHREF, new StringType(playerInfo.getItem().getAlbum().getHref()));
        setChannelValue(CHANNEL_PLAYED_ALBUMURI, new StringType(playerInfo.getItem().getAlbum().getUri()));
        setChannelValue(CHANNEL_PLAYED_ALBUMNAME, new StringType(playerInfo.getItem().getAlbum().getName()));
        setChannelValue(CHANNEL_PLAYED_ALBUMTYPE, new StringType(playerInfo.getItem().getAlbum().getType()));

        // There can be multiple artists, picking the first.
        // TODO: Is there a way to manage multiple artists through channels? Provide a JSON string data channel for
        // artists, playlists etc?
        if (playerInfo.getItem().getArtists().size() > 0) {
            setChannelValue(CHANNEL_PLAYED_ARTISTID, new StringType(playerInfo.getItem().getArtists().get(0).getId()));
            setChannelValue(CHANNEL_PLAYED_ARTISTHREF,
                    new StringType(playerInfo.getItem().getArtists().get(0).getHref()));
            setChannelValue(CHANNEL_PLAYED_ARTISTURI,
                    new StringType(playerInfo.getItem().getArtists().get(0).getUri()));
            setChannelValue(CHANNEL_PLAYED_ARTISTNAME,
                    new StringType(playerInfo.getItem().getArtists().get(0).getName()));
            setChannelValue(CHANNEL_PLAYED_ARTISTTYPE,
                    new StringType(playerInfo.getItem().getArtists().get(0).getType()));
        } else {
            setChannelValue(CHANNEL_PLAYED_ARTISTID, new StringType(""));
            setChannelValue(CHANNEL_PLAYED_ARTISTHREF, new StringType(""));
            setChannelValue(CHANNEL_PLAYED_ARTISTURI, new StringType(""));
            setChannelValue(CHANNEL_PLAYED_ARTISTNAME, new StringType("no data"));
            setChannelValue(CHANNEL_PLAYED_ARTISTTYPE, new StringType("no data"));
        }

        setChannelValue(CHANNEL_DEVICEID, new StringType(playerInfo.getDevice().getId()));
        setChannelValue(CHANNEL_DEVICEACTIVE, playerInfo.getDevice().getIsActive() ? OnOffType.ON : OnOffType.OFF);
        setChannelValue(CHANNEL_DEVICENAME, new StringType(playerInfo.getDevice().getName()));
        setChannelValue(CHANNEL_DEVICETYPE, new StringType(playerInfo.getDevice().getType()));

        // experienced situations where volume seemed to be undefined...
        if (playerInfo.getDevice().getVolumePercent() != null) {
            setChannelValue(CHANNEL_DEVICEVOLUME, new PercentType(playerInfo.getDevice().getVolumePercent()));
        }
    }

    public void setChannelValue(String CHANNEL, State state) {
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {

            if (isLinked(CHANNEL)) {
                Channel channel = getThing().getChannel(CHANNEL);

                logger.trace("Updating status of spotify device {} channel {}.", getThing().getLabel(),
                        channel.getUID());
                updateState(channel.getUID(), state);
            }
        }
    }

    public void initializeSession(String clientId, String clientSecret, String refreshToken) {

        Configuration configuration = editConfiguration();
        configuration.put("clientId", clientId);
        configuration.put("clientSecret", clientSecret);
        configuration.put("refreshToken", refreshToken);
        updateConfiguration(configuration);

        SpotifySession newSession = SpotifySession.getInstance(this, clientId, clientSecret, refreshToken);
        setSpotifySession(newSession);

    }
}
