/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedtest.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SpeedTestBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SpeedTestBindingConstants {

    public static final String BINDING_ID = "speedtest";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SPEEDTEST_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "test");

    // List of all channels
    public static final String CHANNEL_TEST_ISRUNNING = "isRunning";
    public static final String CHANNEL_TEST_PROGRESS = "progress";
    public static final String CHANNEL_RATE_UP = "rateUp";
    public static final String CHANNEL_RATE_DOWN = "rateDown";
    public static final String CHANNEL_TEST_START = "testStart";
    public static final String CHANNEL_TEST_END = "testEnd";
}
