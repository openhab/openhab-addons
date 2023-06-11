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

public class Endpoints
{
    private static final String LOGIN = "login";
    private static final String BED = "bed";
    private static final String SLEEPER = "sleeper";
    private static final String FAMILY_STATUS = "familyStatus";
    private static final String PAUSE_MODE = "pauseMode";

    public static String login()
    {
        return LOGIN;
    }

    public static String bed()
    {
        return BED;
    }

    public static String sleeper()
    {
        return SLEEPER;
    }

    public static String familyStatus()
    {
        return FAMILY_STATUS;
    }

    public static String pauseMode()
    {
        return PAUSE_MODE;
    }

    // @formatter:off
    private Endpoints() {}
    // @formatter:on
}
