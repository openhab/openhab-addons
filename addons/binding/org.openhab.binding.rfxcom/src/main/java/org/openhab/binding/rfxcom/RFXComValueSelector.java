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

    RAW_MESSAGE(RFXComBindingConstants.CHANNEL_RAW_MESSAGE),
    RAW_PAYLOAD(RFXComBindingConstants.CHANNEL_RAW_PAYLOAD),
    SHUTTER(RFXComBindingConstants.CHANNEL_SHUTTER),
    VENETIAN_BLIND(RFXComBindingConstants.CHANNEL_VENETIAN_BLIND),
    SUN_WIND_DETECTOR(RFXComBindingConstants.CHANNEL_SUN_WIND_DETECTOR),
    PROGRAM(RFXComBindingConstants.CHANNEL_PROGRAM),
    COMMAND(RFXComBindingConstants.CHANNEL_COMMAND),
    COMMAND_ID(RFXComBindingConstants.CHANNEL_COMMAND_ID),
    MOOD(RFXComBindingConstants.CHANNEL_MOOD),
    SIGNAL_LEVEL(RFXComBindingConstants.CHANNEL_SIGNAL_LEVEL),
    DIMMING_LEVEL(RFXComBindingConstants.CHANNEL_DIMMING_LEVEL),
    UV(RFXComBindingConstants.CHANNEL_UV),
    TEMPERATURE(RFXComBindingConstants.CHANNEL_TEMPERATURE),
    HUMIDITY(RFXComBindingConstants.CHANNEL_HUMIDITY),
    HUMIDITY_STATUS(RFXComBindingConstants.CHANNEL_HUMIDITY_STATUS),
    BATTERY_LEVEL(RFXComBindingConstants.CHANNEL_BATTERY_LEVEL),
    PRESSURE(RFXComBindingConstants.CHANNEL_PRESSURE),
    FORECAST(RFXComBindingConstants.CHANNEL_FORECAST),
    RAIN_RATE(RFXComBindingConstants.CHANNEL_RAIN_RATE),
    RAIN_TOTAL(RFXComBindingConstants.CHANNEL_RAIN_TOTAL),
    WIND_DIRECTION(RFXComBindingConstants.CHANNEL_WIND_DIRECTION),
    AVG_WIND_SPEED(RFXComBindingConstants.CHANNEL_AVG_WIND_SPEED),
    WIND_SPEED(RFXComBindingConstants.CHANNEL_WIND_SPEED),
    CHILL_TEMPERATURE(RFXComBindingConstants.CHANNEL_CHILL_TEMPERATURE),
    INSTANT_POWER(RFXComBindingConstants.CHANNEL_INSTANT_POWER),
    TOTAL_USAGE(RFXComBindingConstants.CHANNEL_TOTAL_USAGE),
    INSTANT_AMPS(RFXComBindingConstants.CHANNEL_INSTANT_AMPS),
    TOTAL_AMP_HOUR(RFXComBindingConstants.CHANNEL_TOTAL_AMP_HOUR),
    CHANNEL1_AMPS(RFXComBindingConstants.CHANNEL_CHANNEL1_AMPS),
    CHANNEL2_AMPS(RFXComBindingConstants.CHANNEL_CHANNEL2_AMPS),
    CHANNEL3_AMPS(RFXComBindingConstants.CHANNEL_CHANNEL3_AMPS),
    STATUS(RFXComBindingConstants.CHANNEL_STATUS),
    MOTION(RFXComBindingConstants.CHANNEL_MOTION),
    CONTACT(RFXComBindingConstants.CHANNEL_CONTACT),
    CONTACT_1(RFXComBindingConstants.CHANNEL_CONTACT_1),
    CONTACT_2(RFXComBindingConstants.CHANNEL_CONTACT_2),
    CONTACT_3(RFXComBindingConstants.CHANNEL_CONTACT_3),
    VOLTAGE(RFXComBindingConstants.CHANNEL_VOLTAGE),
    SET_POINT(RFXComBindingConstants.CHANNEL_SET_POINT),
    DATE_TIME(RFXComBindingConstants.CHANNEL_DATE_TIME),
    LOW_BATTERY(RFXComBindingConstants.CHANNEL_LOW_BATTERY),
    CHIME_SOUND(RFXComBindingConstants.CHANNEL_CHIME_SOUND);

    private final String text;

    private RFXComValueSelector(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
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
