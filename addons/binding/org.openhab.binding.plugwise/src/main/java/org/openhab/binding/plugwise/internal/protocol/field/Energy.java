/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A simple class to represent energy usage, converting between Plugwise data representations.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class Energy {

    private static final int WATTS_PER_KILOWATT = 1000;
    private static final double PULSES_PER_KW_SECOND = 468.9385193;
    private static final double PULSES_PER_W_SECOND = PULSES_PER_KW_SECOND / WATTS_PER_KILOWATT;

    private ZonedDateTime utcStart; // using UTC resolves wrong local start/end timestamps when DST changes occur
    private ZonedDateTime utcEnd;
    private long pulses;
    private Duration interval;

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

    public Duration getInterval() {
        return interval;
    }

    public long getPulses() {
        return pulses;
    }

    public LocalDateTime getStart() {
        return utcStart.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private double intervalSeconds() {
        double seconds = interval.getSeconds();
        seconds += interval.getNano() / ChronoUnit.SECONDS.getDuration().toNanos();
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
