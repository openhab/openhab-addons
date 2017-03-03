/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom;

import java.io.InvalidClassException;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;

/**
 * Represents all valid value selectors which could be processed by RFXCOM
 * devices.
 *
 * @author Pauli Anttila - Initial contribution
 */
public enum RFXComValueSelector {

    RAW_MESSAGE(RFXComBindingConstants.CHANNEL_RAW_MESSAGE, StringItem.class),
    RAW_PAYLOAD(RFXComBindingConstants.CHANNEL_RAW_PAYLOAD, StringItem.class),
    SHUTTER(RFXComBindingConstants.CHANNEL_SHUTTER, RollershutterItem.class),
    COMMAND(RFXComBindingConstants.CHANNEL_COMMAND, SwitchItem.class),
    MOOD(RFXComBindingConstants.CHANNEL_MOOD, NumberItem.class),
    SIGNAL_LEVEL(RFXComBindingConstants.CHANNEL_SIGNAL_LEVEL, NumberItem.class),
    DIMMING_LEVEL(RFXComBindingConstants.CHANNEL_DIMMING_LEVEL, DimmerItem.class),
    TEMPERATURE(RFXComBindingConstants.CHANNEL_TEMPERATURE, NumberItem.class),
    HUMIDITY(RFXComBindingConstants.CHANNEL_HUMIDITY, NumberItem.class),
    HUMIDITY_STATUS(RFXComBindingConstants.CHANNEL_HUMIDITY_STATUS, StringItem.class),
    BATTERY_LEVEL(RFXComBindingConstants.CHANNEL_BATTERY_LEVEL, NumberItem.class),
    PRESSURE(RFXComBindingConstants.CHANNEL_PRESSURE, NumberItem.class),
    FORECAST(RFXComBindingConstants.CHANNEL_FORECAST, NumberItem.class),
    RAIN_RATE(RFXComBindingConstants.CHANNEL_RAIN_RATE, NumberItem.class),
    RAIN_TOTAL(RFXComBindingConstants.CHANNEL_RAIN_TOTAL, NumberItem.class),
    WIND_DIRECTION(RFXComBindingConstants.CHANNEL_WIND_DIRECTION, NumberItem.class),
    WIND_SPEED(RFXComBindingConstants.CHANNEL_WIND_SPEED, NumberItem.class),
    GUST(RFXComBindingConstants.CHANNEL_GUST, NumberItem.class),
    CHILL_FACTOR(RFXComBindingConstants.CHANNEL_CHILL_FACTOR, NumberItem.class),
    INSTANT_POWER(RFXComBindingConstants.CHANNEL_INSTANT_POWER, NumberItem.class),
    TOTAL_USAGE(RFXComBindingConstants.CHANNEL_TOTAL_USAGE, NumberItem.class),
    INSTANT_AMPS(RFXComBindingConstants.CHANNEL_INSTANT_AMPS, NumberItem.class),
    TOTAL_AMP_HOUR(RFXComBindingConstants.CHANNEL_TOTAL_AMP_HOUR, NumberItem.class),
    CHANNEL1_AMPS(RFXComBindingConstants.CHANNEL_CHANNEL1_AMPS, NumberItem.class),
    CHANNEL2_AMPS(RFXComBindingConstants.CHANNEL_CHANNEL2_AMPS, NumberItem.class),
    CHANNEL3_AMPS(RFXComBindingConstants.CHANNEL_CHANNEL3_AMPS, NumberItem.class),
    STATUS(RFXComBindingConstants.CHANNEL_STATUS, StringItem.class),
    MOTION(RFXComBindingConstants.CHANNEL_MOTION, SwitchItem.class),
    CONTACT(RFXComBindingConstants.CHANNEL_CONTACT, ContactItem.class),
    VOLTAGE(RFXComBindingConstants.CHANNEL_VOLTAGE, NumberItem.class),
    SET_POINT(RFXComBindingConstants.CHANNEL_SET_POINT, NumberItem.class),
    DATE_TIME(RFXComBindingConstants.CHANNEL_DATE_TIME, DateTimeItem.class),
    LOW_BATTERY(RFXComBindingConstants.CHANNEL_LOW_BATTERY, SwitchItem.class);

    private final String text;
    private Class<? extends Item> itemClass;

    private RFXComValueSelector(final String text, Class<? extends Item> itemClass) {
        this.text = text;
        this.itemClass = itemClass;
    }

    @Override
    public String toString() {
        return text;
    }

    public Class<? extends Item> getItemClass() {
        return itemClass;
    }

    /**
     * Procedure to validate selector string.
     *
     * @param valueSelector
     *            selector string e.g. Command, Temperature
     * @return true if item is valid.
     * @throws IllegalArgumentException
     *             Not valid value selector.
     * @throws InvalidClassException
     *             Not valid class for value selector.
     */
    public static boolean validateBinding(String valueSelector, Class<? extends Item> itemClass)
            throws IllegalArgumentException, InvalidClassException {

        for (RFXComValueSelector c : RFXComValueSelector.values()) {
            if (c.text.equals(valueSelector)) {

                if (c.getItemClass().equals(itemClass)) {
                    return true;
                } else {
                    throw new InvalidClassException("Not valid class for value selector");
                }
            }
        }

        throw new IllegalArgumentException("Not valid value selector");

    }

    /**
     * Procedure to convert selector string to value selector class.
     *
     * @param valueSelectorText
     *            selector string e.g. RawData, Command, Temperature
     * @return corresponding selector value.
     * @throws InvalidClassException
     *             Not valid class for value selector.
     */
    public static RFXComValueSelector getValueSelector(String valueSelectorText) throws IllegalArgumentException {

        for (RFXComValueSelector c : RFXComValueSelector.values()) {
            if (c.text.equals(valueSelectorText)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Not valid value selector");
    }
}
