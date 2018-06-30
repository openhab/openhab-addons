/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.openhab.binding.ihc.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimeValue;

/**
 * DateTimeType <-> WSDateValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DateTimeTypeWSDateValueConverter implements Converter<WSDateValue, DateTimeType> {

    @Override
    public DateTimeType convertFromResourceValue(WSDateValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {

        Calendar cal = WSDateTimeToCalendar(from, null);
        // return new DateTimeType(cal);
        return new DateTimeType(ZonedDateTime.ofInstant(cal.toInstant(), TimeZone.getDefault().toZoneId()));
    }

    @Override
    public WSDateValue convertFromOHType(DateTimeType from, WSDateValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {

        // Calendar cal = from.getCalendar();
        Calendar cal = GregorianCalendar.from(from.getZonedDateTime());
        short year = (short) cal.get(Calendar.YEAR);
        byte month = (byte) (cal.get(Calendar.MONTH) + 1);
        byte day = (byte) cal.get(Calendar.DAY_OF_MONTH);
        value.setYear(year);
        value.setMonth(month);
        value.setDay(day);
        return value;
    }

    private Calendar WSDateTimeToCalendar(WSDateValue date, WSTimeValue time) {
        Calendar cal = new GregorianCalendar(2000, 01, 01);
        if (date != null) {
            short year = date.getYear();
            short month = date.getMonth();
            short day = date.getDay();

            cal.set(year, month - 1, day, 0, 0, 0);
        }
        if (time != null) {
            int hour = time.getHours();
            int minute = time.getMinutes();
            int second = time.getSeconds();

            cal.set(2000, 0, 1, hour, minute, second);
        }
        return cal;
    }
}
