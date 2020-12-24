/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.plugwiseha.internal.api.model.converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * The {@link DateTimeConverter} provides a SingleValueConverter for use by XStream when converting
 * XML documents with a zoned date/time field.
 * 
 * @author B. van Wetten - Initial contribution
 */

public class DateTimeConverter extends AbstractSingleValueConverter {

    private final static DateTimeFormatter FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // default Date format that

    @Override
    @SuppressWarnings("rawType")
    public boolean canConvert(Class type) {
        return ZonedDateTime.class.isAssignableFrom(type);
    }

    @Override
    public ZonedDateTime fromString(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        try {
            ZonedDateTime dateTime = ZonedDateTime.parse(str, DateTimeConverter.FORMAT);
            return dateTime;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid datetime format in {" + str + "}");
        }
    }

    public String toString(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeConverter.FORMAT);
    }
}
