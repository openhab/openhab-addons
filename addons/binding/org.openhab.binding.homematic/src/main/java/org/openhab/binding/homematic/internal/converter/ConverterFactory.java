/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter;

import static org.openhab.binding.homematic.HomematicBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.homematic.internal.converter.type.DecimalTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.OnOffTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.OpenClosedTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.PercentTypeConverter;
import org.openhab.binding.homematic.internal.converter.type.StringTypeConverter;

/**
 * A factory for creating converters based on the itemType.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ConverterFactory {
    private static Map<String, TypeConverter<?>> converterCache = new HashMap<String, TypeConverter<?>>();

    /**
     * Returns the converter for a itemType.
     */
    public static TypeConverter<?> createConverter(String itemType) throws ConverterException {
        Class<? extends TypeConverter<?>> converterClass = null;
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

        TypeConverter<?> converter = null;
        if (converterClass != null) {
            converter = converterCache.get(converterClass.getName());
            if (converter == null) {
                try {
                    converter = converterClass.newInstance();
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
