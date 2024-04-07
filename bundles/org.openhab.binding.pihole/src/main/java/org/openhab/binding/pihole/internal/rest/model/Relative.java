package org.openhab.binding.pihole.internal.rest.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

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

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    @Override
    public boolean equals(Object o) {
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
