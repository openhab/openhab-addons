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
import static org.openhab.binding.robonect.internal.handler.RobonectHandler.read;

import java.time.DateTimeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectWeatherApi;
import org.openhab.binding.robonect.internal.model.Clock;
import org.openhab.binding.robonect.internal.model.Weather;
import org.openhab.binding.robonect.internal.model.WeatherCondition;
import org.openhab.binding.robonect.internal.model.WeatherConfig;
import org.openhab.binding.robonect.internal.model.WeatherInfo;
import org.openhab.binding.robonect.internal.model.WeatherLocation;
import org.openhab.binding.robonect.internal.model.WeatherService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the mower's weather API endpoint.
 *
 * @author Nico Brüttner - Initial weather endpoint handling
 */
@NonNullByDefault
final class RobonectWeatherHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectWeatherHandler.class);
    private final RobonectHandler handler;
    private final RobonectWeatherApi weatherApi;

    /**
     * Creates a weather component backed by the Thing handler.
     *
     * @param handler the {@link RobonectHandler} providing state updates
     * @param weatherApi the weather API for the current client
     */
    RobonectWeatherHandler(RobonectHandler handler, RobonectWeatherApi weatherApi) {
        this.handler = handler;
        this.weatherApi = weatherApi;
    }

    /**
     * Validates and writes a supported weather-service setting.
     *
     * @param channelId the channel identifier
     * @param command the command received for the channel
     */
    void handleCommand(String channelId, Command command) {
        String parameter;
        String value;
        if (channelId.equals(CHANNEL_WEATHER_LOCATION_ZIP) && command instanceof StringType string) {
            parameter = "zip";
            value = string.toFullString();
        } else if (channelId.equals(CHANNEL_WEATHER_LOCATION_CITY_ID) && command instanceof DecimalType decimal) {
            int cityId;
            try {
                cityId = decimal.toBigDecimal().intValueExact();
            } catch (ArithmeticException e) {
                logger.debug("Ignoring non-integer or out-of-range weather city ID");
                return;
            }
            if (cityId < 0) {
                return;
            }
            parameter = "cityid";
            value = Integer.toString(cityId);
        } else if (command instanceof OnOffType onOff) {
            parameter = switch (channelId) {
                case CHANNEL_WEATHER_ENABLED -> "service";
                case CHANNEL_WEATHER_SKIP_DAY -> "day";
                case CHANNEL_WEATHER_SKIP_NIGHT -> "night";
                default -> null;
            };
            value = onOff == OnOffType.ON ? "1" : "0";
        } else if (command instanceof Number number) {
            @Nullable
            Number valueNumber = number;
            parameter = switch (channelId) {
                case CHANNEL_WEATHER_INTERVAL -> {
                    if (command instanceof QuantityType<?> quantity) {
                        valueNumber = quantity.toUnit(Units.MINUTE);
                    }
                    yield "interval";
                }
                case CHANNEL_WEATHER_MAX_RAIN -> {
                    if (command instanceof QuantityType<?> quantity) {
                        valueNumber = quantity.toUnit(Units.PERCENT);
                    }
                    yield "rain";
                }
                case CHANNEL_WEATHER_MIN_TEMPERATURE -> {
                    if (command instanceof QuantityType<?> quantity) {
                        valueNumber = quantity.toUnit(SIUnits.CELSIUS);
                    }
                    yield "mintemp";
                }
                case CHANNEL_WEATHER_MAX_TEMPERATURE -> {
                    if (command instanceof QuantityType<?> quantity) {
                        valueNumber = quantity.toUnit(SIUnits.CELSIUS);
                    }
                    yield "maxtemp";
                }
                case CHANNEL_WEATHER_MIN_HUMIDITY -> {
                    if (command instanceof QuantityType<?> quantity) {
                        valueNumber = quantity.toUnit(Units.PERCENT);
                    }
                    yield "minhum";
                }
                case CHANNEL_WEATHER_MAX_HUMIDITY -> {
                    if (command instanceof QuantityType<?> quantity) {
                        valueNumber = quantity.toUnit(Units.PERCENT);
                    }
                    yield "maxhum";
                }
                default -> null;
            };
            if (parameter == null || valueNumber == null) {
                if (parameter == null) {
                    logUnsupportedCommandType(channelId, command);
                }
                if (valueNumber == null) {
                    // unit could not be converted to the expected type
                    logger.debug("Ignoring command with unsupported unit for weather channel {}; value: {}", channelId,
                            command);
                }
                return;
            }
            value = String.valueOf(valueNumber);
        } else {
            logUnsupportedCommandType(channelId, command);
            return;
        }
        if (parameter == null) {
            return;
        }
        handler.cancelCommandRefresh("weather");
        WeatherInfo result = weatherApi.updateWeather(parameter, value);
        if (result.isSuccessful()) {
            applyWeatherInfo(result, false);
            handler.scheduleCommandRefresh("weather", this::refreshWeatherInfo, 5);
        }
    }

    /**
     * Logs a command whose type is unsupported for a recognized weather setting channel.
     *
     * @param channelId the weather channel identifier
     * @param command the unsupported command
     */
    private void logUnsupportedCommandType(String channelId, Command command) {
        @Nullable
        String expectedType = switch (channelId) {
            case CHANNEL_WEATHER_LOCATION_ZIP -> "StringType";
            case CHANNEL_WEATHER_LOCATION_CITY_ID -> "DecimalType";
            case CHANNEL_WEATHER_ENABLED, CHANNEL_WEATHER_SKIP_DAY, CHANNEL_WEATHER_SKIP_NIGHT -> "OnOffType";
            case CHANNEL_WEATHER_INTERVAL, CHANNEL_WEATHER_MAX_RAIN, CHANNEL_WEATHER_MIN_TEMPERATURE,
                    CHANNEL_WEATHER_MAX_TEMPERATURE, CHANNEL_WEATHER_MIN_HUMIDITY, CHANNEL_WEATHER_MAX_HUMIDITY ->
                "Number";
            default -> null;
        };
        if (expectedType != null) {
            logger.debug("Ignoring command of type {} for weather channel {}; {} is expected.",
                    command.getClass().getName(), channelId, expectedType);
        }
    }

    /** Reads and applies weather measurements and service settings. */
    void refreshWeatherInfo() {
        applyWeatherInfo(weatherApi.getWeatherInfo(), true);
    }

    /**
     * Publishes weather measurements and service settings from a successful response.
     *
     * @param info the weather response
     * @param fullResponse whether missing fields should be published as undefined
     */
    private void applyWeatherInfo(WeatherInfo info, boolean fullResponse) {
        if (!info.isSuccessful()) {
            return;
        }
        Weather weather = info.getWeather();
        handler.setState(CHANNEL_WEATHER_BREAK,
                read(weather, Weather::getWeatherBreak).map(OnOffType::from).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_RAIN,
                read(weather, Weather::getRain).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_TEMPERATURE, read(weather, Weather::getTemperature)
                .map(value -> new QuantityType<>(value, SIUnits.CELSIUS)).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_HUMIDITY,
                read(weather, Weather::getHumidity).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_DAY, read(weather, Weather::getDay).map(OnOffType::from).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_CITY, read(weather, Weather::getCity).map(StringType::new).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_REMAINING,
                read(weather, Weather::getRemaining).map(value -> new QuantityType<>(value, Units.SECOND)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_SUNRISE,
                read(weather, Weather::getSunrise).map(this::weatherDateTime).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_SUNSET,
                read(weather, Weather::getSunset).map(this::weatherDateTime).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_ICON, read(weather, Weather::getIcon).map(StringType::new).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_TIMESTAMP,
                read(weather, Weather::getTimestamp).map(Clock::getUnix).map(this::weatherDateTime).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_TOO_RAINY, read(weather, Weather::getCondition)
                .map(WeatherCondition::getTooRainy).map(OnOffType::from).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_TOO_COLD, read(weather, Weather::getCondition)
                .map(WeatherCondition::getTooCold).map(OnOffType::from).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_TOO_WARM, read(weather, Weather::getCondition)
                .map(WeatherCondition::getTooWarm).map(OnOffType::from).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_TOO_DRY,
                read(weather, Weather::getCondition).map(WeatherCondition::getTooDry).map(OnOffType::from).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_TOO_WET,
                read(weather, Weather::getCondition).map(WeatherCondition::getTooWet).map(OnOffType::from).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_DAY,
                read(weather, Weather::getCondition).map(WeatherCondition::getDay).map(OnOffType::from).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_CONDITION_NIGHT,
                read(weather, Weather::getCondition).map(WeatherCondition::getNight).map(OnOffType::from).orElse(null),
                fullResponse);
        WeatherService service = info.getService();
        handler.setState(CHANNEL_WEATHER_LOCATION_CITY_ID, read(service, WeatherService::getLocation)
                .map(WeatherLocation::getCityId).map(DecimalType::new).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_LOCATION_ZIP, read(service, WeatherService::getLocation)
                .map(WeatherLocation::getZip).map(StringType::new).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_LOCATION_COUNTRY, read(service, WeatherService::getLocation)
                .map(WeatherLocation::getCountry).map(StringType::new).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_ENABLED,
                read(service, WeatherService::getEnable).map(OnOffType::from).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_MAX_RAIN, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getMaxrain).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_MIN_TEMPERATURE, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getMintemp).map(value -> new QuantityType<>(value, SIUnits.CELSIUS)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_MAX_TEMPERATURE, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getMaxtemp).map(value -> new QuantityType<>(value, SIUnits.CELSIUS)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_MIN_HUMIDITY, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getMinhumidity).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_MAX_HUMIDITY, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getMaxhumidity).map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_WEATHER_SKIP_DAY, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getDontmowduringday).map(OnOffType::from).orElse(null), fullResponse);
        handler.setState(CHANNEL_WEATHER_SKIP_NIGHT, read(service, WeatherService::getConfig)
                .map(WeatherConfig::getDontmowduringnight).map(OnOffType::from).orElse(null), fullResponse);
        Integer interval = read(service, WeatherService::getInterval).orElse(null);
        if (interval == null && (fullResponse || service != null) && handler.isUnofficialApiEnabled()
                && handler.canPublishState()) {
            try {
                interval = weatherApi.getWeatherIntervalViaWebInterface();
            } catch (RobonectCommunicationException e) {
                logger.debug("Could not read weather interval from web interface: {}", e.getMessage());
            }
        }
        handler.setState(CHANNEL_WEATHER_INTERVAL, interval == null ? null : new QuantityType<>(interval, Units.MINUTE),
                fullResponse);
    }

    /**
     * Converts a weather timestamp using the configured time zone.
     *
     * @param unix the reported Unix timestamp in seconds
     * @return the date-time state, or {@link UnDefType#UNDEF} if the timestamp is invalid
     */
    private State weatherDateTime(String unix) {
        try {
            return handler.convertUnixToDateTimeType(unix);
        } catch (NumberFormatException | DateTimeException e) {
            logger.debug("Ignoring invalid weather timestamp: {}", e.getMessage());
            return UnDefType.UNDEF;
        }
    }
}
