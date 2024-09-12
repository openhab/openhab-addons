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
package org.openhab.binding.keba.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KebaBindingConstants} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class KebaBindingConstants {

    public static final String BINDING_ID = "keba";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_KECONTACTP20 = new ThingTypeUID(BINDING_ID, "kecontact");

    // List of all Channel ids
    public static final String CHANNEL_MODEL = "model";
    public static final String CHANNEL_FIRMWARE = "firmware";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_ERROR_1 = "error1";
    public static final String CHANNEL_ERROR_2 = "error2";
    public static final String CHANNEL_WALLBOX = "wallbox";
    public static final String CHANNEL_VEHICLE = "vehicle";
    public static final String CHANNEL_PLUG_LOCKED = "locked";
    public static final String CHANNEL_ENABLED_SYSTEM = "enabledsystem";
    public static final String CHANNEL_ENABLED_USER = "enableduser";
    public static final String CHANNEL_PILOT_CURRENT = "maxpilotcurrent";
    public static final String CHANNEL_PILOT_PWM = "maxpilotcurrentdutycyle";
    public static final String CHANNEL_MAX_SYSTEM_CURRENT = "maxsystemcurrent";
    public static final String CHANNEL_MAX_PRESET_CURRENT_RANGE = "maxpresetcurrentrange";
    public static final String CHANNEL_MAX_PRESET_CURRENT = "maxpresetcurrent";
    public static final String CHANNEL_FAILSAFE_CURRENT = "failsafecurrent";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_OUTPUT = "output";
    public static final String CHANNEL_SERIAL = "serial";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_I1 = "I1";
    public static final String CHANNEL_I2 = "I2";
    public static final String CHANNEL_I3 = "I3";
    public static final String CHANNEL_U1 = "U1";
    public static final String CHANNEL_U2 = "U2";
    public static final String CHANNEL_U3 = "U3";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_POWER_FACTOR = "powerfactor";
    public static final String CHANNEL_SESSION_CONSUMPTION = "sessionconsumption";
    public static final String CHANNEL_TOTAL_CONSUMPTION = "totalconsumption";
    public static final String CHANNEL_DISPLAY = "display";
    public static final String CHANNEL_AUTHON = "authon";
    public static final String CHANNEL_AUTHREQ = "authreq";
    public static final String CHANNEL_SESSION_RFID_TAG = "sessionrfidtag";
    public static final String CHANNEL_SESSION_RFID_CLASS = "sessionrfidclass";
    public static final String CHANNEL_SESSION_SESSION_ID = "sessionid";
    public static final String CHANNEL_SETENERGY = "setenergylimit";
    public static final String CHANNEL_AUTHENTICATE = "authenticate";

    public enum KebaType {
        P20,
        P30
    }

    public enum KebaSeries {

        /*
         * Mapping derived from:
         * - https://www.keba.com/download/x/ea958eb797/kecontactp30_bden_web.pdf
         * - https://www.keba.com/file/downloads/e-mobility/KeContact_KCP20_30_ih_de.pdf
         * 'G' is still unclear
         */
        E('0'),
        B('1'),
        C('2', '3', 'A'), // '3' is P20 c-series + PLC
        // A('3'), // '3' is also P30 a-series - but P30 a-series doesn't support the required UDS protocol
        X('B', 'C', 'D', 'E', 'G', 'H', 'S', 'U');

        private final List<Character> things = new ArrayList<>();

        KebaSeries(char... e) {
            for (char c : e) {
                things.add(c);
            }
        }

        public boolean matchesSeries(char c) {
            return things.contains(c);
        }

        public static KebaSeries getSeries(char text) throws IllegalArgumentException {
            for (KebaSeries c : KebaSeries.values()) {
                if (c.matchesSeries(text)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not a valid series: '" + text + "'");
        }
    }
}
