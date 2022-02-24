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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.commandToQuantity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.action.RoomActions;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.PropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RoomHandler} is the class handling room things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class RoomHandler extends NetatmoHandler {
    private final Logger logger = LoggerFactory.getLogger(RoomHandler.class);

    public RoomHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet);
    }

    @Override
    protected PropertyHelper getPropertyHelper() {
        return new PropertyHelper(getThing());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            String channelName = channelUID.getIdWithoutGroup();
            String groupName = channelUID.getGroupId();
            NetatmoHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler instanceof HomeHandler) {
                HomeHandler homeHandler = (HomeHandler) bridgeHandler;
                if (channelName.equals(CHANNEL_SETPOINT_MODE)) {
                    SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                    if (targetMode == SetpointMode.MANUAL) {
                        logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                    } else {
                        homeHandler.getEnergyCap().ifPresent(cap -> cap.callSetRoomThermMode(getId(), targetMode));
                    }
                } else if (GROUP_TH_SETPOINT.equals(groupName) && channelName.equals(CHANNEL_VALUE)) {
                    QuantityType<?> quantity = commandToQuantity(command, MeasureClass.INTERIOR_TEMPERATURE);
                    if (quantity != null) {
                        homeHandler.getEnergyCap()
                                .ifPresent(cap -> cap.callSetRoomThermTemp(getId(), quantity.doubleValue()));
                        updateState(channelUID, quantity);
                    } else {
                        logger.warn("Incorrect command '{}' on channel '{}'", command, channelName);
                    }
                }

            }
        }
    }

    public void thingActionCallSetRoomThermTemp(double temperature, long endtime, SetpointMode mode) {
        NetatmoHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler instanceof HomeHandler) {
            HomeHandler homeHandler = (HomeHandler) bridgeHandler;
            homeHandler.getEnergyCap().ifPresent(cap -> cap.callSetRoomThermTemp(getId(), temperature, endtime, mode));
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(RoomActions.class);
    }
}
