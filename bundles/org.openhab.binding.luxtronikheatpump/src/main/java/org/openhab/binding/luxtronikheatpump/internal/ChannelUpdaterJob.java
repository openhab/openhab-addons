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
package org.openhab.binding.luxtronikheatpump.internal;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpChannel;
import org.openhab.binding.luxtronikheatpump.internal.enums.HeatpumpType;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.scheduler.SchedulerRunnable;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelUpdaterJob} updates all channel values
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public class ChannelUpdaterJob implements SchedulerRunnable, Runnable {

    private final Thing thing;
    private final LuxtronikHeatpumpConfiguration config;
    private final LuxtronikTranslationProvider translationProvider;
    private final Logger logger = LoggerFactory.getLogger(ChannelUpdaterJob.class);
    private final LuxtronikHeatpumpHandler handler;

    public ChannelUpdaterJob(LuxtronikHeatpumpHandler handler, LuxtronikTranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
        this.handler = handler;
        this.thing = handler.getThing();
        this.config = this.thing.getConfiguration().as(LuxtronikHeatpumpConfiguration.class);
    }

    public Thing getThing() {
        return thing;
    }

    @Override
    public void run() {
        // connect to heatpump and check if values can be fetched
        final HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            connector.read();
        } catch (IOException e) {
            logger.warn("Could not connect to heatpump (uuid={}, ip={}, port={}): {}", thing.getUID(), config.ipAddress,
                    config.port, e.getMessage());

            handler.setStatusConnectionError();
            return;
        }

        handler.setStatusOnline();

        // read all available values
        Integer[] heatpumpValues = connector.getValues();

        // read all parameters
        Integer[] heatpumpParams = connector.getParams();
        Integer[] heatpumpVisibilities = connector.getVisibilities();

        for (HeatpumpChannel channel : HeatpumpChannel.values()) {
            try {
                Integer rawValue = getChannelValue(channel, heatpumpValues, heatpumpParams, heatpumpVisibilities);

                if (rawValue == null) {
                    continue;
                }

                State value = convertValueToState(rawValue, channel.getItemClass(), channel.getUnit());

                if (value != null) {
                    handleEventType(value, channel);
                }
            } catch (Exception e) {
                // an exception should actually not occur, but is catched nevertheless in case it does
                // this ensures the remaining channels are updated even if one fails for some reason
                logger.warn("An error occurred while updating the channel {}: {}", channel.getCommand(),
                        e.getMessage());
            }
        }

        setExtendedState(heatpumpValues, heatpumpParams, heatpumpVisibilities);

        updateProperties(heatpumpValues);
    }

    private @Nullable State convertValueToState(Integer rawValue, Class<? extends Item> itemClass,
            @Nullable Unit<?> unit) {
        if (itemClass == SwitchItem.class) {
            return (rawValue == 0) ? OnOffType.OFF : OnOffType.ON;
        }

        if (itemClass == DateTimeItem.class && rawValue > 0) {
            try {
                Instant instant = Instant.ofEpochSecond(rawValue.longValue());
                return new DateTimeType(instant.atZone(ZoneId.of("UTC")));
            } catch (DateTimeException e) {
                logger.warn("Invalid timestamp '{}' received from heatpump: {}", rawValue, e.getMessage());
            }
        }

        if (itemClass == NumberItem.class) {
            if (unit == null) {
                return new DecimalType(rawValue);
            }
            if (SIUnits.CELSIUS.equals(unit) || Units.KELVIN.equals(unit) || Units.KILOWATT_HOUR.equals(unit)
                    || Units.PERCENT.equals(unit) || Units.HOUR.equals(unit)) {
                return new QuantityType<>((double) rawValue / 10, unit);
            } else if (Units.HERTZ.equals(unit) || Units.SECOND.equals(unit) || Units.WATT.equals(unit)) {
                return new QuantityType<>((double) rawValue, unit);
            } else if (Units.LITRE_PER_MINUTE.equals(unit)) {
                return new QuantityType<>((double) rawValue / 60, unit);
            } else if (Units.BAR.equals(unit) || Units.VOLT.equals(unit)) {
                return new QuantityType<>((double) rawValue / 100, unit);
            }

            logger.debug("Unhandled unit {} configured for a channel.", unit);
            return new DecimalType(rawValue);
        }

        return null;
    }

    private @Nullable Integer getChannelValue(HeatpumpChannel channel, Integer[] heatpumpValues,
            Integer[] heatpumpParams, Integer[] heatpumpVisibilities) {
        Integer channelId = channel.getChannelId();

        if (channelId == null) {
            return null; // no channel id to read defined (for channels handeled separatly)
        }

        if (!channel.isVisible(heatpumpVisibilities) && config.showAllChannels) {
            logger.debug("Channel {} is not available. Skipped updating it", channel.getCommand());
            return null;
        }

        Integer rawValue = null;

        if (channel.isWritable()) {
            rawValue = heatpumpParams[channelId];
        } else {
            if (heatpumpValues.length <= channelId) {
                return null; // channel not available
            }
            rawValue = heatpumpValues[channelId];
        }

        return rawValue;
    }

    private static String getSoftwareVersion(Integer[] heatpumpValues) {
        StringBuffer softwareVersion = new StringBuffer("");

        for (int i = 81; i <= 90; i++) {
            if (heatpumpValues[i] > 0) {
                softwareVersion.append(Character.toString(heatpumpValues[i]));
            }
        }

        return softwareVersion.toString();
    }

    private static String transformIpAddress(int ip) {
        return String.format("%d.%d.%d.%d", (ip >> 24) & 0xFF, (ip >> 16) & 0xFF, (ip >> 8) & 0xFF, ip & 0xFF);
    }

    private void handleEventType(State state, HeatpumpChannel heatpumpCommandType) {
        handler.updateState(heatpumpCommandType.getCommand(), state);
    }

    private void setExtendedState(Integer[] heatpumpValues, Integer[] heatpumpParams, Integer[] heatpumpVisibilities) {
        Integer row1 = getChannelValue(HeatpumpChannel.CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEILE1, heatpumpValues,
                heatpumpParams, heatpumpVisibilities);
        Integer error = getChannelValue(HeatpumpChannel.CHANNEL_HEATPUMP_ERROR_NR0, heatpumpValues, heatpumpParams,
                heatpumpVisibilities);
        Integer row2 = getChannelValue(HeatpumpChannel.CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEILE2, heatpumpValues,
                heatpumpParams, heatpumpVisibilities);
        Integer row3 = getChannelValue(HeatpumpChannel.CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEILE3, heatpumpValues,
                heatpumpParams, heatpumpVisibilities);
        Integer time = getChannelValue(HeatpumpChannel.CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEIT, heatpumpValues,
                heatpumpParams, heatpumpVisibilities);
        String state = "";

        if (row1 != null && row1 == 4) {
            // 4 means error state
            state = getStateTranslation("errorCodeX", error);
        } else {
            state = getStateTranslation("menuStateLine1", row1);
        }

        var longState = String.format("%s - %s %s - %s", state, getStateTranslation("menuStateLine2", row2),
                formatHours(time), getStateTranslation("menuStateLine3", row3));

        handleEventType(new StringType(longState), HeatpumpChannel.CHANNEL_HEATPUMP_STATUS);
    }

    public static Map<String, Object> getProperties(Integer[] heatpumpValues) {
        Map<String, Object> properties = new HashMap<String, Object>();

        String heatpumpType = HeatpumpType.fromCode(heatpumpValues[78]).getName();

        properties.put("heatpumpType", heatpumpType);

        // Not sure when Typ 2 should be used
        // String heatpumpType2 = HeatpumpType.fromCode(heatpumpValues[230]).getName();
        // properties.put("heatpumpType2", heatpumpType2);

        properties.put("softwareVersion", getSoftwareVersion(heatpumpValues));
        properties.put("ipAddress", transformIpAddress(heatpumpValues[91]));
        properties.put("subnetMask", transformIpAddress(heatpumpValues[92]));
        properties.put("broadcastAddress", transformIpAddress(heatpumpValues[93]));
        properties.put("gateway", transformIpAddress(heatpumpValues[94]));

        return properties;
    }

    private void updateProperties(Integer[] heatpumpValues) {
        Map<String, Object> properties = getProperties(heatpumpValues);

        for (Map.Entry<String, Object> property : properties.entrySet()) {
            handler.updateProperty(property.getKey(), property.getValue().toString());
        }
    }

    private String getStateTranslation(String name, @Nullable Integer option) {
        if (option == null) {
            return "";
        }

        String translation = translationProvider
                .getText("channel-type.luxtronikheatpump." + name + ".state.option." + option);
        return translation == null ? "" : translation;
    }

    private String formatHours(@Nullable Integer value) {
        String returnValue = "";

        if (value == null) {
            return returnValue;
        }

        int intVal = value;

        returnValue += String.format("%02d:", intVal / 3600);
        intVal %= 3600;
        returnValue += String.format("%02d:", intVal / 60);
        intVal %= 60;
        returnValue += String.format("%02d", intVal);
        return returnValue;
    }
}
