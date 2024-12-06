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
package org.openhab.binding.plugwiseha.internal.api.model.converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * The {@link DateTimeConverter} provides a SingleValueConverter for use by XStream when converting
 * XML documents with a zoned date/time field.
 * 
 * @author B. van Wetten - Initial contribution
 */

@NonNullByDefault
public class DateTimeConverter extends AbstractSingleValueConverter {

    private final Logger logger = LoggerFactory.getLogger(DateTimeConverter.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // default Date format that

    @Override
    public boolean canConvert(@Nullable @SuppressWarnings("rawtypes") Class type) {
        return (type == null) ? false : ZonedDateTime.class.isAssignableFrom(type);
    }

    @Override
    public @Nullable ZonedDateTime fromString(@Nullable String str) {
        if (str == null || str.isBlank()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(str, DateTimeConverter.FORMAT);
        } catch (DateTimeParseException e) {
            logger.debug("Invalid datetime format in {}", str);
            return null;
        }
    }

    public String toString(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeConverter.FORMAT);
    }
}
