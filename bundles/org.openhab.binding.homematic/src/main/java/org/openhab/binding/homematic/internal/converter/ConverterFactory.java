/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.converter;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.homematic.internal.converter.type.DecimalTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.OnOffTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.OpenClosedTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.PercentTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.QuantityTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.StringTypeConverter;

/**
 * A factory for creating converters based on the itemType.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - QuantityType support
 */
public class ConverterFactory {
    private static Map<String, TypeConverter<?>> converterCache = new HashMap<>();

    /**
     * Returns the converter for a itemType.
     */
    public static TypeConverter<?> createConverter(String itemType) throws ConverterException {
        Class<? extends TypeConverter<?>> converterClass = null;

        if (itemType.startsWith(ITEM_TYPE_NUMBER + ":")) {
            converterClass = QuantityTypeConverter.class;
        } else {
            switch (itemType) {
                case ITEM_TYPE_SWITCH:
                    converterClass = OnOffTypeConverter.class;
                    break;
                case ITEM_TYPE_ROLLERSHUTTER:
                case ITEM_TYPE_DIMMER:
                    converterClass = PercentTypeConverter.class;
                    break;
                case ITEM_TYPE_CONTACT:
                    converterClass = OpenClosedTypeConverter.class;
                    break;
                case ITEM_TYPE_STRING:
                    converterClass = StringTypeConverter.class;
                    break;
                case ITEM_TYPE_NUMBER:
                    converterClass = DecimalTypeConverter.class;
                    break;
            }
        }

        TypeConverter<?> converter = null;
        if (converterClass != null) {
            converter = converterCache.get(converterClass.getName());
            if (converter == null) {
                try {
                    converter = converterClass.getConstructor().newInstance();
                    converterCache.put(converterClass.getName(), converter);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        if (converter == null) {
            throw new ConverterException("Can't find a converter for type '" + itemType + "'");
        }
        return converter;
    }
}
