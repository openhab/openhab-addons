/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sensorcommunity.internal.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DateTimeUtils} class provides helpers for converting Dates and Times.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DateTimeUtils {
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);

    public static synchronized @Nullable LocalDateTime toDate(String dateTime) {
        try {
            return LocalDateTime.from(DTF.parse(dateTime));
        } catch (DateTimeParseException e) {
            LOGGER.debug("Unable to parse date {}", dateTime);
            return null;
        }
    }
}
