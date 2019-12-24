/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitData;

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
    void touhWandUnitHandleCommand(Command command) {
        switch (command.toString()) {
            case "OFF":
            case "ON":
                bridgeHandler.touchWandClient.cmdSwitchOnOff(unitId, (OnOffType) command);
                break;
            default:
                bridgeHandler.touchWandClient.cmdDimmerPosition(unitId, command.toString());
                break;
        }
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        int status = unitData.getCurrStatus();
        PercentType state = PercentType.ZERO;
        int convertStatus = status;
        state = new PercentType(convertStatus);
        updateState(CHANNEL_DIMMER, state);
    }

}
