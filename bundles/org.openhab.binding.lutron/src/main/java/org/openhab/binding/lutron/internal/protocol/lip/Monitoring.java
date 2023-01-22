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
package org.openhab.binding.lutron.internal.protocol.lip;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Monitoring} class defines constants for LIP Monitoring types
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class Monitoring {
    // Monitoring Actions
    public static final Integer ACTION_ENABLE = 1;
    public static final Integer ACTION_DISABLE = 2;

    // Monitoring Types
    public static final Integer DIAG = 1;
    public static final Integer EVENT = 2;
    public static final Integer BUTTON = 3;
    public static final Integer LED = 4;
    public static final Integer ZONE = 5;
    public static final Integer OCCUPANCY = 6;
    public static final Integer PHOTOSENSOR = 7;
    public static final Integer SCENE = 8;
    public static final Integer TIMECLOCK = 9;
    public static final Integer SYSVAR = 10;
    public static final Integer REPLY = 11;
    public static final Integer PROMPT = 12;
    public static final Integer DEVICE = 14;
    public static final Integer ADDRESS = 15;
    public static final Integer SEQUENCE = 16;
    public static final Integer HVAC = 17;
    public static final Integer MODE = 18;
    public static final Integer PRESET = 19;
    public static final Integer L1RUNTIME = 20;
    public static final Integer L2RUNTIME = 21;
    public static final Integer DIAGERROR = 22;
    public static final Integer SHADEGRP = 23;
    public static final Integer PARTITION = 24;
    public static final Integer SYSTEM = 25;
    public static final Integer SENSORGROUP = 26;
    public static final Integer TEMPSENSOR = 27;
    public static final Integer ALL = 255;

    /** Set of monitoring types which must be enabled */
    public static final Set<Integer> REQUIRED_SET = Set.of(BUTTON, LED, ZONE, OCCUPANCY, SCENE, TIMECLOCK, REPLY, HVAC,
            MODE);
}
