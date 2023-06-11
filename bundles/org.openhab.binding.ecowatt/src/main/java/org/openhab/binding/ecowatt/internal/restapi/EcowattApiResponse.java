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
package org.openhab.binding.ecowatt.internal.restapi;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcowattApiResponse} class contains fields mapping the response to the Ecowatt API request /signals.
 *
 * It also includes an exception field to be set in case the API request fails.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattApiResponse {
    private final Logger logger = LoggerFactory.getLogger(EcowattApiResponse.class);

    public @Nullable List<EcowattDaySignals> signals;
    private @Nullable CommunicationException exception;

    public EcowattApiResponse() {
        this.signals = null;
        this.exception = null;
    }

    public EcowattApiResponse(@Nullable List<EcowattDaySignals> signals) {
        this.signals = signals;
        this.exception = null;
    }

    public EcowattApiResponse(CommunicationException exception) {
        this.signals = null;
        this.exception = exception;
    }

    /**
     * Search the data for the day of the given date and time
     *
     * @param dateTime a date and time
     * @return the data for the searched day or null if no data is found for this day
     */
    public @Nullable EcowattDaySignals getDaySignals(ZonedDateTime dateTime) {
        List<EcowattDaySignals> localSignals = signals;
        if (localSignals != null) {
            for (EcowattDaySignals daySignals : localSignals) {
                ZonedDateTime zdt = daySignals.getDay();
                if (zdt != null) {
                    // Adjust date/times to the same offset/zone
                    ZonedDateTime dateTime2 = dateTime.withZoneSameInstant(zdt.getZone());
                    logger.trace("zdt {} offset {} - dateTime2 {} offset {}",
                            zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), zdt.getOffset(),
                            dateTime2.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), dateTime2.getOffset());
                    // Check if the two date/times are in the same day
                    if (zdt.truncatedTo(ChronoUnit.DAYS).toInstant()
                            .equals(dateTime2.truncatedTo(ChronoUnit.DAYS).toInstant())) {
                        logger.debug("getDaySignals for {} returns signal {} : {} ( {} )",
                                dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), daySignals.getDaySignal(),
                                daySignals.getDayMessage(), zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                        return daySignals;
                    }
                }
            }
        }
        return null;
    }

    public boolean succeeded() {
        return signals != null;
    }

    public @Nullable CommunicationException getException() {
        return exception;
    }
}
