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
package org.openhab.binding.siemenshvac.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.converter.type.CalendarTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.CheckboxTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.DateTimeTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.EnumTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.NumericTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.RadioTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.SchedulerTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.StringTypeConverter;
import org.openhab.binding.siemenshvac.internal.converter.type.TimeOfDayTypeConverter;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * A factory for creating converters based on the itemType.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
public class ConverterFactory {
    private static Map<String, TypeConverter> converterCache = new HashMap<>();

    public static void registerConverter(TimeZoneProvider timeZoneProvider) {
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_DATE_TIME, new DateTimeTypeConverter(timeZoneProvider));
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_ENUM, new EnumTypeConverter());
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_NUMERIC, new NumericTypeConverter());
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_RADIO, new RadioTypeConverter());
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_STRING, new StringTypeConverter());
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_TIMEOFDAY, new TimeOfDayTypeConverter(timeZoneProvider));

        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_CHECKBOX, new CheckboxTypeConverter());
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_SCHEDULER, new SchedulerTypeConverter());
        registerConverter(SiemensHvacBindingConstants.DPT_TYPE_CALENDAR, new CalendarTypeConverter());
    }

    public static void registerConverter(String key, TypeConverter tp) {
        converterCache.put(key, tp);
    }

    /**
     * Returns the converter for an itemType.
     */
    public static TypeConverter getConverter(String itemType) throws ConverterTypeException {
        TypeConverter converter = converterCache.get(itemType);
        if (converter == null) {
            throw new ConverterTypeException("Can't find a converter for type '" + itemType + "'");
        }

        return converter;
    }
}
