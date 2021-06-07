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
package org.openhab.binding.netatmo.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.thing.Bridge;

/**
 * {@link Therm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Therm1Handler extends NetatmoDeviceHandler {

    // private final Logger logger = LoggerFactory.getLogger(Therm1Handler.class);

    public Therm1Handler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    // TODO : if it happens that Therm1Handler does not any longer have code, this class could be removed

    // private @Nullable PlugHandler getPlugHandler() {
    // NetatmoDeviceHandler handler = super.getBridgeHandler(getBridge());
    // return handler != null ? (PlugHandler) handler : null;
    // }

    // @Override
    // public void handleCommand(ChannelUID channelUID, Command command) {
    // if (command instanceof RefreshType) {
    // super.handleCommand(channelUID, command);
    // } else {
    // NAThermostat currentData = (NAThermostat) naThing;
    // PlugHandler handler = getPlugHandler();
    // if (currentData != null && handler != null) {
    // String channelName = channelUID.getIdWithoutGroup();
    // String groupName = channelUID.getGroupId();
    // if (channelName.equals(CHANNEL_SETPOINT_MODE)) {
    // SetpointMode targetMode = SetpointMode.valueOf(command.toString());
    // if (targetMode == SetpointMode.MANUAL) {
    // updateState(channelUID, toStringType(currentData.getSetpointMode()));
    // logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
    // } else {
    // handler.callSetThermMode(config.id, targetMode);
    // }
    // } else if (GROUP_TH_SETPOINT.equals(groupName) && channelName.equals(CHANNEL_VALUE)) {
    // QuantityType<?> quantity = commandToQuantity(command, MeasureClass.INTERIOR_TEMPERATURE);
    // if (quantity != null) {
    // handler.callSetThermTemp(config.id, quantity.doubleValue());
    // } else {
    // logger.warn("Incorrect command '{}' on channel '{}'", command, channelName);
    // }
    // }
    // }
    // }
    // }
}
