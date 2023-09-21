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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.CHANNEL_SHUTTER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

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
    void touchWandUnitHandleCommand(Command command) {
        TouchWandBridgeHandler touchWandBridgeHandler = bridgeHandler;
        if (touchWandBridgeHandler != null) {
            switch (command.toString()) {
                case "OFF":
                case "DOWN":
                    touchWandBridgeHandler.touchWandClient.cmdShutterDown(unitId);
                    break;
                case "ON":
                case "UP":
                    touchWandBridgeHandler.touchWandClient.cmdShutterUp(unitId);
                    break;
                case "STOP":
                    touchWandBridgeHandler.touchWandClient.cmdShutterStop(unitId);
                    break;
                default:
                    touchWandBridgeHandler.touchWandClient.cmdShutterPosition(unitId, command.toString());
                    break;
            }
        }
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandShutterSwitchUnitData shutterSwitchUnitData) {
            int status = shutterSwitchUnitData.getCurrStatus();
            PercentType state = PercentType.ZERO;
            int convertStatus = 100 - status;
            state = new PercentType(convertStatus);
            updateState(CHANNEL_SHUTTER, state);
        } else {
            logger.debug("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }
}
