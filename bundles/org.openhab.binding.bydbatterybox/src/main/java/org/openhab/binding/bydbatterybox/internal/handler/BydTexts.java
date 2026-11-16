/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;

/**
 * Turns bitmaps and event log entries into human-readable, translatable texts.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BydTexts {

    /** Looks up a translated text; returns {@code defaultText} if there is no translation. */
    @FunctionalInterface
    interface TextSource {
        String get(String key, String defaultText, Object... arguments);
    }

    /** System error bits of the status block. */
    private static final String[] ERROR_BITS = { "High temperature while charging", "Low temperature while charging",
            "Overcurrent while discharging", "Overcurrent while charging", "Main circuit failure",
            "Short circuit alarm", "Cell imbalance", "Current sensor failure", "Battery overvoltage",
            "Battery undervoltage", "Cell overvoltage", "Cell undervoltage", "Voltage sensor failure",
            "Temperature sensor failure", "High temperature while discharging", "Low temperature while discharging" };

    /** Tower warning bits of the measurement buffer. */
    private static final String[] WARNING_BITS = { "Battery overvoltage", "Battery undervoltage", "Cell overvoltage",
            "Cell undervoltage", "Cell imbalance", "High temperature while charging", "Low temperature while charging",
            "High temperature while discharging", "Low temperature while discharging", "Overcurrent while charging",
            "Overcurrent while discharging", "Hardware overcurrent while charging", "Short circuit",
            "Inverse connection", "Interlock switch abnormal", "Air switch abnormal" };

    /** Error enumeration of BMU "events record" entries. */
    private static final String[] BMU_ERRORS = { "Total voltage too high", "Total voltage too low",
            "Cell voltage too high", "Cell voltage too low", "Voltage sensor fault", "Temperature sensor fault",
            "Cell discharging temperature too high", "Cell discharging temperature too low",
            "Cell charging temperature too high", "Cell charging temperature too low", "Discharging overcurrent",
            "Charging overcurrent", "Major loop fault", "Short circuit warning", "Battery imbalance",
            "Current sensor fault" };

    private static final String[] SYSTEM_STATES = { "Standby", "Inactive", "Black start", "Active", "Fault", "Updating",
            "Shutdown", "Pre-charge", "Battery check", "Address assignment", "Loading parameters", "Initialisation" };

    private static final int EVENT_EVENTS_RECORD = 2;
    private static final int EVENT_SYSTEM_STATUS_CHANGED = 32;
    /** BMS entries whose data use the common layout (warnings, faults, switches, SoC, voltages, current, cells). */
    private static final Set<Integer> BMS_STANDARD_LAYOUT = Set.of(2, 3, 4, 5, 6, 7, 8, 13, 14, 16, 19);

    private final TextSource source;

    BydTexts(TextSource source) {
        this.source = source;
    }

    String systemErrors(int bits) {
        return bitTexts("error.bit.", ERROR_BITS, bits);
    }

    String towerWarnings(int bits) {
        return bitTexts("warning.bit.", WARNING_BITS, bits);
    }

    String eventSource(int unit) {
        return unit == EventRecord.UNIT_BMU ? source.get("event.source.bmu", "BMU")
                : source.get("event.source.bms", "BMS {0}", unit);
    }

    String event(EventRecord record) {
        String text = source.get("event.code." + record.getCode(), "Event {0}", record.getCode());
        String details = eventDetails(record);
        return details.isEmpty() ? eventSource(record.getUnit()) + ": " + text
                : eventSource(record.getUnit()) + ": " + text + " (" + details + ")";
    }

    private String eventDetails(EventRecord record) {
        int code = record.getCode();
        if (record.isBmu()) {
            if (code == EVENT_SYSTEM_STATUS_CHANGED) {
                return systemState(record.u8(1)) + " → " + systemState(record.u8(0));
            }
            if (code == EVENT_EVENTS_RECORD) {
                String change = record.u8(0) == 0 ? source.get("event.disappeared", "disappeared")
                        : source.get("event.appeared", "appeared");
                int error = record.u8(1);
                return error < BMU_ERRORS.length ? source.get("bmu.error." + error, BMU_ERRORS[error]) + ", " + change
                        : change;
            }
            return "";
        }
        if (BMS_STANDARD_LAYOUT.contains(code)) {
            // the BMS records use little-endian values
            BigDecimal voltage = BigDecimal.valueOf(record.u16LittleEndian(11), 1);
            BigDecimal current = BigDecimal.valueOf((short) record.u16LittleEndian(15), 1);
            return source.get("event.details.bms", "SoC {0} %, {1} V, {2} A", String.valueOf(record.u8(9)),
                    voltage.toPlainString(), current.toPlainString());
        }
        return "";
    }

    private String systemState(int state) {
        return state < SYSTEM_STATES.length ? source.get("system.state." + state, SYSTEM_STATES[state])
                : source.get("system.state.unknown", "State {0}", state);
    }

    private String bitTexts(String keyPrefix, String[] defaults, int bits) {
        List<String> texts = new ArrayList<>();
        for (int bit = 0; bit < defaults.length; bit++) {
            if ((bits & (1 << bit)) != 0) {
                texts.add(source.get(keyPrefix + bit, defaults[bit]));
            }
        }
        return texts.isEmpty() ? source.get("text.none", "None") : String.join(", ", texts);
    }
}
