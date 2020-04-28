/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.HECTO;
import static org.openhab.binding.sagercaster.internal.SagerCasterBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.sagercaster.internal.SagerWeatherCaster;
import org.openhab.binding.sagercaster.internal.WindDirectionStateDescriptionProvider;
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
    private final static String FORECAST_PENDING = "0";
    private final static Set<String> SHOWERS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("G", "K", "L", "R", "S", "T", "U", "W")));

    private final Logger logger = LoggerFactory.getLogger(SagerCasterHandler.class);
    private final SagerWeatherCaster sagerWeatherCaster;
    private final WindDirectionStateDescriptionProvider stateDescriptionProvider;
    private int currentTemp = 0;

    private final ExpiringMap<QuantityType<Pressure>> pressureCache = new ExpiringMap<>();
    private final ExpiringMap<QuantityType<Temperature>> temperatureCache = new ExpiringMap<>();
    private final ExpiringMap<QuantityType<Angle>> bearingCache = new ExpiringMap<>();

    public SagerCasterHandler(Thing thing, WindDirectionStateDescriptionProvider stateDescriptionProvider,
            SagerWeatherCaster sagerWeatherCaster) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.sagerWeatherCaster = sagerWeatherCaster;
    }

    @Override
    public void initialize() {
        String location = (String) getConfig().get(CONFIG_LOCATION);
        int observationPeriod = ((BigDecimal) getConfig().get(CONFIG_PERIOD)).intValue();
        String latitude = location.split(",")[0];
        sagerWeatherCaster.setLatitude(Double.parseDouble(latitude));
        long period = TimeUnit.SECONDS.toMillis(observationPeriod);
        pressureCache.setObservationPeriod(period);
        bearingCache.setObservationPeriod(period);
        temperatureCache.setObservationPeriod(period);
        defineWindDirectionStateDescriptions();
        updateStatus(ThingStatus.ONLINE);
    }

    private void defineWindDirectionStateDescriptions() {
        List<StateOption> options = new ArrayList<>();
        String[] directions = sagerWeatherCaster.getUsedDirections();
        for (int i = 0; i < directions.length; i++) {
            int secondDirection = i < directions.length - 1 ? i + 1 : 0;
            String windDescription = directions[i] + " or " + directions[secondDirection] + " winds";
            options.add(new StateOption(String.valueOf(i + 1), windDescription));
        }

        options.add(new StateOption("9", "Shifting / Variable winds"));
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), GROUP_OUTPUT, CHANNEL_WINDFROM),
                options);
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), GROUP_OUTPUT, CHANNEL_WINDTO),
                options);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            postNewForecast();
        } else {
            String id = channelUID.getIdWithoutGroup();
            switch (id) {
                case CHANNEL_CLOUDINESS:
                    logger.debug("Octa cloud level changed, updating forecast");
                    if (command instanceof QuantityType) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Dimensionless> cloudiness = (QuantityType<Dimensionless>) command;
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setCloudLevel(cloudiness.intValue());
                            postNewForecast();
                        });
                        break;
                    }
                case CHANNEL_IS_RAINING:
                    logger.debug("Rain status updated, updating forecast");
                    if (command instanceof OnOffType) {
                        OnOffType isRaining = ((OnOffType) command);
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setRaining(isRaining == OnOffType.ON);
                            postNewForecast();
                        });
                    } else {
                        logger.debug("Channel '{}' can only accept Switch type commands.", channelUID);
                    }
                    break;
                case CHANNEL_RAIN_QTTY:
                    logger.debug("Rain status updated, updating forecast");
                    if (command instanceof QuantityType) {
                        QuantityType<?> newQtty = ((QuantityType<?>) command);
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setRaining(newQtty.doubleValue() > 0);
                            postNewForecast();
                        });
                    } else if (command instanceof DecimalType) {
                        DecimalType newQtty = ((DecimalType) command);
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setRaining(newQtty.doubleValue() > 0);
                            postNewForecast();
                        });
                    } else {
                        logger.debug("Channel '{}' can accept Number, Number:Speed, Number:Length type commands.",
                                channelUID);
                    }
                    break;
                case CHANNEL_WIND_SPEED:
                    logger.debug("Updated wind speed, updating forecast");
                    if (command instanceof DecimalType) {
                        DecimalType newValue = (DecimalType) command;
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
                        @SuppressWarnings("unchecked")
                        QuantityType<Pressure> newPressure = ((QuantityType<Pressure>) command)
                                .toUnit(HECTO(SIUnits.PASCAL));
                        if (newPressure != null) {
                            pressureCache.put(newPressure);
                            Optional<QuantityType<Pressure>> agedPressure = pressureCache.getAgedValue();
                            if (agedPressure.isPresent()) {
                                scheduler.submit(() -> {
                                    sagerWeatherCaster.setPressure(newPressure.doubleValue(),
                                            agedPressure.get().doubleValue());
                                    updateChannelString(GROUP_OUTPUT, CHANNEL_PRESSURETREND,
                                            String.valueOf(sagerWeatherCaster.getPressureEvolution()));
                                    postNewForecast();
                                });
                            } else {
                                updateChannelString(GROUP_OUTPUT, CHANNEL_FORECAST, FORECAST_PENDING);
                            }
                        }
                    }
                    break;
                case CHANNEL_TEMPERATURE:
                    logger.debug("Temperature updated");
                    if (command instanceof QuantityType) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Temperature> newTemperature = ((QuantityType<Temperature>) command)
                                .toUnit(SIUnits.CELSIUS);
                        if (newTemperature != null) {
                            temperatureCache.put(newTemperature);
                            currentTemp = newTemperature.intValue();
                            Optional<QuantityType<Temperature>> agedTemperature = temperatureCache.getAgedValue();
                            if (agedTemperature.isPresent()) {
                                double delta = newTemperature.doubleValue() - agedTemperature.get().doubleValue();
                                String trend = (delta > 3) ? "1"
                                        : (delta > 0.3) ? "2" : (delta > -0.3) ? "3" : (delta > -3) ? "4" : "5";
                                updateChannelString(GROUP_OUTPUT, CHANNEL_TEMPERATURETREND, trend);
                            }
                        }
                    }
                    break;
                case CHANNEL_WIND_ANGLE:
                    logger.debug("Updated wind direction, updating forecast");
                    if (command instanceof QuantityType) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Angle> newAngle = (QuantityType<Angle>) command;
                        bearingCache.put(newAngle);
                        Optional<QuantityType<Angle>> agedAngle = bearingCache.getAgedValue();
                        if (agedAngle.isPresent()) {
                            scheduler.submit(() -> {
                                sagerWeatherCaster.setBearing(newAngle.intValue(), agedAngle.get().intValue());
                                updateChannelString(GROUP_OUTPUT, CHANNEL_WINDEVOLUTION,
                                        String.valueOf(sagerWeatherCaster.getWindEvolution()));
                                postNewForecast();
                            });
                        }
                    }
                    break;
                default:
                    logger.debug("The binding can not handle command: {} on channel: {}", command, channelUID);
            }
        }
    }

    private void postNewForecast() {
        String forecast = sagerWeatherCaster.getForecast();
        // Sharpens forecast if current temp is below 2 degrees, likely to be flurries rather than shower
        forecast += SHOWERS.contains(forecast) ? (currentTemp > 2) ? "1" : "2" : "";

        updateChannelString(GROUP_OUTPUT, CHANNEL_FORECAST, forecast);
        updateChannelString(GROUP_OUTPUT, CHANNEL_WINDFROM, sagerWeatherCaster.getWindDirection());
        updateChannelString(GROUP_OUTPUT, CHANNEL_WINDTO, sagerWeatherCaster.getWindDirection2());

        String velocity = sagerWeatherCaster.getWindVelocity();
        updateChannelString(GROUP_OUTPUT, CHANNEL_VELOCITY, velocity);
        int predictedBeaufort = sagerWeatherCaster.getBeaufort();
        switch (velocity) {
            case "N":
                predictedBeaufort += 1;
                break;
            case "F":
                predictedBeaufort = 4;
                break;
            case "S":
                predictedBeaufort = 6;
                break;
            case "G":
                predictedBeaufort = 8;
                break;
            case "W":
                predictedBeaufort = 10;
                break;
            case "H":
                predictedBeaufort = 12;
                break;
            case "D":
                predictedBeaufort -= 1;
                break;
        }
        updateChannelDecimal(GROUP_OUTPUT, CHANNEL_VELOCITY_BEAUFORT, predictedBeaufort);
    }

    protected void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }
    }

    protected void updateChannelDecimal(String group, String channelId, int value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new DecimalType(value));
        }
    }
}
