/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.impl;

public class Endpoints {
    private static final String LOGIN = "/rest/login";
    private static final String BED = "/rest/bed";
    private static final String SLEEPER = "/rest/sleeper";
    private static final String FAMILY_STATUS = "/rest/bed/familyStatus";
    private static final String PAUSE_MODE = "/rest/bed/%s/pauseMode";
    private static final String SLEEP_DATA = "/rest/sleepData";
    private static final String SET_SLEEP_NUMBER = "/rest/bed/%s/sleepNumber";
    private static final String SET_PAUSE_MODE = "/rest/bed/%s/pauseMode";

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

    public static String setSleepNumber(String bedId) {
        return String.format(SET_SLEEP_NUMBER, bedId);
    }

    public static String setPauseMode(String bedId) {
        return String.format(SET_PAUSE_MODE, bedId);
    }

    private Endpoints() {
    }
}
