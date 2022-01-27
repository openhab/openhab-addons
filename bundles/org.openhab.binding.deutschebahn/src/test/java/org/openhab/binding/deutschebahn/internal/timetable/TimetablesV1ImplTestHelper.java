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
package org.openhab.binding.deutschebahn.internal.timetable;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper interface for jUnit Tests to provide an {@link TimetablesApiTestModule}.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public interface TimetablesV1ImplTestHelper {

    public static final String EVA_LEHRTE = "8000226";
    public static final String EVA_HANNOVER_HBF = "8000152";
    public static final String AUTH_TOKEN = "354c8161cd7fb0936c840240280c131e";

    /**
     * Creates an {@link TimetablesApiTestModule} that uses http response data from file system.
     */
    public default TimetablesApiTestModule createApiWithTestdata() throws Exception {
        final URL timetablesData = getClass().getResource("/timetablesData");
        assertNotNull(timetablesData);
        final File testDataDir = new File(timetablesData.toURI());
        final TimetableStubHttpCallable httpStub = new TimetableStubHttpCallable(testDataDir);
        final TimetablesV1Impl timeTableApi = new TimetablesV1Impl(AUTH_TOKEN, httpStub);
        return new TimetablesApiTestModule(timeTableApi, httpStub);
    }
}
