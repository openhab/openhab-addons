/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.processor;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.openhab.binding.ambientweather.internal.json.EventDataJson;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Ws8482Processor} is responsible for updating
 * the channels associated with the WS-1400-IP series weather stations in
 * response to the receipt of a weather data update from the Ambient
 * Weather real-time API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Ws8482Processor extends AbstractProcessor {

    public Ws8482Processor() {
        // Set the number of remote sensor channels supported by this station
        remoteSensor.setNumberOfSensors(7);
    }

    @Override
    public void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name, String location) {
        // Update name and location channels
        logger.debug("Station {}: Updating station information channels", station);
        handler.updateChannel(CHGRP_STATION + "#" + CH_NAME, new StringType(name));
        handler.updateChannel(CHGRP_STATION + "#" + CH_LOCATION, new StringType(location));
    }

    @Override
    public void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData) {
        Gson gson = new Gson();
        try {
            EventDataJson data = gson.fromJson(jsonData, EventDataJson.class);
            logger.debug("Station {}: Updating weather data channels", station);

            // Update the weather data channels for the WS-8482
            handler.updateChannel(CHGRP_WS8482 + "#" + CH_OBSERVATION_TIME,
                    getLocalDateTimeType(data.date, handler.getZoneId()));
            handler.updateChannel(CHGRP_WS8482 + "#" + CH_TEMPERATURE,
                    new QuantityType<>(data.tempinf, ImperialUnits.FAHRENHEIT));
            handler.updateChannel(CHGRP_WS8482 + "#" + CH_HUMIDITY,
                    new QuantityType<>(data.humidityin, SmartHomeUnits.PERCENT));
            handler.updateChannel(CHGRP_WS8482 + "#" + CH_BATTERY_INDICATOR, new StringType(data.battout));

            // Update the remote sensor channels
            remoteSensor.updateChannels(handler, jsonData);
        } catch (JsonSyntaxException e) {
            logger.info("Station {}: Data event cannot be parsed: {}", station, e.getMessage());
            return;
        }
    }
}
