/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.handler;

import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.IMPERIAL_UNITS;
import static org.openhab.core.library.unit.MetricPrefix.MILLI;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link BloomAbstractHandler} contains common utilities used by the handlers.
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public abstract class BloomSkyAbstractHandler extends BaseThingHandler {
    protected static final int BLOOMSKY_API_TIMEOUT_SECONDS = 15;
    protected static final int REFRESH_JOB_INITIAL_DELAY_SECONDS = 6;

    private final Logger logger = LoggerFactory.getLogger(BloomSkyAbstractHandler.class);

    protected final Gson gson = new GsonBuilder().serializeNulls().create();

    protected final Map<String, State> weatherDataCache = Collections.synchronizedMap(new HashMap<>());

    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;

    /**
     * Constructor for the {@link BloomSkyAbstractHandler}.
     *
     * @param thing - the specific device this handler will manage.
     * @param httpClient - common HTTP client used to connect to the BloomSky rest API
     * @param timeZoneProvider - used to determine time stamp relevant to users time zone.
     */
    public BloomSkyAbstractHandler(Thing thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * The bridgeStatusChanged method is used to set the status of the thing based on the status
     * the bridge.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * The isBridgeOnline method is used to determine if the Bridge is online.
     *
     * @return - True if bridge is online, false if Offline.
     */
    protected boolean isBridgeOnline() {
        boolean bridgeStatus = false;
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            bridgeStatus = true;
        }
        return bridgeStatus;
    }

    /**
     * The getApiKey method is used to retrieve the BloomSky API authorization key from the Bridge
     * configuration.
     *
     * @return - The API key value if it was configured, or "unknown" if not.
     */
    protected String getApiKey() {
        String apiKey = "unknown";
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            BloomSkyBridgeHandler handler = (BloomSkyBridgeHandler) bridge.getHandler();
            if (handler != null) {
                String key = handler.getApiKey();
                if (key != null) {
                    apiKey = key;
                }
            }
        }
        return apiKey;
    }

    /**
     * The getDisplayUnits method is used to retrieve the measurement units to use when calling the
     * BloomSKy rest API. The default is Imperial.
     *
     * @return - The displayUnits can be either Imperial or Metric.
     */
    protected String getDisplayUnits() {
        String displayUnits = IMPERIAL_UNITS;
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            BloomSkyBridgeHandler handler = (BloomSkyBridgeHandler) bridge.getHandler();
            if (handler != null) {
                String units = handler.getDisplayUnits();
                if (units != null) {
                    displayUnits = units;
                }
            }
        }
        return displayUnits;
    }

    /**
     * The isImperial method determines the display units configured in the Bridge.
     *
     * @return - True if Imperial, False if Metric.
     */
    protected boolean isImperial() {
        return getDisplayUnits().equals(IMPERIAL_UNITS);
    }

    /**
     * The updateChannel method checks to see if the channel is linked to an item. If true, then proceeds
     * to update the channel state with current details from the BloomSky API.
     *
     * @param channelUID - Channel Id to be updated.
     * @param state - The value to be used to update the channel.
     */
    protected void updateChannel(ChannelUID channelUID, State state) {
        // Only update channel if it's linked
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
            weatherDataCache.put(channelUID.getIdWithoutGroup(), state);
        } else {
            logger.debug("channel UId '{}' not updated because it was not linked to an item.", channelUID);
        }
    }

    /**
     * The undefOrXXXX methods set the state of a channel to the passed value.
     * If value is null, set the state to UNDEF
     *
     * @return - value in correct format or Undefined (UNDEF) if null
     */
    protected State undefOrString(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : new StringType(value);
    }

    protected State undefOrStringList(@Nullable List<String> value) {
        String stringValue = "";
        if (value != null) {
            stringValue = value.toString(); // Save list as string, remove "[" from string
            stringValue = value.toString().substring(1, stringValue.length() - 1);
        }
        ;
        return value == null ? UnDefType.UNDEF : new StringType(stringValue);
    }

    protected State undefOrDate(@Nullable Long value) {
        return value == null ? UnDefType.UNDEF : getLocalDateTimeType(value);
    }

    protected State undefOrDate(@Nullable String value) {
        return value == null ? UnDefType.UNDEF : getLocalDateTimeType(value);
    }

    protected State undefOrInteger(@Nullable Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value.intValue());
    }

    protected State undefOrDecimal(@Nullable Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value.doubleValue());
    }

    protected State undefOrQuantity(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected State undefOrWindChill(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected State undefOrPoint(@Nullable Number lat, @Nullable Number lon, @Nullable Number alt) {
        return lat != null && lon != null && alt != null
                ? new PointType(new DecimalType(lat.doubleValue()), new DecimalType(lon.doubleValue()),
                        new DecimalType(alt.doubleValue()))
                : UnDefType.UNDEF;
    }

    /**
     * The getXXXXUnit methods determine the correct unit to use for a channel state.
     * The API will request units based on openHAB's SystemOfUnits setting. Therefore,
     * when setting the QuantityType state, make sure we use the proper unit.
     *
     * @return correct unit for value based on Bridge Configuration setting for display units
     */
    protected Unit<?> getTempUnit() {
        return isImperial() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
    }

    protected Unit<?> getSpeedUnit() {
        return isImperial() ? ImperialUnits.MILES_PER_HOUR : Units.METRE_PER_SECOND;
    }

    protected Unit<?> getLengthUnit() {
        return isImperial() ? ImperialUnits.INCH : MILLI(SIUnits.METRE);
    }

    protected Unit<?> getRainRateUnit() {
        return isImperial() ? ImperialUnits.INCH : MILLI(SIUnits.METRE);
    }

    protected Unit<?> getWindSpeedUnit() {
        return isImperial() ? ImperialUnits.MILES_PER_HOUR : Units.METRE_PER_SECOND;
    }

    protected Unit<?> getWindAngleUnit() {
        return Units.DEGREE_ANGLE;
    }

    protected Unit<?> getDistanceUnit() {
        return isImperial() ? ImperialUnits.FOOT : SIUnits.METRE;
    }

    protected Unit<?> getPressureUnit() {
        return isImperial() ? ImperialUnits.INCH_OF_MERCURY : MILLI(Units.BAR);
    }

    protected Unit<?> getVoltageUnit() {
        return isImperial() ? MILLI(Units.VOLT) : MILLI(Units.VOLT);
    }

    /**
     * The calDewPoint method calculates the DewPoint based on the current temperature and humidity
     * and formats it to 2 decimal places.
     *
     * @param temperature - from current Sky device observation
     * @param humidity - from current Sky device observation
     *
     * @return - calculated dew point
     */
    protected @Nullable Double calcDewPoint(double temperature, double humidity) {
        // If units are Imperial, then temperature is in Fahrenheit; convert it to Celsius.
        DecimalFormat df2 = new DecimalFormat("#.##");
        double tempCelsius = temperature;
        if (getTempUnit().equals(ImperialUnits.FAHRENHEIT)) {
            tempCelsius = (temperature - 32) / (9.0 / 5.0);
        }
        double dewPoint = (243.04 * (Math.log(humidity / 100) + ((17.625 * tempCelsius) / (243.04 + tempCelsius)))
                / (17.625 - Math.log(humidity / 100) - ((17.625 * tempCelsius) / (243.04 + tempCelsius)))) * 9.0 / 5.0
                + 32;
        return Double.parseDouble(df2.format(dewPoint));
    }

    /**
     * The calWindChill method determines the Wind Chill based on the formula used by the National Weather Service.
     * If units are Metric, then temperature is in Celsius; convert it to Fahrenheit.
     * Valid temperature is less than 70.0F (21.1C), return the current temperature, if not,
     *
     * @param temperature - in Fahrenheit or Celsius from current Sky observation
     * @param windSpeed - sustained wind speed from current STORM device observation
     * @return - calculated wind chill temperature if current temperature is below 70.0F (21.1C)
     */
    protected @Nullable Double calcWindChill(double temperature, double windSpeed) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        double tempFahrenheit = temperature;
        if (getTempUnit().equals(SIUnits.CELSIUS)) {
            tempFahrenheit = (temperature * 1.8) + 32;
        }
        if (tempFahrenheit < 70.0) {
            double windChill = 35.74 + 0.6215 * tempFahrenheit - 35.75 * Math.pow(windSpeed, 0.16)
                    + 0.4275 * tempFahrenheit * Math.pow(windSpeed, 0.16);
            return Double.parseDouble(df2.format(windChill));
        } else {
            return temperature;
        }
    }

    /**
     * The calcHeatIndex method uses the current temperature and humidity values to determine the heat index.
     * Valid entries are, air temperatures greater than 80 °F ( 27 °C ),
     * dew point temperatures greater than 65 °F ( 12 °C ), and relative humidities higher than 40 percent.
     *
     * @param currentTemp - in Fahrenheit or Celsius from Sky device
     * @param currentHumidity - percent reading from Sky device
     * @return
     */
    protected @Nullable Double calcHeatIndex(double currentTemp, double currentHumidity) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        double tempFahrenheit = currentTemp;
        if (getTempUnit().equals(SIUnits.CELSIUS)) {
            tempFahrenheit = (currentTemp * 1.8) + 32;
        }
        if (tempFahrenheit >= 80 && currentHumidity >= 40) {
            // Setting parameters for Function
            double temperature = tempFahrenheit;
            double humidity = currentHumidity;
            double answer = 0;
            final double c1 = -42.379;
            final double c2 = 2.04901523;
            final double c3 = 10.14333127;
            final double c4 = -0.22475541;
            final double c5 = -.00683783;
            final double c6 = -5.481717E-2;
            final double c7 = 1.22874E-3;
            final double c8 = 8.5282E-4;
            final double c9 = -1.99E-6;
            double t = temperature;
            double r = humidity;
            double t2 = temperature * temperature;
            double r2 = humidity * humidity;
            // Function to Calculate Heat Index in Fahrenheit
            answer = c1 + (c2 * t) + (c3 * r) + (c4 * t * r) + (c5 * t2) + (c6 * r2) + (c7 * t2 * r) + (c8 * t * r2)
                    + (c9 * t2 * r2);
            return Double.parseDouble(df2.format(answer));
        } else {
            return Double.parseDouble(df2.format(currentTemp));
        }
    }

    /**
     * The getLocalDateTimeType method converts a UTC Unix "epoch" in seconds to local time
     *
     * @return - localized date and time
     */
    protected DateTimeType getLocalDateTimeType(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime localDateTime = instant.atZone(getZoneId());
        DateTimeType dateTimeType = new DateTimeType(localDateTime);
        return dateTimeType;
    }

    /**
     * The getLocalDateTimeType method converts UTC time "String" to local time
     *
     * @param dateTimeString - Input string is of form 2018-12-02T10:47:00.000Z
     * @return - localized date and time
     */
    protected State getLocalDateTimeType(String dateTimeString) {
        State dateTimeType;
        try {
            Instant instant = Instant.parse(dateTimeString);
            ZonedDateTime localDateTime = instant.atZone(getZoneId());
            dateTimeType = new DateTimeType(localDateTime);
        } catch (DateTimeParseException e) {
            logger.debug("Error parsing date/time string: {}", e.getMessage());
            dateTimeType = UnDefType.UNDEF;
        }
        return dateTimeType;
    }

    /**
     * The getZoneId returns the system time zone ID.
     *
     * @return - time zone ID
     */
    private ZoneId getZoneId() {
        return timeZoneProvider.getTimeZone();
    }

    /**
     * The getHttpClient method returns the common HTTP Client used by the binding.
     *
     * @return - Common HTTP Client being used by the binding.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
