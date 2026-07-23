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

import static org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.BatteriesConfig.*;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fronius.internal.api.dto.inverter.PostConfigResponse;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.BatteriesConfig;
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

/**
 * The {@link FroniusBatteryControl} is responsible for controlling the battery of Fronius hybrid inverters through the
 * battery management's time-dependent battery control settings.
 *
 * @author Florian Hotze - Initial contribution
 * @author Christian Jonak-Möchel - Add limit battery (dis)charging power methods, Extend battery settings, Add night
 *         preservation limit getter
 */
@NonNullByDefault
public class FroniusBatteryControl {
    private static final String TIME_OF_USE_ENDPOINT = "/config/timeofuse";
    private static final String BATTERIES_ENDPOINT = "/config/batteries";
    private static final String NIGHT_PRESERVATION_LIMIT_ENDPOINT = "/commands/GetNightPreservationLimit";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final LocalTime BEGIN_OF_DAY = LocalTime.of(0, 0);
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59);

    /**
     * How often written time of use settings are read back for verification before giving up. Measured on a GEN24
     * (firmware 1.41.10-1), reads returned the previous settings for up to roughly 200 ms after a write.
     */
    private static final int WRITE_VERIFY_ATTEMPTS = 3;
    private static final Duration WRITE_VERIFY_RETRY_DELAY = Duration.ofMillis(500);

    private final Logger logger = LoggerFactory.getLogger(FroniusBatteryControl.class);
    private final Gson gson = new Gson();
    private final FroniusConfigApiClient configApiClient;
    private final FroniusConfigApiEndpoint endpoint;
    private final URI timeOfUseUri;
    private final URI batteriesUri;
    private final URI nightPreservationLimitUri;

    /**
     * Creates a new instance of {@link FroniusBatteryControl}.
     *
     * @param configApiClient the client to use for the requests against the inverter's config API
     * @param firmwareVersion the firmware version of the inverter
     * @param scheme http or https
     * @param hostname the hostname or IP address of the inverter
     * @param username the username for the inverter Web UI
     * @param password the password for the inverter Web UI
     */
    public FroniusBatteryControl(FroniusConfigApiClient configApiClient, SemverVersion firmwareVersion, String scheme,
            String hostname, String username, String password) {
        this.configApiClient = configApiClient;
        URI baseUri = getBaseUri(firmwareVersion, scheme, hostname);
        this.endpoint = new FroniusConfigApiEndpoint(baseUri, getHashAlgorithm(firmwareVersion), username, password);
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

    private static String getHashAlgorithm(SemverVersion firmwareVersion) {
        if (firmwareVersion.isGreaterThanOrEqualTo(SemverVersion.fromString("1.36.0"))) {
            return "SHA-256";
        } else {
            return "MD5";
        }
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
        String responseString = configApiClient.executeRequest(endpoint, HttpMethod.POST, uri, gson.toJson(payload));
        logger.trace("Config write response: {}", responseString);
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
    public TimeOfUseRecords getTimeOfUse() throws FroniusCommunicationException, FroniusUnauthorizedException {
        String response = configApiClient.executeRequest(endpoint, HttpMethod.GET, timeOfUseUri, null);
        logger.trace("Time of Use settings read successfully");

        // Parse the response body
        TimeOfUseRecords records;
        try {
            records = gson.fromJson(response, TimeOfUseRecords.class);
        } catch (JsonSyntaxException jse) {
            logger.debug("{}", response);
            throw new FroniusCommunicationException("Failed to parse Time of Use settings", jse);
        }
        if (records == null) {
            logger.debug("{}", response);
            throw new FroniusCommunicationException("Failed to parse Time of Use settings");
        }
        return records;
    }

    /**
     * Sets the time of use settings of the Fronius hybrid inverter and verifies that they became effective.
     *
     * @param records the time of use settings
     * @throws FroniusCommunicationException if an error occurs during communication with the inverter or the inverter
     *             does not confirm the written settings
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    private void setTimeOfUse(TimeOfUseRecords records)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        postConfig(timeOfUseUri, records, "timeofuse");
        logger.trace("Time of Use settings set successfully");
        verifyTimeOfUse(records);
    }

    /**
     * Verifies that the written time of use settings became effective by reading them back until they match. The
     * inverter accepts a write and reports success, but reads shortly afterwards can still return the previous
     * settings. Without this check, a subsequent read-modify-write (e.g. the next addSchedule call) can be based on
     * the stale settings and silently undo the write.
     *
     * @param expected the previously written time of use settings
     * @throws FroniusCommunicationException when the read settings still differ from the written ones after the last
     *             attempt, e.g. because a concurrent change overwrote them
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    private void verifyTimeOfUse(TimeOfUseRecords expected)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        for (int attempt = 1; attempt <= WRITE_VERIFY_ATTEMPTS; attempt++) {
            if (Arrays.equals(getTimeOfUse().records(), expected.records())) {
                return;
            }
            logger.debug("The inverter did not confirm the written time of use settings yet (attempt {}/{})", attempt,
                    WRITE_VERIFY_ATTEMPTS);
            if (attempt < WRITE_VERIFY_ATTEMPTS) {
                try {
                    Thread.sleep(WRITE_VERIFY_RETRY_DELAY.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new FroniusCommunicationException("Interrupted while verifying the time of use settings", e);
                }
            }
        }
        throw new FroniusCommunicationException(
                "The inverter did not confirm the written time of use settings, they may have been overwritten by a concurrent change");
    }

    /**
     * Sets an all-day, all-week schedule entry for the given schedule type, replacing all existing entries of that
     * type. Entries of other schedule types are preserved, unlike with the reset based battery control methods.
     *
     * @param scheduleType the type of the schedule
     * @param power the power value for the schedule
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public void setAllTimeSchedule(ScheduleType scheduleType, QuantityType<Power> power)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        QuantityType<Power> powerInWatts = power.toUnit(Units.WATT);
        if (powerInWatts == null) {
            throw new IllegalArgumentException("power must be convertible to Watt unit");
        }
        if (powerInWatts.intValue() < 0) {
            throw new IllegalArgumentException("power must be non-negative");
        }
        List<TimeOfUseRecord> records = new ArrayList<>();
        for (TimeOfUseRecord record : getTimeOfUse().records()) {
            if (record.scheduleType() != scheduleType) {
                records.add(record);
            } else if (!isAllTimeEntry(record)) {
                // The inverter web UI rejects overlapping entries of the same type, so an all-time entry cannot
                // coexist with other entries of its type, and replacing them would destroy the user's schedules
                throw new IllegalArgumentException("There are existing time of use schedules of type " + scheduleType
                        + ", please manage them through the battery control actions instead");
            }
        }
        TimeOfUseRecord allTimeEntry = new TimeOfUseRecord(true, powerInWatts.intValue(), scheduleType,
                new TimeTableRecord(BEGIN_OF_DAY.format(TIME_FORMATTER), END_OF_DAY.format(TIME_FORMATTER)),
                new WeekdaysRecord(true, true, true, true, true, true, true));
        warnAboutConflicts(records.toArray(TimeOfUseRecord[]::new), allTimeEntry);
        records.add(allTimeEntry);
        setTimeOfUse(new TimeOfUseRecords(records.toArray(TimeOfUseRecord[]::new)));
    }

    /**
     * @return whether the record is an all-day, all-week entry as written by {@link #setAllTimeSchedule}
     */
    private static boolean isAllTimeEntry(TimeOfUseRecord record) {
        WeekdaysRecord weekdays = record.weekdays();
        return BEGIN_OF_DAY.format(TIME_FORMATTER).equals(record.timeTable().start())
                && END_OF_DAY.format(TIME_FORMATTER).equals(record.timeTable().end()) && weekdays.monday()
                && weekdays.tuesday() && weekdays.wednesday() && weekdays.thursday() && weekdays.friday()
                && weekdays.saturday() && weekdays.sunday();
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
        warnAboutConflicts(currentTimeOfUse.records(), holdCharge);
        setTimeOfUse(new TimeOfUseRecords(timeOfUse));
    }

    /**
     * Warns when the new schedule entry conflicts with existing entries. The inverter accepts such entries through
     * its config API, but its web UI marks them as invalid and refuses to save the settings: entries of the same type
     * must not overlap, charging entries must not overlap discharging entries unless both are maximum power limits,
     * and where a minimum and a maximum entry of charging or discharging overlap, the minimum power must be smaller
     * than the maximum power. Entries that only touch (one ends exactly when the other starts) do not count as
     * overlapping.
     *
     * @param existing the existing time of use entries
     * @param added the entry being added
     */
    private void warnAboutConflicts(TimeOfUseRecord[] existing, TimeOfUseRecord added) {
        for (TimeOfUseRecord record : existing) {
            if (!record.active() || !overlaps(record, added)) {
                continue;
            }
            if (record.scheduleType() == added.scheduleType()) {
                logger.warn(
                        "The new schedule ({} {}-{}) overlaps an existing schedule of the same type ({}-{}). The inverter web UI will report the time of use settings as invalid.",
                        added.scheduleType(), added.timeTable().start(), added.timeTable().end(),
                        record.timeTable().start(), record.timeTable().end());
            } else if (isChargeRule(record.scheduleType()) != isChargeRule(added.scheduleType())
                    && !(isMaximumLimit(record.scheduleType()) && isMaximumLimit(added.scheduleType()))) {
                logger.warn(
                        "The new schedule ({} {}-{}) overlaps an existing {} schedule ({}-{}): discharge rules must not overlap with charge rules unless both are maximum power limits. The inverter web UI will report the time of use settings as invalid.",
                        added.scheduleType(), added.timeTable().start(), added.timeTable().end(), record.scheduleType(),
                        record.timeTable().start(), record.timeTable().end());
            } else if (isContradiction(record, added)) {
                logger.warn(
                        "The new schedule ({} {} W {}-{}) contradicts an existing {} schedule ({} W {}-{}): the minimum power must be smaller than the maximum power. The inverter web UI will report the time of use settings as invalid.",
                        added.scheduleType(), added.power(), added.timeTable().start(), added.timeTable().end(),
                        record.scheduleType(), record.power(), record.timeTable().start(), record.timeTable().end());
            }
        }
    }

    private static boolean overlaps(TimeOfUseRecord a, TimeOfUseRecord b) {
        WeekdaysRecord wa = a.weekdays();
        WeekdaysRecord wb = b.weekdays();
        boolean sharedWeekday = (wa.monday() && wb.monday()) || (wa.tuesday() && wb.tuesday())
                || (wa.wednesday() && wb.wednesday()) || (wa.thursday() && wb.thursday())
                || (wa.friday() && wb.friday()) || (wa.saturday() && wb.saturday()) || (wa.sunday() && wb.sunday());
        if (!sharedWeekday) {
            return false;
        }
        LocalTime aStart = LocalTime.parse(a.timeTable().start(), TIME_FORMATTER);
        LocalTime aEnd = LocalTime.parse(a.timeTable().end(), TIME_FORMATTER);
        LocalTime bStart = LocalTime.parse(b.timeTable().start(), TIME_FORMATTER);
        LocalTime bEnd = LocalTime.parse(b.timeTable().end(), TIME_FORMATTER);
        // schedules that only touch (one ends exactly when the other starts) are valid in the inverter web UI
        return aEnd.isAfter(bStart) && bEnd.isAfter(aStart);
    }

    private static boolean isChargeRule(ScheduleType type) {
        return type == ScheduleType.CHARGE_MIN || type == ScheduleType.CHARGE_MAX;
    }

    private static boolean isMaximumLimit(ScheduleType type) {
        return type == ScheduleType.CHARGE_MAX || type == ScheduleType.DISCHARGE_MAX;
    }

    private static boolean isContradiction(TimeOfUseRecord a, TimeOfUseRecord b) {
        TimeOfUseRecord min = null;
        TimeOfUseRecord max = null;
        for (TimeOfUseRecord record : new TimeOfUseRecord[] { a, b }) {
            switch (record.scheduleType()) {
                case CHARGE_MIN, DISCHARGE_MIN -> min = record;
                case CHARGE_MAX, DISCHARGE_MAX -> max = record;
            }
        }
        if (min == null || max == null) {
            return false;
        }
        boolean charging = min.scheduleType() == ScheduleType.CHARGE_MIN
                && max.scheduleType() == ScheduleType.CHARGE_MAX;
        boolean discharging = min.scheduleType() == ScheduleType.DISCHARGE_MIN
                && max.scheduleType() == ScheduleType.DISCHARGE_MAX;
        return (charging || discharging) && min.power() >= max.power();
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
     * Gets the current battery settings (state-of-charge limits and backup reserved capacity) from the inverter.
     *
     * @return the current battery settings
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public BatterySettings getBatterySettings() throws FroniusCommunicationException, FroniusUnauthorizedException {
        String responseString = configApiClient.executeRequest(endpoint, HttpMethod.GET, batteriesUri, null);
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

    /**
     * Gets the night preservation limit from the inverter, i.e. the state of charge that is preserved over night to
     * keep the battery system operational.
     *
     * @return the night preservation limit in percent
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public int getNightPreservationLimit() throws FroniusCommunicationException, FroniusUnauthorizedException {
        String responseString = configApiClient.executeRequest(endpoint, HttpMethod.GET, nightPreservationLimitUri,
                null);
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

    /** Battery settings from the inverter's battery configuration (SoC values in percent). */
    public record BatterySettings(int minSoc, int maxSoc, int backupReservedCapacity, int backupCriticalSoc,
            boolean chargeFromGrid, boolean calibrating) {
    }
}
