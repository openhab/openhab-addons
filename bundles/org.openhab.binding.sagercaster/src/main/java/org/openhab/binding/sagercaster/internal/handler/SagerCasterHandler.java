/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
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
    private final Logger logger = LoggerFactory.getLogger(SagerCasterHandler.class);
    private final static SagerWeatherCaster sagerWeatherCaster = new SagerWeatherCaster();
    private final WindDirectionStateDescriptionProvider stateDescriptionProvider;

    private class ExpiringMap<T> {
        private SortedMap<Long, T> values = new TreeMap<Long, T>();
        private @Nullable T agedValue;
        private long eldestAge = 0;

        public void setObservationPeriod(long eldestAge) {
            if (eldestAge > this.eldestAge) {
            }
            this.eldestAge = eldestAge;
        }

        public void put(T newValue) {
            long now = System.currentTimeMillis();
            values.put(now, newValue);
            Optional<Long> eldestKey = values.keySet().stream().filter(key -> key < now - eldestAge).findFirst();
            if (eldestKey.isPresent()) {
                agedValue = values.get(eldestKey.get());
                values.entrySet().removeIf(map -> map.getKey() <= eldestKey.get());
            }
        }

        public @Nullable T getAgedValue() {
            return agedValue;
        }

        public @Nullable Long getOldestTimeStamp() {
            return values.firstKey();
        }
    }

    private final ExpiringMap<QuantityType<Pressure>> pressureCache = new ExpiringMap<>();
    private final ExpiringMap<QuantityType<Angle>> bearingCache = new ExpiringMap<>();

    public SagerCasterHandler(Thing thing, WindDirectionStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        String location = (String) getConfig().get(CONFIG_LOCATION);
        int observationPeriod = ((BigDecimal) getConfig().get(CONFIG_PERIOD)).intValue();
        String latitude = location.split(",")[0];
        sagerWeatherCaster.setLatitude(Double.parseDouble(latitude));
        pressureCache.setObservationPeriod(TimeUnit.SECONDS.toMillis(observationPeriod));
        bearingCache.setObservationPeriod(TimeUnit.SECONDS.toMillis(observationPeriod));
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
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_WINDFROM), options);
        stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_WINDTO), options);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            postNewForecast();
        } else {
            String id = channelUID.getId();
            switch (id) {
                case CHANNEL_CLOUDINESS:
                    logger.debug("Octa cloud level changed, updating forecast");
                    if (command instanceof QuantityType) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Dimensionless> cloudiness = (QuantityType<Dimensionless>) command;
                        new Thread(() -> {
                            sagerWeatherCaster.setCloudLevel(cloudiness.intValue());
                            postNewForecast();
                        }).start();
                        break;
                    }
                case CHANNEL_IS_RAINING:
                    logger.debug("Rain status updated, updating forecast");
                    OnOffType isRaining = (OnOffType) command;
                    new Thread(() -> {
                        sagerWeatherCaster.setRaining(isRaining == OnOffType.ON);
                        postNewForecast();
                    }).start();
                    break;
                case CHANNEL_WIND_SPEED:
                    logger.debug("Updated wind speed, updating forecast");
                    DecimalType newValue = (DecimalType) command;
                    new Thread(() -> {
                        sagerWeatherCaster.setBeaufort(newValue.intValue());
                        postNewForecast();
                    }).start();
                    break;
                case CHANNEL_PRESSURE:
                    logger.debug("Sea-level pressure updated, updating forecast");
                    if (command instanceof QuantityType) {
                        @SuppressWarnings("unchecked")
                        QuantityType<Pressure> newPressure = ((QuantityType<Pressure>) command)
                                .toUnit(HECTO(SIUnits.PASCAL));
                        if (newPressure != null) {
                            pressureCache.put(newPressure);
                            QuantityType<Pressure> agedPressure = pressureCache.getAgedValue();
                            if (agedPressure != null) {
                                new Thread(() -> {
                                    sagerWeatherCaster.setPressure(newPressure.doubleValue(),
                                            agedPressure.doubleValue());
                                    updateState(CHANNEL_PRESSURETREND,
                                            new StringType(String.valueOf(sagerWeatherCaster.getPressureEvolution())));
                                    postNewForecast();
                                }).start();
                            } else {
                                updateState(CHANNEL_FORECAST, new StringType(
                                        "Not enough historic data to study pressure evolution, wait a bit ..."));
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
                        QuantityType<Angle> agedAngle = bearingCache.getAgedValue();
                        if (agedAngle != null) {
                            new Thread(() -> {
                                sagerWeatherCaster.setBearing(newAngle.intValue(), agedAngle.intValue());
                                updateState(CHANNEL_WINDEVOLUTION,
                                        new StringType(String.valueOf(sagerWeatherCaster.getWindEvolution())));
                                postNewForecast();
                            }).start();
                        } else {
                            updateState(CHANNEL_WINDFROM, new StringType(
                                    "Not enough historic data to study pressure evolution, wait a bit ..."));
                        }
                    }
                    break;
                default:
                    logger.debug("The binding can not handle command: {} on channel: {}", command, channelUID);
            }
        }
    }

    private void postNewForecast() {
        updateState(CHANNEL_FORECAST, new StringType(sagerWeatherCaster.getForecast()));
        updateState(CHANNEL_VELOCITY, new StringType(sagerWeatherCaster.getWindVelocity()));
        updateState(CHANNEL_WINDFROM, new StringType(sagerWeatherCaster.getWindDirection()));
        updateState(CHANNEL_WINDTO, new StringType(sagerWeatherCaster.getWindDirection2()));
        updateState(CHANNEL_TIMESTAMP, new DateTimeType());
    }

}
