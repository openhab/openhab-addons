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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.AudioSourceStatus;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AudioSourceProperties;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link AudioSourceHandler} defines some methods that are used to
 * interface with an OmniLink Audio Source. This by extension also defines the
 * Audio Source thing that openHAB will be able to pick up and interface with.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class AudioSourceHandler extends AbstractOmnilinkHandler {
    private final Logger logger = LoggerFactory.getLogger(AudioSourceHandler.class);
    private final int pollDelaySeconds = 5;
    private final int thingID = getThingNumber();
    private @Nullable ScheduledFuture<?> scheduledPolling = null;
    public @Nullable String number;

    public AudioSourceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateStatus(ThingStatus.ONLINE);
            if (((Boolean) getThing().getConfiguration().get(THING_PROPERTIES_AUTOSTART)).booleanValue()) {
                logger.debug("Autostart enabled, scheduling polling for Audio Source: {}", thingID);
                schedulePolling();
            } else {
                logger.debug("Autostart disabled, not scheduling polling for Audio Source: {}", thingID);
                cancelPolling();
            }
            updateAudioSourceProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Audio Source!");
        }
    }

    private void updateAudioSourceProperties(OmnilinkBridgeHandler bridgeHandler) {
        ObjectPropertyRequest<AudioSourceProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.AUDIO_SOURCE, thingID, 0).selectNamed().build();

        for (AudioSourceProperties audioSourceProperties : objectPropertyRequest) {
            updateProperty(THING_PROPERTIES_NAME, audioSourceProperties.getName());
        }
    }

    @Override
    public synchronized void dispose() {
        cancelPolling();
        super.dispose();
    }

    private synchronized void cancelPolling() {
        final ScheduledFuture<?> scheduledPolling = this.scheduledPolling;
        if (scheduledPolling != null) {
            logger.debug("Cancelling polling for Audio Source: {}", thingID);
            scheduledPolling.cancel(false);
        }
    }

    private synchronized void schedulePolling() {
        cancelPolling();
        logger.debug("Scheduling polling for Audio Source: {}", thingID);
        scheduledPolling = super.scheduler.scheduleWithFixedDelay(this::pollAudioSource, 0, pollDelaySeconds,
                TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);
        final ScheduledFuture<?> scheduledPolling = this.scheduledPolling;

        switch (channelUID.getId()) {
            case CHANNEL_AUDIO_SOURCE_POLLING:
                if (command instanceof RefreshType) {
                    updateState(CHANNEL_AUDIO_SOURCE_POLLING,
                            OnOffType.from((scheduledPolling != null && !scheduledPolling.isDone())));
                } else if (command instanceof OnOffType) {
                    handlePolling(channelUID, (OnOffType) command);
                } else {
                    logger.debug("Invalid command: {}, must be RefreshType or OnOffType", command);
                }
                break;
            default:
                logger.warn("Unknown channel for Audio Source thing: {}", channelUID);
        }
    }

    private void handlePolling(ChannelUID channelUID, OnOffType command) {
        logger.debug("handlePolling called for channel: {}, command: {}", channelUID, command);
        if (OnOffType.ON.equals(command)) {
            schedulePolling();
        } else {
            cancelPolling();
        }
    }

    public void pollAudioSource() {
        try {
            final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
            if (bridge != null) {
                Message message;
                int position = 0;
                while ((message = bridge.requestAudioSourceStatus(thingID, position))
                        .getMessageType() == Message.MESG_TYPE_AUDIO_SOURCE_STATUS) {
                    logger.trace("Polling for Audio Source statuses on thing: {}", thingID);
                    AudioSourceStatus audioSourceStatus = (AudioSourceStatus) message;
                    position = audioSourceStatus.getPosition();
                    switch (position) {
                        case 1:
                            updateState(CHANNEL_AUDIO_SOURCE_TEXT1, new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 2:
                            updateState(CHANNEL_AUDIO_SOURCE_TEXT2, new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 3:
                            updateState(CHANNEL_AUDIO_SOURCE_TEXT3, new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 4:
                            updateState(CHANNEL_AUDIO_SOURCE_TEXT4, new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 5:
                            updateState(CHANNEL_AUDIO_SOURCE_TEXT5, new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 6:
                            updateState(CHANNEL_AUDIO_SOURCE_TEXT6, new StringType(audioSourceStatus.getSourceData()));
                            break;
                    }
                }
            } else {
                logger.debug("Received null bridge while polling Audio Source statuses!");
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Exception recieved while polling for Audio Source statuses: {}", e.getMessage());
        }
    }
}
