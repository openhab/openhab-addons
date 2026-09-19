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
package org.openhab.binding.deutschebahn.internal.timetable;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper interface for jUnit Tests to provide a {@link TimetablesApiTestModule}.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public interface TimetablesV1ImplTestHelper {

    static final String EVA_LEHRTE = "8000226";
    static final String EVA_HANNOVER_HBF = "8000152";
    static final String CLIENT_ID = "bdwrpmxuo6157jrekftlbcc6ju9awo";
    static final String CLIENT_SECRET = "354c8161cd7fb0936c840240280c131e";
    static final ZoneId TIME_ZONE = ZoneId.of("Europe/Berlin");
    static final TimetableTimeConverter TIME_CONVERTER = new TimetableTimeConverter(TIME_ZONE);

    default Date createDate(int year, int month, int dayOfMonth, int hour, int minute) {
        return Date.from(LocalDateTime.of(year, month, dayOfMonth, hour, minute).atZone(TIME_ZONE).toInstant());
    }

    default GregorianCalendar createCalendar(int year, int month, int dayOfMonth, int hour, int minute) {
        return GregorianCalendar.from(LocalDateTime.of(year, month, dayOfMonth, hour, minute).atZone(TIME_ZONE));
    }

    /**
     * Creates a {@link TimetablesApiTestModule} that uses http response data from file system.
     * Uses default-testdata from directory /timetablesData
     */
    default TimetablesApiTestModule createApiWithTestdata() throws Exception {
        return this.createApiWithTestdata("/timetablesData");
    }

    /**
     * Creates a {@link TimetablesApiTestModule} that uses http response data from file system.
     * 
     * @param dataDirectory Directory within test-resources containing the stub-data.
     */
    default TimetablesApiTestModule createApiWithTestdata(String dataDirectory) throws Exception {
        final URL timetablesData = getClass().getResource(dataDirectory);
        assertNotNull(timetablesData);
        final File testDataDir = new File(timetablesData.toURI());
        final TimetableStubHttpCallable httpStub = new TimetableStubHttpCallable(testDataDir);
        final TimetablesV1Impl timeTableApi = new TimetablesV1Impl(CLIENT_ID, CLIENT_SECRET, httpStub, TIME_CONVERTER);
        return new TimetablesApiTestModule(timeTableApi, httpStub);
    }
}
