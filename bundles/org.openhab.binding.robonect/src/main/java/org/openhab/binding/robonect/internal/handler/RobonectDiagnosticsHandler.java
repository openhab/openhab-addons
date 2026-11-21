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
import static org.openhab.binding.robonect.internal.handler.RobonectUtils.read;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectDiagnosticsApi;
import org.openhab.binding.robonect.internal.model.AccessPoint;
import org.openhab.binding.robonect.internal.model.Address;
import org.openhab.binding.robonect.internal.model.Battery;
import org.openhab.binding.robonect.internal.model.BatteryCapacity;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.BladeMotor;
import org.openhab.binding.robonect.internal.model.Dhcp;
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles telemetry and diagnostic API endpoints.
 *
 * @author Nico Brüttner - Initial contribution: diagnostics endpoint handling
 */
@NonNullByDefault
final class RobonectDiagnosticsHandler {
    private static final Pattern BATTERY_CHANNEL_PATTERN = Pattern.compile("battery-\\d+-.+");
    private static final Pattern WIRE_CHANNEL_PATTERN = Pattern.compile("wire-sensor-\\d+-.+");
    private final Logger logger = LoggerFactory.getLogger(RobonectDiagnosticsHandler.class);
    private final RobonectHandler handler;
    private final RobonectDiagnosticsApi diagnosticsApi;

    /**
     * Creates a diagnostics component using the Thing handler for client access and state updates.
     *
     * @param handler the {@link RobonectHandler} providing state updates
     * @param diagnosticsApi the diagnostic API for the current client
     */
    RobonectDiagnosticsHandler(RobonectHandler handler, RobonectDiagnosticsApi diagnosticsApi) {
        this.handler = handler;
        this.diagnosticsApi = diagnosticsApi;
    }

    /** Updates non-sensitive WLAN and access-point state. */
    void refreshWlanInfo() {
        WlanInfo info = diagnosticsApi.getWlanInfo();
        if (!info.isSuccessful()) {
            return;
        }
        WifiStation station = info.getStation();
        AccessPoint ap = info.getAp();
        handler.updateSwitch(CHANNEL_WLAN_ENABLED, read(station, WifiStation::getEnable).orElse(null), true);
        handler.setState(CHANNEL_WLAN_MAC, read(station, WifiStation::getMac).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_SSID, read(station, WifiStation::getSsid).map(StringType::new).orElse(null),
                true);
        handler.setState(CHANNEL_WLAN_IP, read(station, WifiStation::getIp).map(StringType::new).orElse(null), true);
        handler.updateSwitch(CHANNEL_WLAN_DHCP, read(station, WifiStation::getDhcp).orElse(null), true);
        handler.updateSwitch(CHANNEL_WLAN_PING, read(station, WifiStation::getPing).orElse(null), true);
        handler.setState(CHANNEL_WLAN_PING_TIME,
                read(station, WifiStation::getPingtime).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_NETMASK, read(station, WifiStation::getNetmask).map(StringType::new).orElse(null),
                true);
        handler.setState(CHANNEL_WLAN_GATEWAY, read(station, WifiStation::getGateway).map(StringType::new).orElse(null),
                true);
        handler.updateSwitch(CHANNEL_WLAN_AP_ENABLED, read(ap, AccessPoint::getEnable).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_MAC, read(ap, AccessPoint::getMac).map(StringType::new).orElse(null), true);
        handler.updateSwitch(CHANNEL_WLAN_AP_HIDDEN, read(ap, AccessPoint::getHidden).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_SSID, read(ap, AccessPoint::getSsid).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_CHANNEL, read(ap, AccessPoint::getChannel).map(DecimalType::new).orElse(null),
                true);
        handler.setState(CHANNEL_WLAN_AP_ENCRYPTION,
                read(ap, AccessPoint::getEncryption).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_MAX_CONNECTIONS,
                read(ap, AccessPoint::getMaxconn).map(DecimalType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_IP, read(ap, AccessPoint::getIp).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_NETMASK, read(ap, AccessPoint::getNetmask).map(StringType::new).orElse(null),
                true);
        handler.setState(CHANNEL_WLAN_AP_GATEWAY, read(ap, AccessPoint::getGateway).map(StringType::new).orElse(null),
                true);
        handler.updateSwitch(CHANNEL_WLAN_AP_DHCP, read(ap, AccessPoint::getDhcp).map(Dhcp::getEnable).orElse(null),
                true);
        handler.setState(CHANNEL_WLAN_AP_DHCP_START,
                read(ap, AccessPoint::getDhcp).map(Dhcp::getStart).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_WLAN_AP_DHCP_END,
                read(ap, AccessPoint::getDhcp).map(Dhcp::getEnd).map(StringType::new).orElse(null), true);
    }

    /**
     * Updates detailed battery channels when the mower supports the battery endpoint.
     */
    void refreshBatteryInfo() {
        BatteryInfo info = diagnosticsApi.getBatteryInfo();
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

    /**
     * Publishes the available measurements for one battery.
     *
     * @param prefix the channel identifier prefix
     * @param battery the battery information to publish
     */
    private void updateBatteryState(String prefix, @Nullable Battery battery) {
        handler.setState(prefix + "id", read(battery, Battery::getId).map(DecimalType::new).orElse(null), true);
        handler.setState(prefix + "charge",
                read(battery, Battery::getCharge).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                true);
        handler.setState(prefix + "voltage", read(battery, Battery::getVoltage)
                .map(value -> new QuantityType<>(value / 1000.0, Units.VOLT)).orElse(null), true);
        handler.setState(prefix + "current", read(battery, Battery::getCurrent)
                .map(value -> new QuantityType<>(value / 1000.0, Units.AMPERE)).orElse(null), true);
        handler.setState(prefix + "temperature", read(battery, Battery::getTemperature)
                .map(value -> new QuantityType<>(value / 10.0, SIUnits.CELSIUS)).orElse(null), true);
        handler.setState(prefix + "capacity", read(battery, Battery::getCapacity).map(BatteryCapacity::getFull)
                .map(value -> new QuantityType<>(value, Units.MILLIAMPERE_HOUR)).orElse(null), true);
        handler.setState(prefix + "remaining-capacity",
                read(battery, Battery::getCapacity).map(BatteryCapacity::getRemaining)
                        .map(value -> new QuantityType<>(value, Units.MILLIAMPERE_HOUR)).orElse(null),
                true);
    }

    /**
     * Updates motor channels when the mower supports the motor endpoint.
     */
    void refreshMotorInfo() {
        MotorInfo info = diagnosticsApi.getMotorInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve motor information: {}", info.getErrorMessage());
            return;
        }
        DriveMotors drive = info.getDrive();
        updateDriveMotor(CHANNEL_LEFT_DRIVE_POWER, CHANNEL_LEFT_DRIVE_SPEED, CHANNEL_LEFT_DRIVE_CURRENT,
                read(drive, DriveMotors::getLeft).orElse(null));
        updateDriveMotor(CHANNEL_RIGHT_DRIVE_POWER, CHANNEL_RIGHT_DRIVE_SPEED, CHANNEL_RIGHT_DRIVE_CURRENT,
                read(drive, DriveMotors::getRight).orElse(null));
        BladeMotor blade = info.getBlade();
        handler.setState(CHANNEL_BLADE_MOTOR_AVERAGE,
                read(blade, BladeMotor::getAverage).map(DecimalType::new).orElse(null), true);
        handler.setState(CHANNEL_BLADE_MOTOR_SPEED,
                read(blade, BladeMotor::getSpeed).map(value -> new QuantityType<>(value, Units.RPM)).orElse(null),
                true);
        handler.setState(CHANNEL_BLADE_MOTOR_CURRENT, read(blade, BladeMotor::getCurrent)
                .map(value -> new QuantityType<>(value / 1000.0, Units.AMPERE)).orElse(null), true);
    }

    /**
     * Updates aggregate operating statistics when the mower supports the hour endpoint.
     */
    void refreshHourInfo() {
        HourInfo info = diagnosticsApi.getHourInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve operating statistics: {}", info.getErrorMessage());
            return;
        }
        HourGeneral general = info.getGeneral();
        handler.setState(CHANNEL_STATISTICS_RUNNING_HOURS,
                read(general, HourGeneral::getRun).map(value -> new QuantityType<>(value, Units.HOUR)).orElse(null),
                true);
        handler.setState(CHANNEL_STATISTICS_MOWING_HOURS,
                read(general, HourGeneral::getMow).map(value -> new QuantityType<>(value, Units.HOUR)).orElse(null),
                true);
        handler.setState(CHANNEL_STATISTICS_SEARCHING_HOURS,
                read(general, HourGeneral::getSearch).map(value -> new QuantityType<>(value, Units.HOUR)).orElse(null),
                true);
        handler.setState(CHANNEL_STATISTICS_CHARGING_HOURS,
                read(general, HourGeneral::getCharge).map(value -> new QuantityType<>(value, Units.HOUR)).orElse(null),
                true);
        handler.setState(CHANNEL_STATISTICS_CHARGES,
                read(general, HourGeneral::getCharges).map(DecimalType::new).orElse(null), true);
        handler.setState(CHANNEL_STATISTICS_ERRORS,
                read(general, HourGeneral::getErrors).map(DecimalType::new).orElse(null), true);
        updateRecentDurations(CHANNEL_STATISTICS_SEEK, info.getSeek());
        updateRecentDurations(CHANNEL_STATISTICS_MOWING, info.getMowing());
    }

    /**
     * Publishes recent durations and their minimum, maximum, and average in minutes.
     *
     * @param channelId the channel identifier
     * @param entries the duration entries, or {@code null} if not reported
     */
    private void updateRecentDurations(String channelId, @Nullable List<HourInfo.DurationEntry> entries) {
        List<Integer> minutes = entries == null ? List.of()
                : entries.stream().flatMap(entry -> read(entry, HourInfo.DurationEntry::getDuration).stream()).toList();
        handler.setState(channelId,
                entries == null || (!entries.isEmpty() && minutes.isEmpty()) ? null
                        : new StringType(minutes.stream().map(String::valueOf).collect(Collectors.joining(", "))),
                true);
        if (minutes.isEmpty()) {
            handler.setState(channelId + "-min", UnDefType.UNDEF, true);
            handler.setState(channelId + "-max", UnDefType.UNDEF, true);
            handler.setState(channelId + "-average", UnDefType.UNDEF, true);
            return;
        }
        IntSummaryStatistics statistics = minutes.stream().mapToInt(Integer::intValue).summaryStatistics();
        handler.setState(channelId + "-min", new QuantityType<>(statistics.getMin(), Units.MINUTE), true);
        handler.setState(channelId + "-max", new QuantityType<>(statistics.getMax(), Units.MINUTE), true);
        handler.setState(channelId + "-average", new QuantityType<>(statistics.getAverage(), Units.MINUTE), true);
    }

    /**
     * Updates the boundary-wire sensor measurements exposed by the mower.
     */
    void refreshWireInfo() {
        WireInfo info = diagnosticsApi.getWireInfo();
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
        PortalInfo info = diagnosticsApi.getPortalInfo();
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve portal information: {}", info.getErrorMessage());
            return;
        }
        handler.updateSwitch(CHANNEL_PORTAL_ENABLED, read(info, PortalInfo::getEnabled).orElse(null), true);
        handler.updateSwitch(CHANNEL_PORTAL_CONNECTED, read(info, PortalInfo::getConnected).orElse(null), true);
        handler.setState(CHANNEL_PORTAL_URL,
                read(info, PortalInfo::getAddress).map(Address::getUrl).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_PORTAL_HOST, read(info, PortalInfo::getAddress).map(Address::getHost)
                .map(Host::getDefault).map(StringType::new).orElse(null), true);
    }

    /**
     * Publishes the available measurements for one boundary-wire sensor.
     *
     * @param number the one-based wire sensor number
     * @param sensor the wire sensor information to publish
     */
    private void updateWireSensor(int number, @Nullable Sensor sensor) {
        String prefix = "wire-sensor-" + number + "-";
        handler.setState(prefix + "label", read(sensor, Sensor::getDescription).map(StringType::new).orElse(null),
                true);
        handler.setState(prefix + "quality",
                read(sensor, Sensor::getQuality).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                true);
        handler.setState(prefix + "a", read(sensor, Sensor::getA).map(DecimalType::new).orElse(null), true);
        handler.setState(prefix + "n", read(sensor, Sensor::getN).map(DecimalType::new).orElse(null), true);
        handler.setState(prefix + "f", read(sensor, Sensor::getF).map(DecimalType::new).orElse(null), true);
        handler.setState(prefix + "g1", read(sensor, Sensor::getG1).map(DecimalType::new).orElse(null), true);
        handler.setState(prefix + "g2", read(sensor, Sensor::getG2).map(DecimalType::new).orElse(null), true);
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
        handler.setState(powerChannel,
                read(motor, DriveMotor::getPower).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                true);
        handler.setState(speedChannel, read(motor, DriveMotor::getSpeed)
                .map(value -> new QuantityType<>(value / 100.0, Units.METRE_PER_SECOND)).orElse(null), true);
        handler.setState(currentChannel, read(motor, DriveMotor::getCurrent)
                .map(value -> new QuantityType<>(value / 1000.0, Units.AMPERE)).orElse(null), true);
    }

    /** Refreshes the Thing properties that identify the mower and its firmware. */
    void refreshVersionInfo() {
        VersionInfo info = diagnosticsApi.getVersionInfo();
        if (!handler.canPublishState()) {
            return;
        }
        if (info.isSuccessful()) {
            Map<String, String> properties = handler.editEndpointProperties();
            var application = info.getApplication();
            var robonect = info.getRobonect();
            if (application != null) {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, valueOrNotAvailable(info.getSerial()));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION,
                        valueOrNotAvailable(read(robonect, VersionInfo.RobonectVersion::getVersion).orElse(null)));
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, valueOrNotAvailable(application.getVersion()));
                properties.put(PROPERTY_COMPILED, valueOrNotAvailable(application.getCompiled()));
                properties.put(PROPERTY_COMMENT, valueOrNotAvailable(application.getComment()));
            } else {
                properties.put(Thing.PROPERTY_SERIAL_NUMBER,
                        valueOrNotAvailable(read(robonect, VersionInfo.RobonectVersion::getSerial).orElse(null)));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, VersionInfo.NOT_AVAILABLE);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                        valueOrNotAvailable(read(robonect, VersionInfo.RobonectVersion::getVersion).orElse(null)));
                properties.put(PROPERTY_COMPILED,
                        valueOrNotAvailable(read(robonect, VersionInfo.RobonectVersion::getCompiled).orElse(null)));
                properties.put(PROPERTY_COMMENT,
                        valueOrNotAvailable(read(robonect, VersionInfo.RobonectVersion::getComment).orElse(null)));
            }
            handler.updateEndpointProperties(properties);
        } else {
            logger.debug("Could not retrieve mower version info. Robonect error response message: {}",
                    info.getErrorMessage());
        }
    }

    /**
     * Returns the reported version value or the not-available marker when absent.
     *
     * @param value the value returned by the mower, or {@code null} if absent
     * @return the supplied value, or {@link VersionInfo#NOT_AVAILABLE} when it is absent
     */
    private String valueOrNotAvailable(@Nullable String value) {
        return value == null ? VersionInfo.NOT_AVAILABLE : value;
    }

    /** Reads the mower error list and applies its most recent entry. */
    void refreshLastErrorInfo() {
        applyLastErrorInfo(diagnosticsApi.errorList());
    }

    /**
     * Applies a successful error list, clearing last-error channels if the list is absent or empty.
     *
     * @param errorList the response containing the error list
     */
    private void applyLastErrorInfo(ErrorList errorList) {
        if (errorList.isSuccessful()) {
            List<ErrorEntry> errors = errorList.getErrors();
            if (errors != null && !errors.isEmpty()) {
                ErrorEntry lastErrorEntry = errors.get(0);
                updateLastErrorChannels(lastErrorEntry);
            } else {
                handler.setState(CHANNEL_LAST_ERROR_MESSAGE, UnDefType.UNDEF, true);
                handler.setState(CHANNEL_LAST_ERROR_CODE, UnDefType.UNDEF, true);
                handler.setState(CHANNEL_LAST_ERROR_DATE, UnDefType.UNDEF, true);
            }
        } else {
            logger.debug("Could not retrieve mower error list. Robonect error response message: {}",
                    errorList.getErrorMessage());
        }
    }

    /**
     * Publishes the fields present in the most recent error entry.
     *
     * @param error the error information to publish
     */
    private void updateLastErrorChannels(@Nullable ErrorEntry error) {
        handler.setState(CHANNEL_LAST_ERROR_MESSAGE,
                read(error, ErrorEntry::getErrorMessage).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_LAST_ERROR_CODE,
                read(error, ErrorEntry::getErrorCode).map(DecimalType::new).orElse(null), true);
        handler.setState(CHANNEL_LAST_ERROR_DATE,
                read(error, ErrorEntry::getUnix).map(handler::convertUnixToDateTimeType).orElse(null), true);
    }
}
