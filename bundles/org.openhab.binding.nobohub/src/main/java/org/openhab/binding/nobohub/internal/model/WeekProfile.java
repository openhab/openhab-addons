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
package org.openhab.binding.nobohub.internal.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;

/**
 * The normal week profile (used when no {@link OverridePlan}s exist).
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class WeekProfile {

    private final int id;
    private final String name;
    private final String profile;

    public WeekProfile(int id, String name, String profile) {
        this.id = id;
        this.name = name;
        this.profile = profile;
    }

    public static WeekProfile fromH03(String h03) throws NoboDataException {
        String[] parts = h03.split(" ", 4);

        if (parts.length != 4) {
            throw new NoboDataException(
                    String.format("Unexpected number of parts from hub on H3 call: %d", parts.length));
        }

        return new WeekProfile(Integer.parseInt(parts[1]), ModelHelper.toJavaString(parts[2]),
                ModelHelper.toJavaString(parts[3]));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    /**
     * Returns the current status on the week profile (unless there is an override).
     *
     * @param time The current time
     * @return The current status (according to the week profile)
     */
    public WeekProfileStatus getStatusAt(LocalDateTime time) throws NoboDataException {
        final DayOfWeek weekDay = time.getDayOfWeek();
        final int dayNumber = weekDay.getValue();
        final String timeString = time.format(NoboHubBindingConstants.TIME_FORMAT_MINUTES);
        String[] parts = profile.split(",");

        int dayCounter = 0;
        for (int i = 0; i < parts.length; i++) {
            String current = parts[i];
            if (current.startsWith("0000")) {
                dayCounter++;
            }

            if (current.length() != 5) {
                throw new NoboDataException("Illegal week profile entry: " + current);
            }

            if (dayNumber == dayCounter) {
                String next = "24000";
                if (i + 1 < parts.length) {
                    if (!parts[i + 1].startsWith("0000")) {
                        next = parts[i + 1];
                    }
                }

                if (next.length() != 5) {
                    throw new NoboDataException("Illegal week profile entry for next entry: " + next);
                }

                try {
                    String currentTime = current.substring(0, 4);
                    String nextTime = next.substring(0, 4);
                    if (currentTime.compareTo(timeString) <= 0 && timeString.compareTo(nextTime) < 0) {
                        try {
                            return WeekProfileStatus.getByNumber(Integer.parseInt(String.valueOf(current.charAt(4))));
                        } catch (NumberFormatException nfe) {
                            throw new NoboDataException("Failed parsing week profile entry: " + current, nfe);
                        }
                    }
                } catch (IndexOutOfBoundsException oobe) {
                    throw new NoboDataException("Illegal time string" + current + ", " + next, oobe);
                }
            }
        }

        throw new NoboDataException(
                String.format("Failed to calculate %s for day %d in '%s'", timeString, dayNumber, profile));
    }
}
