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
package org.openhab.binding.plugwise.internal.protocol.field;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A simple class to represent energy usage, converting between Plugwise data representations.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class Energy {

    private static final int WATTS_PER_KILOWATT = 1000;
    private static final double PULSES_PER_KW_SECOND = 468.9385193;
    private static final double PULSES_PER_W_SECOND = PULSES_PER_KW_SECOND / WATTS_PER_KILOWATT;

    private @Nullable ZonedDateTime utcStart; // using UTC resolves wrong local start/end timestamps when DST changes
                                              // occur
    private ZonedDateTime utcEnd;
    private long pulses;
    private @Nullable Duration interval;

    public Energy(ZonedDateTime utcEnd, long pulses) {
        this.utcEnd = utcEnd;
        this.pulses = pulses;
    }

    public Energy(ZonedDateTime utcEnd, long pulses, Duration interval) {
        this.utcEnd = utcEnd;
        this.pulses = pulses;
        this.interval = interval;
        updateStart(interval);
    }

    private double correctPulses(double pulses, PowerCalibration calibration) {
        double gainA = calibration.getGainA();
        double gainB = calibration.getGainB();
        double offsetNoise = calibration.getOffsetNoise();
        double offsetTotal = calibration.getOffsetTotal();

        double correctedPulses = Math.pow(pulses + offsetNoise, 2) * gainB + (pulses + offsetNoise) * gainA
                + offsetTotal;
        if ((pulses > 0 && correctedPulses < 0) || (pulses < 0 && correctedPulses > 0)) {
            return 0;
        }
        return correctedPulses;
    }

    public LocalDateTime getEnd() {
        return utcEnd.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public @Nullable Duration getInterval() {
        return interval;
    }

    public long getPulses() {
        return pulses;
    }

    public @Nullable LocalDateTime getStart() {
        ZonedDateTime localUtcStart = utcStart;
        if (localUtcStart == null) {
            return null;
        }
        return localUtcStart.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private double intervalSeconds() {
        Duration localInterval = interval;
        if (localInterval == null) {
            throw new IllegalStateException("Failed to calculate seconds because interval is null");
        }

        double seconds = localInterval.getSeconds();
        seconds += (double) localInterval.getNano() / ChronoUnit.SECONDS.getDuration().toNanos();
        return seconds;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
        updateStart(interval);
    }

    public double tokWh(PowerCalibration calibration) {
        return toWatt(calibration) * intervalSeconds()
                / (ChronoUnit.HOURS.getDuration().getSeconds() * WATTS_PER_KILOWATT);
    }

    @Override
    public String toString() {
        return "Energy [utcStart=" + utcStart + ", utcEnd=" + utcEnd + ", pulses=" + pulses + ", interval=" + interval
                + "]";
    }

    public double toWatt(PowerCalibration calibration) {
        double averagePulses = pulses / intervalSeconds();
        return correctPulses(averagePulses, calibration) / PULSES_PER_W_SECOND;
    }

    private void updateStart(Duration interval) {
        utcStart = utcEnd.minus(interval);
    }
}
