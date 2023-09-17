/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;

/**
 * A factory for creating converters based on the itemType.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
public class SmartThingsConverterFactory {
    private static Map<String, SmartThingsConverter> converterCache = new HashMap<>();

    public static void registerConverters(SmartThingsTypeRegistry typeRegistry) {
        registerConverter(SmartThingsBindingConstants.CHANNEL_NAME_COLOR, new SmartThingsColorConverter(typeRegistry));
        registerConverter(SmartThingsBindingConstants.CHANNEL_NAME_HUE, new SmartThingsHue100Converter(typeRegistry));
        registerConverter(SmartThingsBindingConstants.CHANNEL_NAME_SATURATION,
                new SmartThingsSaturationConverter(typeRegistry));
        registerConverter(SmartThingsBindingConstants.CHANNEL_NAME_DEFAULT,
                new SmartThingsDefaultConverter(typeRegistry));
    }

    private static void registerConverter(String key, SmartThingsConverter tp) {
        converterCache.put(key, tp);
    }

    /**
     * Returns the converter for an itemType.
     */
    public static @Nullable SmartThingsConverter getConverter(String itemType) {
        SmartThingsConverter converter = converterCache.get(itemType);
        if (converter == null) {
            converter = converterCache.get(SmartThingsBindingConstants.CHANNEL_NAME_DEFAULT);
        }
        return converter;
    }
}
