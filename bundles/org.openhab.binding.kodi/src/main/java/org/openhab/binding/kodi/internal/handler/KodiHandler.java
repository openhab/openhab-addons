/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.kodi.internal.handler;

import static org.openhab.binding.kodi.internal.KodiBindingConstants.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.kodi.internal.KodiDynamicCommandDescriptionProvider;
import org.openhab.binding.kodi.internal.KodiDynamicStateDescriptionProvider;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.KodiPlayerState;
import org.openhab.binding.kodi.internal.config.KodiChannelConfig;
import org.openhab.binding.kodi.internal.config.KodiConfig;
import org.openhab.binding.kodi.internal.model.KodiAudioStream;
import org.openhab.binding.kodi.internal.model.KodiFavorite;
import org.openhab.binding.kodi.internal.model.KodiPVRChannel;
import org.openhab.binding.kodi.internal.model.KodiProfile;
import org.openhab.binding.kodi.internal.model.KodiSubtitle;
import org.openhab.binding.kodi.internal.model.KodiSystemProperties;
import org.openhab.binding.kodi.internal.protocol.KodiConnection;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
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
 * @author Meng Yiqi - Added selection of audio and subtitle
 */
public class KodiHandler extends BaseThingHandler implements KodiEventListener {

    private static final String SYSTEM_COMMAND_HIBERNATE = "Hibernate";
    private static final String SYSTEM_COMMAND_REBOOT = "Reboot";
    private static final String SYSTEM_COMMAND_SHUTDOWN = "Shutdown";
    private static final String SYSTEM_COMMAND_SUSPEND = "Suspend";
    private static final String SYSTEM_COMMAND_QUIT = "Quit";

    private final Logger logger = LoggerFactory.getLogger(KodiHandler.class);

    private final KodiConnection connection;
    private final KodiDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final KodiDynamicStateDescriptionProvider stateDescriptionProvider;

    private final ChannelUID screenSaverChannelUID;
    private final ChannelUID inputRequestedChannelUID;
    private final ChannelUID volumeChannelUID;
    private final ChannelUID mutedChannelUID;
    private final ChannelUID favoriteChannelUID;
    private final ChannelUID profileChannelUID;

    private ScheduledFuture<?> connectionCheckerFuture;
    private ScheduledFuture<?> statusUpdaterFuture;

    public KodiHandler(Thing thing, KodiDynamicCommandDescriptionProvider commandDescriptionProvider,
            KodiDynamicStateDescriptionProvider stateDescriptionProvider, WebSocketClient webSocketClient,
            String callbackUrl) {
        super(thing);
        connection = new KodiConnection(this, webSocketClient, callbackUrl);

        this.commandDescriptionProvider = commandDescriptionProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;

        screenSaverChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_SCREENSAVER);
        inputRequestedChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_INPUTREQUESTED);
        volumeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_VOLUME);
        mutedChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_MUTE);
        favoriteChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_PLAYFAVORITE);
        profileChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_PROFILE);
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
            case CHANNEL_SCREENSAVER:
                if (RefreshType.REFRESH == command) {
                    connection.updateScreenSaverState();
                }
                break;
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
                    updateState(favoriteChannelUID, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(favoriteChannelUID, UnDefType.UNDEF);
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
                showNotification(channelUID, command);
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
            case CHANNEL_INPUTBUTTONEVENT:
                logger.debug("handleCommand CHANNEL_INPUTBUTTONEVENT {}.", command);
                if (command instanceof StringType) {
                    connection.inputButtonEvent(command.toString());
                    updateState(CHANNEL_INPUTBUTTONEVENT, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_INPUTBUTTONEVENT, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_SYSTEMCOMMAND:
                if (command instanceof StringType) {
                    handleSystemCommand(command.toString());
                    updateState(CHANNEL_SYSTEMCOMMAND, UnDefType.UNDEF);
                } else if (RefreshType.REFRESH == command) {
                    updateState(CHANNEL_SYSTEMCOMMAND, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PROFILE:
                if (command instanceof StringType) {
                    connection.profile(command.toString());
                } else if (RefreshType.REFRESH == command) {
                    connection.updateCurrentProfile();
                }
                break;
            case CHANNEL_ARTIST:
            case CHANNEL_ALBUM:
            case CHANNEL_TITLE:
            case CHANNEL_SHOWTITLE:
            case CHANNEL_MEDIATYPE:
            case CHANNEL_GENRELIST:
            case CHANNEL_PVR_CHANNEL:
            case CHANNEL_THUMBNAIL:
            case CHANNEL_FANART:
            case CHANNEL_AUDIO_CODEC:
                break;
            case CHANNEL_AUDIO_INDEX:
                if (command instanceof DecimalType) {
                    connection.setAudioStream(((DecimalType) command).intValue());
                }
                break;
            case CHANNEL_VIDEO_CODEC:
            case CHANNEL_VIDEO_INDEX:
                if (command instanceof DecimalType) {
                    connection.setVideoStream(((DecimalType) command).intValue());
                }
                break;
            case CHANNEL_SUBTITLE_ENABLED:
                if (command.equals(OnOffType.ON)) {
                    connection.setSubtitleEnabled(true);
                } else if (command.equals(OnOffType.OFF)) {
                    connection.setSubtitleEnabled(false);
                }
                break;
            case CHANNEL_SUBTITLE_INDEX:
                if (command instanceof DecimalType) {
                    connection.setSubtitle(((DecimalType) command).intValue());
                }
                break;
            case CHANNEL_CURRENTTIME:
                if (command instanceof QuantityType) {
                    connection.setTime(((QuantityType<?>) command).intValue());
                }
                break;
            case CHANNEL_CURRENTTIMEPERCENTAGE:
            case CHANNEL_DURATION:
                if (RefreshType.REFRESH == command) {
                    connection.updatePlayerStatus();
                }
                break;
            default:
                Channel channel = getThing().getChannel(channelUID);
                if (channel != null) {
                    ChannelTypeUID ctuid = channel.getChannelTypeUID();
                    if (ctuid != null) {
                        if (ctuid.getId().equals(CHANNEL_TYPE_SHOWNOTIFICATION)) {
                            showNotification(channelUID, command);
                            break;
                        }
                    }
                }
                logger.debug("Received unknown channel {}", channelUID.getIdWithoutGroup());
                break;
        }
    }

    private void showNotification(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            Channel channel = getThing().getChannel(channelUID);
            if (channel != null) {
                String title = (String) channel.getConfiguration().get(CHANNEL_TYPE_SHOWNOTIFICATION_PARAM_TITLE);
                BigDecimal displayTime = (BigDecimal) channel.getConfiguration()
                        .get(CHANNEL_TYPE_SHOWNOTIFICATION_PARAM_DISPLAYTIME);
                String icon = (String) channel.getConfiguration().get(CHANNEL_TYPE_SHOWNOTIFICATION_PARAM_ICON);
                connection.showNotification(title, displayTime, icon, command.toString());
            }
            updateState(channelUID, UnDefType.UNDEF);
        } else if (RefreshType.REFRESH == command) {
            updateState(channelUID, UnDefType.UNDEF);
        }
    }

    private URI getImageBaseUrl() throws URISyntaxException {
        KodiConfig config = getConfigAs(KodiConfig.class);
        String host = config.getIpAddress();
        int httpPort = config.getHttpPort();
        String httpUser = config.getHttpUser();
        String httpPassword = config.getHttpPassword();
        String userInfo = httpUser == null || httpUser.isEmpty() || httpPassword == null || httpPassword.isEmpty()
                ? null
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
            if (path != null && !path.isEmpty()) {
                connection.playURI(path);
            } else if (windowParameter != null && !windowParameter.isEmpty()) {
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

    private void handleSystemCommand(String command) {
        switch (command) {
            case SYSTEM_COMMAND_QUIT:
                connection.sendApplicationQuit();
                break;
            case SYSTEM_COMMAND_HIBERNATE:
            case SYSTEM_COMMAND_REBOOT:
            case SYSTEM_COMMAND_SHUTDOWN:
            case SYSTEM_COMMAND_SUSPEND:
                connection.sendSystemCommand(command);
                break;
            default:
                logger.debug("Received unknown system command '{}'.", command);
                break;
        }
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
        if (KodiState.PLAY.equals(connection.getState())) {
            // pause if current media is "audio" or "video", stop otherwise
            if (audioPlaylistID == playerState.getSavedPlaylistID()
                    || videoPlaylistID == playerState.getSavedPlaylistID()) {
                connection.playerPlayPause();
                waitForState(KodiState.PAUSE);
            } else {
                connection.playerStop();
                waitForState(KodiState.STOP);
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
        waitForState(KodiState.PLAY);
        // wait for stop if previous playlist wasn't "audio"
        if (audioPlaylistID != playerState.getSavedPlaylistID()) {
            waitForState(KodiState.STOP);
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
            case PLAY:
                if (audioPlaylistID != playerState.getSavedPlaylistID() && -1 != playerState.getSavedPlaylistID()) {
                    connection.playlistPlay(playerState.getSavedPlaylistID(), 0);
                }
                break;
            case PAUSE:
                if (audioPlaylistID == playerState.getSavedPlaylistID()) {
                    connection.playerPlayPause();
                }
                break;
            case STOP:
            case END:
            case FASTFORWARD:
            case REWIND:
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
                        updateProfileStateDescription();
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "No connection established");
                    }
                }, 1, getIntConfigParameter(REFRESH_PARAMETER, 10), TimeUnit.SECONDS);

                statusUpdaterFuture = scheduler.scheduleWithFixedDelay(() -> {
                    if (KodiState.PLAY.equals(connection.getState())) {
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
        if (isLinked(favoriteChannelUID)) {
            List<StateOption> options = new ArrayList<>();
            for (KodiFavorite favorite : connection.getFavorites()) {
                options.add(new StateOption(favorite.getTitle(), favorite.getTitle()));
            }
            stateDescriptionProvider.setStateOptions(favoriteChannelUID, options);
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

    private void updateProfileStateDescription() {
        if (isLinked(profileChannelUID)) {
            List<StateOption> options = new ArrayList<>();
            for (KodiProfile profile : connection.getProfiles()) {
                options.add(new StateOption(profile.getLabel(), profile.getLabel()));
            }
            stateDescriptionProvider.setStateOptions(profileChannelUID, options);
        }
    }

    @Override
    public void updateAudioStreamOptions(List<KodiAudioStream> audios) {
        if (isLinked(CHANNEL_AUDIO_INDEX)) {
            List<StateOption> options = new ArrayList<>();
            for (KodiAudioStream audio : audios) {
                options.add(new StateOption(Integer.toString(audio.getIndex()),
                        audio.getLanguage() + "  [" + audio.getName() + "] (" + audio.getCodec() + "-"
                                + Integer.toString(audio.getChannels()) + " "
                                + Integer.toString(audio.getBitrate() / 1000) + "kb/s)"));
            }
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_AUDIO_INDEX), options);
        }
    }

    @Override
    public void updateSubtitleOptions(List<KodiSubtitle> subtitles) {
        if (isLinked(CHANNEL_SUBTITLE_INDEX)) {
            List<StateOption> options = new ArrayList<>();
            for (KodiSubtitle subtitle : subtitles) {
                options.add(new StateOption(Integer.toString(subtitle.getIndex()),
                        subtitle.getLanguage() + "  [" + subtitle.getName() + "]"));
            }
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SUBTITLE_INDEX),
                    options);
        }
    }

    @Override
    public void updateConnectionState(boolean connected) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
            scheduler.schedule(() -> connection.getSystemProperties(), 1, TimeUnit.SECONDS);
            if (isLinked(volumeChannelUID) || isLinked(mutedChannelUID)) {
                scheduler.schedule(() -> connection.updateVolume(), 1, TimeUnit.SECONDS);
            }
            if (isLinked(profileChannelUID)) {
                scheduler.schedule(() -> connection.updateCurrentProfile(), 1, TimeUnit.SECONDS);
            }
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
    public void updateScreenSaverState(boolean screenSaverActive) {
        updateState(screenSaverChannelUID, OnOffType.from(screenSaverActive));
    }

    @Override
    public void updateInputRequestedState(boolean inputRequested) {
        updateState(inputRequestedChannelUID, OnOffType.from(inputRequested));
    }

    @Override
    public void updatePlaylistState(KodiPlaylistState playlistState) {
    }

    @Override
    public void updateVolume(int volume) {
        updateState(volumeChannelUID, new PercentType(volume));
    }

    @Override
    public void updatePlayerState(KodiState state) {
        switch (state) {
            case PLAY:
                updateState(CHANNEL_CONTROL, PlayPauseType.PLAY);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case PAUSE:
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case STOP:
            case END:
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                updateState(CHANNEL_STOP, OnOffType.ON);
                break;
            case FASTFORWARD:
                updateState(CHANNEL_CONTROL, RewindFastforwardType.FASTFORWARD);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
            case REWIND:
                updateState(CHANNEL_CONTROL, RewindFastforwardType.REWIND);
                updateState(CHANNEL_STOP, OnOffType.OFF);
                break;
        }
    }

    @Override
    public void updateMuted(boolean muted) {
        updateState(mutedChannelUID, OnOffType.from(muted));
    }

    @Override
    public void updateTitle(String title) {
        updateState(CHANNEL_TITLE, createStringState(title));
    }

    @Override
    public void updateOriginalTitle(String title) {
        updateState(CHANNEL_ORIGINALTITLE, createStringState(title));
    }

    @Override
    public void updateShowTitle(String title) {
        updateState(CHANNEL_SHOWTITLE, createStringState(title));
    }

    @Override
    public void updateAlbum(String album) {
        updateState(CHANNEL_ALBUM, createStringState(album));
    }

    @Override
    public void updateArtistList(List<String> artistList) {
        updateState(CHANNEL_ARTIST, createStringListState(artistList));
    }

    @Override
    public void updateMediaFile(String mediaFile) {
        updateState(CHANNEL_MEDIAFILE, createStringState(mediaFile));
    }

    @Override
    public void updateMediaType(String mediaType) {
        updateState(CHANNEL_MEDIATYPE, createStringState(mediaType));
    }

    @Override
    public void updateMediaID(int mediaid) {
        updateState(CHANNEL_MEDIAID, new DecimalType(mediaid));
    }

    @Override
    public void updateRating(double rating) {
        updateState(CHANNEL_RATING, new DecimalType(rating));
    }

    @Override
    public void updateUserRating(double rating) {
        updateState(CHANNEL_USERRATING, new DecimalType(rating));
    }

    @Override
    public void updateMpaa(String mpaa) {
        updateState(CHANNEL_MPAA, createStringState(mpaa));
    }

    @Override
    public void updateUniqueIDDouban(String uniqueid) {
        updateState(CHANNEL_UNIQUEID_DOUBAN, createStringState(uniqueid));
    }

    @Override
    public void updateUniqueIDImdb(String uniqueid) {
        updateState(CHANNEL_UNIQUEID_IMDB, createStringState(uniqueid));
    }

    @Override
    public void updateUniqueIDTmdb(String uniqueid) {
        updateState(CHANNEL_UNIQUEID_TMDB, createStringState(uniqueid));
    }

    @Override
    public void updateUniqueIDImdbtvshow(String uniqueid) {
        updateState(CHANNEL_UNIQUEID_IMDBTVSHOW, createStringState(uniqueid));
    }

    @Override
    public void updateUniqueIDTmdbtvshow(String uniqueid) {
        updateState(CHANNEL_UNIQUEID_TMDBTVSHOW, createStringState(uniqueid));
    }

    @Override
    public void updateUniqueIDTmdbepisode(String uniqueid) {
        updateState(CHANNEL_UNIQUEID_TMDBEPISODE, createStringState(uniqueid));
    }

    @Override
    public void updateSeason(int season) {
        updateState(CHANNEL_SEASON, new DecimalType(season));
    }

    @Override
    public void updateEpisode(int episode) {
        updateState(CHANNEL_EPISODE, new DecimalType(episode));
    }

    @Override
    public void updateGenreList(List<String> genreList) {
        updateState(CHANNEL_GENRELIST, createStringListState(genreList));
    }

    @Override
    public void updatePVRChannel(String channel) {
        updateState(CHANNEL_PVR_CHANNEL, createStringState(channel));
    }

    @Override
    public void updateThumbnail(RawType thumbnail) {
        updateState(CHANNEL_THUMBNAIL, createImageState(thumbnail));
    }

    @Override
    public void updateFanart(RawType fanart) {
        updateState(CHANNEL_FANART, createImageState(fanart));
    }

    @Override
    public void updateAudioCodec(String codec) {
        updateState(CHANNEL_AUDIO_CODEC, createStringState(codec));
    }

    @Override
    public void updateAudioIndex(int index) {
        updateState(CHANNEL_AUDIO_INDEX, new DecimalType(index));
    }

    @Override
    public void updateAudioChannels(int channels) {
        updateState(CHANNEL_AUDIO_CHANNELS, new DecimalType(channels));
    }

    @Override
    public void updateAudioLanguage(String language) {
        updateState(CHANNEL_AUDIO_LANGUAGE, createStringState(language));
    }

    @Override
    public void updateAudioName(String name) {
        updateState(CHANNEL_AUDIO_NAME, createStringState(name));
    }

    @Override
    public void updateVideoCodec(String codec) {
        updateState(CHANNEL_VIDEO_CODEC, createStringState(codec));
    }

    @Override
    public void updateVideoIndex(int index) {
        updateState(CHANNEL_VIDEO_INDEX, new DecimalType(index));
    }

    @Override
    public void updateVideoHeight(int height) {
        updateState(CHANNEL_VIDEO_HEIGHT, new DecimalType(height));
    }

    @Override
    public void updateVideoWidth(int width) {
        updateState(CHANNEL_VIDEO_WIDTH, new DecimalType(width));
    }

    @Override
    public void updateSubtitleEnabled(boolean enabled) {
        updateState(CHANNEL_SUBTITLE_ENABLED, OnOffType.from(enabled));
    }

    @Override
    public void updateSubtitleIndex(int index) {
        updateState(CHANNEL_SUBTITLE_INDEX, new DecimalType(index));
    }

    @Override
    public void updateSubtitleLanguage(String language) {
        updateState(CHANNEL_SUBTITLE_LANGUAGE, createStringState(language));
    }

    @Override
    public void updateSubtitleName(String name) {
        updateState(CHANNEL_SUBTITLE_NAME, createStringState(name));
    }

    @Override
    public void updateCurrentTime(long currentTime) {
        updateState(CHANNEL_CURRENTTIME, createQuantityState(currentTime, Units.SECOND));
    }

    @Override
    public void updateCurrentTimePercentage(double currentTimePercentage) {
        updateState(CHANNEL_CURRENTTIMEPERCENTAGE, createQuantityState(currentTimePercentage, Units.PERCENT));
    }

    @Override
    public void updateDuration(long duration) {
        updateState(CHANNEL_DURATION, createQuantityState(duration, Units.SECOND));
    }

    @Override
    public void updateCurrentProfile(String profile) {
        updateState(profileChannelUID, new StringType(profile));
    }

    @Override
    public void updateSystemProperties(KodiSystemProperties systemProperties) {
        if (systemProperties != null) {
            List<CommandOption> options = new ArrayList<>();
            if (systemProperties.canHibernate()) {
                options.add(new CommandOption(SYSTEM_COMMAND_HIBERNATE, SYSTEM_COMMAND_HIBERNATE));
            }
            if (systemProperties.canReboot()) {
                options.add(new CommandOption(SYSTEM_COMMAND_REBOOT, SYSTEM_COMMAND_REBOOT));
            }
            if (systemProperties.canShutdown()) {
                options.add(new CommandOption(SYSTEM_COMMAND_SHUTDOWN, SYSTEM_COMMAND_SHUTDOWN));
            }
            if (systemProperties.canSuspend()) {
                options.add(new CommandOption(SYSTEM_COMMAND_SUSPEND, SYSTEM_COMMAND_SUSPEND));
            }
            if (systemProperties.canQuit()) {
                options.add(new CommandOption(SYSTEM_COMMAND_QUIT, SYSTEM_COMMAND_QUIT));
            }
            commandDescriptionProvider.setCommandOptions(new ChannelUID(getThing().getUID(), CHANNEL_SYSTEMCOMMAND),
                    options);
        }
    }

    /**
     * Wrap the given String in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the String is empty.
     */
    private State createStringState(String string) {
        if (string == null || string.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return new StringType(string);
        }
    }

    /**
     * Wrap the given list of Strings in a new {@link StringType} or returns {@link UnDefType#UNDEF} if the list of
     * Strings is empty.
     */
    private State createStringListState(List<String> list) {
        if (list == null || list.isEmpty()) {
            return UnDefType.UNDEF;
        } else {
            return createStringState(list.stream().collect(Collectors.joining(", ")));
        }
    }

    /**
     * Wrap the given RawType and return it as {@link State} or return {@link UnDefType#UNDEF} if the RawType is null.
     */
    private State createImageState(@Nullable RawType image) {
        if (image == null) {
            return UnDefType.UNDEF;
        } else {
            return image;
        }
    }

    private State createQuantityState(Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }
}
