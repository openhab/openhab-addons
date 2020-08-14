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
package org.openhab.binding.luftdateninfo.internal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public static final SimpleDateFormat SDF = new SimpleDateFormat("YYYY-mm-dd hh:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);

    public static synchronized @Nullable Date toDate(String dateTime) {
        try {
            return SDF.parse(dateTime);
        } catch (ParseException | NumberFormatException e) {
            LOGGER.debug("Uanble to parse date {}", dateTime);
            return null;
        }
    }
}
