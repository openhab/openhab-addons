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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeterEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcMeter2} class represents the meter Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control meter and has methods to receive meter usage information.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcMeter2 extends NhcMeter {

    private final Logger logger = LoggerFactory.getLogger(NhcMeter2.class);

    private final String deviceType;
    private final String deviceTechnology;
    private final String deviceModel;

    protected Map<String, Double> readings = new ConcurrentHashMap<>();
    protected Map<String, Double> dayReadings = new ConcurrentHashMap<>();

    protected NhcMeter2(String id, String name, MeterType meterType, String deviceType, String deviceTechnology,
            String deviceModel, @Nullable LocalDateTime referenceDate, @Nullable String location,
            NikoHomeControlCommunication nhcComm, ScheduledExecutorService scheduler) {
        super(id, name, meterType, referenceDate, location, nhcComm, scheduler);
        this.deviceType = deviceType;
        this.deviceTechnology = deviceTechnology;
        this.deviceModel = deviceModel;
    }

    /**
     * @return type as returned from Niko Home Control
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * @return technology as returned from Niko Home Control
     */
    public String getDeviceTechnology() {
        return deviceTechnology;
    }

    /**
     * @return model as returned from Niko Home Control
     */
    public String getDeviceModel() {
        return deviceModel;
    }

    @Override
    public Map<String, Double> getReadings() {
        return readings;
    }

    @Override
    public Map<String, Double> getDayReadings() {
        return dayReadings;
    }

    @Override
    public void setReadings(Map<String, Double> readings, Map<String, Double> dayReadings, LocalDateTime lastReading) {
        if (readings.isEmpty() || dayReadings.isEmpty()) {
            return;
        }
        this.readings = readings;
        this.dayReadings = dayReadings;
        this.lastReadingUTC = lastReading;
        updateReadingState();
    }

    @Override
    protected void updateReadingState() {
        NhcMeterEvent handler = eventHandler;
        Map<String, Double> readings = getReadings();
        Map<String, Double> dayReadings = getDayReadings();
        LocalDateTime lastReading = getLastReading();
        if ((handler != null) && (lastReading != null)) {
            if (logger.isDebugEnabled()) {
                String readingsString = "[" + readings.entrySet().stream()
                        .map(e -> String.format("%s: %s", e.getKey(), e.getValue())).collect(Collectors.joining(", "))
                        + "]";
                String dayReadingsString = "[" + dayReadings.entrySet().stream()
                        .map(e -> String.format("%s: %s", e.getKey(), e.getValue())).collect(Collectors.joining(", "))
                        + "]";
                logger.debug("update meter reading channels for {} with {}, day {}", id, readingsString,
                        dayReadingsString);
            }
            handler.meterReadingEvent(readings, dayReadings, lastReading);
        }
    }
}
