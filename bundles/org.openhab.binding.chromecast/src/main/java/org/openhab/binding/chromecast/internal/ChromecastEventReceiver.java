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

import java.util.List;

import org.digitalmediaserver.cast.CastEvent;
import org.digitalmediaserver.cast.CastEvent.CastEventListener;
import org.digitalmediaserver.cast.MediaStatus;
import org.digitalmediaserver.cast.StandardResponse.MediaStatusResponse;
import org.digitalmediaserver.cast.StandardResponse.ReceiverStatusResponse;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for listening to events from the Chromecast.
 *
 * @author Jason Holmes - Initial contribution
 */
@NonNullByDefault
public class ChromecastEventReceiver implements CastEventListener {
    private final Logger logger = LoggerFactory.getLogger(ChromecastEventReceiver.class);

    private final ChromecastScheduler scheduler;
    private final ChromecastStatusUpdater statusUpdater;

    public ChromecastEventReceiver(ChromecastScheduler scheduler, ChromecastStatusUpdater statusUpdater) {
        this.scheduler = scheduler;
        this.statusUpdater = statusUpdater;
    }

    @Override
    public void onEvent(@NonNullByDefault({}) CastEvent<?> event) {
        switch (event.getEventType()) {
            case CONNECTED:
                Boolean isConnected = (Boolean) event.getData();
                if (isConnected == null || !isConnected) {
                    // scheduler.cancelRefresh();
                    statusUpdater.updateStatus(ThingStatus.OFFLINE);
                    // We might have just had a connection problem, let's try to reconnect.
                    scheduler.scheduleConnect();
                } else {
                    statusUpdater.updateStatus(ThingStatus.ONLINE);
                    // scheduler.scheduleRefresh();
                }
            case CLOSE:
                statusUpdater.updateMediaStatus(null);
                break;
            case MEDIA_STATUS:
                List<MediaStatus> statuses = event.getData(MediaStatusResponse.class).getStatuses();
                if (statuses != null && statuses.size() > 1) {
                    logger.warn("Received multiple media statuses, this is not supported. Statuses: {}", statuses);
                } else if (statuses != null && !statuses.isEmpty()) {
                    statusUpdater.updateMediaStatus(statuses.getFirst());
                } else {
                    statusUpdater.updateMediaStatus(null);
                }
                break;
            case RECEIVER_STATUS:
                statusUpdater.processStatusUpdate(event.getData(ReceiverStatusResponse.class).getStatus());
                break;
            case UNKNOWN:
                logger.debug("Received an 'UNKNOWN' event (class={})", event.getEventType().getDataClass());
                break;
            case APPLICATION_AVAILABILITY:
            case CUSTOM_MESSAGE:
            case DEVICE_ADDED:
            case DEVICE_REMOVED:
            case DEVICE_UPDATED:
            case ERROR_RESPONSE:
            case LAUNCH_ERROR:
            case MULTIZONE_STATUS:
            default:
                logger.debug("Unhandled event type: {} with data {}:", event.getEventType(), event.getData());
                break;
        }
    }
}
