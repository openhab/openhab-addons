/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    public static final String DIAGNOSTIC_CHANNEL = "diagnostic";
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

    public static final Map<String, Map<Integer, String>> LOGO_STATES = Map.ofEntries(
            Map.entry(LOGO_0BA7, LOGO_STATES_0BA7), // Possible diagnostic states for LOGO! 7
            Map.entry(LOGO_0BA8, LOGO_STATES_0BA8) // Possible diagnostic states for LOGO! 8
    );

    public record Layout(int address, int length) {
    }

    public static final Map<String, Layout> LOGO_CHANNELS = Map.ofEntries(
            Map.entry(DIAGNOSTIC_CHANNEL, new Layout(984, 1)), // Diagnostic starts at 984 for 1 byte
            // RTC starts at 985 for 6 bytes: year month day hour minute second
            Map.entry(RTC_CHANNEL, new Layout(985, 6)), // "Keep the line break" comment
            Map.entry(DAY_OF_WEEK_CHANNEL, new Layout(998, 1)) // Day of week starts at 998 for 1 byte
    );

    public static final Map<Integer, String> DAY_OF_WEEK = Map.ofEntries(
            // LOGO! start the week on sunday coded as decimal number one
            Map.entry(1, "SUNDAY"), Map.entry(2, "MONDAY"), // "Keep the line break" comment
            Map.entry(3, "TUESDAY"), Map.entry(4, "WEDNESDAY"), // "Keep the line break" comment
            Map.entry(5, "THURSDAY"), Map.entry(6, "FRIDAY"), // "Keep the line break" comment
            Map.entry(7, "SATURDAY") // LOGO! ends the week on saturday
    );

    private static final Map<String, Layout> LOGO_MEMORY_0BA7 = Map.ofEntries(
            Map.entry(MEMORY_BYTE, new Layout(0, 850)), // Virtual Memory starts at 0 for 850 bytes
            Map.entry(MEMORY_DWORD, new Layout(0, 850)), // Virtual Memory starts at 0 for 850 bytes -> 212 dwords
            Map.entry(MEMORY_WORD, new Layout(0, 850)), // Virtual Memory starts at 0 for 850 bytes -> 425 words
            Map.entry(I_DIGITAL, new Layout(923, 3)), // Digital inputs starts at 923 for 3 bytes
            Map.entry(Q_DIGITAL, new Layout(942, 2)), // Digital outputs starts at 942 for 2 bytes
            Map.entry(M_DIGITAL, new Layout(948, 4)), // Digital markers starts at 948 for 4 bytes
            Map.entry(I_ANALOG, new Layout(926, 16)), // Analog inputs starts at 926 for 16 bytes -> 8 words
            Map.entry(Q_ANALOG, new Layout(944, 4)), // Analog outputs starts at 944 for 4 bytes -> 2 words
            Map.entry(M_ANALOG, new Layout(952, 32)), // Analog markers starts at 952 for 32 bytes -> 16 words
            Map.entry(MEMORY_SIZE, new Layout(0, 984))// Size of memory block for LOGO! 7 in bytes
    );

    private static final Map<String, Layout> LOGO_MEMORY_0BA8 = Map.ofEntries(
            Map.entry(MEMORY_BYTE, new Layout(0, 850)), // Virtual Memory starts at 0 for 850 bytes
            Map.entry(MEMORY_DWORD, new Layout(0, 850)), // Virtual Memory starts at 0 for 850 bytes -> 212 dwords
            Map.entry(MEMORY_WORD, new Layout(0, 850)), // Virtual Memory starts at 0 for 850 bytes -> 425 words
            Map.entry(I_DIGITAL, new Layout(1024, 8)), // Digital inputs starts at 1024 for 8 bytes
            Map.entry(Q_DIGITAL, new Layout(1064, 8)), // Digital outputs starts at 1064 for 8 bytes
            Map.entry(M_DIGITAL, new Layout(1104, 14)), // Digital markers starts at 1104 for 14 bytes
            Map.entry(I_ANALOG, new Layout(1032, 32)), // Analog inputs starts at 1032 for 32 bytes -> 16 words
            Map.entry(Q_ANALOG, new Layout(1072, 32)), // Analog outputs starts at 1072 for 32 bytes -> 16 words
            Map.entry(M_ANALOG, new Layout(1118, 128)), // Analog markers starts at 1118 for 128 bytes -> 64 words
            Map.entry(NI_DIGITAL, new Layout(1246, 16)), // Network inputs starts at 1246 for 16 bytes
            Map.entry(NI_ANALOG, new Layout(1262, 128)), // Network analog inputs starts at 1262 for 128 bytes -> 64
                                                         // words
            Map.entry(NQ_DIGITAL, new Layout(1390, 16)), // Network outputs starts at 1390 for 16 bytes
            Map.entry(NQ_ANALOG, new Layout(1406, 64)), // Network analog inputs starts at 1406 for 64 bytes -> 32 words
            Map.entry(MEMORY_SIZE, new Layout(0, 1470)) // Size of memory block for LOGO! 8 in bytes
    );

    public static final Map<String, Map<String, Layout>> LOGO_MEMORY_BLOCK = Map.ofEntries(
            Map.entry(LOGO_0BA7, LOGO_MEMORY_0BA7), // Possible blocks for LOGO! 7
            Map.entry(LOGO_0BA8, LOGO_MEMORY_0BA8) // Possible blocks for LOGO! 8
    );
}
