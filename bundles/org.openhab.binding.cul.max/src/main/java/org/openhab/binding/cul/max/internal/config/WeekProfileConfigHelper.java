/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cul.max.internal.messages.MaxCulWeekProfile;
import org.openhab.binding.cul.max.internal.messages.MaxCulWeekProfileControlPoint;
import org.openhab.binding.cul.max.internal.messages.MaxCulWeekProfilePart;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulWeekdays;
import org.openhab.core.model.item.BindingConfigParseException;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
public class WeekProfileConfigHelper {

    public static MaxCulWeekProfile parseWeekProfileString(String weekProfile) throws BindingConfigParseException {
        MaxCulWeekProfile result = new MaxCulWeekProfile();
        String[] args = weekProfile.split(";");
        if (args.length % 2 == 1) {
            throw new BindingConfigParseException("Number of arguments must be even.");
        }
        for (int i = 0; i < args.length; i += 2) {
            MaxCulWeekdays day = MaxCulWeekdays.getWeekDayFromShortName(args[i]);
            MaxCulWeekProfilePart weekProfilePart = new MaxCulWeekProfilePart(day);
            String[] controlPoints = args[i + 1].split(",");
            if (controlPoints.length > 13 * 2) {
                throw new BindingConfigParseException("Not more than 13 control points are allowed!");
            }
            // Each day has 2 bytes * 13 controlpoints = 26 bytes = 52 hex characters
            // we don't have to update the rest, because the active part is terminated by the time 0:00
            for (int j = 0; j < 13 * 2; j += 2) {
                MaxCulWeekProfileControlPoint controlPoint = new MaxCulWeekProfileControlPoint();
                if (j + 1 == controlPoints.length) {
                    controlPoint.setHour(0);
                    controlPoint.setMin(0);
                    controlPoint.setTemperature(0.0f);
                    break;
                } else {
                    String timeRegex = "^(\\d{1,2})-(\\d{1,2})$";
                    Pattern pattern = Pattern.compile(timeRegex);
                    Matcher matcher = pattern.matcher(controlPoints[j + 1]);
                    if (matcher.matches()) {
                        controlPoint.setHour(Integer.parseInt(matcher.group(1)));
                        controlPoint.setMin(Integer.parseInt(matcher.group(2)));
                    }
                }
                if (controlPoint.getHour() > 23 || controlPoint.getMin() > 59)
                    throw new IllegalArgumentException("Invalid time");
                controlPoint.setTemperature(Float.parseFloat(controlPoints[j]));
                weekProfilePart.addControlPoint(controlPoint);

            }
            result.addWeekProfilePart(weekProfilePart);
        }
        return result;
    }
}
