/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.devices.rf.weathersensor;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.TypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoChildDeviceData;
import org.openhab.binding.tapocontrol.internal.devices.rf.TapoChildDeviceHandler;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Thing;

/**
 * TAPO Smart-Contact-Device.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoWeatherSensorHandler extends TapoChildDeviceHandler {
    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoWeatherSensorHandler(Thing thing) {
        super(thing);
    }

    /**
     * Update properties
     */
    @Override
    protected void devicePropertiesChanged(TapoChildDeviceData deviceInfo) {
        super.devicePropertiesChanged(deviceInfo);

        updateState(getChannelID(CHANNEL_GROUP_SENSOR, CHANNEL_TEMPERATURE),
                getTemperatureType(deviceInfo.getTemperature(), SIUnits.CELSIUS));
        updateState(getChannelID(CHANNEL_GROUP_SENSOR, CHANNEL_HUMIDITY), getPercentType(deviceInfo.getHumidity()));
    }
}
