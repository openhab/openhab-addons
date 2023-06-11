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

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

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
                logger.warn("updateTouchWandUnitState illegal update value {}", status);
                return;
            }
            updateState(CHANNEL_SWITCH, state);
        } else {
            logger.debug("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
        if (command instanceof OnOffType) {
            TouchWandBridgeHandler touchWandBridgeHandler = bridgeHandler;
            if (touchWandBridgeHandler != null) {
                touchWandBridgeHandler.touchWandClient.cmdSwitchOnOff(unitId, (OnOffType) command);
            }
        }
    }
}
