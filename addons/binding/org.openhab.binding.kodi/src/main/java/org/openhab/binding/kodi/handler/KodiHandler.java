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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.kodi.internal.KodiEventListener;
import org.openhab.binding.kodi.internal.config.KodiChannelConfig;
import org.openhab.binding.kodi.internal.config.KodiConfig;
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
 *
 */
public class KodiHandler extends BaseThingHandler implements KodiEventListener {

    private final Logger logger = LoggerFactory.getLogger(KodiHandler.class);

    private final KodiConnection connection;

    private ScheduledFuture<?> connectionCheckerFuture;

    private ScheduledFuture<?> statusUpdaterFuture;

    public KodiHandler(@NonNull Thing thing) {
        super(thing);
        connection = new KodiConnection(this);
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
                } else if (command.equals(RefreshType.REFRESH)) {
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
                } else if (command.equals(RefreshType.REFRESH)) {
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
                } else if (command.equals(RefreshType.REFRESH)) {
                    connection.updatePlayerStatus();
                }
                break;
            case CHANNEL_STOP:
                if (command.equals(OnOffType.ON)) {
                    connection.playerStop();
                } else if (command.equals(RefreshType.REFRESH)) {
                    connection.updatePlayerStatus();
                }
                break;
            case CHANNEL_PLAYURI:
                if (command instanceof StringType) {
                    playURI(command);
                    updateState(CHANNEL_PLAYURI, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_PLAYURI, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PVR_OPEN_TV:
                if (command instanceof StringType) {
                    KodiChannelConfig config = getThing().getChannel(channelUID.getId()).getConfiguration()
                            .as(KodiChannelConfig.class);
                    playPVRChannel(command, "tv", config);
                    updateState(CHANNEL_PVR_OPEN_TV, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_PVR_OPEN_TV, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_PVR_OPEN_RADIO:
                if (command instanceof StringType) {
                    KodiChannelConfig config = getThing().getChannel(channelUID.getId()).getConfiguration()
                            .as(KodiChannelConfig.class);
                    playPVRChannel(command, "radio", config);
                    updateState(CHANNEL_PVR_OPEN_RADIO, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_PVR_OPEN_RADIO, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_SHOWNOTIFICATION:
                if (command instanceof StringType) {
                    connection.showNotification(command.toString());
                    updateState(CHANNEL_SHOWNOTIFICATION, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_SHOWNOTIFICATION, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_INPUT:
                if (command instanceof StringType) {
                    connection.input(command.toString());
                    updateState(CHANNEL_INPUT, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_INPUT, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_INPUTTEXT:
                if (command instanceof StringType) {
                    connection.inputText(command.toString());
                    updateState(CHANNEL_INPUTTEXT, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    updateState(CHANNEL_INPUTTEXT, UnDefType.UNDEF);
                }
                break;
            case CHANNEL_SYSTEMCOMMAND:
                if (command instanceof StringType) {
                    connection.sendSystemCommand(command.toString());
                    updateState(CHANNEL_SYSTEMCOMMAND, UnDefType.UNDEF);
                } else if (command.equals(RefreshType.REFRESH)) {
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
                if (command.equals(RefreshType.REFRESH)) {
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

    public void playURI(Command command) {
        connection.playURI(command.toString());
    }

    public void playPVRChannel(final Command command, final String channelType, final KodiChannelConfig config) {
        int channelGroupID = connection.getChannelGroupID(channelType, config.getGroup());
        if (channelGroupID <= 0) {
            logger.warn("Received unknown PVR channel group {}. Using default.", config.getGroup());
            channelGroupID = (channelType == "tv") ? 1 : 2;
        }
        int channelID = connection.getChannelID(channelGroupID, command.toString());
        if (channelID > 0) {
            connection.playPVRChannel(channelID);
        } else {
            logger.debug("Received unknown PVR channel {}", command.toString());
        }
    }

    public void playNotificationSoundURI(Command command) {
        connection.playNotificationSoundURI(command.toString());
    }

    public PercentType getNotificationSoundVolume() {
        return new PercentType(connection.getVolume());
    }

    public void setNotificationSoundVolume(PercentType volume) {
        connection.setVolume(volume.intValue());
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
                    if (!connection.checkConnection()) {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }, 1, 10, TimeUnit.SECONDS);

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
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void updateScreenSaverState(boolean screenSaveActive) {
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
