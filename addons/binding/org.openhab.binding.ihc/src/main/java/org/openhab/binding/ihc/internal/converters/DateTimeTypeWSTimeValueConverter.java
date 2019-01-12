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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimeValue;

/**
 * DateTimeType <-> WSTimeValue converter.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DateTimeTypeWSTimeValueConverter implements Converter<WSTimeValue, DateTimeType> {

    @Override
    public DateTimeType convertFromResourceValue(WSTimeValue from, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        Calendar cal = dateTimeToCalendar(null, from);
        return new DateTimeType(ZonedDateTime.ofInstant(cal.toInstant(), TimeZone.getDefault().toZoneId()));
    }

    @Override
    public WSTimeValue convertFromOHType(DateTimeType from, WSTimeValue value, ConverterAdditionalInfo convertData)
            throws NumberFormatException {
        Calendar cal = GregorianCalendar.from(from.getZonedDateTime());
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        value.setHours(hours);
        value.setMinutes(minutes);
        value.setSeconds(seconds);
        return value;
    }

    private Calendar dateTimeToCalendar(WSDateValue date, WSTimeValue time) {
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
