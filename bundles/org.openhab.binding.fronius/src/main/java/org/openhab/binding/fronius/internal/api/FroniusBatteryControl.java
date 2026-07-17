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
package org.openhab.binding.fronius.internal.api;

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.API_TIMEOUT;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fronius.internal.api.dto.inverter.PostConfigResponse;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.ScheduleType;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecord;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecords;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeTableRecord;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.WeekdaysRecord;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.firmware.types.SemverVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link FroniusBatteryControl} is responsible for controlling the battery of Fronius hybrid inverters through the
 * battery management's time-dependent battery control settings.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class FroniusBatteryControl {
    private static final String TIME_OF_USE_ENDPOINT = "/config/timeofuse";
    private static final String BATTERIES_ENDPOINT = "/config/batteries";
    private static final String NIGHT_PRESERVATION_LIMIT_ENDPOINT = "/commands/GetNightPreservationLimit";
    private static final String BACKUP_RESERVED_CAPACITY_PARAMETER = "HYB_BACKUP_RESERVED";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final LocalTime BEGIN_OF_DAY = LocalTime.of(0, 0);
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59);

    private final Logger logger = LoggerFactory.getLogger(FroniusBatteryControl.class);
    private final Gson gson = new Gson();
    private final FroniusHttpUtil httpUtil;
    private final HttpClient httpClient;
    private final SemverVersion firmwareVersion;
    private final URI baseUri;
    private final String username;
    private final String password;
    private final URI timeOfUseUri;
    private final URI batteriesUri;
    private final URI nightPreservationLimitUri;

    /**
     * Creates a new instance of {@link FroniusBatteryControl}.
     *
     * @param httpUtil the HTTP utility to use for bridge-scoped request coordination
     * @param httpClient the HTTP client to use
     * @param firmwareVersion the firmware version of the inverter
     * @param scheme http or https
     * @param hostname the hostname or IP address of the inverter
     * @param username the username for the inverter Web UI
     * @param password the password for the inverter Web UI
     */
    public FroniusBatteryControl(FroniusHttpUtil httpUtil, HttpClient httpClient, SemverVersion firmwareVersion,
            String scheme, String hostname, String username, String password) {
        this.httpUtil = httpUtil;
        this.httpClient = httpClient;
        this.firmwareVersion = firmwareVersion;
        this.baseUri = getBaseUri(firmwareVersion, scheme, hostname);
        this.username = username;
        this.password = password;
        this.timeOfUseUri = URI.create(baseUri + TIME_OF_USE_ENDPOINT);
        this.batteriesUri = URI.create(baseUri + BATTERIES_ENDPOINT);
        this.nightPreservationLimitUri = URI.create(baseUri + NIGHT_PRESERVATION_LIMIT_ENDPOINT);
    }

    private static URI getBaseUri(SemverVersion firmwareVersion, String scheme, String hostname) {
        String apiPrefix = "";
        if (firmwareVersion.isGreaterThanOrEqualTo(SemverVersion.fromString("1.36.0"))) {
            apiPrefix = "/api";
        }
        return URI.create(String.format("%s://%s%s", scheme, hostname, apiPrefix));
    }

    /**
     * Performs a request against the inverter's config API by logging in and executing the request with the acquired
     * authentication header.
     *
     * @param method the HTTP method to use
     * @param uri the URI to request
     * @param body the JSON request body for POST requests, or null for GET requests
     * @return the response body
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    private String authorizedRequest(HttpMethod method, URI uri, @Nullable String body)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        try {
            return executeAuthorizedRequest(method, uri, body);
        } catch (FroniusCommunicationException e) {
            // The cached digest session may have expired on the server side, invalidate it and retry once with a
            // fresh login
            FroniusConfigAuthUtil.invalidateSession(baseUri);
            return executeAuthorizedRequest(method, uri, body);
        }
    }

    private String executeAuthorizedRequest(HttpMethod method, URI uri, @Nullable String body)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        String authHeader = FroniusConfigAuthUtil.login(httpClient, firmwareVersion, baseUri, username, password,
                method, uri.getPath(), API_TIMEOUT);
        Properties headers = new Properties();
        headers.put(HttpHeader.AUTHORIZATION.asString(), authHeader);
        ByteArrayInputStream content = body == null ? null
                : new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        String contentType = body == null ? null : "application/json";
        return httpUtil.executeUrl(method, uri.toString(), headers, content, contentType, API_TIMEOUT);
    }

    /**
     * Writes configuration to the inverter's config API and verifies that the write was successful.
     *
     * @param uri the config API endpoint to write to
     * @param payload the payload to serialize as JSON and write
     * @param expectedWriteSuccess the parameters that must be reported as successfully written
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter or the write
     *             was not successful
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    private void postConfig(URI uri, Object payload, String... expectedWriteSuccess)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        String responseString = authorizedRequest(HttpMethod.POST, uri, gson.toJson(payload));
        @Nullable
        PostConfigResponse response = gson.fromJson(responseString, PostConfigResponse.class);
        if (response == null || !response.writeSuccess().containsAll(List.of(expectedWriteSuccess))) {
            logger.debug("{}", responseString);
            throw new FroniusCommunicationException("Failed to write configuration to inverter");
        }
    }

    /**
     * Gets the time of use settings of the Fronius hybrid inverter.
     *
     * @return the time of use settings
     * @throws FroniusCommunicationException if an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    private TimeOfUseRecords getTimeOfUse() throws FroniusCommunicationException, FroniusUnauthorizedException {
        String response = authorizedRequest(HttpMethod.GET, timeOfUseUri, null);
        logger.trace("Time of Use settings read successfully");

        // Parse the response body
        TimeOfUseRecords records;
        try {
            records = gson.fromJson(response, TimeOfUseRecords.class);
        } catch (JsonSyntaxException jse) {
            throw new FroniusCommunicationException("Failed to parse Time of Use settings", jse);
        }
        if (records == null) {
            throw new FroniusCommunicationException("Failed to parse Time of Use settings");
        }
        return records;
    }

    /**
     * Sets the time of use settings of the Fronius hybrid inverter.
     *
     * @param records the time of use settings
     * @throws FroniusCommunicationException if an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    private void setTimeOfUse(TimeOfUseRecords records)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        postConfig(timeOfUseUri, records, "timeofuse");
        logger.trace("Time of Use settings set successfully");
    }

    /**
     * Adds a schedule to the time of use settings of the Fronius hybrid inverter.
     *
     * @param from start time of the forced charge period
     * @param until end time of the forced charge period
     * @param scheduleType the type of the schedule
     * @param power the power value for the schedule
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addSchedule(LocalTime from, LocalTime until, ScheduleType scheduleType, QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, scheduleType, power, EnumSet.allOf(DayOfWeek.class));
    }

    /**
     * Adds a schedule to the time of use settings of the Fronius hybrid inverter, active on the given weekdays only.
     *
     * @param from start time of the schedule
     * @param until end time of the schedule
     * @param scheduleType the type of the schedule
     * @param power the power value for the schedule
     * @param weekdays the weekdays on which the schedule shall be active
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addSchedule(LocalTime from, LocalTime until, ScheduleType scheduleType, QuantityType<Power> power,
            Set<DayOfWeek> weekdays) throws FroniusCommunicationException, FroniusUnauthorizedException {
        TimeOfUseRecords currentTimeOfUse = getTimeOfUse();
        TimeOfUseRecord[] timeOfUse = new TimeOfUseRecord[currentTimeOfUse.records().length + 1];
        System.arraycopy(currentTimeOfUse.records(), 0, timeOfUse, 0, currentTimeOfUse.records().length);

        QuantityType<Power> powerInWatts = power.toUnit(Units.WATT);
        if (powerInWatts == null) {
            throw new IllegalArgumentException("power must be convertible to Watt unit");
        }
        if (powerInWatts.intValue() < 0) {
            throw new IllegalArgumentException("power must be non-negative");
        }
        TimeOfUseRecord holdCharge = new TimeOfUseRecord(true, powerInWatts.intValue(), scheduleType,
                new TimeTableRecord(from.format(TIME_FORMATTER), until.format(TIME_FORMATTER)),
                new WeekdaysRecord(weekdays.contains(DayOfWeek.MONDAY), weekdays.contains(DayOfWeek.TUESDAY),
                        weekdays.contains(DayOfWeek.WEDNESDAY), weekdays.contains(DayOfWeek.THURSDAY),
                        weekdays.contains(DayOfWeek.FRIDAY), weekdays.contains(DayOfWeek.SATURDAY),
                        weekdays.contains(DayOfWeek.SUNDAY)));
        timeOfUse[timeOfUse.length - 1] = holdCharge;
        setTimeOfUse(new TimeOfUseRecords(timeOfUse));
    }

    /**
     * Resets the time of use settings (i.e. removes all time-dependent battery control settings) of the Fronius hybrid
     * inverter.
     *
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void reset() throws FroniusCommunicationException, FroniusUnauthorizedException {
        setTimeOfUse(new TimeOfUseRecords(new TimeOfUseRecord[0]));
    }

    /**
     * Holds the battery charge right now, i.e. prevents the battery from discharging.
     *
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void holdBatteryCharge() throws FroniusCommunicationException, FroniusUnauthorizedException {
        reset();
        addHoldBatteryChargeSchedule(BEGIN_OF_DAY, END_OF_DAY);
    }

    /**
     * Holds the battery charge during a specific time period, i.e. prevents the battery from discharging in that
     * period.
     *
     * @param from start time of the hold charge period
     * @param until end time of the hold charge period
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addHoldBatteryChargeSchedule(LocalTime from, LocalTime until)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, ScheduleType.DISCHARGE_MAX, new QuantityType<>(0, Units.WATT));
    }

    /**
     * Forces the battery to charge right now with the specified power.
     *
     * @param power the power to charge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void forceBatteryCharging(QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        reset();
        addForcedBatteryChargingSchedule(BEGIN_OF_DAY, END_OF_DAY, power);
    }

    /**
     * Forces the battery to charge during a specific time period with the specified power.
     *
     * @param from start time of the forced charge period
     * @param until end time of the forced charge period
     * @param power the power to charge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addForcedBatteryChargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, ScheduleType.CHARGE_MIN, power);
    }

    /**
     * Limits the battery charging power right now, i.e. the battery is charged with at most the specified power.
     *
     * @param power the maximum power to charge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void limitBatteryCharging(QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        reset();
        addBatteryChargingLimitSchedule(BEGIN_OF_DAY, END_OF_DAY, power);
    }

    /**
     * Limits the battery charging power during a specific time period, i.e. the battery is charged with at most the
     * specified power during that period.
     *
     * @param from start time of the charging limit period
     * @param until end time of the charging limit period
     * @param power the maximum power to charge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addBatteryChargingLimitSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, ScheduleType.CHARGE_MAX, power);
    }

    /**
     * Limits the battery discharging power right now, i.e. the battery is discharged with at most the specified power.
     *
     * @param power the maximum power to discharge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void limitBatteryDischarging(QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        reset();
        addBatteryDischargingLimitSchedule(BEGIN_OF_DAY, END_OF_DAY, power);
    }

    /**
     * Limits the battery discharging power during a specific time period, i.e. the battery is discharged with at most
     * the specified power during that period.
     *
     * @param from start time of the discharging limit period
     * @param until end time of the discharging limit period
     * @param power the maximum power to discharge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addBatteryDischargingLimitSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, ScheduleType.DISCHARGE_MAX, power);
    }

    /**
     * Prevents the battery from charging right now.
     *
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void preventBatteryCharging() throws FroniusCommunicationException, FroniusUnauthorizedException {
        reset();
        addPreventBatteryChargingSchedule(BEGIN_OF_DAY, END_OF_DAY);
    }

    /**
     * Prevents the battery from charging during a specific time period.
     *
     * @param from start time of the prevented charging period
     * @param until end time of the prevented charging period
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addPreventBatteryChargingSchedule(LocalTime from, LocalTime until)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, ScheduleType.CHARGE_MAX, new QuantityType<>(0, Units.WATT));
    }

    /**
     * Forces the battery to discharge right now with the specified power.
     *
     * @param power the power to discharge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void forceBatteryDischarging(QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        reset();
        addForcedBatteryDischargingSchedule(BEGIN_OF_DAY, END_OF_DAY, power);
    }

    /**
     * Forces the battery to discharge during a specific time period with the specified power.
     *
     * @param from start time of the prevented charging period
     * @param until end time of the prevented charging period
     * @param power the power to discharge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void addForcedBatteryDischargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        addSchedule(from, until, ScheduleType.DISCHARGE_MIN, power);
    }

    /**
     * Sets the reserved battery capacity for backup power.
     *
     * @param percent the reserved battery capacity for backup power
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws IllegalArgumentException when percent is not in [10,95]
     * @throws FroniusUnauthorizedException when login failed due to invalid credentials
     */
    public void setBackupReservedCapacity(int percent)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        if (percent < 10 || percent > 95) {
            throw new IllegalArgumentException("invalid percent value: " + percent + " (must be in [10,95])");
        }

        postConfig(batteriesUri, Map.of(BACKUP_RESERVED_CAPACITY_PARAMETER, percent),
                BACKUP_RESERVED_CAPACITY_PARAMETER);
        logger.trace("Backup Reserved Capacity setting set successfully");
    }

    /** Battery settings from the inverter's battery configuration (SoC values in percent). */
    public record BatterySettings(int minSoc, int maxSoc, int backupReservedCapacity, int backupCriticalSoc,
            boolean chargeFromGrid, boolean calibrating) {
    }

    private static final String SOC_MIN_PARAMETER = "BAT_M0_SOC_MIN";
    private static final String SOC_MAX_PARAMETER = "BAT_M0_SOC_MAX";
    private static final String BACKUP_CRITICAL_SOC_PARAMETER = "HYB_BACKUP_CRITICALSOC";
    private static final String CHARGE_FROM_GRID_PARAMETER = "HYB_EVU_CHARGEFROMGRID";
    private static final String CALIBRATION_PARAMETER = "BAT_CALIBRATION";

    /**
     * Gets the current battery settings (state-of-charge limits and backup reserved capacity) from the inverter.
     *
     * @return the current battery settings
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public BatterySettings getBatterySettings() throws FroniusCommunicationException, FroniusUnauthorizedException {
        String responseString = authorizedRequest(HttpMethod.GET, batteriesUri, null);
        BatteriesConfig config;
        try {
            config = gson.fromJson(responseString, BatteriesConfig.class);
        } catch (JsonSyntaxException jse) {
            throw new FroniusCommunicationException("Failed to parse battery settings", jse);
        }
        if (config == null) {
            throw new FroniusCommunicationException("Battery settings response is missing expected fields");
        }
        Integer minSoc = config.minSoc();
        Integer maxSoc = config.maxSoc();
        Integer backupReservedCapacity = config.backupReservedCapacity();
        Integer backupCriticalSoc = config.backupCriticalSoc();
        Boolean chargeFromGrid = config.chargeFromGrid();
        Boolean calibrating = config.calibrating();
        if (minSoc == null || maxSoc == null || backupReservedCapacity == null || backupCriticalSoc == null
                || chargeFromGrid == null || calibrating == null) {
            throw new FroniusCommunicationException("Battery settings response is missing expected fields");
        }
        return new BatterySettings(minSoc, maxSoc, backupReservedCapacity, backupCriticalSoc, chargeFromGrid,
                calibrating);
    }

    /** Deserialized response of the batteries config API endpoint. */
    private record BatteriesConfig(@SerializedName(SOC_MIN_PARAMETER) @Nullable Integer minSoc,
            @SerializedName(SOC_MAX_PARAMETER) @Nullable Integer maxSoc,
            @SerializedName(BACKUP_RESERVED_CAPACITY_PARAMETER) @Nullable Integer backupReservedCapacity,
            @SerializedName(BACKUP_CRITICAL_SOC_PARAMETER) @Nullable Integer backupCriticalSoc,
            @SerializedName(CHARGE_FROM_GRID_PARAMETER) @Nullable Boolean chargeFromGrid,
            @SerializedName(CALIBRATION_PARAMETER) @Nullable Boolean calibrating) {
    }

    /**
     * Enables or disables charging the battery from the grid on the inverter.
     *
     * @param enabled whether charging the battery from the grid is allowed
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void setChargeFromGrid(boolean enabled) throws FroniusCommunicationException, FroniusUnauthorizedException {
        postConfig(batteriesUri, Map.of(CHARGE_FROM_GRID_PARAMETER, enabled), CHARGE_FROM_GRID_PARAMETER);
        logger.trace("Charge from Grid setting set successfully");
    }

    /**
     * Gets the night preservation limit from the inverter, i.e. the state of charge that is preserved over night to
     * keep the battery system operational.
     *
     * @return the night preservation limit in percent
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public int getNightPreservationLimit() throws FroniusCommunicationException, FroniusUnauthorizedException {
        String responseString = authorizedRequest(HttpMethod.GET, nightPreservationLimitUri, null);
        NightPreservationLimitResponse response;
        try {
            response = gson.fromJson(responseString, NightPreservationLimitResponse.class);
        } catch (JsonSyntaxException jse) {
            throw new FroniusCommunicationException("Failed to parse night preservation limit", jse);
        }
        NightPreservationLimitResult result = response == null ? null : response.resultData();
        Integer limit = result == null ? null : result.socMinValue();
        if (limit == null) {
            throw new FroniusCommunicationException("Night preservation limit response is missing expected fields");
        }
        return limit;
    }

    private record NightPreservationLimitResult(@Nullable Integer socMinValue) {
    }

    private record NightPreservationLimitResponse(@Nullable NightPreservationLimitResult resultData) {
    }

    /**
     * Sets the critical state of charge for backup power on the inverter, i.e. the state of charge at which the
     * inverter warns about the battery running low in backup power mode.
     *
     * @param percent the critical state of charge for backup power
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws IllegalArgumentException when percent is not in [0,100]
     * @throws FroniusUnauthorizedException when login failed due to invalid credentials
     */
    public void setBackupCriticalSoc(int percent) throws FroniusCommunicationException, FroniusUnauthorizedException {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("invalid percent value: " + percent + " (must be in [0,100])");
        }
        postConfig(batteriesUri, Map.of(BACKUP_CRITICAL_SOC_PARAMETER, percent), BACKUP_CRITICAL_SOC_PARAMETER);
        logger.trace("Backup Critical SoC setting set successfully");
    }

    /**
     * Sets the battery state-of-charge limits on the inverter.
     *
     * @param minSoc minimum state of charge in percent
     * @param maxSoc maximum state of charge in percent
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void setSocLimits(int minSoc, int maxSoc)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        if (minSoc < 0 || maxSoc > 100 || minSoc >= maxSoc) {
            throw new IllegalArgumentException(
                    "invalid SoC limits: min=" + minSoc + ", max=" + maxSoc + " (required: 0 <= min < max <= 100)");
        }
        postConfig(batteriesUri, Map.of(SOC_MIN_PARAMETER, minSoc, SOC_MAX_PARAMETER, maxSoc), SOC_MIN_PARAMETER,
                SOC_MAX_PARAMETER);
        logger.trace("SoC limits set successfully");
    }
}
