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

import java.time.DateTimeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the mower's weather API endpoint.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectWeatherHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectWeatherHandler.class);
    private final RobonectHandler handler;

    RobonectWeatherHandler(RobonectHandler handler) {
        this.handler = handler;
    }

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
                return;
            }
            value = String.valueOf(valueNumber);
        } else {
            return;
        }
        if (parameter == null) {
            return;
        }
        handler.cancelCommandRefresh("weather");
        WeatherInfo result = handler.getRobonectClient().updateWeather(parameter, value);
        if (result.isSuccessful()) {
            applyWeatherInfo(result);
            handler.scheduleCommandRefresh("weather", this::refreshWeatherInfo);
        }
    }

    void refreshWeatherInfo() {
        applyWeatherInfo(handler.getRobonectClient().getWeatherInfo());
    }

    void applyWeatherInfo(WeatherInfo info) {
        Weather weather = info.getWeather();
        if (!info.isSuccessful()) {
            return;
        }
        if (weather != null) {
            handler.updateSwitch(CHANNEL_WEATHER_BREAK, weather.getWeatherBreak());
            updateWeatherPercentage(CHANNEL_WEATHER_RAIN, weather.getRain());
            Integer temperature = weather.getTemperature();
            if (temperature != null)
                handler.setState(CHANNEL_WEATHER_TEMPERATURE, new QuantityType<>(temperature, SIUnits.CELSIUS));
            Integer humidity = weather.getHumidity();
            if (humidity != null)
                handler.setState(CHANNEL_WEATHER_HUMIDITY, new QuantityType<>(humidity, Units.PERCENT));
            handler.updateSwitch(CHANNEL_WEATHER_DAY, weather.getDay());
            if (weather.getCity() != null)
                handler.setState(CHANNEL_WEATHER_CITY, new StringType(weather.getCity()));
            Integer remaining = weather.getRemaining();
            if (remaining != null)
                handler.setState(CHANNEL_WEATHER_REMAINING, new QuantityType<>(remaining, Units.SECOND));
            updateWeatherDateTime(CHANNEL_WEATHER_SUNRISE, weather.getSunrise());
            updateWeatherDateTime(CHANNEL_WEATHER_SUNSET, weather.getSunset());
            if (weather.getIcon() != null) {
                handler.setState(CHANNEL_WEATHER_ICON, new StringType(weather.getIcon()));
            }
            Clock timestamp = weather.getTimestamp();
            if (timestamp != null) {
                updateWeatherDateTime(CHANNEL_WEATHER_TIMESTAMP, timestamp.getUnix());
            }
            WeatherCondition condition = weather.getCondition();
            if (condition != null) {
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_TOO_RAINY, condition.getTooRainy());
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_TOO_COLD, condition.getTooCold());
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_TOO_WARM, condition.getTooWarm());
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_TOO_DRY, condition.getTooDry());
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_TOO_WET, condition.getTooWet());
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_DAY, condition.getDay());
                handler.updateSwitch(CHANNEL_WEATHER_CONDITION_NIGHT, condition.getNight());
            }
        }
        WeatherService service = info.getService();
        if (service != null) {
            WeatherLocation location = service.getLocation();
            if (location != null) {
                updateWeatherNumber(CHANNEL_WEATHER_LOCATION_CITY_ID, location.getCityId());
                if (location.getZip() != null) {
                    handler.setState(CHANNEL_WEATHER_LOCATION_ZIP, new StringType(location.getZip()));
                }
                if (location.getCountry() != null) {
                    handler.setState(CHANNEL_WEATHER_LOCATION_COUNTRY, new StringType(location.getCountry()));
                }
            }
            handler.updateSwitch(CHANNEL_WEATHER_ENABLED, service.getEnable());
            WeatherConfig config = service.getConfig();
            if (config != null) {
                updateWeatherPercentage(CHANNEL_WEATHER_MAX_RAIN, config.getMaxrain());
                updateWeatherTemperature(CHANNEL_WEATHER_MIN_TEMPERATURE, config.getMintemp());
                updateWeatherTemperature(CHANNEL_WEATHER_MAX_TEMPERATURE, config.getMaxtemp());
                updateWeatherPercentage(CHANNEL_WEATHER_MIN_HUMIDITY, config.getMinhumidity());
                updateWeatherPercentage(CHANNEL_WEATHER_MAX_HUMIDITY, config.getMaxhumidity());
                handler.updateSwitch(CHANNEL_WEATHER_SKIP_DAY, config.getDontmowduringday());
                handler.updateSwitch(CHANNEL_WEATHER_SKIP_NIGHT, config.getDontmowduringnight());
            }
            Integer interval = service.getInterval();
            if (interval == null && handler.isUnofficialApiEnabled() && handler.canPublishState()) {
                try {
                    interval = handler.getRobonectClient().getWeatherIntervalViaWebInterface();
                } catch (RobonectCommunicationException e) {
                    logger.debug("Could not read weather interval from web interface: {}", e.getMessage());
                }
            }
            if (interval != null) {
                handler.setState(CHANNEL_WEATHER_INTERVAL, new QuantityType<>(interval, Units.MINUTE));
            }
        }
    }

    private void updateWeatherTemperature(String channel, @Nullable Integer value) {
        if (value != null)
            handler.setState(channel, new QuantityType<>(value, SIUnits.CELSIUS));
    }

    private void updateWeatherDateTime(String channel, @Nullable String unix) {
        if (unix == null) {
            return;
        }
        try {
            handler.setState(channel, handler.convertUnixToDateTimeType(unix));
        } catch (NumberFormatException | DateTimeException e) {
            logger.debug("Ignoring invalid weather timestamp for {}", channel);
            handler.setState(channel, UnDefType.UNDEF);
        }
    }

    private void updateWeatherNumber(String channel, @Nullable Integer value) {
        if (value != null)
            handler.setState(channel, new DecimalType(value));
    }

    private void updateWeatherPercentage(String channel, @Nullable Integer value) {
        if (value != null)
            handler.setState(channel, new QuantityType<>(value, Units.PERCENT));
    }
}
