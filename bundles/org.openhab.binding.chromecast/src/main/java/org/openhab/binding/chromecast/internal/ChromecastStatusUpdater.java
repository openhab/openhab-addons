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

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.digitalmediaserver.cast.message.entity.Application;
import org.digitalmediaserver.cast.message.entity.Media;
import org.digitalmediaserver.cast.message.entity.MediaStatus;
import org.digitalmediaserver.cast.message.entity.ReceiverStatus;
import org.digitalmediaserver.cast.message.entity.Volume;
import org.digitalmediaserver.cast.message.enumeration.PlayerState;
import org.digitalmediaserver.cast.util.MetadataUtil.MetadataType;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chromecast.internal.handler.ChromecastHandler;
import org.openhab.core.cache.ByteArrayFileCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for updating the Thing status based on messages received from a CastDevice. This doesn't query anything -
 * it just parses the messages and updates the Thing. Message handling/scheduling/receiving is done elsewhere.
 * <p>
 * This also maintains state of both volume and the appSessionId (only if we started playing media).
 *
 * @author Jason Holmes - Initial contribution
 */
@NonNullByDefault
public class ChromecastStatusUpdater {

    private final Logger logger = LoggerFactory.getLogger(ChromecastStatusUpdater.class);

    private final Thing thing;
    private final ChromecastHandler callback;
    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache("org.openhab.binding.chromecast");

    private @Nullable String appSessionId;
    private PercentType volume = PercentType.ZERO;

    // Null is valid value for last duration
    private @Nullable Double lastDuration = null;

    public ChromecastStatusUpdater(Thing thing, ChromecastHandler callback) {
        this.thing = thing;
        this.callback = callback;
    }

    public PercentType getVolume() {
        return volume;
    }

    public @Nullable Double getLastDuration() {
        return lastDuration;
    }

    public @Nullable String getAppSessionId() {
        return appSessionId;
    }

    public void setAppSessionId(String appSessionId) {
        this.appSessionId = appSessionId;
    }

    public void processStatusUpdate(final @Nullable ReceiverStatus status) {
        if (status == null) {
            updateStatus(ThingStatus.OFFLINE);
            updateAppStatus(null);
            updateVolumeStatus(null);
            return;
        }

        if (status.getApplications() == null) {
            this.appSessionId = null;
        }

        updateStatus(ThingStatus.ONLINE);
        updateAppStatus(status.getRunningApplication());
        updateVolumeStatus(status.getVolume());
    }

    public void updateAppStatus(final @Nullable Application application) {
        State name = UnDefType.UNDEF;
        State id = UnDefType.UNDEF;
        State statusText = UnDefType.UNDEF;
        OnOffType idling = OnOffType.ON;

        if (application != null) {
            name = new StringType(application.getDisplayName());
            id = new StringType(application.getAppId());
            statusText = new StringType(application.getStatusText());
            idling = OnOffType.from(application.getIsIdleScreen());
        }

        callback.updateState(CHANNEL_APP_NAME, name);
        callback.updateState(CHANNEL_APP_ID, id);
        callback.updateState(CHANNEL_STATUS_TEXT, statusText);
        callback.updateState(CHANNEL_IDLING, idling);
    }

    public void updateVolumeStatus(final @Nullable Volume volume) {
        if (volume == null) {
            return;
        }

        PercentType value = new PercentType((int) (volume.getLevel() * 100));
        this.volume = value;

        callback.updateState(CHANNEL_VOLUME, value);
        callback.updateState(CHANNEL_MUTE, OnOffType.from(volume.getMuted()));
    }

    public void updateMediaStatus(final @Nullable MediaStatus mediaStatus) {
        logger.debug("MEDIA_STATUS {}", mediaStatus);

        // In-between songs? It's thinking? It's not doing anything
        if (mediaStatus == null) {
            callback.updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
            callback.updateState(CHANNEL_STOP, OnOffType.ON);
            callback.updateState(CHANNEL_CURRENT_TIME, UnDefType.UNDEF);
            updateMediaInfoStatus(null);
            return;
        }

        if (mediaStatus.getPlayerState() instanceof PlayerState mediaPlayerState) {
            switch (mediaPlayerState) {
                case IDLE:
                    break;
                case PAUSED:
                    callback.updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                    callback.updateState(CHANNEL_STOP, OnOffType.OFF);
                    break;
                case BUFFERING:
                case PLAYING:
                    callback.updateState(CHANNEL_CONTROL, PlayPauseType.PLAY);
                    callback.updateState(CHANNEL_STOP, OnOffType.OFF);
                    break;
                default:
                    logger.debug("Unknown media status: {}", mediaPlayerState);
                    break;
            }
        }

        callback.updateState(CHANNEL_CURRENT_TIME, new QuantityType<>(mediaStatus.getCurrentTime(), Units.SECOND));

        // If we're playing, paused or buffering but don't have any MEDIA information don't null everything out.
        Media media = mediaStatus.getMedia();
        if (media == null
                && (mediaStatus.getPlayerState() == null || mediaStatus.getPlayerState() == PlayerState.PLAYING
                        || mediaStatus.getPlayerState() == PlayerState.PAUSED
                        || mediaStatus.getPlayerState() == PlayerState.BUFFERING)) {
            return;
        }

        updateMediaInfoStatus(media);
    }

    private void updateMediaInfoStatus(final @Nullable Media media) {
        State duration = UnDefType.UNDEF;
        String metadataType = MetadataType.GENERIC.name();
        if (media != null) {
            metadataType = media.getMetadataType().name();

            lastDuration = media.getDuration();
            // duration can be null when a new song is about to play.
            if (media.getDuration() != null) {
                duration = new QuantityType<>(media.getDuration(), Units.SECOND);
            }
        }

        callback.updateState(CHANNEL_DURATION, duration);
        callback.updateState(CHANNEL_METADATA_TYPE, new StringType(metadataType));

        updateMetadataStatus(
                media == null || media.getMetadata() == null ? Collections.emptyMap() : media.getMetadata());
    }

    private void updateMetadataStatus(Map<String, Object> metadata) {
        updateLocation(metadata);
        updateImage(metadata);

        thing.getChannels().stream() //
                .map(channel -> channel.getUID())
                .filter(channelUID -> METADATA_SIMPLE_CHANNELS.contains(channelUID.getId()))
                .forEach(channelUID -> updateChannel(channelUID, metadata));
    }

    /** Lat/lon are combined into 1 channel so we have to handle them as a special case. */
    private void updateLocation(Map<String, Object> metadata) {
        if (!callback.isLinked(CHANNEL_LOCATION)) {
            return;
        }

        Double lat = (Double) metadata.get(LOCATION_METADATA_LATITUDE);
        Double lon = (Double) metadata.get(LOCATION_METADATA_LONGITUDE);
        if (lat == null || lon == null) {
            callback.updateState(CHANNEL_LOCATION, UnDefType.UNDEF);
        } else {
            PointType pointType = new PointType(new DecimalType(lat), new DecimalType(lon));
            callback.updateState(CHANNEL_LOCATION, pointType);
        }
    }

    private void updateImage(Map<String, Object> metadata) {
        if (!(callback.isLinked(CHANNEL_IMAGE) || (callback.isLinked(CHANNEL_IMAGE_SRC)))) {
            return;
        }

        // Channel name and metadata key don't match.
        Object imagesValue = metadata.get("images");
        if (imagesValue == null) {
            callback.updateState(CHANNEL_IMAGE_SRC, UnDefType.UNDEF);
            return;
        }

        String imageSrc = null;
        if (imagesValue instanceof List<?> imagesList) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> strings = (List<Map<String, String>>) imagesList;
            for (Map<String, String> stringMap : strings) {
                String url = stringMap.get("url");
                if (url != null) {
                    imageSrc = url;
                    break;
                }
            }
        }

        if (callback.isLinked(CHANNEL_IMAGE_SRC)) {
            callback.updateState(CHANNEL_IMAGE_SRC, imageSrc == null ? UnDefType.UNDEF : new StringType(imageSrc));
        }

        if (callback.isLinked(CHANNEL_IMAGE)) {
            State image = imageSrc == null ? UnDefType.UNDEF : downloadImageFromCache(imageSrc);
            callback.updateState(CHANNEL_IMAGE, image == null ? UnDefType.UNDEF : image);
        }
    }

    private @Nullable RawType downloadImage(String url) {
        logger.debug("Trying to download the content of URL '{}'", url);
        try {
            RawType downloadedImage = HttpUtil.downloadImage(url);
            if (downloadedImage == null) {
                logger.debug("Failed to download the content of URL '{}'", url);
            }
            return downloadedImage;
        } catch (IllegalArgumentException e) {
            // we catch this exception to avoid confusion errors in the log file
            // see https://github.com/openhab/openhab-core/issues/2494#issuecomment-970162025
        }
        return null;
    }

    private @Nullable RawType downloadImageFromCache(String url) {
        if (IMAGE_CACHE.containsKey(url)) {
            try {
                byte[] bytes = IMAGE_CACHE.get(url);
                String contentType = HttpUtil.guessContentTypeFromData(bytes);
                return new RawType(bytes,
                        contentType == null || contentType.isEmpty() ? RawType.DEFAULT_MIME_TYPE : contentType);
            } catch (IOException e) {
                logger.trace("Failed to download the content of URL '{}'", url, e);
            }
        } else {
            RawType image = downloadImage(url);
            if (image != null) {
                IMAGE_CACHE.put(url, image.getBytes());
                return image;
            }
        }
        return null;
    }

    private void updateChannel(ChannelUID channelUID, Map<String, Object> metadata) {
        if (!callback.isLinked(channelUID)) {
            return;
        }

        Object value = getValue(channelUID.getId(), metadata);
        State state;

        if (value == null) {
            state = UnDefType.UNDEF;
        } else if (value instanceof Double d) {
            state = new DecimalType(d);
        } else if (value instanceof Integer i) {
            state = new DecimalType(i.longValue());
        } else if (value instanceof String s) {
            state = new StringType(s);
        } else if (value instanceof ZonedDateTime datetime) {
            state = new DateTimeType(datetime);
        } else {
            state = UnDefType.UNDEF;
            logger.warn("Update channel {}: Unsupported value type {}", channelUID, value.getClass().getSimpleName());
        }

        callback.updateState(channelUID, state);
    }

    private @Nullable Object getValue(String channelId, @Nullable Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }

        if (CHANNEL_BROADCAST_DATE.equals(channelId) || CHANNEL_RELEASE_DATE.equals(channelId)
                || CHANNEL_CREATION_DATE.equals(channelId)) {
            Object dateObj = metadata.get(channelId);
            if (dateObj instanceof String dateString) {
                return ZonedDateTime.ofInstant(Instant.parse(dateString), ZoneId.systemDefault());
            } else {
                return null;
            }
        }

        return metadata.get(channelId);
    }

    public void updateStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE, null);
    }

    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        callback.updateStatus(status, statusDetail, description);
    }
}
