/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.touchwand.internal.dto.Csc;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitDataWallController;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

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

    private long timeLastEventMs;
    private static final int ADJACENT_EVENT_FILTER_TIME_MILLISEC = 2000; // 2 seconds

    public TouchWandWallControllerHandler(Thing thing) {
        super(thing);
        timeLastEventMs = Instant.now().toEpochMilli();
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandUnitDataWallController) {
            Csc status = ((TouchWandUnitDataWallController) unitData).getCurrStatus();
            long ts = status.getTs();
            long timeDiff = ts - timeLastEventMs;
            if ((timeDiff) > ADJACENT_EVENT_FILTER_TIME_MILLISEC) {
                int value = status.getKeyAttr();
                String action = (value <= 100) ? "SHORT" : "LONG";
                triggerChannel(CHANNEL_WALLCONTROLLER_ACTION, action);
            }
            timeLastEventMs = status.getTs();
        } else {
            logger.debug("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }
}
