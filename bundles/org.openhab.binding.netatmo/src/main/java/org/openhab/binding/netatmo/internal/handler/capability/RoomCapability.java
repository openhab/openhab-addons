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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.commandToQuantity;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.action.RoomActions;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RoomCapability} give the ability to read weather station api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RoomCapability extends Capability {
    private final Logger logger = LoggerFactory.getLogger(RoomCapability.class);

    public RoomCapability(NACommonInterface handler) {
        super(handler);
    }

    // Command handling capability
    @Override
    public void handleCommand(String channelName, Command command) {
        if (CHANNEL_SETPOINT_MODE.equals(channelName)) {
            SetpointMode targetMode = SetpointMode.valueOf(command.toString());
            if (targetMode == SetpointMode.MANUAL) {
                logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
            } else {
                handler.getHomeCapability(EnergyCapability.class)
                        .ifPresent(cap -> cap.setRoomThermMode(handlerId, targetMode));
            }
        } else if (CHANNEL_VALUE.equals(channelName)) {
            QuantityType<?> quantity = commandToQuantity(command, MeasureClass.INTERIOR_TEMPERATURE);
            if (quantity != null) {
                handler.getHomeCapability(EnergyCapability.class)
                        .ifPresent(cap -> cap.setRoomThermTemp(handlerId, quantity.doubleValue()));
            } else {
                logger.warn("Incorrect command '{}' on channel '{}'", command, channelName);
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(RoomActions.class);
    }
}
