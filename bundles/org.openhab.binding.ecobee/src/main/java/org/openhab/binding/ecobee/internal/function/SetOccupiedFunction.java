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
package org.openhab.binding.ecobee.internal.function;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.enums.HoldType;

/**
 * The set occupied function may only be used by EMS thermostats. The function switches a
 * thermostat from occupied mode to unoccupied, or vice versa. If used on a Smart thermostat,
 * the function will throw an error. Switch occupancy events are treated as Holds.
 * There may only be one Switch Occupancy at one time, and the new event will replace any previous event.
 * Note that an occupancy event is created regardless what the program on the thermostat is set
 * to. For example, if the program is currently unoccupied and you set occupied=false, an
 * occupancy event will be created using the heat/cool settings of the unoccupied program
 * climate. If your intent is to go back to the program and remove the occupancy event,
 * use ResumeProgramFunction instead.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class SetOccupiedFunction extends AbstractFunction {

    public SetOccupiedFunction(@Nullable Boolean occupied, @Nullable Date startDateTime, @Nullable Date endDateTime,
            @Nullable HoldType holdType, @Nullable Integer holdHours) {
        super("setOccupied"); // not in doc; assuming

        if (occupied == null) {
            throw new IllegalArgumentException("occupied state is required.");
        }
        params.put("occupied", occupied);

        if (startDateTime != null) {
            params.put("startDate", YMD.format(startDateTime));
            params.put("startTime", HMS.format(startDateTime));
        }
        if (endDateTime != null) {
            params.put("endDate", YMD.format(endDateTime));
            params.put("endTime", HMS.format(endDateTime));
        }

        if (holdType == HoldType.HOLD_HOURS && holdHours == null) {
            throw new IllegalArgumentException("holdHours must be specified when holdType='holdHours'");
        }
        if (holdType == HoldType.DATE_TIME && endDateTime == null) {
            throw new IllegalArgumentException("endDateTime must be specific when holdType='dateTime'");
        }

        if (holdType != null) {
            params.put("holdType", holdType);
        }
        if (holdHours != null) {
            params.put("holdHours", holdHours);
        }
    }
}
