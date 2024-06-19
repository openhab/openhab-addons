/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.vo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.worxlandroid.internal.api.dto.Commands.ZoneMeterCommand;
import org.openhab.binding.worxlandroid.internal.api.dto.LastStatus;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Battery;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Cfg;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Dat;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Dat.Axis;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Ots;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Rain;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Schedule;
import org.openhab.binding.worxlandroid.internal.api.dto.Payload.Stat;
import org.openhab.binding.worxlandroid.internal.api.dto.ProductItemStatus;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidDayCodes;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidStatusCodes;
import org.openhab.binding.worxlandroid.internal.handler.WorxLandroidMowerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Mower}
 *
 * @author Nils - Initial contribution
 */
@NonNullByDefault
public class Mower {
    private static final int[] MULTI_ZONE_METER_DISABLE = { 0, 0, 0, 0 };
    private static final int[] MULTI_ZONE_METER_ENABLE = { 1, 0, 0, 0 };
    private static final int TIME_EXTENSION_DISABLE = -100;

    private final Logger logger = LoggerFactory.getLogger(Mower.class);
    private final WorxLandroidMowerHandler mowerHandler;
    private final ProductItemStatus product;

    private final int[] zoneMeter;
    private final int[] zoneMeterRestore;
    private final int[] allocations = new int[10];
    private final List<Map<WorxLandroidDayCodes, @Nullable ScheduledDay>> schedules = new ArrayList<>();

    private boolean multiZoneEnable;
    private int timeExtension;
    private int timeExtensionRestore = 0;
    private @NonNullByDefault({}) LastStatus lastStatus;

    private boolean restoreZoneMeter = false;
    private int[] zoneMeterRestoreValues = {};

    public Mower(WorxLandroidMowerHandler mowerHandler, ProductItemStatus product) {
        this.mowerHandler = mowerHandler;
        this.product = product;
        this.zoneMeter = new int[getMultiZoneCount()];
        this.zoneMeterRestore = new int[getMultiZoneCount()];

        schedules.add(new HashMap<WorxLandroidDayCodes, @Nullable ScheduledDay>(7));
        if (product.capabilities.contains("scheduler_two_slots")) {
            schedules.add(new HashMap<WorxLandroidDayCodes, @Nullable ScheduledDay>(7));
        }
        setStatus(product.lastStatus.payload);
    }

    public String getSerialNumber() {
        return product.serialNumber;
    }

    public int getTimeExtension() {
        return timeExtension;
    }

    public Double getFirmwareVersionAsDouble() {
        // Most of the time it is xx.y but also seen xx.y.z+1 we'll keep only the beginning
        String[] versionParts = getFirmwareVersion().split("\\.");
        return Double.valueOf("%s.%s".formatted(versionParts[0], versionParts[1]));
    }

    public String getFirmwareVersion() {
        return product.firmwareVersion;
    }

    /**
     * timeExtension = -100 disables mowing (enable=false).
     * timeExtension > -100 enables mowing (enable=true).
     *
     * @param timeExtension
     */
    public void setTimeExtension(int timeExtension) {
        if (timeExtension == TIME_EXTENSION_DISABLE) {
            storeTimeExtension();
        }
        this.timeExtension = timeExtension;
    }

    public boolean lockSupported() {
        return product.capabilities.contains("lock");
    }

    public boolean rainDelaySupported() {
        return product.capabilities.contains("rain_delay");
    }

    public boolean rainDelayStartSupported() {
        return product.capabilities.contains("rain_delay_start");
    }

    public boolean multiZoneSupported() {
        return product.capabilities.contains("multi_zone");
    }

    public boolean scheduler2Supported() {
        return schedules.size() > 1;
    }

    public boolean oneTimeSchedulerSupported() {
        return product.capabilities.contains("one_time_scheduler");
    }

    public @Nullable ScheduledDay getScheduledDay(int scDSlot, WorxLandroidDayCodes dayCode) {
        return scDSlot == 1 ? schedules.get(0).get(dayCode)
                : scheduler2Supported() ? schedules.get(1).get(dayCode) : null;
    }

    private Object[] getScheduleArray(Map<WorxLandroidDayCodes, @Nullable ScheduledDay> schedules) {
        Object[] result = new Object[7];
        for (WorxLandroidDayCodes dayCode : WorxLandroidDayCodes.values()) {
            ScheduledDay schedule = schedules.get(dayCode);
            result[dayCode.code] = schedule != null ? schedule.asArray() : ScheduledDay.BLANK.asArray();
        }
        return result;
    }

    public Object[] getSheduleArray1() {
        return getScheduleArray(schedules.get(0));
    }

    public Object[] getSheduleArray2() {
        return scheduler2Supported() ? getScheduleArray(schedules.get(1)) : new Object[] {};
    }

    public boolean isMultiZoneEnable() {
        return multiZoneEnable;
    }

    public void setMultiZoneEnable(boolean multiZoneEnable) {
        this.multiZoneEnable = multiZoneEnable;

        if (multiZoneEnable && isZoneMeterDisabled()) {
            restoreZoneMeter();
            if (isZoneMeterDisabled()) {
                System.arraycopy(MULTI_ZONE_METER_ENABLE, 0, zoneMeter, 0, zoneMeter.length);
            }
        } else {
            storeZoneMeter();
            System.arraycopy(MULTI_ZONE_METER_DISABLE, 0, zoneMeter, 0, zoneMeter.length);
        }
    }

    public int getZoneMeter(int zoneIndex) {
        return zoneMeter[zoneIndex];
    }

    public int[] getZoneMeters() {
        return Arrays.copyOf(zoneMeter, zoneMeter.length);
    }

    public int getZonesSize() {
        return getZoneMeters().length;
    }

    public void setZoneMeters(int[] zoneMeterInput) {
        System.arraycopy(zoneMeterInput, 0, zoneMeter, 0, zoneMeter.length);
    }

    public void setZoneMeter(int zoneIndex, int meter) {
        zoneMeter[zoneIndex] = meter;
        this.multiZoneEnable = !isZoneMeterDisabled();
    }

    public int getAllocation(int allocationIndex) {
        return allocations[allocationIndex];
    }

    public int[] getAllocations() {
        return Arrays.copyOf(allocations, allocations.length);
    }

    public int getAllocationsSize() {
        return getZoneMeters().length;
    }

    public void setAllocation(int allocationIndex, int zoneIndex) {
        allocations[allocationIndex] = zoneIndex;
    }

    public boolean isEnable() {
        return timeExtension != TIME_EXTENSION_DISABLE;
    }

    /**
     * Enable/Disables mowing using timeExtension.
     * disable: timeExtension = -100
     * enable: timeExtension > -100
     *
     */
    public void setEnable(boolean enable) {
        if (enable && timeExtension == TIME_EXTENSION_DISABLE) {
            restoreTimeExtension();
        } else {
            storeTimeExtension();
            timeExtension = TIME_EXTENSION_DISABLE;
        }
    }

    /**
     * Stores timeExtension to timeExtensionRestore for restore,
     */
    private void storeTimeExtension() {
        if (this.timeExtension > TIME_EXTENSION_DISABLE) {
            this.timeExtensionRestore = this.timeExtension;
        }
    }

    /**
     * Restores timeExtension from timeExtensionRestore.
     */
    private void restoreTimeExtension() {
        this.timeExtension = this.timeExtensionRestore;
    }

    /**
     * Stores zoneMeter to zoneMeterRestore for restore,
     */
    private void storeZoneMeter() {
        if (!isZoneMeterDisabled()) {
            System.arraycopy(zoneMeter, 0, zoneMeterRestore, 0, zoneMeter.length);
        }
    }

    /**
     * Restores zoneMeter from zoneMeterRestore.
     */
    private void restoreZoneMeter() {
        System.arraycopy(zoneMeterRestore, 0, zoneMeter, 0, zoneMeter.length);
    }

    /**
     * @return false if less than 2 meters are 0
     */
    private boolean isZoneMeterDisabled() {
        return Arrays.stream(zoneMeter).sum() == 0;
    }

    public int getMultiZoneCount() {
        return multiZoneSupported() ? product.lastStatus.payload.cfg.multizoneAllocations.size() : 0;
    }

    public String getMqttCommandIn() {
        return product.mqttTopics.commandIn;
    }

    public String getMqttCommandOut() {
        return product.mqttTopics.commandOut;
    }

    public String getMacAddress() {
        return product.macAddress;
    }

    public String getId() {
        return product.id;
    }

    public String getLanguage() {
        return getPayload().cfg.lg;
    }

    public Payload getPayload() {
        return lastStatus.payload;
    }

    public Dat getPayloadDat() {
        return getPayload().dat;
    }

    public Cfg getPayloadCfg() {
        return getPayload().cfg;
    }

    public void setStatus(Payload payload) {
        this.lastStatus = new LastStatus(payload);
        if (restoreZoneMeter && getStatusCode() != WorxLandroidStatusCodes.HOME
                && getStatusCode() != WorxLandroidStatusCodes.START_SEQUENCE
                && getStatusCode() != WorxLandroidStatusCodes.LEAVING_HOME
                && getStatusCode() != WorxLandroidStatusCodes.SEARCHING_ZONE) {
            restoreZoneMeter = false;
            setZoneMeters(zoneMeterRestoreValues);
            sendCommand(new ZoneMeterCommand(getZoneMeters()));
        }

        getSchedule().ifPresent(schedule -> {
            setTimeExtension(schedule.timeExtension);
            if (schedule.d != null) {
                updateSchedules(0, schedule.d);
                if (schedule.dd != null) {
                    updateSchedules(1, schedule.dd);
                }
            }
        });

        Cfg cfg = getPayloadCfg();
        if (multiZoneSupported()) {
            for (int zoneIndex = 0; zoneIndex < cfg.multiZones.size(); zoneIndex++) {
                setZoneMeter(zoneIndex, cfg.multiZones.get(zoneIndex));
            }

            for (int allocationIndex = 0; allocationIndex < cfg.multizoneAllocations.size(); allocationIndex++) {
                setAllocation(allocationIndex, cfg.multizoneAllocations.get(allocationIndex));
            }
        }
    }

    private void updateSchedules(int scDSlot, List<List<String>> d) {
        Map<WorxLandroidDayCodes, @Nullable ScheduledDay> planning = schedules.get(scDSlot);
        EnumSet.allOf(WorxLandroidDayCodes.class).stream().forEach(dayCode -> {
            List<String> schedule = d.get(dayCode.code);
            planning.put(dayCode,
                    new ScheduledDay(schedule.get(0), Integer.valueOf(schedule.get(1)), "1".equals(schedule.get(2))));
        });
    }

    public Optional<Battery> getBattery() {
        return Optional.ofNullable(getPayloadDat().battery);
    }

    public Optional<Rain> getRain() {
        return Optional.ofNullable(getPayloadDat().rain);
    }

    public double getAngle(Axis axis) {
        return getPayloadDat().getAngle(axis);
    }

    public Optional<Stat> getStats() {
        return Optional.ofNullable(getPayloadDat().st);
    }

    public int getLastZone() {
        return getAllocation(getPayloadDat().lastZone);
    }

    public long getCurrentBladeTime() {
        return getTotalBladeTime() - product.bladeWorkTimeReset;
    }

    public long getTotalBladeTime() {
        return lastStatus.payload.dat.st.bladeWorkTime;
    }

    public int getCurrentChargeCycles() {
        return product.batteryChargeCycles - product.batteryChargeCyclesReset;
    }

    public int getTotalChargeCycles() {
        return lastStatus.payload.dat.battery.chargeCycle;
    }

    public WorxLandroidStatusCodes getStatusCode() {
        return getPayloadDat().statusCode;
    }

    public void setZoneTo(int zoneIndex) {
        zoneMeterRestoreValues = getZoneMeters();
        restoreZoneMeter = true;

        int meter = getZoneMeter(zoneIndex);
        for (int index = 0; index < 4; index++) {
            setZoneMeter(index, meter);
        }
    }

    private void sendCommand(Object command) {
        logger.debug("send command: {}", command);
        mowerHandler.publishMessage(getMqttCommandIn(), command);
    }

    public ZonedDateTime getLastUpdate() {
        return getPayloadCfg().getDateTime(product.timeZone);
    }

    public Optional<Schedule> getSchedule() {
        return Optional.ofNullable(getPayloadCfg().sc);
    }

    public Optional<Ots> getOneTimeSchedule() {
        return getSchedule().map(sc -> Optional.ofNullable(sc.ots)).orElse(Optional.empty());
    }
}
