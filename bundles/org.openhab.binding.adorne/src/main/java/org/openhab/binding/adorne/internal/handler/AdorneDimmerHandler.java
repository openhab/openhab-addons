/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.adorne.internal.handler;

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.CHANNEL_BRIGHTNESS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.adorne.internal.hub.AdorneHubController;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdorneDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels. It supports the brightness channel in addition to the inherited switch channel.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
public class AdorneDimmerHandler extends AdorneSwitchHandler {
    private final Logger logger = LoggerFactory.getLogger(AdorneDimmerHandler.class);

    public AdorneDimmerHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handles refresh and percent commands for channel
     * {@link org.openhab.binding.adorne.internal.AdorneBindingConstants#CHANNEL_BRIGHTNESS}
     * It delegates all other commands to its parent class.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand (channelUID:{} command:{}", channelUID, command);
        try {
            if (channelUID.getId().equals(CHANNEL_BRIGHTNESS)) {
                if (command instanceof RefreshType) {
                    refreshBrightness();
                } else if (command instanceof PercentType) {
                    // Change the brightness through the hub controller
                    AdorneHubController adorneHubController = getAdorneHubController();
                    int level = ((PercentType) command).intValue();
                    if (level >= 1 && level <= 100) { // Ignore commands outside of the supported 1-100 range
                        adorneHubController.setBrightness(zoneId, level);
                    } else {
                        logger.debug("Ignored command to set brightness to level {}", level);
                    }
                }
            } else {
                super.handleCommand(channelUID, command); // Parent can handle everything else
            }
        } catch (IllegalStateException e) {
            // Hub controller could't handle our commands. Unfortunately the framework has no mechanism to report
            // runtime errors. If we throw the exception up the framework logs it as an error - we don't want that - we
            // want the framework to handle it gracefully. No point to update the thing status, since the
            // AdorneHubController already does that. So we are forced to swallow the exception here.
            logger.debug("Failed to execute command {} for channel {} for thing {} ({})", command, channelUID,
                    getThing().getLabel(), e.getMessage());
        }
    }

    /**
     * Refreshes the brightness of our thing to the actual state of the device.
     *
     */
    public void refreshBrightness() {
        // Asynchronously get our brightness from the hub controller and update our state accordingly
        AdorneHubController adorneHubController = getAdorneHubController();
        adorneHubController.getState(zoneId).thenAccept(state -> {
            updateState(CHANNEL_BRIGHTNESS, new PercentType(state.brightness));
            logger.debug("Refreshed dimmer {} with brightness {}", getThing().getLabel(), state.brightness);
        });
    }

    /**
     * Refreshes all supported channels.
     *
     */
    @Override
    public void refresh() {
        super.refresh();
        refreshBrightness();
    }
}
