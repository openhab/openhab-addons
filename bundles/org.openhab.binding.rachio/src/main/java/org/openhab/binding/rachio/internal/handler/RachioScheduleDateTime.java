/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioDateTime;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;

/**
 * DateTime channel conversion for schedule-like Rachio API responses.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
final class RachioScheduleDateTime {
    private RachioScheduleDateTime() {
    }

    static State dateTimeOrUndef(String thingId, Logger logger, String ruleId, String channelId,
            String... fieldNamesAndValues) {
        String unparseableField = "";
        for (int i = 0; i + 1 < fieldNamesAndValues.length; i += 2) {
            String fieldName = nullToEmpty(fieldNamesAndValues[i]);
            String value = nullToEmpty(fieldNamesAndValues[i + 1]).trim();
            if (value.isBlank()) {
                continue;
            }

            ParseResult result = parse(value);
            State state = result.state;
            if (state != null) {
                logger.trace(
                        "{}: DateTime channel '{}' for rule '{}' uses sourceField={}, valuePresent=true, parseSucceeded=true, valueType={}",
                        thingId, channelId, ruleId, fieldName, result.valueType);
                return state;
            }
            if (unparseableField.isBlank()) {
                unparseableField = fieldName;
            }
        }

        if (unparseableField.isBlank()) {
            logger.trace(
                    "{}: DateTime channel '{}' for rule '{}' is UNDEF: sourceField=none, valuePresent=false, parseSucceeded=false, valueType=blank",
                    thingId, channelId, ruleId);
        } else {
            logger.trace(
                    "{}: DateTime channel '{}' for rule '{}' is UNDEF: sourceField={}, valuePresent=true, parseSucceeded=false, valueType=unparseable",
                    thingId, channelId, ruleId, unparseableField);
        }
        return UnDefType.UNDEF;
    }

    private static ParseResult parse(String value) {
        DateTimeType state = RachioDateTime.parse(value);
        String valueType = state == null ? "unparseable" : RachioDateTime.isIntegerValue(value) ? "epoch" : "iso";
        return new ParseResult(state, valueType);
    }

    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }

    private static class ParseResult {
        final @Nullable State state;
        final String valueType;

        ParseResult(@Nullable State state, String valueType) {
            this.state = state;
            this.valueType = valueType;
        }
    }
}
