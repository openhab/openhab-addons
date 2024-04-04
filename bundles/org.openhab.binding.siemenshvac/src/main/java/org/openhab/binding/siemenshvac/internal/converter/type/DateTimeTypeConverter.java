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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;

import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
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
    protected Object toBinding(Type type) throws ConverterException {
        Object valUpdate = null;

        if (type instanceof DateTimeType dateTime) {
            valUpdate = dateTime.toString();
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String type) {
        return true;
    }

    @Override
    protected DateTimeType fromBinding(JsonElement value, String type) throws ConverterException {
        if ("----".equals(value.getAsString())) {
            return new DateTimeType();
        } else {
            String[] formats = { "EEEE, d. MMMM yyyy hh:mm", "d. MMMM yyyy hh:mm", "d. MMMM" };

            for (int i = 0; i < formats.length; i++) {
                try {
                    SimpleDateFormat dtf = new SimpleDateFormat(formats[i]); // first example
                    ZonedDateTime zdt = dtf.parse(value.getAsString()).toInstant()
                            .atZone(this.timeZoneProvider.getTimeZone());

                    if (i == 2) {
                        zdt = zdt.withYear(2024);
                    }

                    return new DateTimeType(zdt);
                } catch (ParseException ex) {
                }
            }
            // logger.debug("Error decoding date: {}", value.getAsString());
        }

        return new DateTimeType();
    }

    @Override
    public String getChannelType(boolean writeAccess) {
        return "datetime";
    }

    @Override
    public String getItemType(boolean writeAccess) {
        return SiemensHvacBindingConstants.ITEM_TYPE_DATETIME;
    }

    @Override
    public boolean hasVariant() {
        return false;
    }

}
