/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.handler;

import static org.openhab.binding.nest.internal.sdm.SDMBindingConstants.*;
import static org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageRequest.EVENT_IMAGE_VALIDITY;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.sdm.SDMBindingConstants;
import org.openhab.binding.nest.internal.sdm.api.SDMAPI;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraImageResults;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraRtspStreamRequest;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraRtspStreamResponse;
import org.openhab.binding.nest.internal.sdm.dto.SDMCommands.SDMGenerateCameraRtspStreamResults;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMDeviceEvent;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMResourceUpdate;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMResourceUpdateEvents;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingSDMDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAccessTokenException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDMCameraHandler} handles state updates of SDM devices with a camera.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMCameraHandler extends SDMBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(SDMCameraHandler.class);

    private @Nullable ZonedDateTime lastChimeEventTimestamp;
    private @Nullable ZonedDateTime lastMotionEventTimestamp;
    private @Nullable ZonedDateTime lastPersonEventTimestamp;
    private @Nullable ZonedDateTime lastSoundEventTimestamp;

    public SDMCameraHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    private void updateLiveStreamChannels() throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        boolean channelLinked = Stream.of(CHANNEL_LIVE_STREAM_CURRENT_TOKEN, CHANNEL_LIVE_STREAM_EXPIRATION_TIMESTAMP,
                CHANNEL_LIVE_STREAM_EXTENSION_TOKEN, CHANNEL_LIVE_STREAM_URL).anyMatch(this::isLinked);
        if (!channelLinked) {
            logger.debug("Not updating live stream channels (channels are not linked)");
            return;
        }

        logger.debug("Updating live stream channels");

        SDMGenerateCameraRtspStreamResponse response = executeDeviceCommand(new SDMGenerateCameraRtspStreamRequest());
        if (response == null) {
            logger.debug("Cannot update live stream channels (empty response)");
            return;
        }

        SDMGenerateCameraRtspStreamResults results = response.results;
        if (results != null) {
            updateState(CHANNEL_LIVE_STREAM_CURRENT_TOKEN, new StringType(results.streamToken));
            updateState(CHANNEL_LIVE_STREAM_EXPIRATION_TIMESTAMP,
                    new DateTimeType(results.expiresAt.withZoneSameInstant(timeZoneProvider.getTimeZone())));
            updateState(CHANNEL_LIVE_STREAM_EXTENSION_TOKEN, new StringType(results.streamExtensionToken));
            updateState(CHANNEL_LIVE_STREAM_URL, new StringType(results.streamUrls.rtspUrl));
        }
    }

    @Override
    public void onEvent(SDMEvent event) {
        super.onEvent(event);

        SDMResourceUpdate resourceUpdate = event.resourceUpdate;
        if (resourceUpdate == null) {
            logger.debug("Skipping event without resource update");
            return;
        }

        SDMResourceUpdateEvents events = resourceUpdate.events;
        if (events == null) {
            logger.debug("Skipping resource update without events");
            return;
        }

        try {
            SDMDeviceEvent deviceEvent = events.cameraMotionEvent;
            if (deviceEvent != null) {
                lastMotionEventTimestamp = updateImageChannelsForEvent(CHANNEL_MOTION_EVENT_TIMESTAMP,
                        CHANNEL_MOTION_EVENT_IMAGE, lastMotionEventTimestamp, event.timestamp, deviceEvent);
            }

            deviceEvent = events.cameraPersonEvent;
            if (deviceEvent != null) {
                lastPersonEventTimestamp = updateImageChannelsForEvent(CHANNEL_PERSON_EVENT_TIMESTAMP,
                        CHANNEL_PERSON_EVENT_IMAGE, lastPersonEventTimestamp, event.timestamp, deviceEvent);
            }

            deviceEvent = events.cameraSoundEvent;
            if (deviceEvent != null) {
                lastSoundEventTimestamp = updateImageChannelsForEvent(CHANNEL_SOUND_EVENT_TIMESTAMP,
                        CHANNEL_SOUND_EVENT_IMAGE, lastSoundEventTimestamp, event.timestamp, deviceEvent);
            }

            deviceEvent = events.doorbellChimeEvent;
            if (deviceEvent != null) {
                lastChimeEventTimestamp = updateImageChannelsForEvent(CHANNEL_CHIME_EVENT_TIMESTAMP,
                        CHANNEL_CHIME_EVENT_IMAGE, lastChimeEventTimestamp, event.timestamp, deviceEvent);
            }
        } catch (FailedSendingSDMDataException | InvalidSDMAccessTokenException e) {
            logger.warn("Handling SDM event failed for {}", thing.getUID(), e);
        }
    }

    private @Nullable ZonedDateTime updateImageChannelsForEvent(String timeChannelName, String imageChannelName,
            @Nullable ZonedDateTime lastEventTimestamp, ZonedDateTime eventTimestamp, SDMDeviceEvent event)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        boolean newerEvent = lastEventTimestamp == null || lastEventTimestamp.isBefore(eventTimestamp);
        if (!newerEvent) {
            logger.debug("Skipping {} channel update (more recent event already occurred)", imageChannelName);
            return lastEventTimestamp;
        }

        if (!isLinked(imageChannelName)) {
            logger.debug("Not downloading image for {} channel update (channel is not linked)", imageChannelName);
        } else if (Duration.between(eventTimestamp, ZonedDateTime.now()).compareTo(EVENT_IMAGE_VALIDITY) > 0) {
            logger.debug("Cannot download image for {} channel update (event image has expired)", imageChannelName);
            updateState(timeChannelName, UnDefType.NULL);
        } else {
            BigDecimal imageWidth = null;
            BigDecimal imageHeight = null;

            Channel channel = getThing().getChannel(imageChannelName);
            if (channel != null) {
                Configuration configuration = channel.getConfiguration();
                imageWidth = (BigDecimal) configuration.get(SDMBindingConstants.CONFIG_PROPERTY_IMAGE_WIDTH);
                imageHeight = (BigDecimal) configuration.get(SDMBindingConstants.CONFIG_PROPERTY_IMAGE_HEIGHT);
            }

            updateState(imageChannelName, getCameraImage(event.eventId, imageWidth, imageHeight));
        }

        updateState(timeChannelName,
                new DateTimeType(eventTimestamp.withZoneSameInstant(timeZoneProvider.getTimeZone())));

        logger.debug("Updated {} channel and {} with image of event at {}", imageChannelName, timeChannelName,
                eventTimestamp);

        updateLiveStreamChannels();

        return eventTimestamp;
    }

    private State getCameraImage(String eventId, @Nullable BigDecimal imageWidth, @Nullable BigDecimal imageHeight)
            throws FailedSendingSDMDataException, InvalidSDMAccessTokenException {
        SDMGenerateCameraImageResponse response = executeDeviceCommand(new SDMGenerateCameraImageRequest(eventId));
        if (response == null) {
            logger.debug("Cannot get image for camera event (empty response)");
            return UnDefType.NULL;
        }

        SDMGenerateCameraImageResults results = response.results;
        if (results == null) {
            logger.debug("Cannot get image for camera event (no results)");
            return UnDefType.NULL;
        }

        SDMAPI api = getAPI();
        if (api == null) {
            logger.debug("Cannot get image for camera event (handler has no bridge)");
            return UnDefType.NULL;
        }

        byte[] imageBytes = api.getCameraImage(results.url, results.token, imageWidth, imageHeight);
        return new RawType(imageBytes, "image/jpeg");
    }
}
