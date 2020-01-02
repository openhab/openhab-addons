/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.CHANNEL_SHUTTER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.touchwand.internal.data.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitData;

/**
 * The {@link TouchWandShutterHandler} is responsible for handling commands for Shutter units
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandShutterHandler extends TouchWandBaseUnitHandler {

    public TouchWandShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    void touhWandUnitHandleCommand(Command command) {
        switch (command.toString()) {
            case "OFF":
            case "DOWN":
                bridgeHandler.touchWandClient.cmdShutterDown(unitId);
                break;
            case "ON":
            case "UP":
                bridgeHandler.touchWandClient.cmdShutterUp(unitId);
                break;
            case "STOP":
                bridgeHandler.touchWandClient.cmdShutterStop(unitId);
                break;
            default:
                bridgeHandler.touchWandClient.cmdShutterPosition(unitId, command.toString());
                break;
        }
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandShutterSwitchUnitData) {
            int status = ((TouchWandShutterSwitchUnitData) unitData).getCurrStatus();
            PercentType state = PercentType.ZERO;
            int convertStatus = 100 - status;
            state = new PercentType(convertStatus);
            updateState(CHANNEL_SHUTTER, state);
        } else {
            logger.warn("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }
}
