/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PLCLogoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
public class PLCLogoBindingConstants {

    public static final String BINDING_ID = "plclogo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_ANALOG = new ThingTypeUID(BINDING_ID, "analog");
    public static final ThingTypeUID THING_TYPE_DIGITAL = new ThingTypeUID(BINDING_ID, "digital");

    // List of all Channel Type UIDs
    public static final String ANALOG_CHANNEL_ID = "value";
    public static final String DIGITAL_CHANNEL_ID = "state";
    public static final String RTC_CHANNEL_ID = "rtc";

    // List of all Channel configurations
    public static final String INPUT_CHANNEL = "input";
    public static final String OUTPUT_CHANNEL = "output";
    public static final String ANALOG_DATE_CHANNEL = "date";
    public static final String ANALOG_TIME_CHANNEL = "time";
    public static final String ANALOG_NUMBER_CHANNEL = "number";

    // LOGO! family definitions
    public static final String LOGO_0BA7 = "0BA7";
    public static final String LOGO_0BA8 = "0BA8";

    // LOGO! diagnostics memory
    public static final Integer LOGO_STATE = 984; // Diagnostics

    @SuppressWarnings("serial")
    private static final Map<?, String> LOGO_STATES_0BA7 = Collections.unmodifiableMap(new TreeMap<Integer, String>() {
        {
            // @formatter:off
        /*
         *   put(   "VB", 0);
         * - Network access errors
         * - Expansion module bus errors
         * - SD card read/write errors
         * - SD card write protection
         *
         * - Netzwerkzugriffsfehler
         * - Erweiterungsmodul-Busfehler
         * - Fehler beim Lesen oder Schreiben der SD-Karte
         * - Schreibschutz der SD-Karte
         */
            // @formatter:on
        }
    });

    @SuppressWarnings("serial")
    private static final Map<?, String> LOGO_STATES_0BA8 = Collections.unmodifiableMap(new TreeMap<Integer, String>() {
        {
            // @formatter:off
            /*
            *   put(   "VB", 0);
            * - Ethernet link errors
            * - Expansion module changed
            * - SD card read/write errors
            */
            put(8, "SD card does not exist");
            /*
            * - SD card is full
            *
            * - Netzwerk Verbindungsfehler
            * - Ausgetauschtes Erweiterungsmodul
            * - Fehler beim Lesen oder Schreiben der SD-Karte
            *
            * put(8, "SD-Karte nicht vorhanden");
            *
            * - SD-Karte voll
            */
            // @formatter:on
        }
    });

    @SuppressWarnings("serial")
    public static final Map<?, Map<?, String>> LOGO_STATES = Collections
            .unmodifiableMap(new TreeMap<String, Map<?, String>>() {
                {
                    put(LOGO_0BA7, LOGO_STATES_0BA7);
                    put(LOGO_0BA8, LOGO_STATES_0BA8);
                }
            });

    // LOGO! RTC memory
    public static final Integer LOGO_DATE = 985; // RTC date for 3 bytes: year month day
    public static final Integer LOGO_TIME = 988; // RTC time for 3 bytes: hour minute second

    @SuppressWarnings("serial")
    private static final Map<?, Integer> LOGO_MEMORY_0BA7 = Collections.unmodifiableMap(new TreeMap<String, Integer>() {
        {
            // @formatter:off
            put(   "VB", 0);
            put(   "VD", 0);
            put(   "VW", 0);
            put(    "I", 923); // Digital inputs starts at 923 for 3 bytes
            put(    "Q", 942); // Digital outputs starts at 942 for 2 bytes
            put(    "M", 948); // Digital markers starts at 948 for 2 bytes
            put(   "AI", 926); // Analog inputs starts at 926 for 8 words
            put(   "AQ", 944); // Analog outputs starts at 944 for 2 words
            put(   "AM", 952); // Analog markers starts at 952 for 16 words
            put( "SIZE", 984); // Size of memory block for LOGO! 7
            // @formatter:on
        }
    });

    @SuppressWarnings("serial")
    private static final Map<?, Integer> LOGO_MEMORY_0BA8 = Collections.unmodifiableMap(new TreeMap<String, Integer>() {
        {
            // @formatter:off
            put(   "VB", 0);
            put(   "VD", 0);
            put(   "VW", 0);
            put(    "I", 1024); // Digital inputs starts at 1024 for 8 bytes
            put(    "Q", 1064); // Digital outputs starts at 1064 for 8 bytes
            put(    "M", 1104); // Digital markers starts at 1104 for 14 bytes
            put(   "AI", 1032); // Analog inputs starts at 1032 for 32 bytes -> 16 words
            put(   "AQ", 1072); // Analog outputs starts at 1072 for 32 bytes -> 16 words
            put(   "AM", 1118); // Analog markers starts at 1118 for 128 bytes(64 words)
            put(   "NI", 1246); // Network inputs starts at 1246 for 16 bytes
            put(  "NAI", 1262); // Network analog inputs starts at 1262 for 128 bytes(64 words)
            put(   "NQ", 1390); // Network outputs starts at 1390 for 16 bytes
            put(  "NAQ", 1406); // Network analog inputs starts at 1406 for 64 bytes(32 words)
            put( "SIZE", 1470); // Size of memory block for LOGO! 8
            // @formatter:on
        }
    });

    @SuppressWarnings("serial")
    public static final Map<?, Map<?, Integer>> LOGO_MEMORY_BLOCK = Collections
            .unmodifiableMap(new TreeMap<String, Map<?, Integer>>() {
                {
                    put(LOGO_0BA7, LOGO_MEMORY_0BA7);
                    put(LOGO_0BA8, LOGO_MEMORY_0BA8);
                }
            });

}
