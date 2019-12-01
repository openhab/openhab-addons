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
    void updateTouchWandUnitState(int status) {
        OnOffType state = OnOffType.OFF;
        String sStatus = Integer.toString(status);

        if (sStatus.equals(SWITCH_STATUS_ON)) {
            state = OnOffType.ON;
        }
        updateState(CHANNEL_SWITCH, state);
    }

    @Override
    void touhWandUnitHandleCommand(Command command) {
        if (command instanceof OnOffType) {
            bridgeHandler.touchWandClient.cmdSwitchOnOff(unitId, (OnOffType) command);
        }

    }
}
