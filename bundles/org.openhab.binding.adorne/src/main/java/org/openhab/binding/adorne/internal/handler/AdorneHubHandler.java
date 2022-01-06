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
package org.openhab.binding.adorne.internal.handler;

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.adorne.internal.configuration.AdorneHubConfiguration;
import org.openhab.binding.adorne.internal.hub.AdorneHubChangeNotify;
import org.openhab.binding.adorne.internal.hub.AdorneHubController;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdorneHubHandler} manages the state and status of the Adorne Hub's devices.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
public class AdorneHubHandler extends BaseBridgeHandler implements AdorneHubChangeNotify {
    private final Logger logger = LoggerFactory.getLogger(AdorneHubHandler.class);
    private @Nullable AdorneHubController adorneHubController = null;

    public AdorneHubHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * The {@link AdorneHubHandler} does not support any commands itself. This method is a NOOP and only provided since
     * its implementation is required.
     *
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Unfortunately BaseBridgeHandler doesn't provide a default implementation of handleCommand. However, hub
        // commands could be added as a future enhancement e.g. to support hub firmware upgrades.
    }

    /**
     * Establishes the hub controller for communication with the hub.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing hub {}", getThing().getLabel());

        updateStatus(ThingStatus.UNKNOWN);
        AdorneHubConfiguration config = getConfigAs(AdorneHubConfiguration.class);
        logger.debug("Configuration host:{} port:{}", config.host, config.port);

        AdorneHubController adorneHubController = new AdorneHubController(config, scheduler, this);
        this.adorneHubController = adorneHubController;
        // Kick off the hub controller that handles all interactions with the hub for us
        adorneHubController.start();
    }

    /**
     * Disposes resources by stopping the hub controller.
     */
    @Override
    public void dispose() {
        AdorneHubController adorneHubController = this.adorneHubController;
        if (adorneHubController != null) {
            adorneHubController.stop();
        }
    }

    /**
     * Returns the hub controller. Returns <code>null</code> if hub controller has not been created yet.
     *
     * @return hub controller
     */
    public @Nullable AdorneHubController getAdorneHubController() {
        return adorneHubController;
    }

    /**
     * The {@link AdorneHubHandler} is notified that the state of one of its physical devices has changed. The
     * {@link AdorneHubHandler} then asks the appropriate thing handler to update the thing to match the new state.
     *
     */
    @Override
    public void stateChangeNotify(int zoneId, boolean onOff, int brightness) {
        logger.debug("State changed (zoneId:{} onOff:{} brightness:{})", zoneId, onOff, brightness);
        getThing().getThings().forEach(thing -> {
            AdorneSwitchHandler thingHandler = (AdorneSwitchHandler) thing.getHandler();
            if (thingHandler != null && thingHandler.getZoneId() == zoneId) {
                thingHandler.updateState(CHANNEL_POWER, OnOffType.from(onOff));
                if (thing.getThingTypeUID().equals(THING_TYPE_DIMMER)) {
                    thingHandler.updateState(CHANNEL_BRIGHTNESS, new PercentType(brightness));
                }
            }
        });
    }

    /**
     * The {@link AdorneHubHandler} is notified that its connectivity has changed.
     *
     */
    @Override
    public void connectionChangeNotify(boolean connected) {
        logger.debug("Status changed (connected:{})", connected);

        if (connected) {
            // Refresh all of our things in case thing states changed while we were disconnected
            getThing().getThings().forEach(thing -> {
                AdorneSwitchHandler thingHandler = (AdorneSwitchHandler) thing.getHandler();
                if (thingHandler != null) {
                    thingHandler.refresh();
                }
            });
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }
}
