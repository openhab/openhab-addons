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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link HaperoTankHandler} handles the Buffer and Boiler Things
 * It parses the PUx Block of the Data Stream for a Buffer Thing
 * and the WWx Block of the Data Stream for a Boiler Thing
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class HaperoTankHandler extends HaperoThingHandler {

    /* Data indices in the data stream block for the channel data */
    private static final int CHANNEL_TEMPERATURETOP_INDEX = 0;
    private static final int CHANNEL_TEMPERATUREBOTTOM_INDEX = 1;
    private static final int CHANNEL_ONTEMPERATURE_INDEX = 2;
    private static final int CHANNEL_OFFTEMPERATURE_INDEX = 3;
    private static final int CHANNEL_PUMP_INDEX = 4;
    private static final int CHANNEL_SWITCHVALVE_INDEX = 5;
    private static final int CHANNEL_CHARGING_INDEX = 6;

    /* Some temperature values are scaled down by 10 */
    private static final int TEMPERATURE_VALUE_SCALE = 10;

    /**
     * Constructor
     *
     * @param thing
     */
    public HaperoTankHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable State getStateForChannel(Channel channel, Device device) {
        State state = null;
        Float value;
        Boolean valueBoolean;
        String channelId = channel.getUID().getIdWithoutGroup();

        switch (channelId) {
            case HaperoBindingConstants.CHANNEL_TEMPERATURETOP:
                value = device.getFloat(CHANNEL_TEMPERATURETOP_INDEX);
                if (value != null) {
                    value /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_TEMPERATUREBOTTOM:
                value = device.getFloat(CHANNEL_TEMPERATUREBOTTOM_INDEX);
                if (value != null) {
                    value /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_ONTEMPERATURE:
                value = device.getFloat(CHANNEL_ONTEMPERATURE_INDEX);
                if (value != null) {
                    value /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_OFFTEMPERATURE:
                value = device.getFloat(CHANNEL_OFFTEMPERATURE_INDEX);
                if (value != null) {
                    value /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                }
                break;
            case HaperoBindingConstants.CHANNEL_PUMP:
                valueBoolean = device.getBoolean(CHANNEL_PUMP_INDEX);
                if (valueBoolean != null) {
                    state = valueBoolean ? OnOffType.ON : OnOffType.OFF;
                }
                break;
            case HaperoBindingConstants.CHANNEL_SWITCHVALVE:
                valueBoolean = device.getBoolean(CHANNEL_SWITCHVALVE_INDEX);
                if (valueBoolean != null) {
                    state = valueBoolean ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                }
                break;
            case HaperoBindingConstants.CHANNEL_CHARGING:
                value = device.getFloat(CHANNEL_CHARGING_INDEX);
                if (value != null) {
                    value /= TEMPERATURE_VALUE_SCALE;
                    state = new QuantityType<Temperature>(value, SIUnits.CELSIUS);
                }
                break;
            default:
                logger.warn("Unknown channel requested for Tank: {}", channelId);
                break;

        }

        if (state == null) {
            logger.warn("Could not update tank channel {}.", channelId);
        }

        return state;
    }
}
