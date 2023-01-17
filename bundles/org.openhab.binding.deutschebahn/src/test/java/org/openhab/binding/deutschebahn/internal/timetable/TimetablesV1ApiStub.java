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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Timetable;

/**
 * Stub Implementation of {@link TimetablesV1Api}, that may return a preconfigured Timetable or
 * throws an {@link IOException} if not data has been set.
 * 
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class TimetablesV1ApiStub implements TimetablesV1Api {

    @Nullable
    private final Timetable result;

    private TimetablesV1ApiStub(@Nullable Timetable result) {
        this.result = result;
    }

    /**
     * Creates a new {@link TimetablesV1ApiStub}, that returns the given result.
     */
    public static TimetablesV1ApiStub createWithResult(Timetable timetable) {
        return new TimetablesV1ApiStub(timetable);
    }

    /**
     * Creates a new {@link TimetablesV1ApiStub} that throws an Exception.
     */
    public static TimetablesV1ApiStub createWithException() {
        return new TimetablesV1ApiStub(null);
    }

    @Override
    public Timetable getPlan(String evaNo, Date time) throws IOException {
        final Timetable currentResult = this.result;
        if (currentResult == null) {
            throw new IOException("No timetable data is available");
        } else {
            return currentResult;
        }
    }

    @Override
    public Timetable getFullChanges(String evaNo) throws IOException {
        return new Timetable();
    }

    @Override
    public Timetable getRecentChanges(String evaNo) throws IOException {
        return new Timetable();
    }
}
