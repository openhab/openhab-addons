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
package org.openhab.binding.plclogo.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PLCLogoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCLogoBindingConstants {

    public static final String BINDING_ID = "plclogo";

    // List of all thing type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_ANALOG = new ThingTypeUID(BINDING_ID, "analog");
    public static final ThingTypeUID THING_TYPE_MEMORY = new ThingTypeUID(BINDING_ID, "memory");
    public static final ThingTypeUID THING_TYPE_DIGITAL = new ThingTypeUID(BINDING_ID, "digital");
    public static final ThingTypeUID THING_TYPE_DATETIME = new ThingTypeUID(BINDING_ID, "datetime");
    public static final ThingTypeUID THING_TYPE_PULSE = new ThingTypeUID(BINDING_ID, "pulse");

    // Something goes wrong...
    public static final String NOT_SUPPORTED = "NOT SUPPORTED";

    // List of all channels
    public static final String STATE_CHANNEL = "state";
    public static final String OBSERVE_CHANNEL = "observed";
    public static final String VALUE_CHANNEL = "value";
    public static final String RTC_CHANNEL = "rtc";
    public static final String DAIGNOSTICS_CHANNEL = "diagnostic";
    public static final String DAY_OF_WEEK_CHANNEL = "weekday";

    // List of all channel properties
    public static final String BLOCK_PROPERTY = "block";

    // List of all item types
    public static final String ANALOG_ITEM = "Number";
    public static final String DATE_TIME_ITEM = "DateTime";
    public static final String DIGITAL_INPUT_ITEM = "Contact";
    public static final String DIGITAL_OUTPUT_ITEM = "Switch";
    public static final String INFORMATION_ITEM = "String";

    // LOGO! family definitions
    public static final String LOGO_0BA7 = "0BA7";
    public static final String LOGO_0BA8 = "0BA8";

    // LOGO! block definitions
    public static final String MEMORY_BYTE = "VB"; // Bit or Byte memory
    public static final String MEMORY_WORD = "VW"; // Word memory
    public static final String MEMORY_DWORD = "VD"; // DWord memory
    public static final String MEMORY_SIZE = "SIZE"; // Size of memory

    public static final String I_DIGITAL = "I"; // Physical digital input
    public static final String Q_DIGITAL = "Q"; // Physical digital output
    public static final String M_DIGITAL = "M"; // Program digital marker
    public static final String NI_DIGITAL = "NI"; // Network digital input
    public static final String NQ_DIGITAL = "NQ"; // Network digital output

    public static final String I_ANALOG = "AI"; // Physical analog input
    public static final String Q_ANALOG = "AQ"; // Physical analog output
    public static final String M_ANALOG = "AM"; // Program analog marker
    public static final String NI_ANALOG = "NAI"; // Network analog input
    public static final String NQ_ANALOG = "NAQ"; // Network analog output

    private static final Map<Integer, String> LOGO_STATES_0BA7;
    static {
        Map<Integer, String> buffer = new HashMap<>();
        // buffer.put(???, "Network access error"); // Netzwerkzugriffsfehler
        // buffer.put(???, "Expansion module bus error"); // Erweiterungsmodul-Busfehler
        // buffer.put(???, "SD card read/write error"); // Fehler beim Lesen oder Schreiben der SD-Karte
        // buffer.put(???, "SD card write protection"); // Schreibschutz der SD-Karte
        LOGO_STATES_0BA7 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<Integer, String> LOGO_STATES_0BA8;
    static {
        Map<Integer, String> buffer = new HashMap<>();
        buffer.put(1, "Ethernet link error"); // Netzwerk Verbindungsfehler
        buffer.put(2, "Expansion module changed"); // Ausgetauschtes Erweiterungsmodul
        buffer.put(4, "SD card read/write error"); // Fehler beim Lesen oder Schreiben der SD-Karte
        buffer.put(8, "SD Card does not exist"); // "SD-Karte nicht vorhanden"
        buffer.put(16, "SD Card is full"); // SD-Karte voll
        // buffer.put(???, "Network S7 Tcp Error"); //
        LOGO_STATES_0BA8 = Collections.unmodifiableMap(buffer);
    }

    public static final Map<String, Map<Integer, String>> LOGO_STATES;
    static {
        Map<String, Map<Integer, String>> buffer = new HashMap<>();
        buffer.put(LOGO_0BA7, LOGO_STATES_0BA7);
        buffer.put(LOGO_0BA8, LOGO_STATES_0BA8);
        LOGO_STATES = Collections.unmodifiableMap(buffer);
    }

    public static final class Layout {
        public final int address;
        public final int length;

        public Layout(int address, int length) {
            this.address = address;
            this.length = length;
        }
    }

    public static final Map<String, Layout> LOGO_CHANNELS;
    static {
        Map<String, Layout> buffer = new HashMap<>();
        buffer.put(DAIGNOSTICS_CHANNEL, new Layout(984, 1)); // Diagnostics starts at 984 for 1 byte
        buffer.put(RTC_CHANNEL, new Layout(985, 6)); // RTC starts at 985 for 6 bytes: year month day hour minute second
        buffer.put(DAY_OF_WEEK_CHANNEL, new Layout(998, 1)); // Diagnostics starts at 998 for 1 byte
        LOGO_CHANNELS = Collections.unmodifiableMap(buffer);
    }

    public static final Map<Integer, String> DAY_OF_WEEK;
    static {
        Map<Integer, String> buffer = new HashMap<>();
        buffer.put(1, "SUNDAY");
        buffer.put(2, "MONDAY");
        buffer.put(3, "TUEsDAY");
        buffer.put(4, "WEDNESDAY");
        buffer.put(5, "THURSDAY");
        buffer.put(6, "FRIDAY");
        buffer.put(7, "SATURDAY");
        DAY_OF_WEEK = Collections.unmodifiableMap(buffer);
    }

    private static final Map<String, Layout> LOGO_MEMORY_0BA7;
    static {
        Map<String, Layout> buffer = new HashMap<>();
        buffer.put(MEMORY_BYTE, new Layout(0, 850));
        buffer.put(MEMORY_DWORD, new Layout(0, 850));
        buffer.put(MEMORY_WORD, new Layout(0, 850));
        buffer.put(I_DIGITAL, new Layout(923, 3)); // Digital inputs starts at 923 for 3 bytes
        buffer.put(Q_DIGITAL, new Layout(942, 2)); // Digital outputs starts at 942 for 2 bytes
        buffer.put(M_DIGITAL, new Layout(948, 4)); // Digital markers starts at 948 for 4 bytes
        buffer.put(I_ANALOG, new Layout(926, 16)); // Analog inputs starts at 926 for 16 bytes -> 8 words
        buffer.put(Q_ANALOG, new Layout(944, 4)); // Analog outputs starts at 944 for 4 bytes -> 2 words
        buffer.put(M_ANALOG, new Layout(952, 32)); // Analog markers starts at 952 for 32 bytes -> 16 words
        buffer.put(MEMORY_SIZE, new Layout(0, 984)); // Size of memory block for LOGO! 7
        LOGO_MEMORY_0BA7 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<String, Layout> LOGO_MEMORY_0BA8;
    static {
        Map<String, Layout> buffer = new HashMap<>();
        buffer.put(MEMORY_BYTE, new Layout(0, 850));
        buffer.put(MEMORY_DWORD, new Layout(0, 850));
        buffer.put(MEMORY_WORD, new Layout(0, 850));
        buffer.put(I_DIGITAL, new Layout(1024, 8)); // Digital inputs starts at 1024 for 8 bytes
        buffer.put(Q_DIGITAL, new Layout(1064, 8)); // Digital outputs starts at 1064 for 8 bytes
        buffer.put(M_DIGITAL, new Layout(1104, 14)); // Digital markers starts at 1104 for 14 bytes
        buffer.put(I_ANALOG, new Layout(1032, 32)); // Analog inputs starts at 1032 for 32 bytes -> 16 words
        buffer.put(Q_ANALOG, new Layout(1072, 32)); // Analog outputs starts at 1072 for 32 bytes -> 16 words
        buffer.put(M_ANALOG, new Layout(1118, 128)); // Analog markers starts at 1118 for 128 bytes -> 64 words
        buffer.put(NI_DIGITAL, new Layout(1246, 16)); // Network inputs starts at 1246 for 16 bytes
        buffer.put(NI_ANALOG, new Layout(1262, 128)); // Network analog inputs starts at 1262 for 128 bytes -> 64 words
        buffer.put(NQ_DIGITAL, new Layout(1390, 16)); // Network outputs starts at 1390 for 16 bytes
        buffer.put(NQ_ANALOG, new Layout(1406, 64)); // Network analog inputs starts at 1406 for 64 bytes -> 32 words
        buffer.put(MEMORY_SIZE, new Layout(0, 1470)); // Size of memory block for LOGO! 8
        LOGO_MEMORY_0BA8 = Collections.unmodifiableMap(buffer);
    }

    public static final Map<String, Map<String, Layout>> LOGO_MEMORY_BLOCK;
    static {
        Map<String, Map<String, Layout>> buffer = new HashMap<>();
        buffer.put(LOGO_0BA7, LOGO_MEMORY_0BA7);
        buffer.put(LOGO_0BA8, LOGO_MEMORY_0BA8);
        LOGO_MEMORY_BLOCK = Collections.unmodifiableMap(buffer);
    }
}
