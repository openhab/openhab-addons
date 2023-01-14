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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.enums.FanMode;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;

/**
 * The create vacation function creates a vacation event on the thermostat. If the
 * start/end date/times are not provided for the vacation event, the vacation event
 * will begin immediately and last 14 days. If both the coolHoldTemp and heatHoldTemp
 * parameters provided to this function have the same value, and the Thermostat is
 * in auto mode, then the two values will be adjusted during processing to be
 * separated by the value stored in Thermostat.Settings#heatCoolMinDelta.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class CreateVacationFunction extends AbstractFunction {

    public CreateVacationFunction(@Nullable String name, @Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp, @Nullable Date startDateTime, @Nullable Date endDateTime,
            @Nullable FanMode fan, @Nullable Integer fanMinOnTime) {
        super("createVacation");

        if (name == null || coolHoldTemp == null || heatHoldTemp == null) {
            throw new IllegalArgumentException("name, coolHoldTemp and heatHoldTemp arguments are required.");
        }
        params.put("name", name);

        QuantityType<Temperature> convertedCoolHoldTemp = coolHoldTemp.toUnit(ImperialUnits.FAHRENHEIT);
        QuantityType<Temperature> convertedHeatHoldTemp = heatHoldTemp.toUnit(ImperialUnits.FAHRENHEIT);
        if (convertedCoolHoldTemp == null || convertedHeatHoldTemp == null) {
            throw new IllegalArgumentException("coolHoldTemp or heatHoldTemp are not proper QuantityTypes");
        }
        params.put("coolHoldTemp", Integer.valueOf(convertedCoolHoldTemp.intValue()));
        params.put("heatHoldTemp", Integer.valueOf(convertedHeatHoldTemp.intValue()));

        if (startDateTime != null) {
            params.put("startDate", YMD.format(startDateTime));
            params.put("startTime", HMS.format(startDateTime));
        }
        if (endDateTime != null) {
            params.put("endDate", YMD.format(endDateTime));
            params.put("endTime", HMS.format(endDateTime));
        }
        if (fan != null) {
            params.put("fan", fan);
        }
        if (fanMinOnTime != null) {
            // doc says String not Integer for fanMinOnTime parameter (@watou)
            params.put("fanMinOnTime", fanMinOnTime.toString());
        }
    }
}
