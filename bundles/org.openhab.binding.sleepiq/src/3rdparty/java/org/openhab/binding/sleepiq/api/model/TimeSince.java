/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.model;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeSince {
    private static final Pattern PATTERN = Pattern.compile("(([0-9]+) d )?([0-9]{2}):([0-9]{2}):([0-9]{2})",
            Pattern.CASE_INSENSITIVE);

    private Duration duration;

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration == null ? null : duration.abs();
    }

    public TimeSince withDuration(long days, long hours, long minutes, long seconds) {
        return withDuration(Duration.ofSeconds(TimeUnit.DAYS.toSeconds(days) + TimeUnit.HOURS.toSeconds(hours)
                + TimeUnit.MINUTES.toSeconds(minutes) + seconds));
    }

    public TimeSince withDuration(Duration duration) {
        setDuration(duration);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TimeSince)) {
            return false;
        }
        TimeSince other = (TimeSince) obj;
        if (duration == null) {
            if (other.duration != null) {
                return false;
            }
        } else if (!duration.equals(other.duration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        long totalDays = duration.toDays();
        long totalHours = duration.toHours();
        long totalMinutes = duration.toMinutes();
        long totalSeconds = duration.getSeconds();

        long hours = totalHours - TimeUnit.DAYS.toHours(totalDays);
        long minutes = totalMinutes - TimeUnit.HOURS.toMinutes(totalHours);
        long seconds = totalSeconds - TimeUnit.MINUTES.toSeconds(totalMinutes);

        if (totalDays > 0) {
            return String.format("%d d %02d:%02d:%02d", totalDays, hours, minutes, seconds);
        }

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static TimeSince parse(CharSequence text) {
        Objects.requireNonNull(text, "text");

        Matcher matcher = PATTERN.matcher(text);
        if (!matcher.matches()) {
            return new TimeSince().withDuration(Duration.ZERO);
        }

        String dayMatch = matcher.group(2);
        String hourMatch = matcher.group(3);
        String minuteMatch = matcher.group(4);
        String secondMatch = matcher.group(5);

        StringBuilder sb = new StringBuilder("P");
        if (dayMatch != null) {
            sb.append(dayMatch).append('D');
        }
        sb.append('T').append(hourMatch).append('H').append(minuteMatch).append('M').append(secondMatch).append('S');

        return new TimeSince().withDuration(Duration.parse(sb.toString()));
    }
}
