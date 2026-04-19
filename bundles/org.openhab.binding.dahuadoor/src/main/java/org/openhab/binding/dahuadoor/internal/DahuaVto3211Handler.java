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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link DahuaVto3211Handler} handles VTO3211 devices (dual button with LockNum detection)
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaVto3211Handler extends DahuaDoorBaseHandler {

    public DahuaVto3211Handler(Thing thing, PlayStreamServlet playStreamServlet) {
        super(thing, playStreamServlet);
    }

    @Override
    protected void handleInvite(JsonObject eventList, JsonObject eventData) {
        logger.debug("Event Invite from VTO3211 (dual button)");

        // Extract LockNum from event data to determine which button was pressed
        int lockNumber = 1; // default to button 1
        JsonElement lockNumElement = eventData.get("LockNum");
        if (lockNumElement != null && !lockNumElement.isJsonNull()) {
            lockNumber = lockNumElement.getAsInt();
            logger.debug("Extracted LockNum: {}", lockNumber);
        } else {
            logger.warn("LockNum not found in Invite event, defaulting to button 1");
        }

        onButtonPressed(lockNumber);
    }

    @Override
    protected void onButtonPressed(int lockNumber) {
        logger.debug("Button {} pressed on VTO3211", lockNumber);

        // Determine channel IDs based on lock number
        String bellButtonChannelId;
        String doorImageChannelId;

        if (lockNumber == 2) {
            bellButtonChannelId = DahuaDoorBindingConstants.CHANNEL_BELL_BUTTON_2;
            doorImageChannelId = DahuaDoorBindingConstants.CHANNEL_DOOR_IMAGE_2;
        } else {
            // Default to button 1 for lockNumber 1 or any other value
            bellButtonChannelId = DahuaDoorBindingConstants.CHANNEL_BELL_BUTTON_1;
            doorImageChannelId = DahuaDoorBindingConstants.CHANNEL_DOOR_IMAGE_1;
        }

        // Trigger bell button channel
        Channel bellChannel = this.getThing().getChannel(bellButtonChannelId);
        if (bellChannel == null) {
            logger.warn("Bell button channel '{}' not found", bellButtonChannelId);
            return;
        }
        triggerChannel(bellChannel.getUID(), "PRESSED");

        // Retrieve image asynchronously to avoid blocking the keepAlive event loop
        DahuaEventClient localClient = client;
        if (localClient == null) {
            logger.warn("Client not initialized, cannot retrieve doorbell image");
            return;
        }

        final String doorImageChannelIdFinal = doorImageChannelId;
        scheduler.submit(() -> {
            byte[] buffer = localClient.requestImage();
            if (buffer != null && buffer.length > 0) {
                // Update image channel for the specific button
                RawType image = new RawType(buffer, "image/jpeg");
                updateState(doorImageChannelIdFinal, image);

                // Save snapshot image
                saveSnapshot(buffer);
            } else {
                logger.warn("Received empty or null image buffer from VTO3211 button {}", lockNumber);
            }
        });
    }
}
