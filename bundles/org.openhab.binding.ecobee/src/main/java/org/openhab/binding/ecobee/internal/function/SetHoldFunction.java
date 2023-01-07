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
package org.openhab.binding.ecobee.internal.function;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.dto.thermostat.EventDTO;
import org.openhab.binding.ecobee.internal.enums.HoldType;

/**
 * The set hold function sets the thermostat into a hold with the specified temperature.
 * Creates a hold for the specified duration. Note that an event is created regardless
 * of whether the program is in the same state as the requested state. There is also
 * support for creating a hold by passing a holdClimateRef request parameter/value pair
 * to this function (See Event). When an existing and valid Thermostat.Climate#climateRef
 * value is passed to this function, the coolHoldTemp, heatHoldTemp and fan mode from
 * that Thermostat.Climate are used in the creation of the hold event. The values from
 * that Climate will take precedence over any coolHoldTemp, heatHoldTemp, and fan
 * mode parameters passed into this function separately. To resume from a hold and
 * return to the program, use the ResumeProgramFunction.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class SetHoldFunction extends AbstractFunction {

    public SetHoldFunction(@Nullable EventDTO event, @Nullable HoldType holdType, @Nullable Integer holdHours,
            @Nullable Date startDateTime, @Nullable Date endDateTime) {
        super("setHold");

        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        if (holdType == HoldType.HOLD_HOURS && holdHours == null) {
            throw new IllegalArgumentException("holdHours must be specified when holdType='holdHours'");
        } else if (holdType == HoldType.DATE_TIME && endDateTime == null) {
            throw new IllegalArgumentException("endDateTime must be specified when holdType='dateTime'");
        }
        if (event.holdClimateRef == null) {
            if (Boolean.TRUE.equals(event.isTemperatureAbsolute) && Boolean.TRUE.equals(event.isTemperatureRelative)) {
                throw new IllegalArgumentException("cannot set both absolute and relative temperatures");
            }
            if (Boolean.TRUE.equals(event.isTemperatureAbsolute)
                    && (event.coolHoldTemp == null || event.heatHoldTemp == null)) {
                throw new IllegalArgumentException(
                        "coolHoldTemp and heatHoldTemp must be specified when 'isTemperatureAbsolute' is true");
            }
            if (Boolean.TRUE.equals(event.isTemperatureRelative)
                    && (event.coolRelativeTemp == null || event.heatRelativeTemp == null)) {
                throw new IllegalArgumentException(
                        "coolRelativeTemp and heatRelativeTemp must be specified when 'isTemperatureRelative' is true");
            }
        }
        // Make parameters from the input event
        if (event.isOccupied != null) {
            params.put("isOccupied", event.isOccupied);
        }
        if (event.isCoolOff != null) {
            params.put("isCoolOff", event.isCoolOff);
        }
        if (event.isHeatOff != null) {
            params.put("isHeatOff", event.isHeatOff);
        }
        if (event.coolHoldTemp != null) {
            params.put("coolHoldTemp", event.coolHoldTemp);
        }
        if (event.heatHoldTemp != null) {
            params.put("heatHoldTemp", event.heatHoldTemp);
        }
        if (event.fan != null) {
            params.put("fan", event.fan);
        }
        if (event.vent != null) {
            params.put("vent", event.vent);
        }
        if (event.ventilatorMinOnTime != null) {
            params.put("ventilatorMinOnTime", event.ventilatorMinOnTime);
        }
        if (event.isOptional != null) {
            params.put("isOptional", event.isOptional);
        }
        if (event.isTemperatureRelative != null) {
            params.put("isTemperatureRelative", event.isTemperatureRelative);
        }
        if (event.coolRelativeTemp != null) {
            params.put("coolRelativeTemp", event.coolRelativeTemp);
        }
        if (event.heatRelativeTemp != null) {
            params.put("heatRelativeTemp", event.heatRelativeTemp);
        }
        if (event.isTemperatureAbsolute != null) {
            params.put("isTemperatureAbsolute", event.isTemperatureAbsolute);
        }
        if (event.fanMinOnTime != null) {
            params.put("fanMinOnTime", event.fanMinOnTime);
        }
        if (event.holdClimateRef != null) {
            params.put("holdClimateRef", event.holdClimateRef);
        }

        // Make parameters from the holdType and hold options
        if (holdType != null) {
            params.put("holdType", holdType);
        }
        if (holdHours != null) {
            params.put("holdHours", holdHours);
        }
        if (startDateTime != null) {
            params.put("startDate", YMD.format(startDateTime));
            params.put("startTime", HMS.format(startDateTime));
        }
        if (endDateTime != null) {
            params.put("endDate", YMD.format(endDateTime));
            params.put("endTime", HMS.format(endDateTime));
        }
    }
}
