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
package org.openhab.binding.tradfri.internal.handler;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tradfri.internal.TradfriCoapClient;
import org.openhab.binding.tradfri.internal.model.TradfriControllerData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriControllerHandler} is responsible for handling commands for individual controllers.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class TradfriControllerHandler extends TradfriThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriControllerHandler.class);

    // keeps track of the current state for handling of increase/decrease
    private @Nullable TradfriControllerData state;

    public TradfriControllerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            state = new TradfriControllerData(data);
            updateStatus(state.getReachabilityStatus() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

            final TradfriControllerData state = this.state;

            if (state == null) {
                return;
            }
            DecimalType batteryLevel = state.getBatteryLevel();
            if (batteryLevel != null) {
                updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            }

            OnOffType batteryLow = state.getBatteryLow();
            if (batteryLow != null) {
                updateState(CHANNEL_BATTERY_LOW, batteryLow);
            }

            updateDeviceProperties(state);

            logger.debug(
                    "Updating thing for controllerId {} to state {batteryLevel: {}, batteryLow: {}, firmwareVersion: {}, modelId: {}, vendor: {}}",
                    state.getDeviceId(), batteryLevel, batteryLow, state.getFirmwareVersion(), state.getModelId(),
                    state.getVendor());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (active) {
            if (command instanceof RefreshType) {
                TradfriCoapClient coapClient = this.coapClient;
                if (coapClient != null) {
                    logger.debug("Refreshing channel {}", channelUID);
                    coapClient.asyncGet(this);
                } else {
                    logger.debug("coapClient is null!");
                }
                return;
            }

            logger.debug("The controller is a read-only device and cannot handle commands.");
        }
    }
}
