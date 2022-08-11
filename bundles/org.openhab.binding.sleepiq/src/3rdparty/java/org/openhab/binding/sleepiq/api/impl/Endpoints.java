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
