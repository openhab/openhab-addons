/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.energy;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.TEMPERATURE_UNIT;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.energy.NAThermostat;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NATherm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NATherm1Handler extends NetatmoDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(NATherm1Handler.class);

    public NATherm1Handler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, @Nullable ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NADescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
    }

    private @Nullable NAPlugHandler getPlugHandler() {
        NetatmoDeviceHandler handler = super.getBridgeHandler(getBridge());
        return handler != null ? (NAPlugHandler) handler : null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            NAThermostat currentData = (NAThermostat) naThing;
            NAPlugHandler handler = getPlugHandler();
            if (currentData != null && handler != null) {
                String channelName = channelUID.getIdWithoutGroup();
                String groupName = channelUID.getGroupId();
                if (channelName.equals(CHANNEL_SETPOINT_MODE)) {
                    SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                    if (targetMode == SetpointMode.MANUAL) {
                        updateState(channelUID, toStringType(currentData.getSetpointMode()));
                        logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                    } else {
                        handler.callSetThermMode(config.id, targetMode);
                    }
                } else if (groupName.equals(GROUP_TH_SETPOINT) && channelName.equals(CHANNEL_VALUE)) {
                    QuantityType<?> quantity = commandToQuantity(command, TEMPERATURE_UNIT);
                    if (quantity != null) {
                        handler.callSetThermTemp(config.id, quantity.doubleValue());
                    } else {
                        logger.warn("Incorrect command '{}' on channel '{}'", command, channelName);
                    }
                }
            }
        }
    }
}
