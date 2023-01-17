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
package org.openhab.binding.ambientweather.internal.processor;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.openhab.binding.ambientweather.internal.model.EventDataJson;
import org.openhab.core.library.unit.ImperialUnits;

/**
 * The {@link Ws0265Processor} is responsible for updating
 * the channels associated with the WS-0265 series weather stations in
 * response to the receipt of a weather data update from the Ambient
 * Weather real-time API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Ws0265Processor extends AbstractProcessor {

    @Override
    public void setChannelGroupId() {
        channelGroupId = CHGRP_WS0265;
    }

    @Override
    public void setNumberOfSensors() {
        remoteSensor.setNumberOfSensors(7);
    }

    @Override
    public void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name, String location) {
        // Update name and location channels
        handler.updateString(CHGRP_STATION, CH_NAME, name);
        handler.updateString(CHGRP_STATION, CH_LOCATION, location);
    }

    @Override
    public void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData) {
        EventDataJson data = parseEventData(station, jsonData);
        if (data == null) {
            return;
        }

        // Update the weather data channels
        handler.updateDate(channelGroupId, CH_OBSERVATION_TIME, data.date);
        handler.updateQuantity(channelGroupId, CH_TEMPERATURE, data.tempinf, ImperialUnits.FAHRENHEIT);

        // Update the remote sensor channels
        remoteSensor.updateChannels(handler, jsonData);
    }
}
