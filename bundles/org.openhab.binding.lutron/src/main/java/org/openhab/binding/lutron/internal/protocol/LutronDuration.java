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
package org.openhab.binding.lutron.internal.protocol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds time durations used by the Lutron protocols
 *
 * @author Bob Adair - Initial contribution
 *
 */
@NonNullByDefault
public class LutronDuration {
    public static final int MAX_SECONDS = 360000 - 1;
    public static final int MAX_HUNDREDTHS = 99;

    private static final Pattern PATTERN_SS = Pattern.compile("^(\\d{1,2})$");
    private static final Pattern PATTERN_SSDEC = Pattern.compile("^(\\d{1,2})\\.(\\d{2})$");
    private static final Pattern PATTERN_MMSS = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private static final Pattern PATTERN_HHMMSS = Pattern.compile("^(\\d{1,2}):(\\d{2}):(\\d{2})$");

    public final Integer seconds;
    public final Integer hundredths;

    /**
     * Constructor accepting duration in seconds
     */
    public LutronDuration(Integer seconds) {
        if (seconds < 0 || seconds > MAX_SECONDS) {
            throw new IllegalArgumentException("Invalid duration");
        }
        this.seconds = seconds;
        this.hundredths = 0;
    }

    /**
     * Constructor accepting duration in seconds and hundredths of seconds
     */
    public LutronDuration(Integer seconds, Integer hundredths) {
        if (seconds < 0 || seconds > MAX_SECONDS || hundredths < 0 || hundredths > MAX_HUNDREDTHS) {
            throw new IllegalArgumentException("Invalid duration");
        }
        this.seconds = seconds;
        this.hundredths = hundredths;
    }

    /**
     * Constructor accepting duration in seconds as a BigDecimal
     */
    public LutronDuration(BigDecimal seconds) {
        if (seconds.compareTo(BigDecimal.ZERO) == -1 || seconds.compareTo(new BigDecimal(MAX_SECONDS)) == 1) {
            new IllegalArgumentException("Invalid duration");
        }
        this.seconds = seconds.intValue();
        BigDecimal fractional = seconds.subtract(new BigDecimal(seconds.intValue()));
        this.hundredths = fractional.movePointRight(2).intValue();
    }

    /**
     * Constructor accepting duration in seconds as a Double
     */
    public LutronDuration(Double seconds) {
        this(new BigDecimal(seconds).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Constructor accepting duration string of the format: SS.ss, SS, MM:SS, or HH:MM:SS
     */
    public LutronDuration(String duration) {
        Matcher matcherSS = PATTERN_SS.matcher(duration);
        if (matcherSS.find()) {
            Integer seconds = Integer.valueOf(matcherSS.group(1));
            this.seconds = seconds;
            this.hundredths = 0;
            return;
        }
        Matcher matcherSSDec = PATTERN_SSDEC.matcher(duration);
        if (matcherSSDec.find()) {
            this.seconds = Integer.valueOf(matcherSSDec.group(1));
            this.hundredths = Integer.valueOf(matcherSSDec.group(2));
            return;
        }
        Matcher matcherMMSS = PATTERN_MMSS.matcher(duration);
        if (matcherMMSS.find()) {
            Integer minutes = Integer.valueOf(matcherMMSS.group(1));
            Integer seconds = Integer.valueOf(matcherMMSS.group(2));
            this.seconds = minutes * 60 + seconds;
            this.hundredths = 0;
            return;
        }
        Matcher matcherHHMMSS = PATTERN_HHMMSS.matcher(duration);
        if (matcherHHMMSS.find()) {
            Integer hours = Integer.valueOf(matcherHHMMSS.group(1));
            Integer minutes = Integer.valueOf(matcherHHMMSS.group(2));
            Integer seconds = Integer.valueOf(matcherHHMMSS.group(3));
            this.seconds = hours * 60 * 60 + minutes * 60 + seconds;
            this.hundredths = 0;
            return;
        }
        throw new IllegalArgumentException("Invalid duration");
    }

    public String asLipString() {
        if (seconds < 100) {
            if (hundredths == 0) {
                return String.valueOf(seconds);
            } else {
                return String.format("%d.%02d", seconds, hundredths);
            }
        } else if (seconds < 3600) {
            return String.format("%d:%02d", seconds / 60, seconds % 60);
        } else {
            return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
        }
    }

    public String asLeapString() {
        Integer seconds = this.seconds;
        if (seconds.equals(0) && hundredths > 0) {
            // use 1 second if interval is > 0 and < 1
            seconds = 1;
        } else if (hundredths >= 50) {
            // else apply normal rounding of hundredths
            seconds++;
        }
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }

    @Override
    public String toString() {
        return asLipString();
    }
}
