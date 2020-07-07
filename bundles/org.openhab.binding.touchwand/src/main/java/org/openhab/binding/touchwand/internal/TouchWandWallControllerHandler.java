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

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.CHANNEL_WALLCONTROLLER_ACTION;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitDataWallController;

/**
 * The {@link TouchWandWallControllerHandler} is responsible for handling commands and triggers
 *
 * for WallController units
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandWallControllerHandler extends TouchWandBaseUnitHandler {

    private long timeSinceLastEvent;
    private static final int ADJUSTENT_EVENT_FILTER_TIME_MILLISEC = 2000; // 2 seconds

    public TouchWandWallControllerHandler(Thing thing) {
        super(thing);
        timeSinceLastEvent = Instant.now().toEpochMilli();
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        int status = ((TouchWandUnitDataWallController) unitData).getCurrStatus();
        long timeDiff = Instant.now().toEpochMilli() - timeSinceLastEvent;
        if ((timeDiff) > ADJUSTENT_EVENT_FILTER_TIME_MILLISEC) {
            String action = status <= 100 ? "SHORT" : "LONG";
            triggerChannel(CHANNEL_WALLCONTROLLER_ACTION, action);
        }
        timeSinceLastEvent = Instant.now().toEpochMilli();
    }
}
