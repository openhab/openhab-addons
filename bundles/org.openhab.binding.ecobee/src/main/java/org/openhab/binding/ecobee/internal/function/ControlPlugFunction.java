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
package org.openhab.binding.ecobee.internal.function;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.enums.HoldType;
import org.openhab.binding.ecobee.internal.enums.PlugState;

/**
 * Control the on/off state of a plug by setting a hold on the plug. Creates a hold for the on or off state of the plug
 * for the specified duration. Note that an event is created regardless of whether the program is in the same state as
 * the requested state.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class ControlPlugFunction extends AbstractFunction {

    public ControlPlugFunction(@Nullable String plugName, @Nullable PlugState plugState, @Nullable Date startDateTime,
            @Nullable Date endDateTime, @Nullable HoldType holdType, @Nullable Integer holdHours) {
        super("controlPlug");

        if (plugName == null || plugState == null) {
            throw new IllegalArgumentException("plugName and plugState arguments are required.");
        }
        if (holdType == HoldType.DATE_TIME && endDateTime == null) {
            throw new IllegalArgumentException("End date/time is required for dateTime hold type.");
        }
        if (holdType == HoldType.HOLD_HOURS && holdHours == null) {
            throw new IllegalArgumentException("holdHours must be specified when using holdHours hold type.");
        }
        params.put("plugName", plugName);
        params.put("plugState", plugState);
        if (startDateTime != null) {
            params.put("startDate", YMD.format(startDateTime));
            params.put("startTime", HMS.format(startDateTime));
        }
        if (endDateTime != null) {
            params.put("endDate", YMD.format(endDateTime));
            params.put("endTime", HMS.format(endDateTime));
        }
        if (holdType != null) {
            params.put("holdType", holdType);
        }
        if (holdHours != null) {
            params.put("holdHours", holdHours);
        }
    }
}
