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
package org.openhab.binding.hapero.internal.handler;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hapero.internal.HaperoBindingConstants;
import org.openhab.binding.hapero.internal.device.Device;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link HaperoHeatingHandler} handles the Heating Circuit Thing
 * It parses the HKx Block of the Data Stream
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class HaperoHeatingHandler extends HaperoThingHandler {

    /* Data indices in the data stream block for the channel data */
    private static final int CHANNEL_FLOWTEMPERATURE_INDEX = 0;
    private static final int CHANNEL_FLOWTEMPERATURESET_INDEX = 1;
    private static final int CHANNEL_PUMP_INDEX = 2;
    private static final int CHANNEL_CIRCUITMODE_INDEX = 5;
    private static final int CHANNEL_ROOMTEMPERATURE_INDEX = 6;
    private static final int CHANNEL_ROOMTEMPERATURESET_INDEX = 7;
    private static final int CHANNEL_CIRCUITSUBMODE_INDEX = 8;
    private static final int CHANNEL_CIRCUITFAULT_INDEX = 9;

    /* Some temperature values are scaled down by 10 */
    private static final int TEMPERATURE_VALUE_SCALE = 10;

    /**
     * Constructor
     *
     * @param thing
     */
    public HaperoHeatingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable State getStateForChannel(Channel channel, Device device) {
        State state = null;
        Float valueFloat;
        Integer valueInt;
        Boolean valueBoolean;
        String channelId = channel.getUID().getIdWithoutGroup();

        switch (channelId) {
            case HaperoBindingConstants.CHANNEL_FLOWTEMPERATURE:
                valueFloat = device.getFloat(CHANNEL_FLOWTEMPERATURE_INDEX);
                if (valueFloat != null) {
                    valueFloat /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(valueFloat, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_FLOWTEMPERATURESET:
                valueFloat = device.getFloat(CHANNEL_FLOWTEMPERATURESET_INDEX);
                if (valueFloat != null) {
                    valueFloat /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(valueFloat, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_PUMP:
                valueBoolean = device.getBoolean(CHANNEL_PUMP_INDEX);
                if (valueBoolean != null) {
                    if (valueBoolean) {
                        state = OnOffType.ON;
                    } else {
                        state = OnOffType.OFF;
                    }
                }
                break;
            case HaperoBindingConstants.CHANNEL_CIRCUITMODE:
                valueInt = device.getInteger(CHANNEL_CIRCUITMODE_INDEX);
                if (valueInt != null) {
                    state = new DecimalType(valueInt);
                }
                break;
            case HaperoBindingConstants.CHANNEL_CIRCUITSUBMODE:
                valueInt = device.getInteger(CHANNEL_CIRCUITSUBMODE_INDEX);
                if (valueInt != null) {
                    state = new DecimalType(valueInt);
                }
                break;
            case HaperoBindingConstants.CHANNEL_CIRCUITFAULT:
                valueInt = device.getInteger(CHANNEL_CIRCUITFAULT_INDEX);
                if (valueInt != null) {
                    state = new DecimalType(valueInt);
                }
                break;
            case HaperoBindingConstants.CHANNEL_ROOMTEMPERATURE:
                valueFloat = device.getFloat(CHANNEL_ROOMTEMPERATURE_INDEX);
                if (valueFloat != null) {
                    valueFloat /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(valueFloat, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_ROOMTEMPERATURESET:
                valueFloat = device.getFloat(CHANNEL_ROOMTEMPERATURESET_INDEX);
                if (valueFloat != null) {
                    valueFloat /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(valueFloat, SIUnits.CELSIUS);
                }
                break;
            default:
                logger.warn("Unknown channel requested for heating: {}", channelId);
                break;

        }

        if (state == null) {
            logger.warn("Could not update heating channel {}.", channelId);
            state = UnDefType.NULL;
        }

        return state;
    }
}
