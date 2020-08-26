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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Converter} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Converter {
    private final static Logger logger = LoggerFactory.getLogger(Converter.class);
    private final static DateTimeFormatter inputPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static DateTimeFormatter outputPattern = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static double round(double value) {
        double scale = Math.pow(10, 1);
        return Math.round(value * scale) / scale;
    }

    public static String getLocalDateTime(String input) {
        LocalDateTime ldt = LocalDateTime.parse(input, Converter.inputPattern);
        logger.info("Update Local DateTime {}", ldt);
        ZonedDateTime zdtUTC = ldt.atZone(ZoneId.of("UTC"));
        logger.info("Update UTC DateTime   {}", zdtUTC);
        ZonedDateTime zdtLZ = zdtUTC.withZoneSameInstant(ZoneId.systemDefault());
        logger.info("Update UTC DateTime   {}", zdtLZ);
        return zdtLZ.format(Converter.outputPattern);
    }
}
