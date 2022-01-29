/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.weatherunderground.internal.handler;

import static org.openhab.core.library.unit.MetricPrefix.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.weatherunderground.internal.config.WeatherUndergroundConfiguration;
import org.openhab.binding.weatherunderground.internal.json.WeatherUndergroundJsonCurrent;
import org.openhab.binding.weatherunderground.internal.json.WeatherUndergroundJsonData;
import org.openhab.binding.weatherunderground.internal.json.WeatherUndergroundJsonForecast;
import org.openhab.binding.weatherunderground.internal.json.WeatherUndergroundJsonForecastDay;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherUndergroundHandler} is responsible for handling the
 * weather things created to use the Weather Underground Service.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Theo Giovanna - Added a bridge for the API key
 * @author Laurent Garnier - refactor bridge/thing handling
 */
@NonNullByDefault
public class WeatherUndergroundHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundHandler.class);

    private static final int DEFAULT_REFRESH_PERIOD = 30;
    private static final String URL_QUERY = "http://api.wunderground.com/api/%APIKEY%/%FEATURES%/%SETTINGS%/q/%QUERY%.json";
    private static final String FEATURE_CONDITIONS = "conditions";
    private static final String FEATURE_FORECAST10DAY = "forecast10day";
    private static final String FEATURE_GEOLOOKUP = "geolookup";
    private static final Set<String> USUAL_FEATURES = Stream.of(FEATURE_CONDITIONS, FEATURE_FORECAST10DAY)
            .collect(Collectors.toSet());

    private static final Map<String, String> LANG_ISO_TO_WU_CODES = new HashMap<>();
    // Codes from https://www.wunderground.com/weather/api/d/docs?d=language-support
    static {
        LANG_ISO_TO_WU_CODES.put("AF", "AF");
        LANG_ISO_TO_WU_CODES.put("SQ", "AL");
        LANG_ISO_TO_WU_CODES.put("AR", "AR");
        LANG_ISO_TO_WU_CODES.put("HY", "HY");
        LANG_ISO_TO_WU_CODES.put("AZ", "AZ");
        LANG_ISO_TO_WU_CODES.put("EU", "EU");
        LANG_ISO_TO_WU_CODES.put("BE", "BY");
        LANG_ISO_TO_WU_CODES.put("BG", "BU");
        LANG_ISO_TO_WU_CODES.put("MY", "MY");
        LANG_ISO_TO_WU_CODES.put("CA", "CA");
        // Chinese - Simplified => CN
        LANG_ISO_TO_WU_CODES.put("ZH", "TW");
        LANG_ISO_TO_WU_CODES.put("HR", "CR");
        LANG_ISO_TO_WU_CODES.put("CS", "CZ");
        LANG_ISO_TO_WU_CODES.put("DA", "DK");
        LANG_ISO_TO_WU_CODES.put("DV", "DV");
        LANG_ISO_TO_WU_CODES.put("NL", "NL");
        LANG_ISO_TO_WU_CODES.put("EN", "EN");
        LANG_ISO_TO_WU_CODES.put("EO", "EO");
        LANG_ISO_TO_WU_CODES.put("ET", "ET");
        LANG_ISO_TO_WU_CODES.put("FA", "FA");
        LANG_ISO_TO_WU_CODES.put("FI", "FI");
        LANG_ISO_TO_WU_CODES.put("FR", "FR");
        LANG_ISO_TO_WU_CODES.put("GL", "GZ");
        LANG_ISO_TO_WU_CODES.put("DE", "DL");
        LANG_ISO_TO_WU_CODES.put("KA", "KA");
        LANG_ISO_TO_WU_CODES.put("EL", "GR");
        LANG_ISO_TO_WU_CODES.put("GU", "GU");
        LANG_ISO_TO_WU_CODES.put("HT", "HT");
        LANG_ISO_TO_WU_CODES.put("HE", "IL");
        LANG_ISO_TO_WU_CODES.put("HI", "HI");
        LANG_ISO_TO_WU_CODES.put("HU", "HU");
        LANG_ISO_TO_WU_CODES.put("IS", "IS");
        LANG_ISO_TO_WU_CODES.put("IO", "IO");
        LANG_ISO_TO_WU_CODES.put("ID", "ID");
        LANG_ISO_TO_WU_CODES.put("GA", "IR");
        LANG_ISO_TO_WU_CODES.put("IT", "IT");
        LANG_ISO_TO_WU_CODES.put("JA", "JP");
        LANG_ISO_TO_WU_CODES.put("JV", "JW");
        LANG_ISO_TO_WU_CODES.put("KM", "KM");
        LANG_ISO_TO_WU_CODES.put("KO", "KR");
        LANG_ISO_TO_WU_CODES.put("KU", "KU");
        LANG_ISO_TO_WU_CODES.put("LA", "LA");
        LANG_ISO_TO_WU_CODES.put("LV", "LV");
        LANG_ISO_TO_WU_CODES.put("LT", "LT");
        // Low German => ND
        LANG_ISO_TO_WU_CODES.put("MK", "MK");
        LANG_ISO_TO_WU_CODES.put("MT", "MT");
        // Mandinka => GM
        LANG_ISO_TO_WU_CODES.put("MI", "MI");
        LANG_ISO_TO_WU_CODES.put("MR", "MR");
        LANG_ISO_TO_WU_CODES.put("MN", "MN");
        LANG_ISO_TO_WU_CODES.put("NO", "NO");
        LANG_ISO_TO_WU_CODES.put("OC", "OC");
        LANG_ISO_TO_WU_CODES.put("PS", "PS");
        // Plautdietsch => GN
        LANG_ISO_TO_WU_CODES.put("PL", "PL");
        LANG_ISO_TO_WU_CODES.put("PT", "BR");
        LANG_ISO_TO_WU_CODES.put("PA", "PA");
        LANG_ISO_TO_WU_CODES.put("RO", "RO");
        LANG_ISO_TO_WU_CODES.put("RU", "RU");
        LANG_ISO_TO_WU_CODES.put("SR", "SR");
        LANG_ISO_TO_WU_CODES.put("SK", "SK");
        LANG_ISO_TO_WU_CODES.put("SL", "SL");
        LANG_ISO_TO_WU_CODES.put("ES", "SP");
        LANG_ISO_TO_WU_CODES.put("SW", "SI");
        LANG_ISO_TO_WU_CODES.put("SV", "SW");
        // Swiss => CH
        LANG_ISO_TO_WU_CODES.put("TL", "TL");
        LANG_ISO_TO_WU_CODES.put("TT", "TT");
        LANG_ISO_TO_WU_CODES.put("TH", "TH");
        LANG_ISO_TO_WU_CODES.put("TR", "TR");
        LANG_ISO_TO_WU_CODES.put("TK", "TK");
        LANG_ISO_TO_WU_CODES.put("UK", "UA");
        LANG_ISO_TO_WU_CODES.put("UZ", "UZ");
        LANG_ISO_TO_WU_CODES.put("VI", "VU");
        LANG_ISO_TO_WU_CODES.put("CY", "CY");
        LANG_ISO_TO_WU_CODES.put("WO", "SN");
        // Yiddish - transliterated => JI
        LANG_ISO_TO_WU_CODES.put("YI", "YI");
    }
    private static final Map<String, String> LANG_COUNTRY_TO_WU_CODES = new HashMap<>();
    static {
        LANG_COUNTRY_TO_WU_CODES.put("en-GB", "LI"); // British English
        LANG_COUNTRY_TO_WU_CODES.put("fr-CA", "FC"); // French Canadian
    }

    private final LocaleProvider localeProvider;
    private final UnitProvider unitProvider;
    private final TimeZoneProvider timeZoneProvider;
    private final Gson gson;
    private final Map<String, Integer> forecastMap;

    private @Nullable ScheduledFuture<?> refreshJob;

    private @Nullable WeatherUndergroundJsonData weatherData;

    private @Nullable WeatherUndergroundBridgeHandler bridgeHandler;

    public WeatherUndergroundHandler(Thing thing, LocaleProvider localeProvider, UnitProvider unitProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        this.unitProvider = unitProvider;
        this.timeZoneProvider = timeZoneProvider;
        gson = new Gson();
        forecastMap = initForecastDayMap();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WeatherUnderground handler for thing {}", getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThingHandler(null, null);
        } else {
            initializeThingHandler(bridge.getHandler(), bridge.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThingHandler(null, bridgeStatusInfo.getStatus());
        } else {
            initializeThingHandler(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeThingHandler(@Nullable ThingHandler bridgeHandler, @Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThingHandler {}", getThing().getUID());
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                this.bridgeHandler = (WeatherUndergroundBridgeHandler) bridgeHandler;

                WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);

                logger.debug("config location = {}", config.location);
                logger.debug("config language = {}", config.language);
                logger.debug("config refresh = {}", config.refresh);

                boolean validConfig = true;
                String errors = "";
                String statusDescr = null;

                if (config.location == null || config.location.trim().isEmpty()) {
                    errors += " Parameter 'location' must be configured.";
                    statusDescr = "@text/offline.conf-error-missing-location";
                    validConfig = false;
                }
                if (config.language != null) {
                    if (config.language.trim().length() != 2) {
                        errors += " Parameter 'language' must be 2 letters.";
                        statusDescr = "@text/offline.conf-error-syntax-language";
                        validConfig = false;
                    }
                }
                if (config.refresh != null && config.refresh < 5) {
                    errors += " Parameter 'refresh' must be at least 5 minutes.";
                    statusDescr = "@text/offline.conf-error-min-refresh";
                    validConfig = false;
                }
                errors = errors.trim();

                if (validConfig) {
                    updateStatus(ThingStatus.ONLINE);
                    startAutomaticRefresh();
                } else {
                    logger.debug("Setting thing '{}' to OFFLINE: {}", getThing().getUID(), errors);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, statusDescr);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    /**
     * Start the job refreshing the weather data
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Request new weather data to the Weather Underground service
                        updateWeatherData(USUAL_FEATURES);

                        // Update all channels from the updated weather data
                        for (Channel channel : getThing().getChannels()) {
                            updateChannel(channel.getUID().getId());
                        }
                    } catch (Exception e) {
                        logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);
            int period = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, period, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing WeatherUnderground handler.");

        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        } else {
            logger.debug("The Weather Underground binding is a read-only binding and cannot handle command {}",
                    command);
        }
    }

    /**
     * Update the channel from the last Weather Underground data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId) {
        if (isLinked(channelId)) {
            State state = null;
            WeatherUndergroundJsonData data = weatherData;
            if (data != null) {
                if (channelId.startsWith("current")) {
                    state = updateCurrentObservationChannel(channelId, data.getCurrent());
                } else if (channelId.startsWith("forecast")) {
                    state = updateForecastChannel(channelId, data.getForecast());
                }
            }

            logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            } else {
                updateState(channelId, UnDefType.NULL);
            }
        }
    }

    private @Nullable State updateCurrentObservationChannel(String channelId, WeatherUndergroundJsonCurrent current) {
        WUQuantity quantity;
        String channelTypeId = getChannelTypeId(channelId);
        switch (channelTypeId) {
            case "location":
                return undefOrState(current.getLocation(), new StringType(current.getLocation()));
            case "stationId":
                return undefOrState(current.getStationId(), new StringType(current.getStationId()));
            case "observationTime":
                ZoneId zoneId = timeZoneProvider.getTimeZone();
                return undefOrState(current.getObservationTime(zoneId),
                        new DateTimeType(current.getObservationTime(zoneId)));
            case "conditions":
                return undefOrState(current.getConditions(), new StringType(current.getConditions()));
            case "temperature":
                quantity = getTemperature(current.getTemperatureC(), current.getTemperatureF());
                return undefOrQuantity(quantity);
            case "relativeHumidity":
                return undefOrState(current.getRelativeHumidity(),
                        new QuantityType<>(current.getRelativeHumidity(), Units.PERCENT));
            case "windDirection":
                return undefOrState(current.getWindDirection(), new StringType(current.getWindDirection()));
            case "windDirectionDegrees":
                return undefOrState(current.getWindDirectionDegrees(),
                        new QuantityType<>(current.getWindDirectionDegrees(), Units.DEGREE_ANGLE));
            case "windSpeed":
                quantity = getSpeed(current.getWindSpeedKmh(), current.getWindSpeedMph());
                return undefOrQuantity(quantity);
            case "windGust":
                quantity = getSpeed(current.getWindGustKmh(), current.getWindGustMph());
                return undefOrQuantity(quantity);
            case "pressure":
                quantity = getPressure(current.getPressureHPa(), current.getPressureInHg());
                return undefOrQuantity(quantity);
            case "pressureTrend":
                return undefOrState(current.getPressureTrend(), new StringType(current.getPressureTrend()));
            case "dewPoint":
                quantity = getTemperature(current.getDewPointC(), current.getDewPointF());
                return undefOrQuantity(quantity);
            case "heatIndex":
                quantity = getTemperature(current.getHeatIndexC(), current.getHeatIndexF());
                return undefOrQuantity(quantity);
            case "windChill":
                quantity = getTemperature(current.getWindChillC(), current.getWindChillF());
                return undefOrQuantity(quantity);
            case "feelingTemperature":
                quantity = getTemperature(current.getFeelingTemperatureC(), current.getFeelingTemperatureF());
                return undefOrQuantity(quantity);
            case "visibility":
                quantity = getWUQuantity(KILO(SIUnits.METRE), ImperialUnits.MILE, current.getVisibilityKm(),
                        current.getVisibilityMi());
                return undefOrQuantity(quantity);
            case "solarRadiation":
                return undefOrQuantity(new WUQuantity(current.getSolarRadiation(), Units.IRRADIANCE));
            case "UVIndex":
                return undefOrDecimal(current.getUVIndex());
            case "precipitationDay":
                quantity = getPrecipitation(current.getPrecipitationDayMm(), current.getPrecipitationDayIn());
                return undefOrQuantity(quantity);
            case "precipitationHour":
                quantity = getPrecipitation(current.getPrecipitationHourMm(), current.getPrecipitationHourIn());
                return undefOrQuantity(quantity);
            case "iconKey":
                return undefOrState(current.getIconKey(), new StringType(current.getIconKey()));
            case "icon":
                State icon = HttpUtil.downloadImage(current.getIcon().toExternalForm());
                if (icon == null) {
                    logger.debug("Failed to download the content of URL {}", current.getIcon().toExternalForm());
                    return null;
                }
                return icon;
            default:
                return null;
        }
    }

    private @Nullable State updateForecastChannel(String channelId, WeatherUndergroundJsonForecast forecast) {
        WUQuantity quantity;
        int day = getDay(channelId);
        WeatherUndergroundJsonForecastDay dayForecast = forecast.getSimpleForecast(day);

        String channelTypeId = getChannelTypeId(channelId);
        switch (channelTypeId) {
            case "forecastTime":
                ZoneId zoneId = timeZoneProvider.getTimeZone();
                return undefOrState(dayForecast.getForecastTime(zoneId),
                        new DateTimeType(dayForecast.getForecastTime(zoneId)));
            case "conditions":
                return undefOrState(dayForecast.getConditions(), new StringType(dayForecast.getConditions()));
            case "minTemperature":
                quantity = getTemperature(dayForecast.getMinTemperatureC(), dayForecast.getMinTemperatureF());
                return undefOrQuantity(quantity);
            case "maxTemperature":
                quantity = getTemperature(dayForecast.getMaxTemperatureC(), dayForecast.getMaxTemperatureF());
                return undefOrQuantity(quantity);
            case "relativeHumidity":
                return undefOrState(dayForecast.getRelativeHumidity(),
                        new QuantityType<>(dayForecast.getRelativeHumidity(), Units.PERCENT));
            case "probaPrecipitation":
                return undefOrState(dayForecast.getProbaPrecipitation(),
                        new QuantityType<>(dayForecast.getProbaPrecipitation(), Units.PERCENT));
            case "precipitationDay":
                quantity = getPrecipitation(dayForecast.getPrecipitationDayMm(), dayForecast.getPrecipitationDayIn());
                return undefOrQuantity(quantity);
            case "snow":
                quantity = getWUQuantity(CENTI(SIUnits.METRE), ImperialUnits.INCH, dayForecast.getSnowCm(),
                        dayForecast.getSnowIn());
                return undefOrQuantity(quantity);
            case "maxWindDirection":
                return undefOrState(dayForecast.getMaxWindDirection(),
                        new StringType(dayForecast.getMaxWindDirection()));
            case "maxWindDirectionDegrees":
                return undefOrState(dayForecast.getMaxWindDirectionDegrees(),
                        new QuantityType<>(dayForecast.getMaxWindDirectionDegrees(), Units.DEGREE_ANGLE));
            case "maxWindSpeed":
                quantity = getSpeed(dayForecast.getMaxWindSpeedKmh(), dayForecast.getMaxWindSpeedMph());
                return undefOrQuantity(quantity);
            case "averageWindDirection":
                return undefOrState(dayForecast.getAverageWindDirection(),
                        new StringType(dayForecast.getAverageWindDirection()));
            case "averageWindDirectionDegrees":
                return undefOrState(dayForecast.getAverageWindDirectionDegrees(),
                        new QuantityType<>(dayForecast.getAverageWindDirectionDegrees(), Units.DEGREE_ANGLE));
            case "averageWindSpeed":
                quantity = getSpeed(dayForecast.getAverageWindSpeedKmh(), dayForecast.getAverageWindSpeedMph());
                return undefOrQuantity(quantity);
            case "iconKey":
                return undefOrState(dayForecast.getIconKey(), new StringType(dayForecast.getIconKey()));
            case "icon":
                State icon = HttpUtil.downloadImage(dayForecast.getIcon().toExternalForm());
                if (icon == null) {
                    logger.debug("Failed to download the content of URL {}", dayForecast.getIcon().toExternalForm());
                    return null;
                }
                return icon;
            default:
                return null;
        }
    }

    private @Nullable State undefOrState(@Nullable Object value, State state) {
        return value == null ? null : state;
    }

    private @Nullable <T extends Quantity<T>> State undefOrQuantity(WUQuantity quantity) {
        return quantity.value == null ? null : new QuantityType<>(quantity.value, quantity.unit);
    }

    private @Nullable State undefOrDecimal(@Nullable Number value) {
        return value == null ? null : new DecimalType(value.doubleValue());
    }

    private int getDay(String channelId) {
        String channel = channelId.split("#")[0];

        return forecastMap.get(channel);
    }

    private String getChannelTypeId(String channelId) {
        return channelId.substring(channelId.indexOf("#") + 1);
    }

    private Map<String, Integer> initForecastDayMap() {
        Map<String, Integer> forecastMap = new HashMap<>();
        forecastMap.put("forecastToday", Integer.valueOf(1));
        forecastMap.put("forecastTomorrow", Integer.valueOf(2));
        forecastMap.put("forecastDay2", Integer.valueOf(3));
        forecastMap.put("forecastDay3", Integer.valueOf(4));
        forecastMap.put("forecastDay4", Integer.valueOf(5));
        forecastMap.put("forecastDay5", Integer.valueOf(6));
        forecastMap.put("forecastDay6", Integer.valueOf(7));
        forecastMap.put("forecastDay7", Integer.valueOf(8));
        forecastMap.put("forecastDay8", Integer.valueOf(9));
        forecastMap.put("forecastDay9", Integer.valueOf(10));
        return forecastMap;
    }

    /**
     * Request new current conditions and forecast 10 days to the Weather Underground service
     * and store the data in weatherData
     *
     * @param features the list of features to be requested
     * @return true if success or false in case of error
     */
    private boolean updateWeatherData(Set<String> features) {
        WeatherUndergroundJsonData result = null;
        boolean resultOk = false;
        String error = null;
        String errorDetail = null;
        String statusDescr = null;

        // Request new weather data to the Weather Underground service

        try {
            WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);

            String urlStr = URL_QUERY.replace("%FEATURES%", String.join("/", features));

            String lang = config.language == null ? "" : config.language.trim();
            if (lang.isEmpty()) {
                // If language is not set in the configuration, you try deducing it from the system language
                lang = getCodeFromLanguage(localeProvider.getLocale());
                logger.debug("Use language deduced from system locale {}: {}", localeProvider.getLocale().getLanguage(),
                        lang);
            }
            if (lang.isEmpty()) {
                urlStr = urlStr.replace("%SETTINGS%", "");
            } else {
                urlStr = urlStr.replace("%SETTINGS%", "lang:" + lang.toUpperCase());
            }

            String location = config.location == null ? "" : config.location.trim();
            urlStr = urlStr.replace("%QUERY%", location);
            if (logger.isDebugEnabled()) {
                logger.debug("URL = {}", urlStr.replace("%APIKEY%", "***"));
            }

            urlStr = urlStr.replace("%APIKEY%", bridgeHandler.getApikey());

            // Run the HTTP request and get the JSON response from Weather Underground
            String response = null;
            try {
                response = HttpUtil.executeUrl("GET", urlStr, WeatherUndergroundBridgeHandler.FETCH_TIMEOUT_MS);
                logger.debug("weatherData = {}", response);
            } catch (IllegalArgumentException e) {
                // catch Illegal character in path at index XX: http://api.wunderground.com/...
                error = "Error creating URI with location parameter: '" + location + "'";
                errorDetail = e.getMessage();
                statusDescr = "@text/offline.uri-error";
            }

            // Map the JSON response to an object
            result = gson.fromJson(response, WeatherUndergroundJsonData.class);
            if (result.getResponse() == null) {
                errorDetail = "missing response sub-object";
            } else if (result.getResponse().getErrorDescription() != null) {
                if ("keynotfound".equals(result.getResponse().getErrorType())) {
                    error = "API key has to be fixed";
                    statusDescr = "@text/offline.comm-error-invalid-api-key";
                }
                errorDetail = result.getResponse().getErrorDescription();
            } else {
                resultOk = true;
                for (String feature : features) {
                    if (feature.equals(FEATURE_CONDITIONS) && result.getCurrent() == null) {
                        resultOk = false;
                        errorDetail = "missing current_observation sub-object";
                    } else if (feature.equals(FEATURE_FORECAST10DAY) && result.getForecast() == null) {
                        resultOk = false;
                        errorDetail = "missing forecast sub-object";
                    } else if (feature.equals(FEATURE_GEOLOOKUP) && result.getLocation() == null) {
                        resultOk = false;
                        errorDetail = "missing location sub-object";
                    }
                }
            }
            if (!resultOk && error == null) {
                error = "Error in Weather Underground response";
                statusDescr = "@text/offline.comm-error-response";
            }
        } catch (IOException e) {
            error = "Error running Weather Underground request";
            errorDetail = e.getMessage();
            statusDescr = "@text/offline.comm-error-running-request";
        } catch (JsonSyntaxException e) {
            error = "Error parsing Weather Underground response";
            errorDetail = e.getMessage();
            statusDescr = "@text/offline.comm-error-parsing-response";
        }

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE);
            weatherData = result;
        } else {
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", getThing().getUID(), error, errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, statusDescr);
            weatherData = null;
        }

        return resultOk;
    }

    /**
     * Get the WU code associated to a language
     *
     * @param locale the locale settings with language and country
     * @return the associated WU code or an empty string if not found
     */
    public static String getCodeFromLanguage(Locale locale) {
        String key = locale.getLanguage() + "-" + locale.getCountry();
        String language = LANG_COUNTRY_TO_WU_CODES.get(key);
        if (language == null) {
            language = LANG_ISO_TO_WU_CODES.get(locale.getLanguage().toUpperCase());
        }
        return language != null ? language : "";
    }

    private WUQuantity getTemperature(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(SIUnits.CELSIUS, ImperialUnits.FAHRENHEIT, siValue, imperialValue);
    }

    private WUQuantity getSpeed(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(SIUnits.KILOMETRE_PER_HOUR, ImperialUnits.MILES_PER_HOUR, siValue, imperialValue);
    }

    private WUQuantity getPressure(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(HECTO(SIUnits.PASCAL), ImperialUnits.INCH_OF_MERCURY, siValue, imperialValue);
    }

    private WUQuantity getPrecipitation(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(MILLI(SIUnits.METRE), ImperialUnits.INCH, siValue, imperialValue);
    }

    private <T extends Quantity<T>> WUQuantity getWUQuantity(Unit<T> siUnit, Unit<T> imperialUnit, BigDecimal siValue,
            BigDecimal imperialValue) {
        boolean isSI = unitProvider.getMeasurementSystem().equals(SIUnits.getInstance());
        return new WUQuantity(isSI ? siValue : imperialValue, isSI ? siUnit : imperialUnit);
    }

    private class WUQuantity {
        private WUQuantity(BigDecimal value, Unit<?> unit) {
            this.value = value;
            this.unit = unit;
        }

        private final Unit<?> unit;
        private final BigDecimal value;
    }
}
