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
package org.openhab.binding.siemenshvac.internal.converter.type;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class DateTimeTypeConverter extends AbstractTypeConverter {

    private final TimeZoneProvider timeZoneProvider;

    public DateTimeTypeConverter(final TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected @Nullable Object toBinding(Type type, ChannelType tp) throws ConverterException {
        Object valUpdate = null;

        if (type instanceof DateTimeType dateTime) {
            valUpdate = dateTime.toString();
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String unit, String type) {
        return true;
    }

    @Override
    protected DateTimeType fromBinding(JsonElement value, String unit, String type, ChannelType tp, Locale locale)
            throws ConverterException {
        if ("----".equals(value.getAsString())) {
            return new DateTimeType(ZonedDateTime.now(this.timeZoneProvider.getTimeZone()));
        } else {
            String[] formats = { "EEEE, d. LLLL yyyy HH:mm[:ss]", "d. LLLL yyyy HH:mm[:ss]", "d. LLLL yyyy",
                    "d. LLLL" };

            for (int i = 0; i < formats.length; i++) {
                try {
                    DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder().parseCaseInsensitive()
                            .appendPattern(formats[i]).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);

                    if (i == 3) {
                        formatterBuilder = formatterBuilder.parseDefaulting(ChronoField.YEAR,
                                ZonedDateTime.now(this.timeZoneProvider.getTimeZone()).getYear());
                    }

                    LocalDateTime parsedDate = LocalDateTime.parse(value.getAsString(),
                            formatterBuilder.toFormatter(locale));

                    ZonedDateTime zdt = parsedDate.atZone(this.timeZoneProvider.getTimeZone());

                    return new DateTimeType(zdt);
                } catch (DateTimeParseException ex) {
                    // Silently ignore, we are proceeding to next format.
                }
            }
        }

        throw new ConverterException("Can't parse the date for:" + value);
    }

    @Override
    public String getChannelType(SiemensHvacMetadataDataPoint dpt) {
        return "datetime";
    }

    @Override
    public String getItemType(SiemensHvacMetadataDataPoint dpt) {
        return CoreItemFactory.DATETIME;
    }

    @Override
    public boolean hasVariant() {
        return false;
    }
}
