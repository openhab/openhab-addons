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
package org.openhab.binding.deutschebahn.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.filter.FilterParser;
import org.openhab.binding.deutschebahn.internal.filter.FilterParserException;
import org.openhab.binding.deutschebahn.internal.filter.FilterScanner;
import org.openhab.binding.deutschebahn.internal.filter.FilterScannerException;
import org.openhab.binding.deutschebahn.internal.filter.FilterToken;
import org.openhab.binding.deutschebahn.internal.filter.TimetableStopPredicate;

/**
 * The {@link DeutscheBahnTimetableConfiguration} for the Timetable bridge-type.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class DeutscheBahnTimetableConfiguration {

    /**
     * Access-Token.
     */
    public String accessToken = "";

    /**
     * evaNo of the station to be queried.
     */
    public String evaNo = "";

    /**
     * Filter for timetable stops.
     */
    public String trainFilter = "";

    /**
     * Specifies additional filters for trains to be displayed within the timetable.
     */
    public String additionalFilter = "";

    /**
     * Returns the {@link TimetableStopFilter}.
     */
    public TimetableStopFilter getTrainFilterFilter() {
        return TimetableStopFilter.valueOf(this.trainFilter.toUpperCase());
    }

    /**
     * Returns the additional configured {@link TimetableStopPredicate} or <code>null</code> if not specified.
     */
    public @Nullable TimetableStopPredicate getAdditionalFilter() throws FilterScannerException, FilterParserException {
        if (additionalFilter.isBlank()) {
            return null;
        } else {
            final FilterScanner scanner = new FilterScanner();
            final List<FilterToken> filterTokens = scanner.processInput(additionalFilter);
            return FilterParser.parse(filterTokens);
        }
    }
}
