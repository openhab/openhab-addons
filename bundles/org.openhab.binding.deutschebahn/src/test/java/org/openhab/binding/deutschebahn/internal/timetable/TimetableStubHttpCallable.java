/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1Impl.HttpCallable;

/**
 * Stub Implementation for {@link HttpCallable}, that provides Data for the selected station, date and hour from file
 * system.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public class TimetableStubHttpCallable implements HttpCallable {

    private static final Pattern PLAN_URL_PATTERN = Pattern
            .compile("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/(\\d+)/(\\d+)/(\\d+)");
    private static final Pattern FULL_CHANGES_URL_PATTERN = Pattern
            .compile("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/(\\d+)");
    private static final Pattern RECENT_CHANGES_URL_PATTERN = Pattern
            .compile("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/rchg/(\\d+)");

    private final File testdataDir;
    private final List<String> requestedPlanUrls;
    private final List<String> requestedFullChangesUrls;
    private final List<String> requestedRecentChangesUrls;

    // Allows simulation of available data.
    // if not set all available files will be served.
    private @Nullable Set<String> availableUrls = null;

    public TimetableStubHttpCallable(File testdataDir) {
        this.testdataDir = testdataDir;
        this.requestedPlanUrls = new ArrayList<>();
        this.requestedFullChangesUrls = new ArrayList<>();
        this.requestedRecentChangesUrls = new ArrayList<>();
    }

    public void addAvailableUrl(String url) {
        if (this.availableUrls == null) {
            availableUrls = new HashSet<>();
        }
        this.availableUrls.add(url);
    }

    @Override
    public String executeUrl( //
            String httpMethod, //
            String url, //
            Properties httpHeaders, //
            @Nullable InputStream content, //
            @Nullable String contentType, //
            int timeout) throws IOException {
        final Matcher planMatcher = PLAN_URL_PATTERN.matcher(url);
        if (planMatcher.matches()) {
            requestedPlanUrls.add(url);
            return processRequest(url, planMatcher, this::getPlanData);
        }

        final Matcher fullChangesMatcher = FULL_CHANGES_URL_PATTERN.matcher(url);
        if (fullChangesMatcher.matches()) {
            requestedFullChangesUrls.add(url);
            return processRequest(url, fullChangesMatcher, this::getFullChanges);
        }

        final Matcher recentChangesMatcher = RECENT_CHANGES_URL_PATTERN.matcher(url);
        if (recentChangesMatcher.matches()) {
            requestedRecentChangesUrls.add(url);
            return processRequest(url, recentChangesMatcher, this::getRecentChanges);
        }
        return "";
    }

    private String processRequest(String url, Matcher matcher, Function<Matcher, String> responseSupplier) {
        if (availableUrls != null && !availableUrls.contains(url)) {
            return "";
        } else {
            return responseSupplier.apply(matcher);
        }
    }

    private String getPlanData(final Matcher planMatcher) {
        final String evaNo = planMatcher.group(1);
        final String day = planMatcher.group(2);
        final String hour = planMatcher.group(3);

        final File responseFile = new File(this.testdataDir, "plan/" + evaNo + "/" + day + "/" + hour + ".xml");
        return serveFileContentIfExists(responseFile);
    }

    private String serveFileContentIfExists(File responseFile) {
        if (!responseFile.exists()) {
            return "";
        }

        try {
            return Files.readString(responseFile.toPath());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private String getRecentChanges(Matcher recentChangesMatcher) {
        final String evaNo = recentChangesMatcher.group(1);
        File responseFile = new File(this.testdataDir, "rchg/" + evaNo + ".xml");
        return serveFileContentIfExists(responseFile);
    }

    private String getFullChanges(Matcher fullChangesMatcher) {
        final String evaNo = fullChangesMatcher.group(1);
        File responseFile = new File(this.testdataDir, "fchg/" + evaNo + ".xml");
        return serveFileContentIfExists(responseFile);
    }

    public List<String> getRequestedPlanUrls() {
        return requestedPlanUrls;
    }

    public List<String> getRequestedFullChangesUrls() {
        return requestedFullChangesUrls;
    }

    public List<String> getRequestedRecentChangesUrls() {
        return requestedRecentChangesUrls;
    }
}
