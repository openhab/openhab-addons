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
package org.openhab.binding.chromecast.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.litvak.chromecast.api.v2.ChromeCastConnectionEvent;
import su.litvak.chromecast.api.v2.ChromeCastConnectionEventListener;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEventListener;
import su.litvak.chromecast.api.v2.MediaStatus;
import su.litvak.chromecast.api.v2.Status;

/**
 * Responsible for listening to events from the Chromecast.
 *
 * @author Jason Holmes - Initial contribution
 */
@NonNullByDefault
public class ChromecastEventReceiver implements ChromeCastSpontaneousEventListener, ChromeCastConnectionEventListener {
    private final Logger logger = LoggerFactory.getLogger(ChromecastEventReceiver.class);

    private final ChromecastScheduler scheduler;
    private final ChromecastStatusUpdater statusUpdater;

    public ChromecastEventReceiver(ChromecastScheduler scheduler, ChromecastStatusUpdater statusUpdater) {
        this.scheduler = scheduler;
        this.statusUpdater = statusUpdater;
    }

    @Override
    public void connectionEventReceived(final @NonNullByDefault({}) ChromeCastConnectionEvent event) {
        if (event.isConnected()) {
            statusUpdater.updateStatus(ThingStatus.ONLINE);
            scheduler.scheduleRefresh();
        } else {
            scheduler.cancelRefresh();
            statusUpdater.updateStatus(ThingStatus.OFFLINE);
            // We might have just had a connection problem, let's try to reconnect.
            scheduler.scheduleConnect();
        }
    }

    @Override
    public void spontaneousEventReceived(final @NonNullByDefault({}) ChromeCastSpontaneousEvent event) {
        switch (event.getType()) {
            case CLOSE:
                statusUpdater.updateMediaStatus(null);
                break;
            case MEDIA_STATUS:
                statusUpdater.updateMediaStatus(event.getData(MediaStatus.class));
                break;
            case STATUS:
                statusUpdater.processStatusUpdate(event.getData(Status.class));
                break;
            case UNKNOWN:
                logger.debug("Received an 'UNKNOWN' event (class={})", event.getType().getDataClass());
                break;
            default:
                logger.debug("Unhandled event type: {}", event.getType());
                break;
        }
    }
}
