/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.CHANNEL_DIMMER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link TouchWandDimmerHandler} is responsible for handling commands for Dimmer units
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandDimmerHandler extends TouchWandBaseUnitHandler {

    public TouchWandDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
        TouchWandBridgeHandler touchWandBridgeHandler = bridgeHandler;
        if (touchWandBridgeHandler != null) {
            if (command instanceof OnOffType onOffCommand) {
                touchWandBridgeHandler.touchWandClient.cmdSwitchOnOff(unitId, onOffCommand);
            } else {
                touchWandBridgeHandler.touchWandClient.cmdDimmerPosition(unitId, command.toString());
            }
        }
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandShutterSwitchUnitData shutterSwitchUnitData) {
            int status = shutterSwitchUnitData.getCurrStatus();
            PercentType state = PercentType.ZERO;
            int convertStatus = status;
            state = new PercentType(convertStatus);
            updateState(CHANNEL_DIMMER, state);
        } else {
            logger.debug("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }
}
