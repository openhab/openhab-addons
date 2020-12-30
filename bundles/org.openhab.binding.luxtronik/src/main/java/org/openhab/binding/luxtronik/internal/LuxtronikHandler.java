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
package org.openhab.binding.luxtronik.internal;

import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.HOUR_PARAMS;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.INTEGER_VALUES;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.KILOWATT_HOUR_VALUES;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.ONOFF_VALUES;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.SECOND_VALUES;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.STRING_PARAMS;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.STRING_VALUES;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.TEMPERATURE_PARAMS;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.TEMPERATURE_VALUES;
import static org.openhab.binding.luxtronik.internal.LuxtronikBindingConstants.TIMESTAMP_VALUES;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ValueRange;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Heatpump binding connects to a Luxtronik/Novelan (Siemens) Heatpump with the
 * {@link HeatpumpConnector} and read the internal state array every minute.
 * With the state array each binding will be updated.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 * @author John Cocula - made port configurable
 * @author Hilbrand Bouwkamp - Migrated to openHAB 3
 * @author Christoph Scholz - Finished migration to openHAB 3
 */
@NonNullByDefault
public class LuxtronikHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LuxtronikHandler.class);

    private @NonNullByDefault({}) LuxtronikConfiguration config;
    private @Nullable ScheduledFuture<?> refreshTask;

    public LuxtronikHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Refresh is done in scheduled refresh.
            return;
        }
        String channelId = channelUID.getId();
        if (TEMPERATURE_PARAMS.containsKey(channelId)) {
            if (command instanceof QuantityType<?> || command instanceof DecimalType) {
                QuantityType<?> temperatureInDegreesCelcius = (command instanceof QuantityType<?>)
                        ? ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS)
                        : new QuantityType<>(((DecimalType) command), SIUnits.CELSIUS);
                if (temperatureInDegreesCelcius != null) {
                    int value = (int) (temperatureInDegreesCelcius.floatValue() * 10.);
                    Integer i = TEMPERATURE_PARAMS.get(channelId);
                    if (i != null) {
                        sendParamToHeatpump(i, value);
                    }
                }
            } else {
                logger.warn("Heatpump temperature item {} must be from type {} or {}.", channelUID,
                        QuantityType.class.getSimpleName(), DecimalType.class.getSimpleName());
            }
        } else if (HOUR_PARAMS.containsKey(channelId)) {
            if (command instanceof DecimalType) {
                float hours = ((DecimalType) command).floatValue();
                int value = (int) (hours * 10.);
                Integer i = HOUR_PARAMS.get(channelId);
                if (i != null) {
                    sendParamToHeatpump(i, value);
                }
            } else {
                logger.warn("Heatpump cooling start/stop after hours item {} must be from type: {}.", channelUID,
                        DecimalType.class.getSimpleName());
            }
        } else if (STRING_PARAMS.containsKey(channelId)) {
            if (command instanceof StringType) {
                try {
                    final int value = Integer.parseInt(((StringType) command).toString());
                    if (ValueRange.of(0, 4).isValidValue(value)) {
                        Integer i = STRING_PARAMS.get(channelId);
                        if (i != null) {
                            sendParamToHeatpump(i, value);
                        }
                    } else {
                        logger.warn("Heatpump operation mode for channel {} with value {} is unknown.", channelUID,
                                value);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Heatpump operation mode for channel {} with value {} is unknown.", channelUID,
                            command);
                }
            } else {
                logger.warn("Heatpump operation mode item {} must be from type:{}.", channelUID,
                        StringType.class.getSimpleName());
            }
        }
    }

    /**
     * Set a parameter on the heatpump.
     *
     * @param param
     * @param value
     */
    private void sendParamToHeatpump(int param, int value) {
        synchronized (this) {
            try (HeatpumpConnector connection = new HeatpumpConnector(config.host, config.port,
                    config.connectionTimeout)) {
                connection.setParam(param, value);
            } catch (UnknownHostException e) {
                logger.warn("The given address '{}' of the heatpump is unknown", getAddress());
            } catch (IOException e) {
                logger.warn("Couldn't establish network connection [address '{}']", getAddress());
            }
        }
    }

    @Override
    public void initialize() {
        LuxtronikConfiguration config = getConfigAs(LuxtronikConfiguration.class);
        this.config = config;

        updateStatus(ThingStatus.UNKNOWN);
        refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.pollingInterval, TimeUnit.SECONDS);
    }

    private String getAddress() {
        return config.host + ":" + config.port;
    }

    private ZonedDateTime getZdtFromTimestamp(long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault());
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> refreshTask = this.refreshTask;

        if (refreshTask != null) {
            refreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    public void refresh() {
        try {
            final int[] heatpumpValues;
            final int[] heatpumpParams;

            synchronized (this) {
                try (HeatpumpConnector connection = new HeatpumpConnector(config.host, config.port,
                        config.connectionTimeout)) {
                    heatpumpValues = connection.getValues();
                    heatpumpParams = connection.getParams();
                } catch (UnknownHostException e) {
                    if (isInitialized()) {
                        logger.debug("The given address '{}' of the heatpump is unknown", getAddress());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                    return;
                } catch (IOException e) {
                    if (isInitialized()) {
                        logger.debug("Couldn't establish network connection [address '{}']", getAddress());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                    return;
                }
            }

            // workaround for thermal energies
            // the thermal energies can be unreasonably high in some cases, probably due to a sign bug in the firmware
            // trying to correct this issue here
            if (heatpumpValues[151] >= 214748364) {
                heatpumpValues[151] -= 214748364;
            }
            if (heatpumpValues[152] >= 214748364) {
                heatpumpValues[152] -= 214748364;
            }
            if (heatpumpValues[153] >= 214748364) {
                heatpumpValues[153] -= 214748364;
            }
            if (heatpumpValues[154] >= 214748364) {
                heatpumpValues[154] -= 214748364;
            }

            for (Entry<String, Integer> pair : INTEGER_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(), new DecimalType((double) heatpumpValues[pair.getValue()]));
                }
            }

            for (Entry<String, Integer> pair : KILOWATT_HOUR_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(),
                            new QuantityType<>((double) heatpumpValues[pair.getValue()] / 10, Units.KILOWATT_HOUR));
                }
            }

            for (Entry<String, Integer> pair : ONOFF_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(), OnOffType.from(heatpumpValues[pair.getValue()] != 0));
                }
            }

            for (Entry<String, Integer> pair : SECOND_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(), new QuantityType<>(heatpumpValues[pair.getValue()], Units.SECOND));
                }
            }

            for (Entry<String, Integer> pair : STRING_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(), new StringType(String.valueOf(heatpumpValues[pair.getValue()])));
                }
            }

            for (Entry<String, Integer> pair : TEMPERATURE_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(),
                            new QuantityType<>((double) heatpumpValues[pair.getValue()] / 10, SIUnits.CELSIUS));
                }
            }

            for (Entry<String, Integer> pair : TIMESTAMP_VALUES.entrySet()) {
                if (pair.getValue() < heatpumpValues.length) {
                    updateState(pair.getKey(), new DateTimeType(getZdtFromTimestamp(heatpumpValues[pair.getValue()])));
                }
            }

            for (Entry<String, Integer> pair : TEMPERATURE_PARAMS.entrySet()) {
                if (pair.getValue() < heatpumpParams.length) {
                    updateState(pair.getKey(),
                            new QuantityType<>((double) heatpumpParams[pair.getValue()] / 10, SIUnits.CELSIUS));
                }
            }

            for (Entry<String, Integer> pair : HOUR_PARAMS.entrySet()) {
                if (pair.getValue() < heatpumpParams.length) {
                    updateState(pair.getKey(),
                            new QuantityType<>((double) heatpumpParams[pair.getValue()] / 10, Units.HOUR));
                }
            }

            for (Entry<String, Integer> pair : STRING_PARAMS.entrySet()) {
                if (pair.getValue() < heatpumpParams.length) {
                    updateState(pair.getKey(), new StringType(String.valueOf(heatpumpParams[pair.getValue()])));
                }
            }

            if (isInitialized()) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (RuntimeException e) {
            logger.debug("Unexpected error for thing {} ", getThing().getUID(), e);
            if (isInitialized()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            }
        }
    }
}
