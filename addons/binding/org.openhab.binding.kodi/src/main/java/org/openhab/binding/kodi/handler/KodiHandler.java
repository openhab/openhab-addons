/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.handler;

import static org.openhab.binding.kodi.KodiBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.kodi.internal.KodiDynamicStateDescriptionProvider;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiPlayerState;
import org.openhab.binding.kodi.internal.config.KodiChannelConfig;
import org.openhab.binding.kodi.internal.config.KodiConfig;
import org.openhab.binding.kodi.internal.model.KodiFavorite;
import org.openhab.binding.kodi.internal.model.KodiPVRChannel;
import org.openhab.binding.kodi.internal.protocol.KodiConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KodiHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Andreas Reinhardt & Christoph Weitkamp - Added channels for thumbnail and fanart
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public class KodiHandler extends BaseThingHandler implements KodiEventListener {

    private final Logger logger = LoggerFactory.getLogger(KodiHandler.class);

    private final KodiConnection connection;

    private ScheduledFuture<?> connectionCheckerFuture;

    private ScheduledFuture<?> statusUpdaterFuture;

    private final KodiDynamicStateDescriptionProvider stateDescriptionProvider;

    public KodiHandler(@NonNull Thing thing, KodiDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        connection = new KodiConnection(this);

        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (connectionCheckerFuture != null) {
            connectionCheckerFuture.cancel(true);
        }
        if (statusUpdaterFuture != null) {
            statusUpdaterFuture.cancel(true);
        }
        if (connection != null) {
            connection.close();
        }
    }

    private int getIntConfigParameter(String key, int defaultValue) {
        Object obj = this.getConfig().get(key);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt(obj.toString());
        }
        return defaultValue;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_MUTE:
                if (command.equals(OnOffType.ON)) {
                    connection.setMute(true);
                } else if (command.equals(OnOffType.OFF)) {
                    connection.setMute(false);
                } else if (RefreshType.REFRESH == command) {
                    connection.updateVolume();
                }
                break;
            case CHANNEL_VOLUME:
                if (command instanceof PercentType) {
                    connection.setVolume(((PercentType) command).intValue());
                } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    connection.increaseVolume();
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    connection.decreaseVolume();
                } else if (command.equals(OnOffType.OFF)) {
                    connection.setVolume(0);
                } else if (command.equals(OnOffType.ON)) {
                    connection.setVolume(100);
                } else if (RefreshType.REFRESH == command) {
                    connection.updateVolume();
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        connection.playerPlayPause();
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        connection.playerPlayPause();
                    }
                } else if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        connection.playerNext();
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        connection.playerPrevious();
                    }
                } else if (command instanceof RewindFastforwardType) {
                    if (command.equals(RewindFastforwardType.REWIND)) {
                        connection.playerRewind();
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        connection.playerFastForward();
                    }
                } else if (RefreshType.REFRESH == command) {
                    connection.updatePlayerStatus();
                }
                break;
            case CHANNEL_STOP:
                if (command.equals(OnOffType.ON)) {
                    stop();
                } else if (RefreshType.REFRESH == command) {
                    connection.updatePlayerStatus();
                }
                break;
            case CHANNEL_PLAYURI:
                if (command instanceof StringType) {
                    playURI(command);
                    updateState(CHANNEL_PLAYURI, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_PLAYURI, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PLAYNOTIFICATION:
                if (command instanceof StringType) {
                    playNotificationSoundURI((StringType) command);
                    updateState(CHANNEL_PLAYNOTIFICATION, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_PLAYNOTIFICATION, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PLAYFAVORITE:
                if (command instanceof StringType) {
                    playFavorite(command);
                    updateState(CHANNEL_PLAYFAVORITE, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_PLAYFAVORITE, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PVR_OPEN_TV:
                if (command instanceof StringType) {
                    playPVRChannel(command, PVR_TV, CHANNEL_PVR_OPEN_TV);
                    updateState(CHANNEL_PVR_OPEN_TV, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_PVR_OPEN_TV, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PVR_OPEN_RADIO:
                if (command instanceof StringType) {
                    playPVRChannel(command, PVR_RADIO, CHANNEL_PVR_OPEN_RADIO);
                    updateState(CHANNEL_PVR_OPEN_RADIO, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_PVR_OPEN_RADIO, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_SHOWNOTIFICATION:
                if (command instanceof StringType) {
                    connection.showNotification(command.toString());
                    updateState(CHANNEL_SHOWNOTIFICATION, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_SHOWNOTIFICATION, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_INPUT:
                if (command instanceof StringType) {
                    connection.input(command.toString());
                    updateState(CHANNEL_INPUT, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_INPUT, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_INPUTTEXT:
                if (command instanceof StringType) {
                    connection.inputText(command.toString());
                    updateState(CHANNEL_INPUTTEXT, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_INPUTTEXT, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_INPUTACTION:
                if (command instanceof StringType) {
                    connection.inputAction(command.toString());
                    updateState(CHANNEL_INPUTACTION, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_INPUTACTION, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_SYSTEMCOMMAND:
                if (command instanceof StringType) {
                    connection.sendSystemCommand(command.toString());
                    updateState(CHANNEL_SYSTEMCOMMAND, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_SYSTEMCOMMAND, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_ARTIST:
            case CHANNEL_ALBUM:
            case CHANNEL_TITLE:
            case CHANNEL_SHOWTITLE:
            case CHANNEL_MEDIATYPE:
            case CHANNEL_PVR_CHANNEL:
            case CHANNEL_THUMBNAIL:
            case CHANNEL_FANART:
                if (RefreshType.REFRESH == command) {
                    connection.updatePlayerStatus();
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelUID.getIdWithoutGroup());
                break;
        }
    }

    private URI getImageBaseUrl() throws URISyntaxException {
        KodiConfig config = getConfigAs(KodiConfig.class);
        String host = config.getIpAddress();
        int httpPort = config.getHttpPort();
        String httpUser = config.getHttpUser();
        String httpPassword = config.getHttpPassword();
        String userInfo = (StringUtils.isEmpty(httpUser) || StringUtils.isEmpty(httpPassword)) ? null
                : String.format("%s:%s", httpUser, httpPassword);
        return new URI("http", userInfo, host, httpPort, "/image/", null, null);
    }

    public void stop() {
        connection.playerStop();
    }

    public void playURI(Command command) {
        connection.playURI(command.toString());
    }

    private void playFavorite(Command command) {
        KodiFavorite favorite = connection.getFavorite(command.toString());
        if (favorite != null) {
            String path = favorite.getPath();
            String windowParameter = favorite.getWindowParameter();
            if (StringUtils.isNotEmpty(path)) {
                connection.playURI(path);
            } else if (StringUtils.isNotEmpty(windowParameter)) {
                String[] windowParameters = { windowParameter };
                connection.activateWindow(favorite.getWindow(), windowParameters);
            } else {
                connection.activateWindow(favorite.getWindow());
            }
        } else {
            logger.debug("Received unknown favorite '{}'.", command);
        }
    }

    public void playPVRChannel(final Command command, final String pvrChannelType, final String channelId) {
        int pvrChannelGroupId = getPVRChannelGroupId(pvrChannelType, channelId);
        int pvrChannelId = connection.getPVRChannelId(pvrChannelGroupId, command.toString());
        if (pvrChannelId > 0) {
            connection.playPVRChannel(pvrChannelId);
        } else {
            logger.debug("Received unknown PVR channel '{}'.", command);
        }
    }

    private int getPVRChannelGroupId(final String pvrChannelType, final String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel != null) {
            KodiChannelConfig config = channel.getConfiguration().as(KodiChannelConfig.class);
            String pvrChannelGroupName = config.getGroup();
            int pvrChannelGroupId = connection.getPVRChannelGroupId(pvrChannelType, pvrChannelGroupName);
            if (pvrChannelGroupId <= 0) {
                logger.debug("Received unknown PVR channel group '{}'. Using default.", pvrChannelGroupName);
                pvrChannelGroupId = PVR_TV.equals(pvrChannelType) ? 1 : 2;
            }
            return pvrChannelGroupId;
        }
        return 0;
    }

    /*
     * Play the notification by 1) saving the state of the player, 2) stopping the current
     * playlist item, 3) adding the notification as a new playlist item, 4) playing the new
     * playlist item, and 5) restoring the player to its previous state.
     */
    public void playNotificationSoundURI(StringType uri) {
        // save the current state of the player
        logger.trace("Saving current player state");
        KodiPlayerState playerState = new KodiPlayerState();
        playerState.setSavedVolume(connection.getVolume());
        playerState.setPlaylistID(connection.getActivePlaylist());
        playerState.setSavedState(connection.getState());

        int audioPlaylistID = connection.getPlaylistID("audio");
        int videoPlaylistID = connection.getPlaylistID("video");

        // pause playback
        if (KodiState.Play.equals(connection.getState())) {
            // pause if current media is "audio" or "video", stop otherwise
            if (audioPlaylistID == playerState.getSavedPlaylistID()
                    || videoPlaylistID == playerState.getSavedPlaylistID()) {
                connection.playerPlayPause();
                waitForState(KodiState.Pause);
            } else {
                connection.playerStop();
                waitForState(KodiState.Stop);
            }
        }

        // set notification sound volume
        logger.trace("Setting up player for notification");
        int notificationVolume = getNotificationSoundVolume().intValue();
        connection.setVolume(notificationVolume);
        waitForVolume(notificationVolume);

        // add the notification uri to the playlist and play it
        logger.trace("Playing notification");
        connection.playlistInsert(audioPlaylistID, uri.toString(), 0);
        waitForPlaylistState(KodiPlaylistState.ADDED);

        connection.playlistPlay(audioPlaylistID, 0);
        waitForState(KodiState.Play);
        // wait for stop if previous playlist wasn't "audio"
        if (audioPlaylistID != playerState.getSavedPlaylistID()) {
            waitForState(KodiState.Stop);
        }

        // remove the notification uri from the playlist
        connection.playlistRemove(audioPlaylistID, 0);
        waitForPlaylistState(KodiPlaylistState.REMOVED);

        // restore previous volume
        connection.setVolume(playerState.getSavedVolume());
        waitForVolume(playerState.getSavedVolume());

        // resume playing save playlist item if player wasn't stopped
        logger.trace("Restoring player state");
        switch (playerState.getSavedState()) {
            case Play:
                if (audioPlaylistID != playerState.getSavedPlaylistID() && -1 != playerState.getSavedPlaylistID()) {
                    connection.playlistPlay(playerState.getSavedPlaylistID(), 0);
                }
                break;
            case Pause:
                if (audioPlaylistID == playerState.getSavedPlaylistID()) {
                    connection.playerPlayPause();
                }
                break;
            case Stop:
            case End:
            case FastForward:
            case Rewind:
                // nothing to do
                break;
        }
    }

    /*
     * Wait for the volume status to equal the targetVolume
     */
    private boolean waitForVolume(int targetVolume) {
        int timeoutMaxCount = 20, timeoutCount = 0;
        logger.trace("Waiting up to {} ms for the volume to be updated ...", timeoutMaxCount * 100);
        while (targetVolume != connection.getVolume() && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        return checkForTimeout(timeoutCount, timeoutMaxCount, "volume to be updated");
    }

    /*
     * Wait for the player state so that we know when the notification has started or finished playing
     */
    private boolean waitForState(KodiState state) {
        int timeoutMaxCount = getConfigAs(KodiConfig.class).getNotificationTimeout().intValue(), timeoutCount = 0;
        logger.trace("Waiting up to {} ms for state '{}' to be set ...", timeoutMaxCount * 100, state);
        while (!state.equals(connection.getState()) && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        return checkForTimeout(timeoutCount, timeoutMaxCount, "state to '" + state.toString() + "' be set");
    }

    /*
     * Wait for the playlist state so that we know when the notification has started or finished playing
     */
    private boolean waitForPlaylistState(KodiPlaylistState playlistState) {
        int timeoutMaxCount = 20, timeoutCount = 0;
        logger.trace("Waiting up to {} ms for playlist state '{}' to be set ...", timeoutMaxCount * 100, playlistState);
        while (!playlistState.equals(connection.getPlaylistState()) && timeoutCount < timeoutMaxCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            timeoutCount++;
        }
        return checkForTimeout(timeoutCount, timeoutMaxCount,
                "playlist state to '" + playlistState.toString() + "' be set");
    }

    /*
     * Log timeout for wait
     */
    private boolean checkForTimeout(int timeoutCount, int timeoutLimit, String message) {
        if (timeoutCount >= timeoutLimit) {
            logger.debug("TIMEOUT after {} ms waiting for {}!", timeoutCount * 100, message);
            return false;
        } else {
            logger.trace("Done waiting {} ms for {}", timeoutCount * 100, message);
            return true;
        }
    }

    /**
     * Gets the current volume level
     */
    public PercentType getVolume() {
        return new PercentType(connection.getVolume());
    }

    /**
     * Sets the volume level
     *
     * @param volume Volume to be set
     */
    public void setVolume(PercentType volume) {
        if (volume != null) {
            connection.setVolume(volume.intValue());
        }
    }

    /**
     * Gets the volume level for a notification sound
     */
    public PercentType getNotificationSoundVolume() {
        Integer notificationSoundVolume = getConfigAs(KodiConfig.class).getNotificationVolume();
        if (notificationSoundVolume == null) {
            // if no value is set we use the current volume instead
            return new PercentType(connection.getVolume());
        }
        return new PercentType(notificationSoundVolume);
    }

    /**
     * Sets the volume level for a notification sound
     *
     * @param notificationSoundVolume Volume to be set
     */
    public void setNotificationSoundVolume(PercentType notificationSoundVolume) {
        if (notificationSoundVolume != null) {
            connection.setVolume(notificationSoundVolume.intValue());
        }
    }

    @Override
    public void initialize() {
        try {
            String host = getConfig().get(HOST_PARAMETER).toString();
            if (host == null || host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No network address specified");
            } else {
                connection.connect(host, getIntConfigParameter(WS_PORT_PARAMETER, 9090), scheduler, getImageBaseUrl());

                connectionCheckerFuture = scheduler.scheduleWithFixedDelay(() -> {
                    if (connection.checkConnection()) {
                        updateFavoriteChannelStateDescription();
                        updatePVRChannelStateDescription(PVR_TV, CHANNEL_PVR_OPEN_TV);
                        updatePVRChannelStateDescription(PVR_RADIO, CHANNEL_PVR_OPEN_RADIO);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection could not be established");
                    }
                }, 1, getIntConfigParameter(REFRESH_PARAMETER, 10), TimeUnit.SECONDS);

                statusUpdaterFuture = scheduler.scheduleWithFixedDelay(() -> {
                    if (KodiState.Play.equals(connection.getState())) {
                        connection.updatePlayerStatus();
                    }
                }, 1, getIntConfigParameter(REFRESH_PARAMETER, 10), TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.debug("error during opening connection: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private void updateFavoriteChannelStateDescription() {
        if (isLinked(CHANNEL_PLAYFAVORITE)) {
            List<StateOption> options = new ArrayList<>();
            for (KodiFavorite favorite : connection.getFavorites()) {
                options.add(new StateOption(favorite.getTitle(), favorite.getTitle()));
            }
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_PLAYFAVORITE),
                    options);
        }
    }

    private void updatePVRChannelStateDescription(final String pvrChannelType, final String channelId) {
        if (isLinked(channelId)) {
            int pvrChannelGroupId = getPVRChannelGroupId(pvrChannelType, channelId);
            List<StateOption> options = new ArrayList<>();
            for (KodiPVRChannel pvrChannel : connection.getPVRChannels(pvrChannelGroupId)) {
                options.add(new StateOption(pvrChannel.getLabel(), pvrChannel.getLabel()));
            }
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId), options);
        }
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
            try {
                String version = connection.getVersion();
                thing.setProperty(PROPERTY_VERSION, version);
            } catch (Exception e) {
                logger.debug("error during reading version: {}", e.getMessage(), e);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No connection established");
        }
    }

    @Override
    public void updateScreenSaverState(boolean screenSaveActive) {
    }

    @Override
    public void updatePlaylistState(KodiPlaylistState playlistState) {
    }

    @Override
    public void updateVolume(int volume) {
        updateState(CHANNEL_VOLUME, new PercentType(volume));
    }

    @Override
    public void updatePlayerState(KodiState state) {
        switch (state) {
            case Play:
                updateState(CHANNEL_CONTROL, PlayPauseType.PLAY);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case Pause:
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case Stop:
            case End:
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                updateState(CHANNEL_STOP, OnOffType.ON);
                break;
            case FastForward:
                updateState(CHANNEL_CONTROL, RewindFastforwardType.FASTFORWARD);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case Rewind:
                updateState(CHANNEL_CONTROL, RewindFastforwardType.REWIND);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
        }
    }

    @Override
    public void updateMuted(boolean muted) {
        if (muted) {
            updateState(CHANNEL_MUTE, OnOffType.ON);
        } else {
            updateState(CHANNEL_MUTE, OnOffType.OFF);
        }
    }

    @Override
    public void updateTitle(String title) {
        updateState(CHANNEL_TITLE, createState(title));
    }

    @Override
    public void updateShowTitle(String title) {
        updateState(CHANNEL_SHOWTITLE, createState(title));
    }

    @Override
    public void updateAlbum(String album) {
        updateState(CHANNEL_ALBUM, createState(album));
    }

    @Override
    public void updateArtist(String artist) {
        updateState(CHANNEL_ARTIST, createState(artist));
    }

    @Override
    public void updateMediaType(String mediaType) {
        updateState(CHANNEL_MEDIATYPE, createState(mediaType));
    }

    @Override
    public void updatePVRChannel(final String channel) {
        updateState(CHANNEL_PVR_CHANNEL, createState(channel));
    }

    @Override
    public void updateThumbnail(RawType thumbnail) {
        updateState(CHANNEL_THUMBNAIL, createImage(thumbnail));
    }

    @Override
    public void updateFanart(RawType fanart) {
        updateState(CHANNEL_FANART, createImage(fanart));
    }

    /**
     * Wrap the given String in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the String is empty.
     */
    private State createState(String string) {
        if (string == null || string.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return new StringType(string);
        }
    }

    /**
     * Wrap the given RawType and return it as {@link State} or return {@link UnDefType#UNDEF} if the RawType is null.
     */
    private State createImage(RawType image) {
        if (image == null) {
            return UnDefType.UNDEF;
        } else {
            return image;
        }
    }
}
