/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.keba;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KebaBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
public class KebaBindingConstants {

    public static final String BINDING_ID = "keba";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_KECONTACTP20 = new ThingTypeUID(BINDING_ID, "kecontactp20");

    // List of all Channel ids
    public static final String CHANNEL_MODEL = "model";
    public static final String CHANNEL_FIRMWARE = "firmware";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_WALLBOX = "wallbox";
    public static final String CHANNEL_VEHICLE = "vehicle";
    public static final String CHANNEL_PLUG_LOCKED = "locked";
    public static final String CHANNEL_ENABLED = "enabled";
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

    public enum KebaType {
        P20,
        P30
    };

    public enum KebaSeries {

        E('0'),
        B('1'),
        C('2', '3'),
        X('A', 'B', 'C', 'D');

        private final List<Character> things = new ArrayList<Character>();

        KebaSeries(char... e) {
            Character[] cArray = ArrayUtils.toObject(e);
            for (char c : cArray) {
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

            throw new IllegalArgumentException("Not a valid series");
        }
    };

    public enum KebaFirmware {
        V201M21("2.01m21"),
        V22A1("2.2a1"),
        V23A2("2.3a2"),
        V23A3("2.3a3"),
        V25A3("2.5a3"),
        V3042A1("3.04.2a1"),
        V3062A5("3.06.2a5"),
        V3071A1("3.07.1a1");

        private final String id;

        private KebaFirmware(final String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }

        public static KebaFirmware getFirmware(String text) throws IllegalArgumentException {
            for (KebaFirmware c : KebaFirmware.values()) {
                if (text.contains(c.id)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not a valid firmware");
        }

    };
}
