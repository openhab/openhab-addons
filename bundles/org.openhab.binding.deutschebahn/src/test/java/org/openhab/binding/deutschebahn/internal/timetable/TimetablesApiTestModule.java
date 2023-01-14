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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1Impl.HttpCallable;

/**
 * Testmodule that contains the {@link TimetablesV1Api} and {@link TimetableStubHttpCallable}.
 * Used in tests to check which http calls have been made.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public final class TimetablesApiTestModule {

    private final TimetablesV1Api api;
    private final TimetableStubHttpCallable httpStub;

    public TimetablesApiTestModule(TimetablesV1Api api, TimetableStubHttpCallable httpStub) {
        this.api = api;
        this.httpStub = httpStub;
    }

    public TimetablesV1Api getApi() {
        return api;
    }

    public void addAvailableUrl(String url) {
        this.httpStub.addAvailableUrl(url);
    }

    public List<String> getRequestedPlanUrls() {
        return httpStub.getRequestedPlanUrls();
    }

    public List<String> getRequestedFullChangesUrls() {
        return httpStub.getRequestedFullChangesUrls();
    }

    public List<String> getRequestedRecentChangesUrls() {
        return httpStub.getRequestedRecentChangesUrls();
    }

    public TimetablesV1ApiFactory getApiFactory() {
        return (String clientId, String clientSecret, HttpCallable httpCallable) -> api;
    }
}
