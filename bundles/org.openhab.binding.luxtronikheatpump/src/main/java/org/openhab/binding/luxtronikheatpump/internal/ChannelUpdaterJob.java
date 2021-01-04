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
package org.openhab.binding.luxtronikheatpump.internal;

import java.time.Instant;
import java.time.ZoneId;

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
    private final Logger logger = LoggerFactory.getLogger(ChannelUpdaterJob.class);

    public ChannelUpdaterJob(Thing thing) {
        this.thing = thing;
        this.config = thing.getConfiguration().as(LuxtronikHeatpumpConfiguration.class);
    }

    public Thing getThing() {
        return thing;
    }

    @Override
    public void run() {
        // connect to heatpump and check if values can be fetched
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            connector.read();

            // read all available values
            Integer[] heatpumpValues = connector.getValues();

            // read all parameters
            Integer[] heatpumpParams = connector.getParams();
            Integer[] heatpumpVisibilities = connector.getVisibilities();

            for (HeatpumpChannel channel : HeatpumpChannel.values()) {

                Integer channelId = channel.getChannelId();

                if (channelId == null) {
                    continue; // no channel id to read defined (for channels handeled separatly)
                }

                if (channel.isVisible(heatpumpVisibilities).equals(Boolean.FALSE) && config.hideChannels) {
                    logger.debug("Channel {} is not availabe. Skipped updating it", channel.getCommand());
                    continue; // channel not available for heat pump
                }

                Integer rawValue = null;

                if (channel.isWritable().equals(Boolean.TRUE)) {
                    rawValue = heatpumpParams[channelId];
                } else {
                    if (heatpumpValues.length < channelId) {
                        continue; // channel not available
                    }
                    rawValue = heatpumpValues[channelId];
                }

                State value = convertValueToState(rawValue, channel.getItemClass(), channel.getUnit());

                if (value != null) {
                    handleEventType(value, channel);
                }
            }

            updateProperties(heatpumpValues);

        } catch (Exception e) {
            logger.warn("Could not connect to heatpump (uuid={}, ip={}, port={}): {}", thing.getUID(), config.ipAddress,
                    config.port, e.getStackTrace());
        }
    }

    private @Nullable State convertValueToState(Integer rawValue, Class<? extends Item> itemClass,
            @Nullable Unit<?> unit) {

        if (itemClass == SwitchItem.class) {
            return (rawValue == 0) ? OnOffType.OFF : OnOffType.ON;
        }

        if (itemClass == DateTimeItem.class) {
            try {
                if (rawValue > 0) {
                    var instant = Instant.ofEpochSecond((long) rawValue);
                    return new DateTimeType(instant.atZone(ZoneId.of("UTC")));
                }
            } catch (Exception e) {
                logger.info("Error parsing timestamp: {}", rawValue);
            }
        }

        if (itemClass == NumberItem.class) {
            if (unit == SIUnits.CELSIUS) {
                return new QuantityType<>((double) rawValue / 10, SIUnits.CELSIUS);
            } else if (unit == Units.KELVIN) {
                return new QuantityType<>((double) rawValue / 10, Units.KELVIN);
            } else if (unit == Units.KILOWATT_HOUR) {
                return new QuantityType<>((double) rawValue / 10, Units.KILOWATT_HOUR);
            } else if (unit == Units.PERCENT) {
                return new QuantityType<>((double) rawValue / 10, Units.PERCENT);
            } else if (unit == Units.HOUR) {
                return new QuantityType<>((double) rawValue / 10, Units.HOUR);
            } else if (unit == Units.HERTZ) {
                return new QuantityType<>((double) rawValue, Units.HERTZ);
            } else if (unit == Units.SECOND) {
                return new QuantityType<>((double) rawValue, Units.SECOND);
            } else if (unit == Units.LITRE_PER_MINUTE) {
                return new QuantityType<>((double) rawValue * 60, Units.LITRE_PER_MINUTE);
            } else if (unit == Units.BAR) {
                return new QuantityType<>((double) rawValue / 100, Units.BAR);
            } else if (unit == Units.VOLT) {
                return new QuantityType<>((double) rawValue / 100, Units.VOLT);
            } else {
                return new DecimalType(rawValue);
            }
        }

        return null;
    }

    private String getSoftwareVersion(Integer[] heatpumpValues) {
        String softwareVersion = "";

        for (int i = 81; i <= 90; i++) {
            if (heatpumpValues[i] > 0) {
                softwareVersion += Character.toString(heatpumpValues[i]);
            }
        }

        return softwareVersion;
    }

    private String transformIpAddress(int ip) {
        return String.format("%d.%d.%d.%d", (ip >> 24) & 0xFF, (ip >> 16) & 0xFF, (ip >> 8) & 0xFF, ip & 0xFF);
    }

    private void handleEventType(org.openhab.core.types.State state, HeatpumpChannel heatpumpCommandType) {
        LuxtronikHeatpumpHandler handler = LuxtronikHeatpumpHandlerFactory.getHandler(thing.getUID().toString());
        if (handler == null) {
            logger.warn("Trying to update a channel for a thing without a handler");
            return;
        }
        handler.updateState(heatpumpCommandType.getCommand(), state);
    }

    private void updateProperties(Integer[] heatpumpValues) {
        String heatpumpType = HeatpumpType.fromCode(heatpumpValues[78]).getName();

        setProperty("Heatpump type", heatpumpType);

        // Not sure when Typ 2 should be used
        // String heatpumpType2 = HeatpumpType.fromCode(heatpumpValues[230]).getName();
        // setProperty("Type 2", heatpumpType2);

        setProperty("Software version", getSoftwareVersion(heatpumpValues));
        setProperty("IP address", transformIpAddress(heatpumpValues[91]));
        setProperty("Subnet mask", transformIpAddress(heatpumpValues[92]));
        setProperty("Broadcast address", transformIpAddress(heatpumpValues[93]));
        setProperty("Gateway", transformIpAddress(heatpumpValues[94]));
    }

    private void setProperty(String name, String value) {
        LuxtronikHeatpumpHandler handler = LuxtronikHeatpumpHandlerFactory.getHandler(thing.getUID().toString());
        if (handler == null) {
            logger.warn("Trying to update a channel for a thing without a handler");
            return;
        }
        handler.updateProperty(name, value);
    }
}
