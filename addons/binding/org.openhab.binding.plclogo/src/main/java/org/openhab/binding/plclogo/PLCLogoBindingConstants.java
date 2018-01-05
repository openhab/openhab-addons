/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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

    // List of all channels
    public static final String STATE_CHANNEL = "state";
    public static final String OBSERVE_CHANNEL = "observed";
    public static final String VALUE_CHANNEL = "value";
    public static final String RTC_CHANNEL = "rtc";

    // List of all channel properties
    public static final String BLOCK_PROPERTY = "block";

    // List of all item types
    public static final String ANALOG_ITEM = "Number";
    public static final String DATE_TIME_ITEM = "DateTime";
    public static final String DIGITAL_INPUT_ITEM = "Contact";
    public static final String DIGITAL_OUTPUT_ITEM = "Switch";

    // LOGO! family definitions
    public static final String LOGO_0BA7 = "0BA7";
    public static final String LOGO_0BA8 = "0BA8";

    // LOGO! diagnostics memory
    public static final Integer LOGO_STATE = 984; // Diagnostics

    private static final Map<Integer, @Nullable String> LOGO_STATES_0BA7;
    static {
        Map<Integer, String> buffer = new HashMap<>();
        // buffer.put(???, "Network access errors"); // Netzwerkzugriffsfehler
        // buffer.put(???, "Expansion module bus errors"); // Erweiterungsmodul-Busfehler
        // buffer.put(???, "SD card read/write errors"); // Fehler beim Lesen oder Schreiben der SD-Karte
        // buffer.put(???, "SD card write protection"); // Schreibschutz der SD-Karte
        LOGO_STATES_0BA7 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<Integer, @Nullable String> LOGO_STATES_0BA8;
    static {
        Map<Integer, String> buffer = new HashMap<>();
        // buffer.put(???, "Ethernet link errors"); // Netzwerk Verbindungsfehler
        // buffer.put(???, "Expansion module changed"); // Ausgetauschtes Erweiterungsmodul
        // buffer.put(???, "SD card read/write errors"); // Fehler beim Lesen oder Schreiben der SD-Karte
        buffer.put(8, "SD card does not exist"); // "SD-Karte nicht vorhanden"
        // buffer.put(???, "SD card is full"); // SD-Karte voll
        LOGO_STATES_0BA8 = Collections.unmodifiableMap(buffer);
    }

    public static final Map<String, Map<Integer, @Nullable String>> LOGO_STATES;
    static {
        Map<String, Map<Integer, @Nullable String>> buffer = new HashMap<>();
        buffer.put(LOGO_0BA7, LOGO_STATES_0BA7);
        buffer.put(LOGO_0BA8, LOGO_STATES_0BA8);
        LOGO_STATES = Collections.unmodifiableMap(buffer);
    }

    // LOGO! RTC memory
    public static final Integer LOGO_DATE = 985; // RTC date for 3 bytes: year month day
    public static final Integer LOGO_TIME = 988; // RTC time for 3 bytes: hour minute second

    public static final class Layout {
        public final int address;
        public final int length;

        public Layout(int address, int length) {
            this.address = address;
            this.length = length;
        }
    }

    private static final Map<String, @Nullable Layout> LOGO_MEMORY_0BA7;
    static {
        Map<String, @Nullable Layout> buffer = new HashMap<>();
        buffer.put("VB", new Layout(0, 850));
        buffer.put("VD", new Layout(0, 850));
        buffer.put("VW", new Layout(0, 850));
        buffer.put("I", new Layout(923, 3)); // Digital inputs starts at 923 for 3 bytes
        buffer.put("Q", new Layout(942, 2)); // Digital outputs starts at 942 for 2 bytes
        buffer.put("M", new Layout(948, 4)); // Digital markers starts at 948 for 4 bytes
        buffer.put("AI", new Layout(926, 16)); // Analog inputs starts at 926 for 16 bytes -> 8 words
        buffer.put("AQ", new Layout(944, 4)); // Analog outputs starts at 944 for 4 bytes -> 2 words
        buffer.put("AM", new Layout(952, 32)); // Analog markers starts at 952 for 32 bytes -> 16 words
        buffer.put("SIZE", new Layout(0, 984)); // Size of memory block for LOGO! 7
        LOGO_MEMORY_0BA7 = Collections.unmodifiableMap(buffer);
    }

    private static final Map<String, @Nullable Layout> LOGO_MEMORY_0BA8;
    static {
        Map<String, @Nullable Layout> buffer = new HashMap<>();
        buffer.put("VB", new Layout(0, 850));
        buffer.put("VD", new Layout(0, 850));
        buffer.put("VW", new Layout(0, 850));
        buffer.put("I", new Layout(1024, 8)); // Digital inputs starts at 1024 for 8 bytes
        buffer.put("Q", new Layout(1064, 8)); // Digital outputs starts at 1064 for 8 bytes
        buffer.put("M", new Layout(1104, 14)); // Digital markers starts at 1104 for 14 bytes
        buffer.put("AI", new Layout(1032, 32)); // Analog inputs starts at 1032 for 32 bytes -> 16 words
        buffer.put("AQ", new Layout(1072, 32)); // Analog outputs starts at 1072 for 32 bytes -> 16 words
        buffer.put("AM", new Layout(1118, 128)); // Analog markers starts at 1118 for 128 bytes -> 64 words
        buffer.put("NI", new Layout(1246, 16)); // Network inputs starts at 1246 for 16 bytes
        buffer.put("NAI", new Layout(1262, 128)); // Network analog inputs starts at 1262 for 128 bytes -> 64 words
        buffer.put("NQ", new Layout(1390, 16)); // Network outputs starts at 1390 for 16 bytes
        buffer.put("NAQ", new Layout(1406, 64)); // Network analog inputs starts at 1406 for 64 bytes -> 32 words
        buffer.put("SIZE", new Layout(0, 1470)); // Size of memory block for LOGO! 8
        LOGO_MEMORY_0BA8 = Collections.unmodifiableMap(buffer);
    }

    public static final Map<String, Map<String, @Nullable Layout>> LOGO_MEMORY_BLOCK;
    static {
        Map<String, Map<String, @Nullable Layout>> buffer = new HashMap<>();
        buffer.put(LOGO_0BA7, LOGO_MEMORY_0BA7);
        buffer.put(LOGO_0BA8, LOGO_MEMORY_0BA8);
        LOGO_MEMORY_BLOCK = Collections.unmodifiableMap(buffer);
    }

}
