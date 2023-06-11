/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sagercaster.internal.WindDirectionStateDescriptionProvider;
import org.openhab.binding.sagercaster.internal.caster.SagerWeatherCaster;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
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

    private final Logger logger = LoggerFactory.getLogger(SagerCasterHandler.class);

    private final SagerWeatherCaster sagerWeatherCaster;

    private final WindDirectionStateDescriptionProvider stateDescriptionProvider;

    private final ExpiringMap<Double> pressureCache = new ExpiringMap<>();
    private final ExpiringMap<Double> temperatureCache = new ExpiringMap<>();
    private final ExpiringMap<Integer> bearingCache = new ExpiringMap<>();

    private double currentTemp = 0;
    private String currentSagerCode = SagerWeatherCaster.UNDEF;

    public SagerCasterHandler(Thing thing, WindDirectionStateDescriptionProvider stateDescriptionProvider,
            SagerWeatherCaster sagerWeatherCaster) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.sagerWeatherCaster = sagerWeatherCaster;
    }

    @Override
    public void initialize() {
        int observationPeriod = ((BigDecimal) getConfig().get(CONFIG_PERIOD)).intValue();
        long period = TimeUnit.HOURS.toMillis(observationPeriod);
        pressureCache.setObservationPeriod(period);
        bearingCache.setObservationPeriod(period);
        temperatureCache.setObservationPeriod(period);

        String location = (String) getConfig().get(CONFIG_LOCATION);
        String latitude = location.split(",")[0];
        sagerWeatherCaster.setLatitude(Double.parseDouble(latitude));
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
        ThingUID thingUID = getThing().getUID();
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, GROUP_OUTPUT, CHANNEL_WINDFROM), options);
        stateDescriptionProvider.setStateOptions(new ChannelUID(thingUID, GROUP_OUTPUT, CHANNEL_WINDTO), options);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            postNewForecast();
        } else {
            String id = channelUID.getIdWithoutGroup();
            switch (id) {
                case CHANNEL_CLOUDINESS:
                    logger.debug("Cloud level changed, updating forecast");
                    if (command instanceof QuantityType) {
                        QuantityType<?> cloudiness = (QuantityType<?>) command;
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setCloudLevel(cloudiness.intValue());
                            postNewForecast();
                        });
                        break;
                    }
                case CHANNEL_IS_RAINING:
                    logger.debug("Rain status updated, updating forecast");
                    if (command instanceof OnOffType) {
                        OnOffType isRaining = (OnOffType) command;
                        scheduler.submit(() -> {
                            sagerWeatherCaster.setRaining(isRaining == OnOffType.ON);
                            postNewForecast();
                        });
                    } else {
                        logger.debug("Channel '{}' accepts Switch commands.", channelUID);
                    }
                    break;
                case CHANNEL_RAIN_QTTY:
                    logger.debug("Rain status updated, updating forecast");
                    if (command instanceof QuantityType) {
                        updateRain((QuantityType<?>) command);
                    } else if (command instanceof DecimalType) {
                        updateRain((DecimalType) command);
                    } else {
                        logger.debug("Channel '{}' accepts Number, Number:(Speed|Length) commands.", channelUID);
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
                        QuantityType<Pressure> pressQtty = ((QuantityType<Pressure>) command)
                                .toUnit(HECTO(SIUnits.PASCAL));
                        if (pressQtty != null) {
                            double newPressureValue = pressQtty.doubleValue();
                            pressureCache.put(newPressureValue);
                            pressureCache.getAgedValue().ifPresentOrElse(oldPressure -> scheduler.submit(() -> {
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
                        @SuppressWarnings("unchecked")
                        QuantityType<Temperature> tempQtty = ((QuantityType<Temperature>) command)
                                .toUnit(SIUnits.CELSIUS);
                        if (tempQtty != null) {
                            currentTemp = tempQtty.doubleValue();
                            temperatureCache.put(currentTemp);
                            temperatureCache.getAgedValue().ifPresent(oldTemperature -> {
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
                    if (command instanceof QuantityType) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Angle> angleQtty = (QuantityType<Angle>) command;
                        int newAngleValue = angleQtty.intValue();
                        bearingCache.put(newAngleValue);
                        bearingCache.getAgedValue().ifPresent(oldAngle -> {
                            scheduler.submit(() -> {
                                sagerWeatherCaster.setBearing(newAngleValue, oldAngle);
                                updateChannelString(GROUP_OUTPUT, CHANNEL_WINDEVOLUTION,
                                        String.valueOf(sagerWeatherCaster.getWindEvolution()));
                                postNewForecast();
                            });
                        });
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
        String newSagerCode = sagerWeatherCaster.getSagerCode();
        if (!newSagerCode.equals(currentSagerCode)) {
            logger.debug("Sager prediction changed to {}", newSagerCode);
            currentSagerCode = newSagerCode;
            updateChannelTimeStamp(GROUP_OUTPUT, CHANNEL_TIMESTAMP, ZonedDateTime.now());
            String forecast = sagerWeatherCaster.getForecast();
            // Sharpens forecast if current temp is below 2 degrees, likely to be flurries rather than shower
            forecast += SHOWERS.contains(forecast) ? (currentTemp > 2) ? "1" : "2" : "";

            updateChannelString(GROUP_OUTPUT, CHANNEL_FORECAST, forecast);
            updateChannelString(GROUP_OUTPUT, CHANNEL_WINDFROM, sagerWeatherCaster.getWindDirection());
            updateChannelString(GROUP_OUTPUT, CHANNEL_WINDTO, sagerWeatherCaster.getWindDirection2());
            updateChannelString(GROUP_OUTPUT, CHANNEL_VELOCITY, sagerWeatherCaster.getWindVelocity());
            updateChannelDecimal(GROUP_OUTPUT, CHANNEL_VELOCITY_BEAUFORT, sagerWeatherCaster.getPredictedBeaufort());
        }
    }

    private void updateChannelTimeStamp(String group, String channelId, ZonedDateTime zonedDateTime) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new DateTimeType(zonedDateTime));
        }
    }

    private void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }
    }

    private void updateChannelDecimal(String group, String channelId, int value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new DecimalType(value));
        }
    }
}
