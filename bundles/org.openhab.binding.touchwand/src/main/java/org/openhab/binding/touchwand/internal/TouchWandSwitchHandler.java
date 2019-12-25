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

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.touchwand.internal.data.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitData;

/**
 * The {@link TouchWandSwitchHandler} is responsible for handling command for Switch unit
 *
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandSwitchHandler extends TouchWandBaseUnitHandler {

    public TouchWandSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandShutterSwitchUnitData) {
            OnOffType state;
            int status = ((TouchWandShutterSwitchUnitData) unitData).getCurrStatus();
            String sStatus = Integer.toString(status);

            if (sStatus.equals(SWITCH_STATUS_OFF)) {
                state = OnOffType.OFF;
            } else if ((status >= 1) && (status <= 255)) {
                state = OnOffType.ON;
            } else {
                logger.warn("updateTouchWandUnitState illigal update value {}", status);
                return;
            }
            updateState(CHANNEL_SWITCH, state);
        } else {
            logger.warn("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }

    @Override
    void touhWandUnitHandleCommand(Command command) {
        if (command instanceof OnOffType) {
            bridgeHandler.touchWandClient.cmdSwitchOnOff(unitId, (OnOffType) command);
        }

    }

}
