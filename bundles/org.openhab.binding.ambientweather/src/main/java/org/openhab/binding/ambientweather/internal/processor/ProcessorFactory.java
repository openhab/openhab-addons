/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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

    // Map of thing types to their processor suppliers
    private static final Map<String, Supplier<AbstractProcessor>> PROCESSOR_SUPPLIERS = Map.of(
            "ambientweather:ws1400ip", Ws1400ipProcessor::new, "ambientweather:ws2902a", Ws2902aProcessor::new,
            "ambientweather:ws2902b", Ws2902bProcessor::new, "ambientweather:ws8482", Ws8482Processor::new,
            "ambientweather:ws0900ip", Ws0900ipProcessor::new, "ambientweather:ws0265", Ws0265Processor::new);

    // Thread-safe cache for processor instances
    private static final Map<String, AbstractProcessor> PROCESSOR_CACHE = new ConcurrentHashMap<>();

    /**
     * Individual weather station processors use this one Gson instance,
     * rather than create their own.
     *
     * @return instance of a Gson object
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Get a processor for a specific weather station type.
     *
     * @param thing the Thing for which to get a processor
     * @return instance of a weather station processor
     * @throws ProcessorNotFoundException if no processor exists for the thing type
     */
    public static @Nullable AbstractProcessor getProcessor(Thing thing) throws ProcessorNotFoundException {
        String thingType = thing.getThingTypeUID().getAsString().toLowerCase();

        // Check if we have a supplier for this thing type
        Supplier<AbstractProcessor> supplier = PROCESSOR_SUPPLIERS.get(thingType);
        if (supplier == null) {
            throw new ProcessorNotFoundException("No processor for thing type " + thingType);
        }

        // Use computeIfAbsent for thread-safe caching
        return PROCESSOR_CACHE.computeIfAbsent(thingType, type -> supplier.get());
    }
}
