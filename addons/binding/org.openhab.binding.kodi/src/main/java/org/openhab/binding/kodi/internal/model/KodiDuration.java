/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.model;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Class representing a Kodi duration
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class KodiDuration {
    /**
     * The hours of the duration
     */
    private long hours = 0;
    /**
     * The minutes of the duration
     */
    private long minutes = 0;
    /**
     * The seconds of the duration
     */
    private long seconds = 0;
    /**
     * The milliseconds of the duration
     */
    private long milliseconds = 0;

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Converts this KodiDuration to the total length in seconds.
     *
     * @return the total length of the duration in seconds
     */
    public long toSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(toMillis());
    }

    /**
     * Converts this KodiDuration to the total length in milliseconds.
     *
     * @return the total length of the duration in milliseconds
     */
    public long toMillis() {
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusMillis(milliseconds).toMillis();
    }
}
