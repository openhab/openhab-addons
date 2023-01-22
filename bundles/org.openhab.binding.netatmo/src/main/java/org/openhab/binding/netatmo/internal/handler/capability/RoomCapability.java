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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.commandToQuantity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.action.RoomActions;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RoomCapability} gives the ability to handle Room specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RoomCapability extends Capability {
    private final Logger logger = LoggerFactory.getLogger(RoomCapability.class);
    private Optional<EnergyCapability> energyCapability = Optional.empty();

    public RoomCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void initialize() {
        energyCapability = handler.getHomeCapability(EnergyCapability.class);
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if (CHANNEL_SETPOINT_MODE.equals(channelName)) {
            try {
                SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                if (targetMode == SetpointMode.MANUAL) {
                    logger.info("Switch to 'Manual' mode is done by setting a setpoint temp, command ignored");
                } else {
                    energyCapability.ifPresent(cap -> cap.setRoomThermMode(handler.getId(), targetMode));
                }
            } catch (IllegalArgumentException e) {
                logger.info("Command '{}' is not a valid setpoint mode for channel '{}'", command, channelName);
            }
        } else if (CHANNEL_VALUE.equals(channelName)) {
            QuantityType<?> quantity = commandToQuantity(command, MeasureClass.INSIDE_TEMPERATURE);
            if (quantity != null) {
                energyCapability.ifPresent(cap -> cap.setRoomThermTemp(handler.getId(), quantity.doubleValue()));
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
