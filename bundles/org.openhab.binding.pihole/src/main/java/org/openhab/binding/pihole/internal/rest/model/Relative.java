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
package org.openhab.binding.pihole.internal.rest.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class Relative {
    @Nullable
    private Integer days;
    @Nullable
    private Integer hours;
    @Nullable
    private Integer minutes;

    public Relative() {
    }

    public Relative(Integer days, Integer hours, Integer minutes) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    @Nullable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    @Nullable
    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    @Nullable
    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Relative relative = (Relative) o;

        if (!Objects.equals(days, relative.days))
            return false;
        if (!Objects.equals(hours, relative.hours))
            return false;
        return Objects.equals(minutes, relative.minutes);
    }

    @Override
    public int hashCode() {
        int result = days != null ? days.hashCode() : 0;
        result = 31 * result + (hours != null ? hours.hashCode() : 0);
        result = 31 * result + (minutes != null ? minutes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Relative{" + //
                "days=" + days + //
                ", hours=" + hours + //
                ", minutes=" + minutes + //
                '}';
    }
}
