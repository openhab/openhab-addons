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

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.CHANNEL_POWER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.adorne.internal.configuration.AdorneSwitchConfiguration;
import org.openhab.binding.adorne.internal.hub.AdorneHubController;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
     * {@link org.openhab.binding.adorne.internal.AdorneBindingConstants#CHANNEL_POWER}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand (channelUID:{} command:{}", channelUID, command);
        try {
            if (channelUID.getId().equals(CHANNEL_POWER)) {
                if (command instanceof OnOffType) {
                    AdorneHubController adorneHubController = getAdorneHubController();
                    adorneHubController.setOnOff(zoneId, command.equals(OnOffType.ON));
                } else if (command instanceof RefreshType) {
                    refreshOnOff();
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
        Integer configZoneId = config.zoneId;
        if (configZoneId != null) {
            zoneId = configZoneId;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
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
            throw new IllegalStateException("Hub Controller not available yet.");
        }
        return adorneHubController;
    }

    /**
     * Returns the zone ID that represents this {@link AdorneSwitchHandler}'s thing
     *
     * @return zone ID
     */
    public int getZoneId() {
        return zoneId;
    }

    /**
     * Refreshes the on/off state of our thing to the actual state of the device.
     *
     */
    public void refreshOnOff() {
        // Asynchronously get our onOff state from the hub controller and update our state accordingly
        AdorneHubController adorneHubController = getAdorneHubController();
        adorneHubController.getState(zoneId).thenAccept(state -> {
            OnOffType onOffState = OnOffType.from(state.onOff);
            updateState(CHANNEL_POWER, onOffState);
            logger.debug("Refreshed switch {} with switch state {}", getThing().getLabel(), onOffState);
        });
    }

    /**
     * Refreshes all supported channels.
     *
     */
    public void refresh() {
        refreshOnOff();
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
