/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.model.AccessPoint;
import org.openhab.binding.robonect.internal.model.Address;
import org.openhab.binding.robonect.internal.model.Battery;
import org.openhab.binding.robonect.internal.model.BatteryCapacity;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.BladeMotor;
import org.openhab.binding.robonect.internal.model.DriveMotor;
import org.openhab.binding.robonect.internal.model.DriveMotors;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.Host;
import org.openhab.binding.robonect.internal.model.HourGeneral;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.MotorInfo;
import org.openhab.binding.robonect.internal.model.PortalInfo;
import org.openhab.binding.robonect.internal.model.Sensor;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.WifiStation;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.WlanInfo;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles telemetry and diagnostic API endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectDiagnosticsHandler {
    private static final Pattern BATTERY_CHANNEL_PATTERN = Pattern.compile("battery-\\d+-.+");
    private static final Pattern WIRE_CHANNEL_PATTERN = Pattern.compile("wire-sensor-\\d+-.+");
    private final Logger logger = LoggerFactory.getLogger(RobonectDiagnosticsHandler.class);
    private final RobonectHandler handler;

    RobonectDiagnosticsHandler(RobonectHandler handler) {
        this.handler = handler;
    }

    /** Updates non-sensitive WLAN and access-point state. */
    void refreshWlanInfo() {
        WlanInfo info = handler.getRobonectClient().getWlanInfo();
        if (!info.isSuccessful()) {
            return;
        }
        WifiStation station = info.getStation();
        if (station != null) {
            if (station.getEnable() != null)
                handler.setState(CHANNEL_WLAN_ENABLED, OnOffType.from(station.getEnable()));
            if (station.getMac() != null)
                handler.setState(CHANNEL_WLAN_MAC, new StringType(station.getMac()));
            if (station.getSsid() != null)
                handler.setState(CHANNEL_WLAN_SSID, new StringType(station.getSsid()));
            if (station.getIp() != null)
                handler.setState(CHANNEL_WLAN_IP, new StringType(station.getIp()));
            if (station.getDhcp() != null)
                handler.setState(CHANNEL_WLAN_DHCP, OnOffType.from(station.getDhcp()));
            if (station.getPing() != null)
                handler.setState(CHANNEL_WLAN_PING, OnOffType.from(station.getPing()));
            if (station.getPingtime() != null)
                handler.setState(CHANNEL_WLAN_PING_TIME, new StringType(station.getPingtime()));
            if (station.getNetmask() != null)
                handler.setState(CHANNEL_WLAN_NETMASK, new StringType(station.getNetmask()));
            if (station.getGateway() != null)
                handler.setState(CHANNEL_WLAN_GATEWAY, new StringType(station.getGateway()));
        }
        AccessPoint ap = info.getAp();
        if (ap != null) {
            if (ap.getEnable() != null)
                handler.setState(CHANNEL_WLAN_AP_ENABLED, OnOffType.from(ap.getEnable()));
            if (ap.getMac() != null)
                handler.setState(CHANNEL_WLAN_AP_MAC, new StringType(ap.getMac()));
            if (ap.getHidden() != null)
                handler.setState(CHANNEL_WLAN_AP_HIDDEN, OnOffType.from(ap.getHidden()));
            if (ap.getSsid() != null)
                handler.setState(CHANNEL_WLAN_AP_SSID, new StringType(ap.getSsid()));
            Integer channel = ap.getChannel();
            if (channel != null)
                handler.setState(CHANNEL_WLAN_AP_CHANNEL, new DecimalType(channel));
            if (ap.getEncryption() != null)
                handler.setState(CHANNEL_WLAN_AP_ENCRYPTION, new StringType(ap.getEncryption()));
            Integer maxConnections = ap.getMaxconn();
            if (maxConnections != null)
                handler.setState(CHANNEL_WLAN_AP_MAX_CONNECTIONS, new DecimalType(maxConnections));
            if (ap.getIp() != null)
                handler.setState(CHANNEL_WLAN_AP_IP, new StringType(ap.getIp()));
            if (ap.getNetmask() != null)
                handler.setState(CHANNEL_WLAN_AP_NETMASK, new StringType(ap.getNetmask()));
            if (ap.getGateway() != null)
                handler.setState(CHANNEL_WLAN_AP_GATEWAY, new StringType(ap.getGateway()));
            if (ap.getDhcp() != null) {
                if (ap.getDhcp().getEnable() != null)
                    handler.setState(CHANNEL_WLAN_AP_DHCP, OnOffType.from(ap.getDhcp().getEnable()));
                if (ap.getDhcp().getStart() != null)
                    handler.setState(CHANNEL_WLAN_AP_DHCP_START, new StringType(ap.getDhcp().getStart()));
                if (ap.getDhcp().getEnd() != null)
                    handler.setState(CHANNEL_WLAN_AP_DHCP_END, new StringType(ap.getDhcp().getEnd()));
            }
        }
    }

    /**
     * Updates detailed battery channels when the mower supports the battery endpoint.
     */
    void refreshBatteryInfo() {
        BatteryInfo info = handler.getRobonectClient().getBatteryInfo();
        List<Battery> batteries = info.getBatteries();
        if (!info.isSuccessful() || batteries == null) {
            logger.debug("Could not retrieve detailed battery information: {}", info.getErrorMessage());
            return;
        }
        List<Channel> channels = new ArrayList<>();
        for (int index = 0; index < batteries.size(); index++) {
            String prefix = "battery-" + (index + 1) + "-";
            String label = "Battery " + (index + 1) + " ";
            channels.add(handler.createChannel(prefix + "id", "Number", "batteryIdType", label + "ID"));
            channels.add(handler.createChannel(prefix + "charge", "Number:Dimensionless", "batteryChargeType",
                    label + "Charge"));
            channels.add(handler.createChannel(prefix + "voltage", "Number:ElectricPotential", "batteryVoltageType",
                    label + "Voltage"));
            channels.add(handler.createChannel(prefix + "current", "Number:ElectricCurrent", "batteryCurrentType",
                    label + "Current"));
            channels.add(handler.createChannel(prefix + "temperature", "Number:Temperature", "batteryTemperatureType",
                    label + "Temperature"));
            channels.add(handler.createChannel(prefix + "capacity", "Number:ElectricCharge", "batteryCapacityType",
                    label + "Full Capacity"));
            channels.add(handler.createChannel(prefix + "remaining-capacity", "Number:ElectricCharge",
                    "batteryRemainingCapacityType", label + "Remaining Capacity"));
        }
        handler.updateDynamicChannels(BATTERY_CHANNEL_PATTERN, channels);
        for (int index = 0; index < batteries.size(); index++) {
            updateBatteryState("battery-" + (index + 1) + "-", batteries.get(index));
        }
    }

    private void updateBatteryState(String prefix, Battery battery) {
        Integer id = battery.getId();
        if (id != null) {
            handler.setState(prefix + "id", new DecimalType(id));
        }
        Integer charge = battery.getCharge();
        if (charge != null) {
            handler.setState(prefix + "charge", new QuantityType<>(charge, Units.PERCENT));
        }
        Integer voltage = battery.getVoltage();
        if (voltage != null) {
            handler.setState(prefix + "voltage", new QuantityType<>(voltage / 1000.0, Units.VOLT));
        }
        Integer current = battery.getCurrent();
        if (current != null) {
            handler.setState(prefix + "current", new QuantityType<>(current / 1000.0, Units.AMPERE));
        }
        Integer temperature = battery.getTemperature();
        if (temperature != null) {
            handler.setState(prefix + "temperature", new QuantityType<>(temperature / 10.0, SIUnits.CELSIUS));
        }
        BatteryCapacity capacity = battery.getCapacity();
        if (capacity != null) {
            Integer full = capacity.getFull();
            if (full != null) {
                handler.setState(prefix + "capacity", new QuantityType<>(full, Units.MILLIAMPERE_HOUR));
            }
            Integer remainingCapacity = capacity.getRemaining();
            if (remainingCapacity != null) {
                handler.setState(prefix + "remaining-capacity",
                        new QuantityType<>(remainingCapacity, Units.MILLIAMPERE_HOUR));
            }
        }
    }

    /**
     * Updates motor channels when the mower supports the motor endpoint.
     */
    void refreshMotorInfo() {
        MotorInfo info = handler.getRobonectClient().getMotorInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve motor information: {}", info.getErrorMessage());
            return;
        }
        DriveMotors drive = info.getDrive();
        if (drive != null) {
            updateDriveMotor(CHANNEL_LEFT_DRIVE_POWER, CHANNEL_LEFT_DRIVE_SPEED, CHANNEL_LEFT_DRIVE_CURRENT,
                    drive.getLeft());
            updateDriveMotor(CHANNEL_RIGHT_DRIVE_POWER, CHANNEL_RIGHT_DRIVE_SPEED, CHANNEL_RIGHT_DRIVE_CURRENT,
                    drive.getRight());
        }
        BladeMotor blade = info.getBlade();
        if (blade != null) {
            Integer average = blade.getAverage();
            if (average != null) {
                handler.setState(CHANNEL_BLADE_MOTOR_AVERAGE, new DecimalType(average));
            }
            Integer speed = blade.getSpeed();
            if (speed != null) {
                handler.setState(CHANNEL_BLADE_MOTOR_SPEED, new QuantityType<>(speed, Units.RPM));
            }
            if (blade.getCurrent() != null) {
                handler.setState(CHANNEL_BLADE_MOTOR_CURRENT,
                        new QuantityType<>(blade.getCurrent() / 1000.0, Units.AMPERE));
            }
        }
    }

    /**
     * Updates aggregate operating statistics when the mower supports the hour endpoint.
     */
    void refreshHourInfo() {
        HourInfo info = handler.getRobonectClient().getHourInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve operating statistics: {}", info.getErrorMessage());
            return;
        }
        HourGeneral general = info.getGeneral();
        if (general != null) {
            updateHours(CHANNEL_STATISTICS_RUNNING_HOURS, general.getRun());
            updateHours(CHANNEL_STATISTICS_MOWING_HOURS, general.getMow());
            updateHours(CHANNEL_STATISTICS_SEARCHING_HOURS, general.getSearch());
            updateHours(CHANNEL_STATISTICS_CHARGING_HOURS, general.getCharge());
            Integer charges = general.getCharges();
            if (charges != null) {
                handler.setState(CHANNEL_STATISTICS_CHARGES, new DecimalType(charges));
            }
            Integer errors = general.getErrors();
            if (errors != null) {
                handler.setState(CHANNEL_STATISTICS_ERRORS, new DecimalType(errors));
            }
        }
        updateRecentDurations(CHANNEL_STATISTICS_SEEK, info.getSeek());
        updateRecentDurations(CHANNEL_STATISTICS_MOWING, info.getMowing());
    }

    private void updateRecentDurations(String channelId, @Nullable List<HourInfo.DurationEntry> entries) {
        if (entries == null) {
            return;
        }
        List<Integer> minutes = new ArrayList<>();
        for (HourInfo.DurationEntry entry : entries) {
            @Nullable
            Integer duration = entry.getDuration();
            if (duration != null) {
                minutes.add(duration);
            }
        }
        handler.setState(channelId,
                new StringType(minutes.stream().map(String::valueOf).collect(Collectors.joining(", "))));
        if (minutes.isEmpty()) {
            handler.setState(channelId + "-min", UnDefType.UNDEF);
            handler.setState(channelId + "-max", UnDefType.UNDEF);
            handler.setState(channelId + "-average", UnDefType.UNDEF);
            return;
        }
        IntSummaryStatistics statistics = minutes.stream().mapToInt(Integer::intValue).summaryStatistics();
        handler.setState(channelId + "-min", new QuantityType<>(statistics.getMin(), Units.MINUTE));
        handler.setState(channelId + "-max", new QuantityType<>(statistics.getMax(), Units.MINUTE));
        handler.setState(channelId + "-average", new QuantityType<>(statistics.getAverage(), Units.MINUTE));
    }

    /**
     * Updates the boundary-wire sensor measurements exposed by the mower.
     */
    void refreshWireInfo() {
        WireInfo info = handler.getRobonectClient().getWireInfo();
        List<Sensor> sensors = info.getSensors();
        if (!info.isSuccessful() || sensors == null) {
            logger.debug("Could not retrieve boundary-wire information: {}", info.getErrorMessage());
            return;
        }
        List<Channel> channels = new ArrayList<>();
        for (int index = 0; index < sensors.size(); index++) {
            String prefix = "wire-sensor-" + (index + 1) + "-";
            String label = "Wire Sensor " + (index + 1) + " ";
            channels.add(handler.createChannel(prefix + "label", "String", "wireSensorLabelType", label + "Label"));
            channels.add(handler.createChannel(prefix + "quality", "Number:Dimensionless", "wireSensorQualityType",
                    label + "Signal Quality"));
            for (String measurement : List.of("a", "n", "f", "g1", "g2")) {
                channels.add(handler.createChannel(prefix + measurement, "Number", "wireSensorValueType",
                        label + "Signal " + measurement.toUpperCase(Locale.ROOT)));
            }
        }
        handler.updateDynamicChannels(WIRE_CHANNEL_PATTERN, channels);
        for (int index = 0; index < sensors.size(); index++) {
            updateWireSensor(index + 1, sensors.get(index));
        }
    }

    /**
     * Updates remote portal status when supported by the module.
     */
    void refreshPortalInfo() {
        PortalInfo info = handler.getRobonectClient().getPortalInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve portal information: {}", info.getErrorMessage());
            return;
        }
        if (info.getEnabled() != null)
            handler.setState(CHANNEL_PORTAL_ENABLED, OnOffType.from(info.getEnabled()));
        if (info.getConnected() != null)
            handler.setState(CHANNEL_PORTAL_CONNECTED, OnOffType.from(info.getConnected()));
        Address address = info.getAddress();
        if (address != null) {
            if (address.getUrl() != null)
                handler.setState(CHANNEL_PORTAL_URL, new StringType(address.getUrl()));
            Host host = address.getHost();
            if (host != null && host.getDefault() != null)
                handler.setState(CHANNEL_PORTAL_HOST, new StringType(host.getDefault()));
        }
    }

    private void updateWireSensor(int number, Sensor sensor) {
        String prefix = "wire-sensor-" + number + "-";
        if (sensor.getDescription() != null)
            handler.setState(prefix + "label", new StringType(sensor.getDescription()));
        Integer quality = sensor.getQuality();
        if (quality != null)
            handler.setState(prefix + "quality", new QuantityType<>(quality, Units.PERCENT));
        Integer a = sensor.getA();
        if (a != null)
            handler.setState(prefix + "a", new DecimalType(a));
        Integer n = sensor.getN();
        if (n != null)
            handler.setState(prefix + "n", new DecimalType(n));
        Integer f = sensor.getF();
        if (f != null)
            handler.setState(prefix + "f", new DecimalType(f));
        Integer g1 = sensor.getG1();
        if (g1 != null)
            handler.setState(prefix + "g1", new DecimalType(g1));
        Integer g2 = sensor.getG2();
        if (g2 != null)
            handler.setState(prefix + "g2", new DecimalType(g2));
    }

    private void updateHours(String channel, @Nullable Integer value) {
        if (value != null) {
            handler.setState(channel, new QuantityType<>(value, Units.HOUR));
        }
    }

    /**
     * Updates the channels for one drive motor.
     *
     * @param powerChannel the channel for motor power
     * @param speedChannel the channel for motor speed
     * @param currentChannel the channel for motor current
     * @param motor the API result for the motor
     */
    private void updateDriveMotor(String powerChannel, String speedChannel, String currentChannel,
            @Nullable DriveMotor motor) {
        if (motor == null) {
            return;
        }
        Integer power = motor.getPower();
        if (power != null) {
            handler.setState(powerChannel, new QuantityType<>(power, Units.PERCENT));
        }
        if (motor.getSpeed() != null) {
            handler.setState(speedChannel, new QuantityType<>(motor.getSpeed() / 100.0, Units.METRE_PER_SECOND));
        }
        if (motor.getCurrent() != null) {
            handler.setState(currentChannel, new QuantityType<>(motor.getCurrent() / 1000.0, Units.AMPERE));
        }
    }

    void refreshVersionInfo() {
        VersionInfo info = handler.getRobonectClient().getVersionInfo();
        if (!handler.canPublishState()) {
            return;
        }
        if (info.isSuccessful()) {
            Map<String, String> properties = handler.editEndpointProperties();
            if (info.getApplication() != null) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, valueOrNotAvailable(info.getSerial()));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, valueOrNotAvailable(info.getRobonect().getVersion()));
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                        valueOrNotAvailable(info.getApplication().getVersion()));
                properties.put(PROPERTY_COMPILED, valueOrNotAvailable(info.getApplication().getCompiled()));
                properties.put(PROPERTY_COMMENT, valueOrNotAvailable(info.getApplication().getComment()));
            } else {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, valueOrNotAvailable(info.getRobonect().getSerial()));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, VersionInfo.NOT_AVAILABLE);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, valueOrNotAvailable(info.getRobonect().getVersion()));
                properties.put(PROPERTY_COMPILED, valueOrNotAvailable(info.getRobonect().getCompiled()));
                properties.put(PROPERTY_COMMENT, valueOrNotAvailable(info.getRobonect().getComment()));
            }
            handler.updateEndpointProperties(properties);
        } else {
            logger.debug("Could not retrieve mower version info. Robonect error response message: {}",
                    info.getErrorMessage());
        }
    }

    private String valueOrNotAvailable(@Nullable String value) {
        return value == null ? VersionInfo.NOT_AVAILABLE : value;
    }

    void refreshLastErrorInfo() {
        applyLastErrorInfo(handler.getRobonectClient().errorList());
    }

    private void applyLastErrorInfo(ErrorList errorList) {
        if (errorList.isSuccessful()) {
            List<ErrorEntry> errors = errorList.getErrors();
            if (errors != null && !errors.isEmpty()) {
                ErrorEntry lastErrorEntry = errors.get(0);
                updateLastErrorChannels(lastErrorEntry);
            } else {
                handler.setState(CHANNEL_LAST_ERROR_MESSAGE, UnDefType.UNDEF);
                handler.setState(CHANNEL_LAST_ERROR_CODE, UnDefType.UNDEF);
                handler.setState(CHANNEL_LAST_ERROR_DATE, UnDefType.UNDEF);
            }
        } else {
            logger.debug("Could not retrieve mower error list. Robonect error response message: {}",
                    errorList.getErrorMessage());
        }
    }

    private void updateLastErrorChannels(ErrorEntry error) {
        if (error.getErrorMessage() != null) {
            handler.setState(CHANNEL_LAST_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
        if (error.getErrorCode() != null) {
            handler.setState(CHANNEL_LAST_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if (error.getDate() != null) {
            State dateTime = handler.convertUnixToDateTimeType(error.getUnix());
            handler.setState(CHANNEL_LAST_ERROR_DATE, dateTime);
        }
    }
}
