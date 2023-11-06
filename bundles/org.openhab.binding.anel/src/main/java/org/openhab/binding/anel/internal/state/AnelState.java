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
package org.openhab.binding.anel.internal.state;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anel.internal.IAnelConstants;

/**
 * Parser and data structure for the state of an Anel device.
 * <p>
 * Documentation in <a href="https://forum.anel.eu/viewtopic.php?f=16&t=207">Anel forum</a> (German).
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelState {

    /** Pattern for temp, e.g. 26.4°C or -1°F */
    private static final Pattern PATTERN_TEMPERATURE = Pattern.compile("(\\-?\\d+(?:\\.\\d)?).[CF]");
    /** Pattern for switch state: [name],[state: 1=on,0=off] */
    private static final Pattern PATTERN_SWITCH_STATE = Pattern.compile("(.+),(0|1)");
    /** Pattern for IO state: [name],[1=input,0=output],[state: 1=on,0=off] */
    private static final Pattern PATTERN_IO_STATE = Pattern.compile("(.+),(0|1),(0|1)");

    /** The raw status this state was created from. */
    public final String status;

    /** Device IP address; read-only. */
    public final @Nullable String ip;
    /** Device name; read-only. */
    public final @Nullable String name;
    /** Device mac address; read-only. */
    public final @Nullable String mac;

    /** Device relay names; read-only. */
    public final String[] relayName = new String[8];
    /** Device relay states; changeable. */
    public final Boolean[] relayState = new Boolean[8];
    /** Device relay locked status; read-only. */
    public final Boolean[] relayLocked = new Boolean[8];

    /** Device IO names; read-only. */
    public final String[] ioName = new String[8];
    /** Device IO states; changeable if they are configured as input. */
    public final Boolean[] ioState = new Boolean[8];
    /** Device IO input states (<code>true</code> means changeable); read-only. */
    public final Boolean[] ioIsInput = new Boolean[8];

    /** Device temperature (optional); read-only. */
    public final @Nullable String temperature;

    /** Sensor temperature, e.g. "20.61" (optional); read-only. */
    public final @Nullable String sensorTemperature;
    /** Sensor Humidity, e.g. "40.7" (optional); read-only. */
    public final @Nullable String sensorHumidity;
    /** Sensor Brightness, e.g. "7.0" (optional); read-only. */
    public final @Nullable String sensorBrightness;

    private static final AnelState INVALID_STATE = new AnelState();

    public static AnelState of(@Nullable String status) {
        if (status == null || status.isEmpty()) {
            return INVALID_STATE;
        }
        return new AnelState(status);
    }

    private AnelState() {
        status = "<invalid>";
        ip = null;
        name = null;
        mac = null;
        temperature = null;
        sensorTemperature = null;
        sensorHumidity = null;
        sensorBrightness = null;
    }

    private AnelState(@Nullable String status) throws IllegalFormatException {
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("status must not be null or empty");
        }
        this.status = status;
        final String[] segments = status.split(IAnelConstants.STATUS_SEPARATOR);
        if (!segments[0].equals(IAnelConstants.STATUS_RESPONSE_PREFIX)) {
            throw new IllegalArgumentException(
                    "Data must start with '" + IAnelConstants.STATUS_RESPONSE_PREFIX + "' but it didn't: " + status);
        }
        if (segments.length < 16) {
            throw new IllegalArgumentException("Data must have at least 16 segments but it didn't: " + status);
        }
        final List<String> issues = new LinkedList<>();

        // name, host, mac
        name = segments[1].trim();
        ip = segments[2];
        mac = segments[5];

        // 8 switches / relays
        Integer lockedSwitches;
        try {
            lockedSwitches = Integer.parseInt(segments[14]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Segment 15 (" + segments[14] + ") is expected to be a number but it's not: " + status);
        }
        for (int i = 0; i < 8; i++) {
            final Matcher matcher = PATTERN_SWITCH_STATE.matcher(segments[6 + i]);
            if (matcher.matches()) {
                relayName[i] = matcher.group(1);
                relayState[i] = "1".equals(matcher.group(2));
            } else {
                issues.add("Unexpected format for switch " + i + ": '" + segments[6 + i]);
                relayName[i] = "";
                relayState[i] = false;
            }
            relayLocked[i] = (lockedSwitches & (1 << i)) > 0;
        }

        // 8 IO ports (devices with IO ports have >=24 segments)
        if (segments.length >= 24) {
            for (int i = 0; i < 8; i++) {
                final Matcher matcher = PATTERN_IO_STATE.matcher(segments[16 + i]);
                if (matcher.matches()) {
                    ioName[i] = matcher.group(1);
                    ioIsInput[i] = "1".equals(matcher.group(2));
                    ioState[i] = "1".equals(matcher.group(3));
                } else {
                    issues.add("Unexpected format for IO " + i + ": '" + segments[16 + i]);
                    ioName[i] = "";
                }
            }
        }

        // temperature
        temperature = segments.length > 24 ? parseTemperature(segments[24], issues) : null;

        if (segments.length > 34 && "p".equals(segments[27])) {
            // optional sensor (if device supports it and firmware >= 6.1) after power management
            if (segments.length > 38 && "s".equals(segments[35])) {
                sensorTemperature = segments[36];
                sensorHumidity = segments[37];
                sensorBrightness = segments[38];
            } else {
                sensorTemperature = null;
                sensorHumidity = null;
                sensorBrightness = null;
            }
        } else if (segments.length > 31 && "n".equals(segments[27]) && "s".equals(segments[28])) {
            // but sensor! (if device supports it and firmware >= 6.1)
            sensorTemperature = segments[29];
            sensorHumidity = segments[30];
            sensorBrightness = segments[31];
        } else {
            // firmware <= 6.0 or unknown format; skip rest
            sensorTemperature = null;
            sensorBrightness = null;
            sensorHumidity = null;
        }

        if (!issues.isEmpty()) {
            throw new IllegalArgumentException(String.format("Anel status string contains %d issue%s: %s\n%s", //
                    issues.size(), issues.size() == 1 ? "" : "s", status,
                    issues.stream().collect(Collectors.joining("\n"))));
        }
    }

    private static @Nullable String parseTemperature(String temp, List<String> issues) {
        if (!temp.isEmpty()) {
            final Matcher matcher = PATTERN_TEMPERATURE.matcher(temp);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            issues.add("Unexpected format for temperature: " + temp);
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + status + "]";
    }

    /* generated */
    @Override
    @SuppressWarnings("null")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((mac == null) ? 0 : mac.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(ioIsInput);
        result = prime * result + Arrays.hashCode(ioName);
        result = prime * result + Arrays.hashCode(ioState);
        result = prime * result + Arrays.hashCode(relayLocked);
        result = prime * result + Arrays.hashCode(relayName);
        result = prime * result + Arrays.hashCode(relayState);
        result = prime * result + ((temperature == null) ? 0 : temperature.hashCode());
        result = prime * result + ((sensorBrightness == null) ? 0 : sensorBrightness.hashCode());
        result = prime * result + ((sensorHumidity == null) ? 0 : sensorHumidity.hashCode());
        result = prime * result + ((sensorTemperature == null) ? 0 : sensorTemperature.hashCode());
        return result;
    }

    /* generated */
    @Override
    @SuppressWarnings("null")
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AnelState other = (AnelState) obj;
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (!Arrays.equals(ioIsInput, other.ioIsInput)) {
            return false;
        }
        if (!Arrays.equals(ioName, other.ioName)) {
            return false;
        }
        if (!Arrays.equals(ioState, other.ioState)) {
            return false;
        }
        if (mac == null) {
            if (other.mac != null) {
                return false;
            }
        } else if (!mac.equals(other.mac)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (sensorBrightness == null) {
            if (other.sensorBrightness != null) {
                return false;
            }
        } else if (!sensorBrightness.equals(other.sensorBrightness)) {
            return false;
        }
        if (sensorHumidity == null) {
            if (other.sensorHumidity != null) {
                return false;
            }
        } else if (!sensorHumidity.equals(other.sensorHumidity)) {
            return false;
        }
        if (sensorTemperature == null) {
            if (other.sensorTemperature != null) {
                return false;
            }
        } else if (!sensorTemperature.equals(other.sensorTemperature)) {
            return false;
        }
        if (!Arrays.equals(relayLocked, other.relayLocked)) {
            return false;
        }
        if (!Arrays.equals(relayName, other.relayName)) {
            return false;
        }
        if (!Arrays.equals(relayState, other.relayState)) {
            return false;
        }
        if (temperature == null) {
            if (other.temperature != null) {
                return false;
            }
        } else if (!temperature.equals(other.temperature)) {
            return false;
        }
        return true;
    }
}
