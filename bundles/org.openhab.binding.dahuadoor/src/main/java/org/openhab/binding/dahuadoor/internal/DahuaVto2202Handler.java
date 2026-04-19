/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dahuadoor.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dahuadoor.internal.dahuaeventhandler.DahuaEventClient;
import org.openhab.binding.dahuadoor.internal.media.PlayStreamServlet;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;

import com.google.gson.JsonObject;

/**
 * The {@link DahuaVto2202Handler} handles VTO2202 devices (single button)
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaVto2202Handler extends DahuaDoorBaseHandler {

    public DahuaVto2202Handler(Thing thing, PlayStreamServlet playStreamServlet) {
        super(thing, playStreamServlet);
    }

    @Override
    protected void handleInvite(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event Invite from VTO2202 (single button)");
        // VTO2202 has only one button, always use lockNumber 1
        onButtonPressed(1);
    }

    @Override
    protected void onButtonPressed(int lockNumber) {
        logger.debug("Button pressed on VTO2202 (lockNumber ignored, single button)");

        // Trigger bell button channel synchronously (fast, no blocking)
        Channel channel = this.getThing().getChannel(DahuaDoorBindingConstants.CHANNEL_BELL_BUTTON);
        if (channel == null) {
            logger.warn("Bell button channel not found");
            return;
        }
        triggerChannel(channel.getUID(), "PRESSED");

        // Retrieve image asynchronously to avoid blocking the keepAlive event loop
        DahuaEventClient localClient = client;
        if (localClient == null) {
            logger.warn("Client not initialized, cannot retrieve doorbell image");
            return;
        }

        scheduler.submit(() -> {
            byte[] buffer = localClient.requestImage();
            if (buffer != null && buffer.length > 0) {
                // Update image channel
                RawType image = new RawType(buffer, "image/jpeg");
                updateState(DahuaDoorBindingConstants.CHANNEL_DOOR_IMAGE, image);

                // Save snapshot
                saveSnapshot(buffer);
            } else {
                logger.warn("Received empty or null image buffer from VTO2202");
            }
        });
    }
}
