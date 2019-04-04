/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;

import com.google.gson.Gson;

/**
 * The {@link ProcessorFactory} is responsible for returning the right
 * processor for handling info update and weather data events.
 *
 * There is one processor for each supported station type.
 * To add support for a new station type, this class needs to be modified to
 * return a processor for the new thing type. In addition, an
 * AmbientWeatherXXXXXProcessor class needs to be created for the new station.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ProcessorFactory {
    // Single Gson instance shared by processors
    private static final Gson GSON = new Gson();

    // Supported weather stations
    @Nullable
    private static Ws1400ipProcessor WS1400IP_PROCESSOR;
    @Nullable
    private static Ws2902aProcessor WS2902A_PROCESSOR;
    @Nullable
    private static Ws8482Processor WS8482_PROCESSOR;
    @Nullable
    private static Ws0900ipProcessor WS0900IP_PROCESSOR;

    /**
     * Individual weather station processors use this one Gson instance,
     * rather than create their own.
     *
     * @return instance of a Gson object
     */
    public static Gson getGson() {
        return (GSON);
    }

    /**
     * Get a processor for a specific weather station type.
     *
     * @param thing
     * @return instance of a weather station processor
     * @throws ProcessorNotFoundException
     */
    @Nullable
    public static AbstractProcessor getProcessor(Thing thing) throws ProcessorNotFoundException {
        // Return the processor for this thing type
        String thingType = thing.getThingTypeUID().getAsString().toLowerCase();
        switch (thingType) {
            case "ambientweather:ws1400ip": {
                if (WS1400IP_PROCESSOR == null) {
                    WS1400IP_PROCESSOR = new Ws1400ipProcessor();
                }
                return WS1400IP_PROCESSOR;
            }
            case "ambientweather:ws2902a": {
                if (WS2902A_PROCESSOR == null) {
                    WS2902A_PROCESSOR = new Ws2902aProcessor();
                }
                return WS2902A_PROCESSOR;
            }
            case "ambientweather:ws8482": {
                if (WS8482_PROCESSOR == null) {
                    WS8482_PROCESSOR = new Ws8482Processor();
                }
                return WS8482_PROCESSOR;
            }
            case "ambientweather:ws0900ip": {
                if (WS0900IP_PROCESSOR == null) {
                    WS0900IP_PROCESSOR = new Ws0900ipProcessor();
                    ;
                }
                return WS0900IP_PROCESSOR;
            }
        }
        throw new ProcessorNotFoundException("No processor for thing type " + thingType);
    }
}
