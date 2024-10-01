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
package org.openhab.binding.weathercompany.internal.handler;

import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.weathercompany.internal.config.WeatherCompanyForecastConfig;
import org.openhab.binding.weathercompany.internal.model.DayPartDTO;
import org.openhab.binding.weathercompany.internal.model.ForecastDTO;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherCompanyForecastHandler} is responsible for pulling weather forecast
 * information from the Weather Company API.
 *
 * API documentation is located here
 * - https://docs.google.com/document/d/1eKCnKXI9xnoMGRRzOL1xPCBihNV2rOet08qpE_gArAY/edit
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class WeatherCompanyForecastHandler extends WeatherCompanyAbstractHandler {
    private static final String BASE_FORECAST_URL = "https://api.weather.com/v3/wx/forecast/daily/5day";

    private final Logger logger = LoggerFactory.getLogger(WeatherCompanyForecastHandler.class);

    private final LocaleProvider localeProvider;

    private int refreshIntervalSeconds;
    private String locationQueryString = "";
    private String languageQueryString = "";

    private @Nullable Future<?> refreshForecastJob;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshForecast();
        }
    };

    public WeatherCompanyForecastHandler(Thing thing, TimeZoneProvider timeZoneProvider, HttpClient httpClient,
            UnitProvider unitProvider, LocaleProvider localeProvider) {
        super(thing, timeZoneProvider, httpClient, unitProvider);
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Forecast handler initializing with configuration: {}",
                getConfigAs(WeatherCompanyForecastConfig.class).toString());

        refreshIntervalSeconds = getConfigAs(WeatherCompanyForecastConfig.class).refreshInterval * 60;
        if (isValidLocation()) {
            weatherDataCache.clear();
            setLanguage();
            scheduleRefreshJob();
            updateStatus(isBridgeOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            State state = weatherDataCache.get(channelUID.getId());
            if (state != null) {
                updateChannel(channelUID.getId(), state);
            }
        }
    }

    private boolean isValidLocation() {
        boolean validLocation = false;
        WeatherCompanyForecastConfig config = getConfigAs(WeatherCompanyForecastConfig.class);
        String locationType = config.locationType;
        if (locationType == null) {
            return validLocation;
        }
        switch (locationType) {
            case CONFIG_LOCATION_TYPE_POSTAL_CODE:
                String postalCode = config.postalCode;
                if (postalCode == null || postalCode.isBlank()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-unset-postal-code");
                } else {
                    locationQueryString = "&postalKey=" + postalCode.replace(" ", "");
                    validLocation = true;
                }
                break;
            case CONFIG_LOCATION_TYPE_GEOCODE:
                String geocode = config.geocode;
                if (geocode == null || geocode.isBlank()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-unset-geocode");
                } else {
                    locationQueryString = "&geocode=" + geocode.replace(" ", "");
                    validLocation = true;
                }
                break;
            case CONFIG_LOCATION_TYPE_IATA_CODE:
                String iataCode = config.iataCode;
                if (iataCode == null || iataCode.isBlank()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-unset-iata-code");
                } else {
                    locationQueryString = "&iataCode=" + iataCode.replace(" ", "").toUpperCase();
                    validLocation = true;
                }
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-unset-location-type");
                break;
        }
        return validLocation;
    }

    private void setLanguage() {
        WeatherCompanyForecastConfig config = getConfigAs(WeatherCompanyForecastConfig.class);
        String language = config.language;
        if (language == null || language.isBlank()) {
            // Nothing in the thing config, so try to get a match from the openHAB locale
            String derivedLanguage = WeatherCompanyAbstractHandler.lookupLanguage(localeProvider.getLocale());
            languageQueryString = "&language=" + derivedLanguage;
            logger.debug("Language not set in thing config, using {}", derivedLanguage);
        } else {
            // Use what is set in the thing config
            languageQueryString = "&language=" + language.trim();
        }
    }

    /*
     * Build the URL for requesting the 5-day forecast. It's important to request
     * the desired language AND units so that the forecast narrative contains
     * the consistent language and units (e.g. wind gusts to 30 mph).
     */
    private String buildForecastUrl() {
        String apiKey = getApiKey();
        StringBuilder sb = new StringBuilder(BASE_FORECAST_URL);
        // Set response type as JSON
        sb.append("?format=json");
        // Set language from config
        sb.append(languageQueryString);
        // Set API key from config
        sb.append("&apiKey=").append(apiKey);
        // Set the units to Imperial or Metric
        sb.append("&units=").append(getUnitsQueryString());
        // Set the location from config
        sb.append(locationQueryString);
        String url = sb.toString();
        logger.debug("Forecast URL is {}", url.replace(apiKey, REPLACE_API_KEY));
        return url.toString();
    }

    private synchronized void refreshForecast() {
        if (!isBridgeOnline()) {
            // If bridge is not online, API has not been validated yet
            logger.debug("Handler: Can't refresh forecast because bridge is not online");
            return;
        }
        logger.debug("Handler: Requesting forecast from The Weather Company API");
        String response = executeApiRequest(buildForecastUrl());
        if (response == null) {
            return;
        }
        try {
            logger.trace("Handler: Parsing forecast response: {}", response);
            ForecastDTO forecast = Objects.requireNonNull(gson.fromJson(response, ForecastDTO.class));
            logger.debug("Handler: Successfully parsed daily forecast response object");
            updateStatus(ThingStatus.ONLINE);
            updateDailyForecast(forecast);
            updateDaypartForecast(forecast.daypart);
        } catch (JsonSyntaxException e) {
            logger.debug("Handler: Error parsing daily forecast response object", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-parsing-daily-forecast");
            return;
        }
    }

    private void updateDailyForecast(ForecastDTO forecast) {
        for (int day = 0; day < forecast.dayOfWeek.length; day++) {
            logger.debug("Processing daily forecast for '{}'", forecast.dayOfWeek[day]);
            updateDaily(day, CH_DAY_OF_WEEK, undefOrString(forecast.dayOfWeek[day]));
            updateDaily(day, CH_NARRATIVE, undefOrString(forecast.narrative[day]));
            updateDaily(day, CH_VALID_TIME_LOCAL, undefOrDate(forecast.validTimeUtc[day]));
            updateDaily(day, CH_EXPIRATION_TIME_LOCAL, undefOrDate(forecast.expirationTimeUtc[day]));
            updateDaily(day, CH_TEMP_MAX, undefOrQuantity(forecast.temperatureMax[day], getTempUnit()));
            updateDaily(day, CH_TEMP_MIN, undefOrQuantity(forecast.temperatureMin[day], getTempUnit()));
            updateDaily(day, CH_PRECIP_RAIN, undefOrQuantity(forecast.qpf[day], getLengthUnit()));
            updateDaily(day, CH_PRECIP_SNOW, undefOrQuantity(forecast.qpfSnow[day], getLengthUnit()));
        }
    }

    private void updateDaypartForecast(Object daypartObject) {
        DayPartDTO[] dayparts;
        try {
            String innerJson = gson.toJson(daypartObject);
            logger.debug("Parsing daypartsObject: {}", innerJson);
            dayparts = gson.fromJson(innerJson.toString(), DayPartDTO[].class);
            logger.debug("Handler: Successfully parsed daypart forecast object");
        } catch (JsonSyntaxException e) {
            logger.debug("Handler: Error parsing daypart forecast object: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-parsing-daypart-forecast");
            return;
        }
        logger.debug("There are {} daypart forecast entries", dayparts.length);
        if (dayparts.length == 0) {
            logger.debug("There is no daypart forecast object in this message");
            return;
        }
        logger.debug("There are {} daypartName entries in this forecast", dayparts[0].daypartName.length);
        for (int i = 0; i < dayparts[0].daypartName.length; i++) {
            // Note: All dayparts[0] (i.e. today day) values are null after 3 pm local time
            DayPartDTO dp = dayparts[0];
            // Even daypart indexes are Day (D); odd daypart indexes are Night (N)
            String dOrN = dp.dayOrNight[i] == null ? (i % 2 == 0 ? "D" : "N") : dp.dayOrNight[i];
            logger.debug("Processing daypart forecast for '{}'", dp.daypartName[i]);
            updateDaypart(i, dOrN, CH_DP_NAME, undefOrString(dp.daypartName[i]));
            updateDaypart(i, dOrN, CH_DP_DAY_OR_NIGHT, undefOrString(dayparts[0].dayOrNight[i]));
            updateDaypart(i, dOrN, CH_DP_NARRATIVE, undefOrString(dayparts[0].narrative[i]));
            updateDaypart(i, dOrN, CH_DP_WX_PHRASE_SHORT, undefOrString(dayparts[0].wxPhraseShort[i]));
            updateDaypart(i, dOrN, CH_DP_WX_PHRASE_LONG, undefOrString(dayparts[0].wxPhraseLong[i]));
            updateDaypart(i, dOrN, CH_DP_QUALIFIER_PHRASE, undefOrString(dayparts[0].qualifierPhrase[i]));
            updateDaypart(i, dOrN, CH_DP_QUALIFIER_CODE, undefOrString(dayparts[0].qualifierCode[i]));
            updateDaypart(i, dOrN, CH_DP_TEMP, undefOrQuantity(dp.temperature[i], getTempUnit()));
            updateDaypart(i, dOrN, CH_DP_TEMP_HEAT_INDEX, undefOrQuantity(dp.temperatureHeatIndex[i], getTempUnit()));
            updateDaypart(i, dOrN, CH_DP_TEMP_WIND_CHILL, undefOrQuantity(dp.temperatureWindChill[i], getTempUnit()));
            updateDaypart(i, dOrN, CH_DP_HUMIDITY, undefOrQuantity(dp.relativeHumidity[i], Units.PERCENT));
            updateDaypart(i, dOrN, CH_DP_CLOUD_COVER, undefOrQuantity(dp.cloudCover[i], Units.PERCENT));
            updateDaypart(i, dOrN, CH_DP_PRECIP_CHANCE, undefOrQuantity(dp.precipChance[i], Units.PERCENT));
            updateDaypart(i, dOrN, CH_DP_PRECIP_TYPE, undefOrString(dp.precipType[i]));
            updateDaypart(i, dOrN, CH_DP_PRECIP_RAIN, undefOrQuantity(dp.qpf[i], getLengthUnit()));
            updateDaypart(i, dOrN, CH_DP_PRECIP_SNOW, undefOrQuantity(dp.qpfSnow[i], getLengthUnit()));
            updateDaypart(i, dOrN, CH_DP_SNOW_RANGE, undefOrString(dp.snowRange[i]));
            updateDaypart(i, dOrN, CH_DP_WIND_SPEED, undefOrQuantity(dp.windSpeed[i], getSpeedUnit()));
            updateDaypart(i, dOrN, CH_DP_WIND_DIR_CARDINAL, undefOrString(dp.windDirectionCardinal[i]));
            updateDaypart(i, dOrN, CH_DP_WIND_PHRASE, undefOrString(dp.windPhrase[i]));
            updateDaypart(i, dOrN, CH_DP_WIND_DIR, undefOrQuantity(dp.windDirection[i], Units.DEGREE_ANGLE));
            updateDaypart(i, dOrN, CH_DP_THUNDER_CATEGORY, undefOrString(dp.thunderCategory[i]));
            updateDaypart(i, dOrN, CH_DP_THUNDER_INDEX, undefOrDecimal(dp.thunderIndex[i]));
            updateDaypart(i, dOrN, CH_DP_UV_DESCRIPTION, undefOrString(dp.uvDescription[i]));
            updateDaypart(i, dOrN, CH_DP_UV_INDEX, undefOrDecimal(dp.uvIndex[i]));
            updateDaypart(i, dOrN, CH_DP_ICON_CODE, undefOrDecimal(dp.iconCode[i]));
            updateDaypart(i, dOrN, CH_DP_ICON_CODE_EXTEND, undefOrDecimal(dp.iconCodeExtend[i]));
            updateDaypart(i, dOrN, CH_DP_ICON_IMAGE, getIconImage(dp.iconCode[i]));
        }
    }

    private State getIconImage(Integer iconCode) {
        // First try to get the image associated with the icon code
        byte[] image = getImage("icons" + File.separator + String.format("%02d", iconCode) + ".png");
        if (image != null) {
            return new RawType(image, "image/png");
        }
        // Next try to get the N/A image
        image = getImage("icons" + File.separator + "na.png");
        if (image != null) {
            return new RawType(image, "image/png");
        }
        // Couldn't get any icon image, so set to UNDEF
        return UnDefType.UNDEF;
    }

    private byte @Nullable [] getImage(String iconPath) {
        byte[] data = null;
        URL url = FrameworkUtil.getBundle(getClass()).getResource(iconPath);
        logger.trace("Path to icon image resource is: {}", url);
        if (url != null) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                InputStream is = url.openStream();
                BufferedImage image = ImageIO.read(is);
                ImageIO.write(image, "png", out);
                out.flush();
                data = out.toByteArray();
            } catch (IOException e) {
                logger.debug("I/O exception occurred getting image data: {}", e.getMessage(), e);
            }
        }
        return data;
    }

    private void updateDaily(int day, String channelId, State state) {
        updateChannel(CH_GROUP_FORECAST_DAY + String.valueOf(day) + "#" + channelId, state);
    }

    private void updateDaypart(int daypartIndex, String dayOrNight, String channelId, State state) {
        int day = daypartIndex / 2;
        String dON = "D".equals(dayOrNight) ? CH_GROUP_FORECAST_DAYPART_DAY : CH_GROUP_FORECAST_DAYPART_NIGHT;
        updateChannel(CH_GROUP_FORECAST_DAY + String.valueOf(day) + dON + "#" + channelId, state);
    }

    /*
     * The refresh job updates the daily forecast on the
     * refresh interval set in the thing config
     */
    private void scheduleRefreshJob() {
        logger.debug("Handler: Scheduling forecast refresh job in {} seconds", REFRESH_JOB_INITIAL_DELAY_SECONDS);
        cancelRefreshJob();
        refreshForecastJob = scheduler.scheduleWithFixedDelay(refreshRunnable, REFRESH_JOB_INITIAL_DELAY_SECONDS,
                refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    private void cancelRefreshJob() {
        if (refreshForecastJob != null) {
            refreshForecastJob.cancel(true);
            logger.debug("Handler: Canceling forecast refresh job");
        }
    }
}
