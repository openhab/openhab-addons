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
package org.openhab.binding.smartthings.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;

/**
 * A factory for creating converters based on the itemType.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
public class SmartthingsConverterFactory {
    private static Map<String, SmartthingsConverter> converterCache = new HashMap<>();

    public static void registerConverters(SmartthingsTypeRegistry typeRegistry) {
        registerConverter("color", new SmartthingsColorConverter(typeRegistry));
        registerConverter("default", new SmartthingsDefaultConverter(typeRegistry));
    }

    private static void registerConverter(String key, SmartthingsConverter tp) {
        converterCache.put(key, tp);
    }

    /**
     * Returns the converter for an itemType.
     */
    public static @Nullable SmartthingsConverter getConverter(String itemType) {
        SmartthingsConverter converter = converterCache.get(itemType);
        return converter;
    }
}
