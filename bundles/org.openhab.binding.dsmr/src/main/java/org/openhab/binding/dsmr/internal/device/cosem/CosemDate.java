/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CosemDate represents a datetime value and will try to autodetect the format
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Class now a factory instead of data containing class
 */
@NonNullByDefault
class CosemDate extends CosemValueDescriptor<DateTimeType> {

    public static final CosemDate INSTANCE = new CosemDate("timestamp");
    /*
     * Some meters can return the following value when something is wrong.
     */
    public static final String INVALID_METER_VALUE = "632525252525W";

    private final Logger logger = LoggerFactory.getLogger(CosemDate.class);

    public CosemDate(String ohChannelId) {
        super(ohChannelId);
    }

    /**
     * This enum contains the known date formats for the DSMR-specification
     */
    private enum CosemDateFormat {
        /*
         * Ignore DST setting for general format. We use local time that is already DST
         */
        COSEM_DATE_GENERAL("(\\d{12})([S,W]?)", "yyMMddHHmmss"),
        COSEM_DATE_DSMR_V2("(\\d{2}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})", "yy-MM-dd HH:mm:ss");

        /**
         * Cached compiled pattern
         */
        private final Pattern pattern;

        /**
         * Cached java date formatter
         */
        private final DateTimeFormatter formatter;

        /**
         * Constructs a new CosemDateFormat
         *
         * @param regex String containing the regular expression to check the value against (the date format
         *            should at least contain 1 regex group
         * @param javaDateFormat String containing the datetime format to use for parsing
         */
        private CosemDateFormat(String regex, String javaDateFormat) {
            pattern = Pattern.compile(regex);
            formatter = DateTimeFormatter.ofPattern(javaDateFormat);
        }
    }

    /**
     * Parses a String value to an openHAB DateTimeType
     * <p>
     * The input string must be in the format yyMMddHHmmssX
     * <p>
     * Based on the DSMR specification X is:
     * <p>
     * <ul>
     * <li>''. Valid for DSMR v3 specification
     * <li>'S'. Specifies a summer time (DST = 1) datetime
     * <li>'W'. Specifies a winter time (DST = 0) datetime
     * </ul>
     *
     * @param cosemValue the value to parse
     * @return {@link DateTimeType} representing the value the cosem value
     * @throws ParseException if parsing failed
     */
    @Override
    protected DateTimeType getStateValue(String cosemValue) throws ParseException {
        for (CosemDateFormat cosemDateFormat : CosemDateFormat.values()) {
            logger.trace("Trying pattern: {}", cosemDateFormat.pattern);

            Matcher m = cosemDateFormat.pattern.matcher(cosemValue);

            if (m.matches()) {
                logger.trace("{} matches pattern: {}", cosemValue, cosemDateFormat.pattern);

                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(m.group(1), cosemDateFormat.formatter);
                    return new DateTimeType(ZonedDateTime.of(localDateTime, ZoneId.systemDefault()));
                } catch (DateTimeParseException e) {
                    if (INVALID_METER_VALUE.equals(cosemValue)) {
                        throw new ParseException(
                                "Cosem value: '" + cosemValue + "' might indicate something is wrong with the meter.",
                                0);
                    }
                }
            }
        }
        throw new ParseException("Cosem value: '" + cosemValue + "' is not a known CosemDate string", 0);
    }
}
