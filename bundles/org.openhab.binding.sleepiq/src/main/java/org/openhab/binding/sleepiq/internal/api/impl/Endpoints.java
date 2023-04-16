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
package org.openhab.binding.sleepiq.internal.api.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Endpoints} class contains all endpoints for the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class Endpoints {
    private static final String LOGIN = "/rest/login";
    private static final String BED = "/rest/bed";
    private static final String SLEEPER = "/rest/sleeper";
    private static final String FAMILY_STATUS = "/rest/bed/familyStatus";
    private static final String PAUSE_MODE = "/rest/bed/%s/pauseMode";
    private static final String SLEEP_DATA = "/rest/sleepData";
    private static final String SLEEP_NUMBER = "/rest/bed/%s/sleepNumber";
    private static final String FOUNDATION_STATUS = "/rest/bed/%s/foundation/status";
    private static final String FOUNDATION_FEATURES = "/rest/bed/%s/foundation/system";
    private static final String FOUNDATION_POSITION = "/rest/bed/%s/foundation/adjustment/micro";
    private static final String FOUNDATION_PRESET = "/rest/bed/%s/foundation/preset";
    private static final String FOUNDATION_OUTLET = "/rest/bed/%s/foundation/outlet";

    public static String login() {
        return LOGIN;
    }

    public static String bed() {
        return BED;
    }

    public static String sleeper() {
        return SLEEPER;
    }

    public static String familyStatus() {
        return FAMILY_STATUS;
    }

    public static String pauseMode(String bedId) {
        return String.format(PAUSE_MODE, bedId);
    }

    public static String sleepData() {
        return SLEEP_DATA;
    }

    public static String sleepNumber(String bedId) {
        return String.format(SLEEP_NUMBER, bedId);
    }

    public static String foundationStatus(String bedId) {
        return String.format(FOUNDATION_STATUS, bedId);
    }

    public static String foundationFeatures(String bedId) {
        return String.format(FOUNDATION_FEATURES, bedId);
    }

    public static String foundationPosition(String bedId) {
        return String.format(FOUNDATION_POSITION, bedId);
    }

    public static String foundationPreset(String bedId) {
        return String.format(FOUNDATION_PRESET, bedId);
    }

    public static String foundationOutlet(String bedId) {
        return String.format(FOUNDATION_OUTLET, bedId);
    }

    private Endpoints() {
    }
}
