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
package org.openhab.binding.powermax.internal.message;

import static java.util.Map.entry;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants used in Powermax messages
 *
 * @author Ron Isaacson - Initial contribution
 */
@NonNullByDefault
public class PowermaxMessageConstants {

    private PowermaxMessageConstants() {
    }

    // System events

    public static enum PowermaxSysEventType {
        NONE,
        ALARM,
        SILENT_ALARM,
        ALERT,
        PANIC,
        TROUBLE,
        RESTORE,
        GENERAL_RESTORE,
        CANCEL,
        RESET;
    }

    public static class PowermaxSysEvent {
        private final String name;
        private final PowermaxSysEventType type;
        private final int restoreFor;

        protected PowermaxSysEvent(String name, PowermaxSysEventType type, int restoreFor) {
            this.name = name;
            this.type = type;
            this.restoreFor = restoreFor;
        }

        protected static PowermaxSysEvent of(String name) {
            return new PowermaxSysEvent(name, PowermaxSysEventType.NONE, 0);
        }

        protected static PowermaxSysEvent of(String name, PowermaxSysEventType type) {
            return new PowermaxSysEvent(name, type, 0);
        }

        protected static PowermaxSysEvent of(String name, PowermaxSysEventType type, int restoreFor) {
            return new PowermaxSysEvent(name, type, restoreFor);
        }

        public PowermaxSysEventType getType() {
            return this.type;
        }

        public int getRestoreFor() {
            return this.restoreFor;
        }

        public boolean isAlarm() {
            return (this.type == PowermaxSysEventType.ALARM);
        }

        public boolean isSilentAlarm() {
            return (this.type == PowermaxSysEventType.SILENT_ALARM);
        }

        public boolean isAlert() {
            return (this.type == PowermaxSysEventType.ALERT);
        }

        public boolean isPanic() {
            return (this.type == PowermaxSysEventType.PANIC);
        }

        public boolean isTrouble() {
            return (this.type == PowermaxSysEventType.TROUBLE);
        }

        public boolean isRestore() {
            return (this.type == PowermaxSysEventType.RESTORE);
        }

        public boolean isGeneralRestore() {
            return (this.type == PowermaxSysEventType.GENERAL_RESTORE);
        }

        public boolean isCancel() {
            return (this.type == PowermaxSysEventType.CANCEL);
        }

        public boolean isReset() {
            return (this.type == PowermaxSysEventType.RESET);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Important note: in all of the following lists, each entry line ends
    // with an empty "//" comment. This is to prevent the "spotless" code
    // formatter from trying to wrap these lines in a way that makes them
    // much less readable.

    private static final PowermaxSysEvent UNKNOWN_SYSTEM_EVENT = PowermaxSysEvent.of("UNKNOWN");

    private static final Map<Integer, PowermaxSysEvent> SYSTEM_EVENTS = Map.ofEntries( //
            entry(0x00, PowermaxSysEvent.of("None")), //
            entry(0x01, PowermaxSysEvent.of("Interior Alarm", PowermaxSysEventType.ALARM)), //
            entry(0x02, PowermaxSysEvent.of("Perimeter Alarm", PowermaxSysEventType.ALARM)), //
            entry(0x03, PowermaxSysEvent.of("Delay Alarm", PowermaxSysEventType.ALARM)), //
            entry(0x04, PowermaxSysEvent.of("24h Silent Alarm", PowermaxSysEventType.SILENT_ALARM)), //
            entry(0x05, PowermaxSysEvent.of("24h Audible Alarm", PowermaxSysEventType.ALARM)), //
            entry(0x06, PowermaxSysEvent.of("Tamper", PowermaxSysEventType.ALERT)), //
            entry(0x07, PowermaxSysEvent.of("Control Panel Tamper", PowermaxSysEventType.ALARM)), //
            entry(0x08, PowermaxSysEvent.of("Tamper Alarm", PowermaxSysEventType.ALARM)), //
            entry(0x09, PowermaxSysEvent.of("Tamper Alarm", PowermaxSysEventType.TROUBLE)), //
            entry(0x0A, PowermaxSysEvent.of("Communication Loss", PowermaxSysEventType.ALARM)), //
            entry(0x0B, PowermaxSysEvent.of("Panic From KeyKeyfob", PowermaxSysEventType.PANIC)), //
            entry(0x0C, PowermaxSysEvent.of("Panic From Control Panel", PowermaxSysEventType.PANIC)), //
            entry(0x0D, PowermaxSysEvent.of("Duress", PowermaxSysEventType.SILENT_ALARM)), //
            entry(0x0E, PowermaxSysEvent.of("Confirm Alarm", PowermaxSysEventType.ALARM)), //
            entry(0x0F, PowermaxSysEvent.of("General Trouble", PowermaxSysEventType.TROUBLE)), //
            entry(0x10, PowermaxSysEvent.of("General Trouble Restore", PowermaxSysEventType.RESTORE, 0x0F)), //
            entry(0x11, PowermaxSysEvent.of("Interior Restore")), //
            entry(0x12, PowermaxSysEvent.of("Perimeter Restore")), //
            entry(0x13, PowermaxSysEvent.of("Delay Restore")), //
            entry(0x14, PowermaxSysEvent.of("24h Silent Restore")), //
            entry(0x15, PowermaxSysEvent.of("24h Audible Restore")), //
            entry(0x16, PowermaxSysEvent.of("Tamper Restore", PowermaxSysEventType.RESTORE, 0x06)), //
            entry(0x17, PowermaxSysEvent.of("Control Panel Tamper Restore")), //
            entry(0x18, PowermaxSysEvent.of("Tamper Restore")), //
            entry(0x19, PowermaxSysEvent.of("Tamper Restore")), //
            entry(0x1A, PowermaxSysEvent.of("Communication Restore")), //
            entry(0x1B, PowermaxSysEvent.of("Cancel Alarm", PowermaxSysEventType.CANCEL)), //
            entry(0x1C, PowermaxSysEvent.of("General Restore", PowermaxSysEventType.GENERAL_RESTORE)), //
            entry(0x1D, PowermaxSysEvent.of("Trouble Restore")), //
            entry(0x1E, PowermaxSysEvent.of("Not used")), //
            entry(0x1F, PowermaxSysEvent.of("Recent Close")), //
            entry(0x20, PowermaxSysEvent.of("Fire", PowermaxSysEventType.ALARM)), //
            entry(0x21, PowermaxSysEvent.of("Fire Restore")), //
            entry(0x22, PowermaxSysEvent.of("No Activity", PowermaxSysEventType.ALERT)), //
            entry(0x23, PowermaxSysEvent.of("Emergency", PowermaxSysEventType.ALERT)), //
            entry(0x24, PowermaxSysEvent.of("Not used")), //
            entry(0x25, PowermaxSysEvent.of("Disarm Latchkey", PowermaxSysEventType.ALERT)), //
            entry(0x26, PowermaxSysEvent.of("Panic Restore")), //
            entry(0x27, PowermaxSysEvent.of("Supervision (Inactive)", PowermaxSysEventType.TROUBLE)), //
            entry(0x28, PowermaxSysEvent.of("Supervision Restore (Active)", PowermaxSysEventType.RESTORE, 0x27)), //
            entry(0x29, PowermaxSysEvent.of("Low Battery", PowermaxSysEventType.TROUBLE)), //
            entry(0x2A, PowermaxSysEvent.of("Low Battery Restore", PowermaxSysEventType.RESTORE, 0x29)), //
            entry(0x2B, PowermaxSysEvent.of("AC Fail", PowermaxSysEventType.TROUBLE)), //
            entry(0x2C, PowermaxSysEvent.of("AC Restore", PowermaxSysEventType.RESTORE, 0x2B)), //
            entry(0x2D, PowermaxSysEvent.of("Control Panel Low Battery", PowermaxSysEventType.TROUBLE)), //
            entry(0x2E, PowermaxSysEvent.of("Control Panel Low Battery Restore", PowermaxSysEventType.RESTORE, 0x2D)), //
            entry(0x2F, PowermaxSysEvent.of("RF Jamming", PowermaxSysEventType.TROUBLE)), //
            entry(0x30, PowermaxSysEvent.of("RF Jamming Restore", PowermaxSysEventType.RESTORE, 0x2F)), //
            entry(0x31, PowermaxSysEvent.of("Communications Failure", PowermaxSysEventType.TROUBLE)), //
            entry(0x32, PowermaxSysEvent.of("Communications Restore", PowermaxSysEventType.RESTORE, 0x31)), //
            entry(0x33, PowermaxSysEvent.of("Telephone Line Failure", PowermaxSysEventType.TROUBLE)), //
            entry(0x34, PowermaxSysEvent.of("Telephone Line Restore", PowermaxSysEventType.RESTORE, 0x33)), //
            entry(0x35, PowermaxSysEvent.of("Auto Test")), //
            entry(0x36, PowermaxSysEvent.of("Fuse Failure", PowermaxSysEventType.TROUBLE)), //
            entry(0x37, PowermaxSysEvent.of("Fuse Restore", PowermaxSysEventType.RESTORE, 0x36)), //
            entry(0x38, PowermaxSysEvent.of("KeyKeyfob Low Battery", PowermaxSysEventType.TROUBLE)), //
            entry(0x39, PowermaxSysEvent.of("KeyKeyfob Low Battery Restore", PowermaxSysEventType.RESTORE, 0x38)), //
            entry(0x3A, PowermaxSysEvent.of("Engineer Reset")), //
            entry(0x3B, PowermaxSysEvent.of("Battery Disconnect")), //
            entry(0x3C, PowermaxSysEvent.of("1-Way Keypad Low Battery", PowermaxSysEventType.TROUBLE)), //
            entry(0x3D, PowermaxSysEvent.of("1-Way Keypad Low Battery Restore", PowermaxSysEventType.RESTORE, 0x3C)), //
            entry(0x3E, PowermaxSysEvent.of("1-Way Keypad Inactive", PowermaxSysEventType.TROUBLE)), //
            entry(0x3F, PowermaxSysEvent.of("1-Way Keypad Restore Active", PowermaxSysEventType.RESTORE, 0x3E)), //
            entry(0x40, PowermaxSysEvent.of("Low Battery")), //
            entry(0x41, PowermaxSysEvent.of("Clean Me", PowermaxSysEventType.TROUBLE)), //
            entry(0x42, PowermaxSysEvent.of("Fire Trouble", PowermaxSysEventType.TROUBLE)), //
            entry(0x43, PowermaxSysEvent.of("Low Battery", PowermaxSysEventType.TROUBLE)), //
            entry(0x44, PowermaxSysEvent.of("Battery Restore", PowermaxSysEventType.RESTORE, 0x43)), //
            entry(0x45, PowermaxSysEvent.of("AC Fail", PowermaxSysEventType.TROUBLE)), //
            entry(0x46, PowermaxSysEvent.of("AC Restore", PowermaxSysEventType.RESTORE, 0x45)), //
            entry(0x47, PowermaxSysEvent.of("Supervision (Inactive)", PowermaxSysEventType.TROUBLE)), //
            entry(0x48, PowermaxSysEvent.of("Supervision Restore (Active)", PowermaxSysEventType.RESTORE, 0x47)), //
            entry(0x49, PowermaxSysEvent.of("Gas Alert", PowermaxSysEventType.ALARM)), //
            entry(0x4A, PowermaxSysEvent.of("Gas Alert Restore")), //
            entry(0x4B, PowermaxSysEvent.of("Gas Trouble", PowermaxSysEventType.TROUBLE)), //
            entry(0x4C, PowermaxSysEvent.of("Gas Trouble Restore", PowermaxSysEventType.RESTORE, 0x4B)), //
            entry(0x4D, PowermaxSysEvent.of("Flood Alert", PowermaxSysEventType.ALARM)), //
            entry(0x4E, PowermaxSysEvent.of("Flood Alert Restore")), //
            entry(0x4F, PowermaxSysEvent.of("X-10 Trouble", PowermaxSysEventType.TROUBLE)), //
            entry(0x50, PowermaxSysEvent.of("X-10 Trouble Restore", PowermaxSysEventType.RESTORE, 0x4F)), //
            entry(0x51, PowermaxSysEvent.of("Arm Home")), //
            entry(0x52, PowermaxSysEvent.of("Arm Away")), //
            entry(0x53, PowermaxSysEvent.of("Quick Arm Home")), //
            entry(0x54, PowermaxSysEvent.of("Quick Arm Away")), //
            entry(0x55, PowermaxSysEvent.of("Disarm")), //
            entry(0x56, PowermaxSysEvent.of("Fail To Auto-Arm")), //
            entry(0x57, PowermaxSysEvent.of("Enter To Test Mode")), //
            entry(0x58, PowermaxSysEvent.of("Exit From Test Mode")), //
            entry(0x59, PowermaxSysEvent.of("Force Arm")), //
            entry(0x5A, PowermaxSysEvent.of("Auto Arm")), //
            entry(0x5B, PowermaxSysEvent.of("Instant Arm")), //
            entry(0x5C, PowermaxSysEvent.of("Bypass")), //
            entry(0x5D, PowermaxSysEvent.of("Fail To Arm")), //
            entry(0x5E, PowermaxSysEvent.of("Door Open")), //
            entry(0x5F, PowermaxSysEvent.of("Communication Established By Control Panel")), //
            entry(0x60, PowermaxSysEvent.of("System Reset", PowermaxSysEventType.RESET)), //
            entry(0x61, PowermaxSysEvent.of("Installer Programming")), //
            entry(0x62, PowermaxSysEvent.of("Wrong Password")), //
            entry(0x63, PowermaxSysEvent.of("Not Sys Event")), //
            entry(0x64, PowermaxSysEvent.of("Not Sys Event")), //
            entry(0x65, PowermaxSysEvent.of("Extreme Hot Alert")), //
            entry(0x66, PowermaxSysEvent.of("Extreme Hot Alert Restore")), //
            entry(0x67, PowermaxSysEvent.of("Freeze Alert")), //
            entry(0x68, PowermaxSysEvent.of("Freeze Alert Restore")), //
            entry(0x69, PowermaxSysEvent.of("Human Cold Alert")), //
            entry(0x6A, PowermaxSysEvent.of("Human Cold Alert Restore")), //
            entry(0x6B, PowermaxSysEvent.of("Human Hot Alert")), //
            entry(0x6C, PowermaxSysEvent.of("Human Hot Alert Restore")), //
            entry(0x6D, PowermaxSysEvent.of("Temperature Sensor Trouble")), //
            entry(0x6E, PowermaxSysEvent.of("Temperature Sensor Trouble Restore")), //
            entry(0x6F, PowermaxSysEvent.of("PIR Mask")), //
            entry(0x70, PowermaxSysEvent.of("PIR Mask Restore")), //
            entry(0x7B, PowermaxSysEvent.of("Alarmed")), //
            entry(0x7C, PowermaxSysEvent.of("Restore")), //
            entry(0x7D, PowermaxSysEvent.of("Alarmed")), //
            entry(0x7E, PowermaxSysEvent.of("Restore")), //
            entry(0x8E, PowermaxSysEvent.of("Exit Installer")), //
            entry(0x8F, PowermaxSysEvent.of("Enter Installer")) //
    );

    /**
     * System event lookup
     */
    public static PowermaxSysEvent getSystemEvent(int code) {
        return SYSTEM_EVENTS.getOrDefault(code, UNKNOWN_SYSTEM_EVENT);
    }

    // Zone/User codes

    private static final Map<Integer, String> ZONES_OR_USERS = Map.ofEntries( //
            entry(0x00, "System"), //
            entry(0x01, "Zone 1"), //
            entry(0x02, "Zone 2"), //
            entry(0x03, "Zone 3"), //
            entry(0x04, "Zone 4"), //
            entry(0x05, "Zone 5"), //
            entry(0x06, "Zone 6"), //
            entry(0x07, "Zone 7"), //
            entry(0x08, "Zone 8"), //
            entry(0x09, "Zone 9"), //
            entry(0x0A, "Zone 10"), //
            entry(0x0B, "Zone 11"), //
            entry(0x0C, "Zone 12"), //
            entry(0x0D, "Zone 13"), //
            entry(0x0E, "Zone 14"), //
            entry(0x0F, "Zone 15"), //
            entry(0x10, "Zone 16"), //
            entry(0x11, "Zone 17"), //
            entry(0x12, "Zone 18"), //
            entry(0x13, "Zone 19"), //
            entry(0x14, "Zone 20"), //
            entry(0x15, "Zone 21"), //
            entry(0x16, "Zone 22"), //
            entry(0x17, "Zone 23"), //
            entry(0x18, "Zone 24"), //
            entry(0x19, "Zone 25"), //
            entry(0x1A, "Zone 26"), //
            entry(0x1B, "Zone 27"), //
            entry(0x1C, "Zone 28"), //
            entry(0x1D, "Zone 29"), //
            entry(0x1E, "Zone 30"), //
            entry(0x1F, "Keyfob 1"), //
            entry(0x20, "Keyfob 2"), //
            entry(0x21, "Keyfob 3"), //
            entry(0x22, "Keyfob 4"), //
            entry(0x23, "Keyfob 5"), //
            entry(0x24, "Keyfob 6"), //
            entry(0x25, "Keyfob 7"), //
            entry(0x26, "Keyfob 8"), //
            entry(0x27, "User 1"), //
            entry(0x28, "User 2"), //
            entry(0x29, "User 3"), //
            entry(0x2A, "User 4"), //
            entry(0x2B, "User 5"), //
            entry(0x2C, "User 6"), //
            entry(0x2D, "User 7"), //
            entry(0x2E, "User 8"), //
            entry(0x2F, "Wireless Commander 1"), //
            entry(0x30, "Wireless Commander 2"), //
            entry(0x31, "Wireless Commander 3"), //
            entry(0x32, "Wireless Commander 4"), //
            entry(0x33, "Wireless Commander 5"), //
            entry(0x34, "Wireless Commander 6"), //
            entry(0x35, "Wireless Commander 7"), //
            entry(0x36, "Wireless Commander 8"), //
            entry(0x37, "Wireless Siren 1"), //
            entry(0x38, "Wireless Siren 2"), //
            entry(0x39, "Two-Way Wireless Keypad 1"), //
            entry(0x3A, "Two-Way Wireless Keypad 2"), //
            entry(0x3B, "Two-Way Wireless Keypad 3"), //
            entry(0x3C, "Two-Way Wireless Keypad 4"), //
            entry(0x3D, "X10 1"), //
            entry(0x3E, "X10 2"), //
            entry(0x3F, "X10 3"), //
            entry(0x40, "X10 4"), //
            entry(0x41, "X10 5"), //
            entry(0x42, "X10 6"), //
            entry(0x43, "X10 7"), //
            entry(0x44, "X10 8"), //
            entry(0x45, "X10 9"), //
            entry(0x46, "X10 10"), //
            entry(0x47, "X10 11"), //
            entry(0x48, "X10 12"), //
            entry(0x49, "X10 13"), //
            entry(0x4A, "X10 14"), //
            entry(0x4B, "X10 15"), //
            entry(0x4C, "PGM"), //
            entry(0x4D, "GSM"), //
            entry(0x4E, "Powerlink"), //
            entry(0x4F, "Proxy Tag 1"), //
            entry(0x50, "Proxy Tag 2"), //
            entry(0x51, "Proxy Tag 3"), //
            entry(0x52, "Proxy Tag 4"), //
            entry(0x53, "Proxy Tag 5"), //
            entry(0x54, "Proxy Tag 6"), //
            entry(0x55, "Proxy Tag 7"), //
            entry(0x56, "Proxy Tag 8") //
    );

    /**
     * Zone/User lookup
     */
    public static String getZoneOrUser(int code) {
        return ZONES_OR_USERS.getOrDefault(code, "UNKNOWN");
    }

    // Zone events

    private static final Map<Integer, String> ZONE_EVENTS = Map.ofEntries( //
            entry(0x00, "None"), //
            entry(0x01, "Tamper Alarm"), //
            entry(0x02, "Tamper Restore"), //
            entry(0x03, "Open"), //
            entry(0x04, "Closed"), //
            entry(0x05, "Violated (Motion)"), //
            entry(0x06, "Panic Alarm"), //
            entry(0x07, "RF Jamming"), //
            entry(0x08, "Tamper Open"), //
            entry(0x09, "Communication Failure"), //
            entry(0x0A, "Line Failure"), //
            entry(0x0B, "Fuse"), //
            entry(0x0C, "Not Active"), //
            entry(0x0D, "Low Battery"), //
            entry(0x0E, "AC Failure"), //
            entry(0x0F, "Fire Alarm"), //
            entry(0x10, "Emergency"), //
            entry(0x11, "Siren Tamper"), //
            entry(0x12, "Siren Tamper Restore"), //
            entry(0x13, "Siren Low Battery"), //
            entry(0x14, "Siren AC Fail") //
    );

    /**
     * Zone Event lookup
     */
    public static String getZoneEvent(int code) {
        return ZONE_EVENTS.getOrDefault(code, "UNKNOWN");
    }

    // Message types

    private static final Map<Integer, String> ZONE_EVENT_TYPES = Map.ofEntries( //
            entry(0x00, "None"), //
            entry(0x01, "Alarm Message"), //
            entry(0x02, "Open/Battery Message"), //
            entry(0x03, "Inactive/Tamper Message"), //
            entry(0x04, "Zone Message"), //
            entry(0x06, "Enroll/Bypass Message") //
    );

    /**
     * Message type lookup
     */
    public static String getZoneEventType(int code) {
        return ZONE_EVENT_TYPES.getOrDefault(code, "UNKNOWN");
    }
}
