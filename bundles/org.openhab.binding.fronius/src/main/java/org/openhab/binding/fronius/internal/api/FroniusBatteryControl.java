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
package org.openhab.binding.fronius.internal.api;

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.API_TIMEOUT;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.ScheduleType;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecord;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecords;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeTableRecord;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.WeekdaysRecord;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FroniusBatteryControl} is responsible for controlling the battery of Fronius hybrid inverters through the
 * battery management's time-dependent battery control settings.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class FroniusBatteryControl {
    private static final String TIME_OF_USE_ENDPOINT = "/config/timeofuse";

    private static final Logger LOGGER = LoggerFactory.getLogger(FroniusBatteryControl.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final WeekdaysRecord ALL_WEEKDAYS_RECORD = new WeekdaysRecord(true, true, true, true, true, true,
            true);
    private static final LocalTime BEGIN_OF_DAY = LocalTime.of(0, 0);
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59);

    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final URI baseUri;
    private final String username;
    private final String password;
    private final URI timeOfUseUri;

    public FroniusBatteryControl(HttpClient httpClient, URI baseUri, String username, String password) {
        this.httpClient = httpClient;
        this.baseUri = baseUri;
        this.username = username;
        this.password = password;
        this.timeOfUseUri = baseUri.resolve(URI.create(TIME_OF_USE_ENDPOINT));
    }

    /**
     * Gets the time of use settings of the Fronius hybrid inverter.
     *
     * @return the time of use settings
     * @throws FroniusCommunicationException if an error occurs during communication with the inverter
     */
    private TimeOfUseRecords getTimeOfUse() throws FroniusCommunicationException {
        // Login and get the auth header for the next request
        String authHeader = FroniusConfigAuthUtil.login(httpClient, baseUri, username, password, HttpMethod.GET,
                timeOfUseUri.getPath(), API_TIMEOUT);
        Properties headers = new Properties();
        headers.put(HttpHeader.AUTHORIZATION.asString(), authHeader);
        // Get the time of use settings
        String response = FroniusHttpUtil.executeUrl(HttpMethod.GET, timeOfUseUri.toString(), headers, null, null,
                API_TIMEOUT);
        LOGGER.trace("Time of Use settings read successfully");

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
     */
    private void setTimeOfUse(TimeOfUseRecords records) throws FroniusCommunicationException {
        // Login and get the auth header for the next request
        String authHeader = FroniusConfigAuthUtil.login(httpClient, baseUri, username, password, HttpMethod.POST,
                timeOfUseUri.getPath(), API_TIMEOUT);
        Properties headers = new Properties();
        headers.put(HttpHeader.AUTHORIZATION.asString(), authHeader);

        // Set the time of use settings
        String json = gson.toJson(records);
        FroniusHttpUtil.executeUrl(HttpMethod.POST, timeOfUseUri.toString(), headers,
                new ByteArrayInputStream(json.getBytes()), "application/json", API_TIMEOUT);
        LOGGER.trace("Time of Use settings set successfully");
    }

    /**
     * Resets the time of use settings (i.e. removes all time-dependent battery control settings) of the Fronius hybrid
     * inverter.
     *
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     */
    public void reset() throws FroniusCommunicationException {
        setTimeOfUse(new TimeOfUseRecords(new TimeOfUseRecord[0]));
    }

    /**
     * Holds the battery charge right now, i.e. prevents the battery from discharging.
     *
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     */
    public void holdBatteryCharge() throws FroniusCommunicationException {
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
     */
    public void addHoldBatteryChargeSchedule(LocalTime from, LocalTime until) throws FroniusCommunicationException {
        TimeOfUseRecord[] currentTimeOfUse = getTimeOfUse().records();
        TimeOfUseRecord[] timeOfUse = new TimeOfUseRecord[currentTimeOfUse.length + 1];
        System.arraycopy(currentTimeOfUse, 0, timeOfUse, 0, currentTimeOfUse.length);

        TimeOfUseRecord holdCharge = new TimeOfUseRecord(true, 0, ScheduleType.DISCHARGE_MAX,
                new TimeTableRecord(from.format(TIME_FORMATTER), until.format(TIME_FORMATTER)), ALL_WEEKDAYS_RECORD);
        timeOfUse[timeOfUse.length - 1] = holdCharge;
        setTimeOfUse(new TimeOfUseRecords(timeOfUse));
    }

    /**
     * Forces the battery to charge right now with the specified power.
     *
     * @param power the power to charge the battery with
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     */
    public void forceBatteryCharging(QuantityType<Power> power) throws FroniusCommunicationException {
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
     */
    public void addForcedBatteryChargingSchedule(LocalTime from, LocalTime until, QuantityType<Power> power)
            throws FroniusCommunicationException {
        TimeOfUseRecords currentTimeOfUse = getTimeOfUse();
        TimeOfUseRecord[] timeOfUse = new TimeOfUseRecord[currentTimeOfUse.records().length + 1];
        System.arraycopy(currentTimeOfUse.records(), 0, timeOfUse, 0, currentTimeOfUse.records().length);

        TimeOfUseRecord holdCharge = new TimeOfUseRecord(true, power.toUnit(Units.WATT).intValue(),
                ScheduleType.CHARGE_MIN, new TimeTableRecord(from.format(TIME_FORMATTER), until.format(TIME_FORMATTER)),
                ALL_WEEKDAYS_RECORD);
        timeOfUse[timeOfUse.length - 1] = holdCharge;
        setTimeOfUse(new TimeOfUseRecords(timeOfUse));
    }
}
