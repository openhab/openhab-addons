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
package org.openhab.binding.energenie.internal;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnergenieBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EnergenieBindingConstants {

    private static final String BINDING_ID = "energenie";

    public static final int TCP_PORT = 5000;

    public static final int STATCRYP_LEN = 4;
    public static final int CTRLCRYP_LEN = 4;
    public static final int KEY_LEN = 8;
    public static final int TASK_LEN = 4;
    public static final int SOLUTION_LEN = 4;

    public static final String STATE_ON = "0x11";
    public static final String STATE_ON_NO_VOLTAGE = "0x12";
    public static final String STATE_OFF = "0x22";
    public static final String STATE_OFF_NO_VOLTAGE = "0x21";

    public static final String V21_STATE_ON = "0x41";
    public static final String V21_STATE_OFF = "0x82";

    public static final String WLAN_STATE_ON = "0x51";
    public static final String WLAN_STATE_OFF = "0x92";

    public static final byte SWITCH_ON = 0x01;
    public static final byte SWITCH_OFF = 0x02;
    public static final byte DONT_SWITCH = 0x04;

    public static final int SOCKET_COUNT = 4; // AC power sockets, not network ones

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PM2LAN = new ThingTypeUID(BINDING_ID, "pm2lan");
    public static final ThingTypeUID THING_TYPE_PMSLAN = new ThingTypeUID(BINDING_ID, "pmslan");
    public static final ThingTypeUID THING_TYPE_PMS2LAN = new ThingTypeUID(BINDING_ID, "pms2lan");
    public static final ThingTypeUID THING_TYPE_PMSWLAN = new ThingTypeUID(BINDING_ID, "pmswlan");
    public static final ThingTypeUID THING_TYPE_PWMLAN = new ThingTypeUID(BINDING_ID, "pwmlan");

    // List of all Channel ids
    public static final Pattern CHANNEL_SOCKET = Pattern.compile("socket(\\d)");

    public static final String VOLTAGE = "voltage";
    public static final String CURRENT = "current";
    public static final String POWER = "power";
    public static final String ENERGY = "energy";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_PM2LAN, THING_TYPE_PMSLAN, THING_TYPE_PMS2LAN, THING_TYPE_PMSLAN, THING_TYPE_PWMLAN)
                    .collect(Collectors.toSet()));
}
