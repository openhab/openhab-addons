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
package org.openhab.binding.novelanheatpump.internal;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.novelanheatpump.internal.enums.HeatpumpChannel;
import org.openhab.binding.novelanheatpump.internal.enums.HeatpumpState;
import org.openhab.binding.novelanheatpump.internal.enums.HeatpumpType;
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

@NonNullByDefault
public class ChannelUpdaterJob implements SchedulerRunnable, Runnable {

    private final Thing thing;
    private final NovelanHeatpumpConfiguration config;
    private final Logger logger = LoggerFactory.getLogger(ChannelUpdaterJob.class);
    private final SimpleDateFormat sdateformat = new SimpleDateFormat("dd.MM.yy HH:mm"); //$NON-NLS-1$

    public ChannelUpdaterJob(Thing thing) {
        this.thing = thing;
        this.config = thing.getConfiguration().as(NovelanHeatpumpConfiguration.class);
    }

    public Thing getThing() {
        return thing;
    }

    @Override
    public void run() {
        // connect to heatpump and check if values can be fetched
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            connector.connect();

            // read all available values
            int[] heatpumpValues = connector.getValues();

            // all temperatures are 0.2 degree Celsius exact
            // but use int to save values
            // example 124 is 12.4 degree Celsius

            // read all parameters
            int[] heatpumpParams = connector.getParams();

            for (HeatpumpChannel channel : HeatpumpChannel.values()) {

                if (channel.getChannelId() == null) {
                    continue; // no channel id to read defined (for channels based on others)
                }

                Integer rawValue = null;

                if (channel.isWritable().equals(Boolean.TRUE)) {
                    rawValue = heatpumpParams[channel.getChannelId()];
                } else {
                    if (heatpumpValues.length < channel.getChannelId()) {
                        continue; // channel not available
                    }
                    rawValue = heatpumpValues[channel.getChannelId()];
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
        } finally {
            connector.disconnect();
        }
    }

    private @Nullable State convertValueToState(int rawValue, Class<? extends Item> itemClass, Unit unit) {

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

    private String getSoftwareVersion(int[] heatpumpValues) {
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
        NovelanHeatpumpHandler handler = NovelanHeatpumpHandlerFactory.getHandler(thing.getUID().toString());
        if (handler == null) {
            logger.warn("Trying to update a channel for a thing without a handler");
            return;
        }
        handler.updateState(heatpumpCommandType.getCommand(), state);
    }

    private void updateProperties(int[] heatpumpValues) {
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
        NovelanHeatpumpHandler handler = NovelanHeatpumpHandlerFactory.getHandler(thing.getUID().toString());
        if (handler == null) {
            logger.warn("Trying to update a channel for a thing without a handler");
            return;
        }
        handler.updateProperty(name, value);
    }

    /**
     * generate a readable string containing the time since the heatpump is in
     * the state.
     *
     * @param heatpumpValues
     *            the internal state array of the heatpump
     * @return a human readable time string
     */
    private String getStateTime(int[] heatpumpValues) {
        String returnValue = ""; //$NON-NLS-1$
        // for a long time create a date
        if (heatpumpValues[118] == 2) {
            long value = heatpumpValues[95];
            if (value < 0) {
                value = 0;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(value * 1000L);
            returnValue += sdateformat.format(cal.getTime());
        } else {
            // for a shorter time use the counted time (HH:mm:ss)
            int value = heatpumpValues[120];
            returnValue = formatHours(value);
        }
        return returnValue;
    }

    private String formatHours(int value) {
        String returnValue = "";
        returnValue += String.format("%02d:", new Object[] { Integer.valueOf(value / 3600) }); //$NON-NLS-1$
        value %= 3600;
        returnValue += String.format("%02d:", new Object[] { Integer.valueOf(value / 60) }); //$NON-NLS-1$
        value %= 60;
        returnValue += String.format("%02d", new Object[] { Integer.valueOf(value) }); //$NON-NLS-1$
        return returnValue;
    }
}
