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
package org.openhab.binding.sagercaster.internal.handler;

import static org.openhab.binding.sagercaster.internal.SagerCasterBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sagercaster.internal.WindDirectionStateDescriptionProvider;
import org.openhab.binding.sagercaster.internal.caster.SagerWeatherCaster;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SagerCasterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SagerCasterHandler extends BaseThingHandler {
    private static final Duration ALGORITHM_WINDOW = Duration.ofHours(6);
    private final Logger logger = LoggerFactory.getLogger(SagerCasterHandler.class);

    private final SagerWeatherCaster sagerWeatherCaster;

    private final WindDirectionStateDescriptionProvider stateDescriptionProvider;

    private final ExpiringMap<Double> pressureCache = new ExpiringMap<>(ALGORITHM_WINDOW);
    private final ExpiringMap<Double> temperatureCache = new ExpiringMap<>(ALGORITHM_WINDOW);
    private final ExpiringMap<Integer> bearingCache = new ExpiringMap<>(ALGORITHM_WINDOW);

    private double currentTemp = 0;
    private @Nullable String currentSagerCode;

    public SagerCasterHandler(final Thing thing, final WindDirectionStateDescriptionProvider stateDescriptionProvider,
            final SagerWeatherCaster sagerWeatherCaster) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.sagerWeatherCaster = sagerWeatherCaster;
    }

    @Override
    public void initialize() {
        if (getConfig().get(CONFIG_LOCATION) instanceof String locationString) {
            try {
                PointType location = new PointType(locationString);
                sagerWeatherCaster.setLatitude(location.getLatitude().doubleValue());
                defineWindDirectionStateDescriptions();
                updateStatus(ThingStatus.ONLINE);
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Location incorrectly configured");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Location incorrectly configured");
        }
    }

    private void defineWindDirectionStateDescriptions() {
        List<StateOption> options = new ArrayList<>();
        String[] directions = sagerWeatherCaster.getUsedDirections();
        for (int i = 0; i < directions.length; i++) {
            int secondDirection = i < directions.length - 1 ? i + 1 : 0;
            String windDescription = "%s or %s winds".formatted(directions[i], directions[secondDirection]);
            options.add(new StateOption(String.valueOf(i + 1), windDescription));
        }

        options.add(new StateOption("9", "Shifting / Variable winds"));
        ThingUID thingUID = getThing().getUID();
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, GROUP_OUTPUT, CHANNEL_WINDFROM), options);
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, GROUP_OUTPUT, CHANNEL_WINDTO), options);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            postNewForecast();
        } else {
            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_CLOUDINESS:
                    logger.debug("Cloud level changed, updating forecast");
                    if (command instanceof QuantityType cloudiness) {
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setCloudLevel(cloudiness.intValue());
                            postNewForecast();
                        });
                    }
                    break;
                case CHANNEL_IS_RAINING:
                    logger.debug("Rain status updated, updating forecast");
                    if (command instanceof OnOffType isRaining) {
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setRaining(OnOffType.ON.equals(isRaining));
                            postNewForecast();
                        });
                    } else {
                        logger.debug("Channel '{}' accepts Switch commands.", channelUID);
                    }
                    break;
                case CHANNEL_RAIN_QTTY:
                    logger.debug("Rain status updated, updating forecast");
                    switch (command) {
                        case QuantityType<?> quantity -> updateRain(quantity);
                        case DecimalType decimal -> updateRain(decimal);
                        default ->
                            logger.debug("Channel '{}' accepts Number, Number:(Speed|Length) commands.", channelUID);
                    }
                    break;
                case CHANNEL_WIND_SPEED:
                    logger.debug("Updated wind speed, updating forecast");
                    if (command instanceof DecimalType newValue) {
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setBeaufort(newValue.intValue());
                            postNewForecast();
                        });
                    } else {
                        logger.debug("Channel '{}' only accepts DecimalType commands.", channelUID);
                    }
                    break;
                case CHANNEL_PRESSURE:
                    logger.debug("Sea-level pressure updated, updating forecast");
                    if (command instanceof QuantityType) {
                        QuantityType<?> pressure = ((QuantityType<Pressure>) command).toUnit(HECTO(SIUnits.PASCAL));
                        if (pressure != null) {
                            double newPressureValue = pressure.doubleValue();
                            pressureCache.put(newPressureValue).ifPresentOrElse(oldPressure -> scheduler.submit(() -> {
                                sagerWeatherCaster.setPressure(newPressureValue, oldPressure);
                                updateChannelString(GROUP_OUTPUT, CHANNEL_PRESSURETREND,
                                        String.valueOf(sagerWeatherCaster.getPressureEvolution()));
                                postNewForecast();
                            }), () -> updateChannelString(GROUP_OUTPUT, CHANNEL_FORECAST, FORECAST_PENDING));
                        }
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    logger.debug("Temperature updated");
                    if (command instanceof QuantityType) {
                        QuantityType<?> temperature = ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS);
                        if (temperature != null) {
                            currentTemp = temperature.doubleValue();
                            temperatureCache.put(currentTemp).ifPresent(oldTemperature -> {
                                double delta = currentTemp - oldTemperature;
                                String trend = (delta > 3) ? "1"
                                        : (delta > 0.3) ? "2" : (delta > -0.3) ? "3" : (delta > -3) ? "4" : "5";
                                updateChannelString(GROUP_OUTPUT, CHANNEL_TEMPERATURETREND, trend);
                            });
                        }
                    }
                    break;
                case CHANNEL_WIND_ANGLE:
                    logger.debug("Updated wind direction, updating forecast");
                    if (command instanceof QuantityType angle) {
                        int newAngleValue = angle.intValue();
                        bearingCache.put(newAngleValue).ifPresent(oldAngle -> scheduler.submit(() -> {
                            sagerWeatherCaster.setBearing(newAngleValue, oldAngle);
                            updateChannelString(GROUP_OUTPUT, CHANNEL_WINDEVOLUTION,
                                    String.valueOf(sagerWeatherCaster.getWindEvolution()));
                            postNewForecast();
                        }));
                    }
                    break;
                default:
                    logger.debug("The binding can not handle command: {} on channel: {}", command, channelUID);
            }
        }
    }

    private void updateRain(Number newQtty) {
        scheduler.submit(() -> {
            sagerWeatherCaster.setRaining(newQtty.doubleValue() > 0);
            postNewForecast();
        });
    }

    private void postNewForecast() {
        String newCode = sagerWeatherCaster.getSagerCode();
        if (!Objects.equals(newCode, currentSagerCode)) {
            logger.debug("Sager prediction changed to {}", newCode);
            currentSagerCode = newCode;
            updateChannelTimeStamp(GROUP_OUTPUT, CHANNEL_TIMESTAMP, Instant.now());
            String forecast = sagerWeatherCaster.getForecast();
            // Sharpens forecast if current temp is below 2 degrees, likely to be flurries rather than shower
            forecast += SHOWERS.contains(forecast) ? (currentTemp > 2) ? "1" : "2" : "";

            updateChannelString(GROUP_OUTPUT, CHANNEL_FORECAST, forecast);
            updateChannelString(GROUP_OUTPUT, CHANNEL_WINDFROM, sagerWeatherCaster.getWindDirection());
            updateChannelString(GROUP_OUTPUT, CHANNEL_WINDTO, sagerWeatherCaster.getWindDirection2());
            updateChannelString(GROUP_OUTPUT, CHANNEL_VELOCITY, sagerWeatherCaster.getWindVelocity());
            updateChannelDecimal(GROUP_OUTPUT, CHANNEL_VELOCITY_BEAUFORT, sagerWeatherCaster.getPredictedBeaufort());
            updateReliability(GROUP_OUTPUT, CHANNEL_RELIABILITY);
        }
    }

    private @Nullable ChannelUID getChannelId(String group, String channelId) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        return isLinked(id) ? id : null;
    }

    private void updateChannelTimeStamp(String group, String channelId, Instant instant) {
        if (getChannelId(group, channelId) instanceof ChannelUID id) {
            updateState(id, new DateTimeType(instant));
        }
    }

    private void updateChannelString(String group, String channelId, @Nullable String value) {
        if (getChannelId(group, channelId) instanceof ChannelUID id) {
            updateState(id, value != null ? new StringType(value) : UnDefType.NULL);
        }
    }

    private void updateChannelDecimal(String group, String channelId, int value) {
        if (getChannelId(group, channelId) instanceof ChannelUID id) {
            updateState(id, new DecimalType(value));
        }
    }

    private void updateReliability(String group, String channelId) {
        if (getChannelId(group, channelId) instanceof ChannelUID id) {
            long minDuration = Math.min(bearingCache.getDataAgeInMin(),
                    Math.min(pressureCache.getDataAgeInMin(), temperatureCache.getDataAgeInMin()));
            double ratio = minDuration > 0 ? Math.min(1.0, minDuration / (ALGORITHM_WINDOW.toMinutes() * 1.0)) : 0.0;
            updateState(id, new PercentType((int) Math.round(ratio * 100.0)));
        }
    }
}
