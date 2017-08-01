/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom;

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
    VENETIAN_BLIND(RFXComBindingConstants.CHANNEL_VENETIAN_BLIND, DimmerItem.class),
    SUN_WIND_DETECTOR(RFXComBindingConstants.CHANNEL_SUN_WIND_DETECTOR, SwitchItem.class),
    PROGRAM(RFXComBindingConstants.CHANNEL_PROGRAM, SwitchItem.class),
    COMMAND(RFXComBindingConstants.CHANNEL_COMMAND, SwitchItem.class),
    COMMAND_ID(RFXComBindingConstants.CHANNEL_COMMAND_ID, NumberItem.class),
    MOOD(RFXComBindingConstants.CHANNEL_MOOD, NumberItem.class),
    SIGNAL_LEVEL(RFXComBindingConstants.CHANNEL_SIGNAL_LEVEL, NumberItem.class),
    DIMMING_LEVEL(RFXComBindingConstants.CHANNEL_DIMMING_LEVEL, DimmerItem.class),
    UV(RFXComBindingConstants.CHANNEL_UV, NumberItem.class),
    TEMPERATURE(RFXComBindingConstants.CHANNEL_TEMPERATURE, NumberItem.class),
    FOOD_TEMPERATURE(RFXComBindingConstants.CHANNEL_FOOD_TEMPERATURE, NumberItem.class),
    BBQ_TEMPERATURE(RFXComBindingConstants.CHANNEL_BBQ_TEMPERATURE, NumberItem.class),
    HUMIDITY(RFXComBindingConstants.CHANNEL_HUMIDITY, NumberItem.class),
    HUMIDITY_STATUS(RFXComBindingConstants.CHANNEL_HUMIDITY_STATUS, StringItem.class),
    BATTERY_LEVEL(RFXComBindingConstants.CHANNEL_BATTERY_LEVEL, NumberItem.class),
    PRESSURE(RFXComBindingConstants.CHANNEL_PRESSURE, NumberItem.class),
    FORECAST(RFXComBindingConstants.CHANNEL_FORECAST, StringItem.class),
    RAIN_RATE(RFXComBindingConstants.CHANNEL_RAIN_RATE, NumberItem.class),
    RAIN_TOTAL(RFXComBindingConstants.CHANNEL_RAIN_TOTAL, NumberItem.class),
    WIND_DIRECTION(RFXComBindingConstants.CHANNEL_WIND_DIRECTION, NumberItem.class),
    AVG_WIND_SPEED(RFXComBindingConstants.CHANNEL_AVG_WIND_SPEED, NumberItem.class),
    WIND_SPEED(RFXComBindingConstants.CHANNEL_WIND_SPEED, NumberItem.class),
    CHILL_TEMPERATURE(RFXComBindingConstants.CHANNEL_CHILL_TEMPERATURE, NumberItem.class),
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
    CONTACT_1(RFXComBindingConstants.CHANNEL_CONTACT_1, ContactItem.class),
    CONTACT_2(RFXComBindingConstants.CHANNEL_CONTACT_2, ContactItem.class),
    CONTACT_3(RFXComBindingConstants.CHANNEL_CONTACT_3, ContactItem.class),
    VOLTAGE(RFXComBindingConstants.CHANNEL_VOLTAGE, NumberItem.class),
    SET_POINT(RFXComBindingConstants.CHANNEL_SET_POINT, NumberItem.class),
    DATE_TIME(RFXComBindingConstants.CHANNEL_DATE_TIME, DateTimeItem.class),
    LOW_BATTERY(RFXComBindingConstants.CHANNEL_LOW_BATTERY, SwitchItem.class),
    CHIME_SOUND(RFXComBindingConstants.CHANNEL_CHIME_SOUND, NumberItem.class);

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
     * Procedure to convert selector string to value selector class.
     *
     * @param valueSelectorText
     *            selector string e.g. RawData, Command, Temperature
     * @return corresponding selector value.
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
