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
import org.eclipse.smarthome.core.thing.Thing;

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
    // Supported weather stations
    private static final Ws1400ipProcessor WS1400IP_PROCESSOR = new Ws1400ipProcessor();
    private static final Ws2902aProcessor WS2902A_PROCESSOR = new Ws2902aProcessor();
    private static final Ws8482Processor WS8482_PROCESSOR = new Ws8482Processor();
    private static final Ws0900ipProcessor WS0900IP_PROCESSOR = new Ws0900ipProcessor();

    public static AbstractProcessor getProcessor(Thing thing) throws ProcessorNotFoundException {
        // Return the processor for this thing type
        String thingType = thing.getThingTypeUID().getAsString().toLowerCase();
        switch (thingType) {
            case "ambientweather:ws1400ip": {
                return WS1400IP_PROCESSOR;
            }
            case "ambientweather:ws2902a": {
                return WS2902A_PROCESSOR;
            }
            case "ambientweather:ws8482": {
                return WS8482_PROCESSOR;
            }
            case "ambientweather:ws0900ip": {
                return WS0900IP_PROCESSOR;
            }
        }
        throw new ProcessorNotFoundException("No processor for thing type " + thingType);
    }
}
