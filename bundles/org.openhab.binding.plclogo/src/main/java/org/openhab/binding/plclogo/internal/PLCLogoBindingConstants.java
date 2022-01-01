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

    public static final class Layout {
        public final int address;
        public final int length;

        public Layout(int address, int length) {
            this.address = address;
            this.length = length;
        }
    }

    public static final Map<String, Layout> LOGO_CHANNELS = Map.ofEntries(
            // Diagnostics starts at 984 for 1 byte
            Map.entry(DIAGNOSTIC_CHANNEL, new Layout(984, 1)),
            // RTC starts at 985 for 6 bytes: year month day hour minute second
            Map.entry(RTC_CHANNEL, new Layout(985, 6)),
            // Diagnostics starts at 998 for 1 byte
            Map.entry(DAY_OF_WEEK_CHANNEL, new Layout(998, 1)));

    public static final Map<Integer, String> DAY_OF_WEEK = Map.ofEntries(
            // Logo begin the week on sunday and start with index 1
            Map.entry(1, "SUNDAY"), Map.entry(2, "MONDAY"), Map.entry(3, "TUESDAY"), Map.entry(4, "WEDNESDAY"),
            Map.entry(5, "THURSDAY"), Map.entry(6, "FRIDAY"), Map.entry(7, "SATURDAY"));

    private static final Map<String, Layout> LOGO_MEMORY_0BA7 = Map.ofEntries(
            Map.entry(MEMORY_BYTE, new Layout(0, 850)), Map.entry(MEMORY_DWORD, new Layout(0, 850)),
            Map.entry(MEMORY_WORD, new Layout(0, 850)),
            // Digital inputs starts at 923 for 3 bytes
            Map.entry(I_DIGITAL, new Layout(923, 3)),
            // Digital outputs starts at 942 for 2 bytes
            Map.entry(Q_DIGITAL, new Layout(942, 2)),
            // Digital markers starts at 948 for 4 bytes
            Map.entry(M_DIGITAL, new Layout(948, 4)),
            // Analog inputs starts at 926 for 16 bytes -> 8 words
            Map.entry(I_ANALOG, new Layout(926, 16)),
            // Analog outputs starts at 944 for 4 bytes -> 2 words
            Map.entry(Q_ANALOG, new Layout(944, 4)),
            // Analog markers starts at 952 for 32 bytes -> 16 words
            Map.entry(M_ANALOG, new Layout(952, 32)),
            // Size of memory block for LOGO! 7
            Map.entry(MEMORY_SIZE, new Layout(0, 984)));

    private static final Map<String, Layout> LOGO_MEMORY_0BA8 = Map.ofEntries(
            Map.entry(MEMORY_BYTE, new Layout(0, 850)), Map.entry(MEMORY_DWORD, new Layout(0, 850)),
            Map.entry(MEMORY_WORD, new Layout(0, 850)),
            // Digital inputs starts at 1024 for 8 bytes
            Map.entry(I_DIGITAL, new Layout(1024, 8)),
            // Digital outputs starts at 1064 for 8 bytes
            Map.entry(Q_DIGITAL, new Layout(1064, 8)),
            // Digital markers starts at 1104 for 14 bytes
            Map.entry(M_DIGITAL, new Layout(1104, 14)),
            // Analog inputs starts at 1032 for 32 bytes -> 16 words
            Map.entry(I_ANALOG, new Layout(1032, 32)),
            // Analog outputs starts at 1072 for 32 bytes -> 16 words
            Map.entry(Q_ANALOG, new Layout(1072, 32)),
            // Analog markers starts at 1118 for 128 bytes -> 64 words
            Map.entry(M_ANALOG, new Layout(1118, 128)),
            // Network inputs starts at 1246 for 16 bytes
            Map.entry(NI_DIGITAL, new Layout(1246, 16)),
            // Network analog inputs starts at 1262 for 128 bytes -> 64 words
            Map.entry(NI_ANALOG, new Layout(1262, 128)),
            // Network outputs starts at 1390 for 16 bytes
            Map.entry(NQ_DIGITAL, new Layout(1390, 16)),
            // Network analog inputs starts at 1406 for 64 bytes -> 32 words
            Map.entry(NQ_ANALOG, new Layout(1406, 64)),
            // Size of memory block for LOGO! 8
            Map.entry(MEMORY_SIZE, new Layout(0, 1470)));

    public static final Map<String, Map<String, Layout>> LOGO_MEMORY_BLOCK = Map.ofEntries( //
            Map.entry(LOGO_0BA7, LOGO_MEMORY_0BA7), // Possible blocks for Logo7
            Map.entry(LOGO_0BA8, LOGO_MEMORY_0BA8) // Possible blocks for Logo8
    );
}
