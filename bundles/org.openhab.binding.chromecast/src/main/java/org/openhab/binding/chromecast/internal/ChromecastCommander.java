/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.chromecast.internal;

import static org.openhab.binding.chromecast.internal.ChromecastBindingConstants.*;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;

import java.io.IOException;

import org.digitalmediaserver.cast.CastDevice;
import org.digitalmediaserver.cast.Session;
import org.digitalmediaserver.cast.message.entity.Application;
import org.digitalmediaserver.cast.message.entity.Media.MediaBuilder;
import org.digitalmediaserver.cast.message.entity.MediaStatus;
import org.digitalmediaserver.cast.message.entity.ReceiverStatus;
import org.digitalmediaserver.cast.message.enumeration.IdleReason;
import org.digitalmediaserver.cast.message.enumeration.PlayerState;
import org.digitalmediaserver.cast.message.enumeration.StreamType;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sends the various commands to the Chromecast.
 *
 * @author Jason Holmes - Initial contribution
 */
@NonNullByDefault
public class ChromecastCommander {
    private final Logger logger = LoggerFactory.getLogger(ChromecastCommander.class);

    private final CastDevice chromeCast;
    private final ChromecastScheduler scheduler;
    private final ChromecastStatusUpdater statusUpdater;

    private static final int VOLUMESTEP = 10;

    private static final String SOURCE = "openHAB";

    public ChromecastCommander(CastDevice chromeCast, ChromecastScheduler scheduler,
            ChromecastStatusUpdater statusUpdater) {
        this.chromeCast = chromeCast;
        this.scheduler = scheduler;
        this.statusUpdater = statusUpdater;
    }

    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            scheduler.scheduleRefresh();
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_APP_ID:
                if (command instanceof StringType) {
                    startApp(command.toString());
                }
                break;
            case CHANNEL_CONTROL:
                handleControl(command);
                break;
            case CHANNEL_STOP:
                handleCloseApp(command);
                break;
            case CHANNEL_VOLUME:
                handleVolume(command);
                break;
            case CHANNEL_MUTE:
                handleMute(command);
                break;
            case CHANNEL_PLAY_URI:
                handlePlayUri(command);
                break;
            default:
                logger.debug("Received command {} for unknown channel: {}", command, channelUID);
                break;
        }
    }

    public void handleRefresh() {
        if (!chromeCast.isConnected()) {
            scheduler.cancelRefresh();
            scheduler.scheduleConnect();
            return;
        }

        ReceiverStatus status;
        try {
            status = chromeCast.getReceiverStatus();
            statusUpdater.processStatusUpdate(status);

            if (status == null) {
                scheduler.cancelRefresh();
            }
        } catch (IOException ex) {
            logger.debug("Failed to request status: {}", ex.getMessage());
            statusUpdater.updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR, ex.getMessage());
            scheduler.cancelRefresh();
            return;
        }

        try {
            if (status != null && status.getRunningApplication() instanceof Application application) {
                if (application.getTransportId() == null) {
                    logger.debug("Running app has no transportId, cannot request media status");
                    return;
                }

                Session session = chromeCast.startSession(SOURCE, application);
                MediaStatus mediaStatus = session.getMediaStatus();
                statusUpdater.updateMediaStatus(mediaStatus);

                if (mediaStatus != null && mediaStatus.getPlayerState() == PlayerState.IDLE
                        && mediaStatus.getIdleReason() != null
                        && mediaStatus.getIdleReason() != IdleReason.INTERRUPTED) {
                    closeApp(MEDIA_PLAYER);
                }
            }
        } catch (IOException ex) {
            logger.debug("Failed to request media status with a running app: {}", ex.getMessage());
            // We were just able to request status, so let's not put the device OFFLINE.
        }
    }

    public void handleCloseApp(final Command command) {
        if (command == OnOffType.ON) {
            Application app;
            try {
                app = chromeCast.getRunningApplication();
            } catch (final IOException e) {
                logger.info("{} command failed: {}", command, e.getMessage());
                statusUpdater.updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR, e.getMessage());
                return;
            }

            if (app != null) {
                closeApp(app.getAppId());
            }
        }
    }

    private void handlePlayUri(Command command) {
        if (command instanceof StringType) {
            playMedia(null, command.toString(), null);
        }
    }

    private void handleControl(final Command command) {
        try {
            Application app = chromeCast.getRunningApplication();
            statusUpdater.updateStatus(ThingStatus.ONLINE);
            if (app == null) {
                logger.debug("{} command ignored because media player app is not running", command);
                return;
            }

            Session session = chromeCast.startSession(SOURCE, app);
            MediaStatus mediaStatus = session.getMediaStatus();
            logger.debug("mediaStatus {}", mediaStatus);
            int mediaSessionId = -1;
            if (mediaStatus != null) {
                mediaSessionId = mediaStatus.getMediaSessionId();
            }

            if (command instanceof PlayPauseType playPauseCommand) {
                if (mediaStatus == null || mediaStatus.getPlayerState() == PlayerState.IDLE) {
                    logger.debug("{} command ignored because media is not loaded", command);
                    return;
                }
                if (playPauseCommand == PlayPauseType.PLAY) {
                    session.play(mediaSessionId, false);
                } else if (playPauseCommand == PlayPauseType.PAUSE
                        && ((mediaStatus.getSupportedMediaCommands() & 0x00000001) == 0x1)) {
                    session.pause(mediaSessionId, false);
                } else {
                    logger.warn("{} command not supported by current media", command);
                }
            }

            if (command instanceof NextPreviousType) {
                // Next is implemented by seeking to the end of the current media
                if (command == NextPreviousType.NEXT) {
                    Double duration = statusUpdater.getLastDuration();
                    if (duration != null) {
                        session.seek(mediaSessionId, (duration.doubleValue() - 5), null, false);
                    } else {
                        logger.info("{} command failed - unknown media duration", command);
                    }
                } else {
                    logger.info("{} command not yet implemented", command);
                    return;
                }
            }

        } catch (final IOException e) {
            logger.debug("{} command failed: {}", command, e.getMessage());
            statusUpdater.updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void handleVolume(final Command command) {
        if (command instanceof PercentType percentCommand) {
            setVolumeInternal(percentCommand);
        } else if (command == IncreaseDecreaseType.INCREASE) {
            setVolumeInternal(new PercentType(
                    Math.min(statusUpdater.getVolume().intValue() + VOLUMESTEP, PercentType.HUNDRED.intValue())));
        } else if (command == IncreaseDecreaseType.DECREASE) {
            setVolumeInternal(new PercentType(
                    Math.max(statusUpdater.getVolume().intValue() - VOLUMESTEP, PercentType.ZERO.intValue())));
        }
    }

    private void setVolumeInternal(PercentType volume) {
        try {
            chromeCast.setVolumeLevel(volume.floatValue() / 100);
            statusUpdater.updateStatus(ThingStatus.ONLINE);
        } catch (final IOException ex) {
            logger.debug("Set volume failed: {}", ex.getMessage());
            statusUpdater.updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR, ex.getMessage());
        }
    }

    private void handleMute(final Command command) {
        if (command instanceof OnOffType) {
            final boolean mute = command == OnOffType.ON;
            try {
                chromeCast.setMuteState(mute);
                statusUpdater.updateStatus(ThingStatus.ONLINE);
            } catch (final IOException ex) {
                logger.debug("Mute/unmute volume failed: {}", ex.getMessage());
                statusUpdater.updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR, ex.getMessage());
            }
        }
    }

    public void startApp(@Nullable String appId) {
        if (appId == null) {
            return;
        }
        try {
            if (chromeCast.isApplicationAvailable(appId)) {
                if (!chromeCast.isApplicationRunning(appId)) {
                    final ReceiverStatus receiverStatus = chromeCast.launchApplication(appId, true);
                    statusUpdater.setAppSessionId(receiverStatus.getRunningApplication().getSessionId());
                    logger.debug("Application launched: {}", appId);
                }
            } else {
                logger.warn("Failed starting app, app probably not installed. Appid: {}", appId);
            }
            statusUpdater.updateStatus(ThingStatus.ONLINE);
        } catch (final IOException e) {
            logger.warn("Failed starting app: {}. Message: {}", appId, e.getMessage());
        }
    }

    public void closeApp(@Nullable String appId) {
        if (appId == null) {
            return;
        }

        try {
            if (chromeCast.isApplicationAvailable(appId)) {
                Application app = chromeCast.getRunningApplication();
                if (app.getAppId().equals(MEDIA_PLAYER) && app.getSessionId().equals(statusUpdater.getAppSessionId())) {
                    chromeCast.stopApplication(app, false);
                    logger.debug("Media player app stopped");
                }
            }
        } catch (final IOException e) {
            logger.debug("Failed stopping app: {} with message: {}", appId, e.getMessage());
        }
    }

    public void playMedia(@Nullable String title, @Nullable String url, @Nullable String mimeType) {
        startApp(MEDIA_PLAYER);
        try {
            if (url != null && chromeCast.isApplicationRunning(MEDIA_PLAYER)) {
                // If the current track is paused, launching a new request results in nothing happening, therefore
                // resume current track.
                Session session = chromeCast.startSession(SOURCE, chromeCast.getRunningApplication());
                MediaStatus ms = session.getMediaStatus();
                if (ms != null && PlayerState.PAUSED == ms.getPlayerState()
                        && url.equals(ms.getMedia().getUrl())) {
                    logger.debug("Current stream paused, resuming");
                    session.play(ms.getMediaSessionId(), false);
                } else {
                    MediaBuilder builder = new MediaBuilder(url, mimeType, StreamType.NONE);
                    session.load(builder, true, 0.0, false);
                }
            } else {
                logger.warn("Missing media player app - cannot process media.");
            }
            statusUpdater.updateStatus(ThingStatus.ONLINE);
        } catch (final IOException e) {
            if ("Unable to load media".equals(e.getMessage())) {
                logger.warn("Unable to load media: {}", url);
            } else {
                logger.debug("Failed playing media: {}", e.getMessage());
                statusUpdater.updateStatus(ThingStatus.OFFLINE, COMMUNICATION_ERROR,
                        "IOException while trying to play media: " + e.getMessage());
            }
        }
    }

    public void dispose() {
        scheduler.destroy();
        if (chromeCast.isConnected()) {
            try {
                chromeCast.disconnect();
            } catch (IOException e) {
                logger.debug("Failed to disconnect from chromecast: {}", e.getMessage());
            }
        }
    }
}
