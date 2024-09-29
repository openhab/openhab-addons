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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;

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
    private @Nullable static Ws1400ipProcessor WS1400IP_PROCESSOR;
    private @Nullable static Ws2902aProcessor WS2902A_PROCESSOR;
    private @Nullable static Ws2902bProcessor WS2902B_PROCESSOR;
    private @Nullable static Ws8482Processor WS8482_PROCESSOR;
    private @Nullable static Ws0900ipProcessor WS0900IP_PROCESSOR;
    private @Nullable static Ws0265Processor WS0265_PROCESSOR;

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
    public static AbstractProcessor getProcessor(Thing thing) throws ProcessorNotFoundException {
        // Return the processor for this thing type
        String thingType = thing.getThingTypeUID().getAsString().toLowerCase();
        switch (thingType) {
            case "ambientweather:ws1400ip": {
                Ws1400ipProcessor processor = WS1400IP_PROCESSOR;
                if (processor == null) {
                    processor = new Ws1400ipProcessor();
                    WS1400IP_PROCESSOR = processor;
                }
                return processor;
            }
            case "ambientweather:ws2902a": {
                Ws2902aProcessor processor = WS2902A_PROCESSOR;
                if (processor == null) {
                    processor = new Ws2902aProcessor();
                    WS2902A_PROCESSOR = processor;
                }
                return processor;
            }
            case "ambientweather:ws2902b": {
                Ws2902bProcessor processor = WS2902B_PROCESSOR;
                if (processor == null) {
                    processor = new Ws2902bProcessor();
                    WS2902B_PROCESSOR = processor;
                }
                return processor;
            }
            case "ambientweather:ws8482": {
                Ws8482Processor processor = WS8482_PROCESSOR;
                if (processor == null) {
                    processor = new Ws8482Processor();
                    WS8482_PROCESSOR = processor;
                }
                return processor;
            }
            case "ambientweather:ws0900ip": {
                Ws0900ipProcessor processor = WS0900IP_PROCESSOR;
                if (processor == null) {
                    processor = new Ws0900ipProcessor();
                    WS0900IP_PROCESSOR = processor;
                }
                return processor;
            }
            case "ambientweather:ws0265": {
                Ws0265Processor processor = WS0265_PROCESSOR;
                if (processor == null) {
                    processor = new Ws0265Processor();
                    WS0265_PROCESSOR = processor;
                }
                return processor;
            }
        }
        throw new ProcessorNotFoundException("No processor for thing type " + thingType);
    }
}
