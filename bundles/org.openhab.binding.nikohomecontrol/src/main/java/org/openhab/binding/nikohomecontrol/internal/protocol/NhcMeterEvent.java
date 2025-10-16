/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NhcMeterEvent} interface is used to pass meter events received from the Niko Home Control controller to
 * the consuming client. It is designed to pass events to openHAB handlers that implement this interface. Because of the
 * design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used independent of
 * openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcMeterEvent extends NhcBaseEvent {

    /**
     * This method is called when a meter event is received from the Niko Home Control controller.
     *
     * @param power current power consumption/production in W (positive for consumption), null for an empty reading
     */
    void meterPowerEvent(@Nullable Double power);

    /**
     * This method is called when a meter event is received from the Niko Home Control controller and separate
     * information is available for power to grid and power from grid.
     *
     * @param power current power consumption/production in W (positive for consumption), null for an empty reading
     * @param powerFromGrid current power consumption from grid in W, null for an empty reading
     * @param powerToGrid current power sent to grid in W, null for an empty reading
     */
    default void meterPowerEvent(@Nullable Double power, @Nullable Double powerFromGrid, @Nullable Double powerToGrid) {
        meterPowerEvent(power);
    }

    /**
     * This method is called when a meter peak power from grid event is received from the Niko Home Control controller.
     *
     * @param peakPowerFromGrid current month peak power from grid
     */
    default void meterPeakPowerFromGridEvent(double peakPowerFromGrid) {
    }

    /**
     * This method is called when a meter reading is received from the Niko Home Control controller.
     *
     * @param reading meter reading
     * @param dayReading meter reading for current day
     * @param lastReadingUTC last meter reading date and time, UTC
     */
    void meterReadingEvent(double reading, double dayReading, LocalDateTime lastReadingUTC);

    /**
     * This method is called when meter readings are received from the Niko Home Control controller.
     * This method should be used for meters that register multiple measurements at the same time.
     * The keys of the argument maps are the keys to the readings as received from the controller.
     *
     * @param readings meter readings
     * @param dayReadings meter readings for current day
     * @param lastReadingUTC last meter reading date and time, UTC
     */
    void meterReadingEvent(Map<String, Double> readings, Map<String, Double> dayReadings, LocalDateTime lastReadingUTC);
}
