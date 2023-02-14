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
package org.openhab.binding.freeboxos.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link ShutterHandler} is responsible for handling everything associated to any Freebox Home shutter thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ShutterHandler extends AlarmHandler {

    public ShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        super.initializeProperties(properties);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        /*
         * ShutterConfiguration config = getConfiguration();
         * EndpointState state = getManager(HomeManager.class).getEndpointsState(config.id, config.stateSignalId);
         * EndpointState position = getManager(HomeManager.class).getEndpointsState(config.id, config.positionSignalId);
         * Double percent = null;
         * if (state != null && position != null) {
         * if (ValueType.BOOL.equals(position.valueType())) {
         * percent = Boolean.TRUE.equals(position.asBoolean()) ? 1.0 : 0.0;
         * } else if (ValueType.INT.equals(position.valueType())) {
         * Integer inValue = position.asInt();
         * if (inValue != null) {
         * percent = inValue.doubleValue() / 100.0;
         * }
         * }
         * }
         * updateChannelDecimal(BASIC_SHUTTER, BASIC_SHUTTER_CMD, percent);
         */
    }

    // private ShutterConfiguration getConfiguration() {
    // return getConfigAs(ShutterConfiguration.class);
    // }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        /*
         * if (BASIC_SHUTTER_CMD.equals(channelId)) {
         * ShutterConfiguration config = getConfiguration();
         * if (StopMoveType.STOP.equals(command)) {
         * getManager(HomeManager.class).putCommand(config.id, config.stopSlotId, true);
         * return true;
         * }
         * }
         */
        return super.internalHandleCommand(channelId, command);
    }
}
