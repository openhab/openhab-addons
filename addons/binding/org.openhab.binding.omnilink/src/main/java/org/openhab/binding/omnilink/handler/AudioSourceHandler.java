/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.AudioSourceStatus;

public class AudioSourceHandler extends AbstractOmnilinkHandler {

    private final static Logger logger = LoggerFactory.getLogger(AudioSourceHandler.class);

    private final static long POLL_DELAY = 5; // 5 Second polling

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> scheduledPolling = null;

    public static synchronized void shutdownExecutor() {
        logger.debug("Shutting down audio polling executor service");
        executorService.shutdownNow();
    }

    private static synchronized void startExecutor() {
        if (executorService.isShutdown()) {
            logger.debug("Starting audio polling executor service");
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
    }

    public AudioSourceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (scheduledPolling != null) {
            scheduledPolling.cancel(false);
        }

        boolean autoStart = ((Boolean) getThing().getConfiguration()
                .get(OmnilinkBindingConstants.THING_PROPERTIES_AUTO_START)).booleanValue();
        int sourceNumber = getThingNumber();

        if (autoStart) {
            logger.debug("Autostart enabled, scheduling polling for Audio Source {}", sourceNumber);
            schedulePolling();
        } else {
            logger.debug("Autostart disabled, not scheduling polling for Audio Source {}", sourceNumber);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public synchronized void dispose() {
        cancelPolling();
        super.dispose();
    }

    private synchronized void cancelPolling() {
        if (scheduledPolling != null) {
            if (scheduledPolling.isDone() == false) {
                int sourceNumber = getThingNumber();
                logger.debug("Cancelling polling for Audio Source {}", sourceNumber);
                scheduledPolling.cancel(false);
            }
        }
    }

    private synchronized void schedulePolling() {
        cancelPolling();
        int sourceNumber = getThingNumber();
        logger.debug("Scheduling polling for Audio Source {}", sourceNumber);
        startExecutor();
        scheduledPolling = executorService.scheduleWithFixedDelay(new PollAudioSource(getThingNumber()), 0, POLL_DELAY,
                TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();

        switch (channelID) {
            case OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_POLLING:
                if (command == RefreshType.REFRESH) {
                    OnOffType pollingState = (scheduledPolling != null && !scheduledPolling.isDone()) ? OnOffType.ON
                            : OnOffType.OFF;
                    updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_POLLING, pollingState);
                } else if (command instanceof OnOffType) {
                    OnOffType pollingState = (OnOffType) command;
                    switch (pollingState) {
                        case ON:
                            schedulePolling();
                            break;
                        case OFF:
                            cancelPolling();
                            break;
                    }
                }
                break;
            default:
                logger.warn("Channel ID ({}) not processed", channelID);
                break;

        }

    }

    private class PollAudioSource implements Runnable {

        private final int sourceNumber;

        private PollAudioSource(int sourceNumber) {
            this.sourceNumber = sourceNumber;
        }

        @Override
        public void run() {
            logger.debug("Polling Audio Source {} Status", sourceNumber);
            try {
                int position = 0;
                Message message;
                while ((message = getOmnilinkBridgeHandler().requestAudioSourceStatus(sourceNumber, position))
                        .getMessageType() == Message.MESG_TYPE_AUDIO_SOURCE_STATUS) {
                    AudioSourceStatus audioSourceStatus = (AudioSourceStatus) message;
                    position = audioSourceStatus.getPosition();
                    switch (position) {
                        case 1:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT1,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 2:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT2,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 3:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT3,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 4:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT4,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 5:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT5,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 6:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT6,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                    }

                }
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.warn("Exception Polling Audio Status", e);
            }

        }

    }

}
