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
package org.openhab.binding.boschshc.internal;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_POWER_SWITCH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoschSHCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschSHCHandler extends BaseThingHandler {

    // TODO: Might want to have something that inherits from BaseBridgeHandler too

    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandler.class);

    private @Nullable BoschSHCConfiguration config;

    public BoschSHCHandler(Thing thing) {
        super(thing);
        logger.warn("Creating thing: {}", thing.getLabel());
    }

    public @Nullable String getBoschID() {
        if (this.config != null) {
            return this.config.id;
        } else {
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.warn("Handle command for: {}", config.id);

        if (CHANNEL_POWER_SWITCH.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            else {

                // TODO: Find bridge - updates have to be done via the bridge.

                // TODO: handle command

                // Note: if communication with thing fails for some reason,
                // indicate that by setting the status with detail information:
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                // "Could not control device at IP address x.x.x.x");
            }
        }
    }

    @Override
    public void initialize() {

        config = getConfigAs(BoschSHCConfiguration.class);
        logger.warn("Initializating thing: {}", config.id);

        // Mark immediately as online - if the bridge is online, the thing is too.
        updateStatus(ThingStatus.ONLINE);
    }

    public void processUpdate(DeviceStatusUpdate update) {

        String channel = null;
        State state = null;

        logger.warn("Processing update in BoschSHCHandler: {}", update);

        // TODO Make this work for other kind of updates too!
        if (update.state.type.equals("powerSwitchState")) {
            channel = CHANNEL_POWER_SWITCH;
            state = OnOffType.from(update.state.switchState);
        }

        if (channel != null && state != null) {
            this.updateState(channel, state);
        } else {
            logger.warn("Could not process update: {}", update);
        }
    }

}
