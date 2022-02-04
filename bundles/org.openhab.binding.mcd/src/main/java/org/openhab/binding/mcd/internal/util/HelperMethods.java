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
package org.openhab.binding.mcd.internal.util;

import static org.openhab.binding.mcd.internal.McdBindingConstants.*;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * This class contains some useful, static methods.
 * 
 * @author Simon Dengler - Initial contribution
 */
public class HelperMethods {

    private static final SensorEventDef[] array = SensorEventDef.values();

    /**
     * returns State for the given sensor event id.
     * THIS IS ONLY USED FOR CHANNELS THAT CAN SEND TWO DIFFERENT EVENTS
     * 
     * @param id sensor event id as int
     * @return State for the id
     */
    public static State getSwitchStateByEventId(int id) {
        if (id >= array.length) {
            return null;
        }
        SensorEventDef event = array[id];
        switch (event) {
            case ROOM_EXIT:
            case BED_EXIT:
            case STAND_UP:
            case OFF:
            case CLOSE:
                return OnOffType.OFF;
            case ON:
            case SIT_DOWN:
            case ROOM_ENTRY:
            case BED_ENTRY:
            case OPEN:
                return OnOffType.ON;
            default:
                return null;
        }
    }

    /**
     * returns channel for the given sensor event id
     * 
     * @param id sensor event id as int
     * @return channel as string
     */
    public static String getChannelByEventId(int id) {
        SensorEventDef event = array[id];
        switch (event) {
            case BED_ENTRY:
            case BED_EXIT:
                return BED_STATUS;
            case OFF:
            case ON:
                return LIGHT;
            case ROOM_EXIT:
            case ROOM_ENTRY:
                return PRESENCE;
            case STAND_UP:
            case SIT_DOWN:
                return SIT_STATUS;
            case OPEN:
            case CLOSE:
                return OPEN_SHUT;
            case URINE:
                return URINE;
            case FALL:
                return FALL;
            default:
                return null;
        }
    }
}
