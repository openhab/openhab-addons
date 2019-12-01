/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.CHANNEL_SWITCH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.adorne.internal.configuration.AdorneSwitchConfiguration;
import org.openhab.binding.adorne.internal.hub.AdorneHubController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdorneSwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
public class AdorneSwitchHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AdorneSwitchHandler.class);

    /**
     * The zone ID that represents this {@link AdorneSwitchHandler}'s thing
     */
    protected int zoneId;

    public AdorneSwitchHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handles refresh and on/off commands for channel
     * {@link org.openhab.binding.adorne.internal.AdorneBindingConstants#CHANNEL_SWITCH}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand (channelUID:{} command:{}", channelUID, command);
        try {
            if (channelUID.getId().equals(CHANNEL_SWITCH)) {
                if (command.equals(OnOffType.ON) || command.equals(OnOffType.OFF) || command instanceof RefreshType) {
                    AdorneHubController adorneHubController = getAdorneHubController();
                    if (command instanceof RefreshType) {
                        // Asynchronously get our onOff state from the hub controller and update our state accordingly
                        adorneHubController.getState(zoneId).thenApply(state -> {
                            OnOffType onOffState = state.onOff ? OnOffType.ON : OnOffType.OFF;
                            updateState(CHANNEL_SWITCH, onOffState);
                            logger.debug("Refreshed switch {} with switch state {}", getThing().getLabel(), onOffState);
                            return null;
                        });
                    } else {
                        adorneHubController.setOnOff(zoneId, command.equals(OnOffType.ON));
                    }
                }
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
     * Sets the handled thing to online.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing switch {}", getThing().getLabel());

        AdorneSwitchConfiguration config = getConfigAs(AdorneSwitchConfiguration.class);
        if (config.zoneId != null) {
            zoneId = config.zoneId;
        } else {
            throw new IllegalStateException("zoneId must not be null as it is a required configuration parameter");
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Updates thing status in response to bridge status changes.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.trace("bridgeStatusChanged bridgeStatusInfo:{}", bridgeStatusInfo.getStatus());
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(bridgeStatusInfo.getStatus());
        }
    }

    /**
     * Returns the hub controller.
     *
     * @throws IllegalStateException if hub controller is not available yet.
     */
    protected AdorneHubController getAdorneHubController() {
        Bridge bridge;
        AdorneHubHandler hubHandler;
        AdorneHubController adorneHubController = null;

        bridge = getBridge();
        if (bridge != null) {
            hubHandler = (AdorneHubHandler) bridge.getHandler();
            if (hubHandler != null) {
                adorneHubController = hubHandler.getAdorneHubController();
            }
        }
        if (adorneHubController == null) {
            throw new IllegalStateException(); // This should not happen - we should always have a hub controller
        }
        return adorneHubController;
    }

    /**
     * Returns the zone ID that represents this {@link AdorneSwitchHandler}'s thing
     */
    public int getZoneId() {
        return zoneId;
    }

    /**
     * Provides a public version of updateState.
     *
     */
    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);// Leverage our base class' protected method
    }
}