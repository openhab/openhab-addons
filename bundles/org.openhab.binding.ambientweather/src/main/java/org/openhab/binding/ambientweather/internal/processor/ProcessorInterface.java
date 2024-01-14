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
package org.openhab.binding.ambientweather.internal.processor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;

/**
 * The {@link ProcessorInterface} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public interface ProcessorInterface {
    /*
     * Set the channel group Id for the station
     */
    void setChannelGroupId();

    /*
     * Set the number of remote sensors supported by the station
     */
    void setNumberOfSensors();

    /*
     * Updates the info channels (i.e. name and location) for a station
     */
    void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name, String location);

    /*
     * Updates the weather data channels for a station
     */
    void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData);
}
