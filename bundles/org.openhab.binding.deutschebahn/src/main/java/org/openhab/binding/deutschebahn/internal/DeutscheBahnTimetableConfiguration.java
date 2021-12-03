/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

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
     * Returns the {@link TimetableStopFilter}.
     */
    public TimetableStopFilter getTimetableStopFilter() {
        return TimetableStopFilter.valueOf(this.trainFilter.toUpperCase());
    }
}
