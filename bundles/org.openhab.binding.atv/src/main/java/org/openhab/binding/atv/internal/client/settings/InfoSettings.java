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
package org.openhab.binding.atv.internal.client.settings;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.SettingsError;

/**
 * Information related settings, i.e. how the library identifies itself to a device.
 *
 * <p>
 * All components are non-null; {@code null} arguments are replaced by default identity values (and a random
 * {@code rpId}).
 *
 * @param name name of the client
 * @param mac MAC address of the client
 * @param model client device model
 * @param deviceId client device id
 * @param rpId client remote pairing id
 * @param osName client operating system name
 * @param osBuild client operating system build number
 * @param osVersion client operating system version
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record InfoSettings(String name, String mac, String model, String deviceId, String rpId, String osName,
        String osBuild, String osVersion) {

    /** Default client name. */
    public static final String DEFAULT_NAME = "openHAB";
    /** Default MAC address: locally administrated (02) prefix. */
    public static final String DEFAULT_MAC = "02:70:79:61:74:76";
    /** Default device id: 0xFF prefix. */
    public static final String DEFAULT_DEVICE_ID = "FF:70:79:61:74:76";
    /** Default client model. */
    public static final String DEFAULT_MODEL = "iPhone10,6";
    /** Default client operating system name. */
    public static final String DEFAULT_OS_NAME = "iPhone OS";
    /** Default client operating system build number. */
    public static final String DEFAULT_OS_BUILD = "18G82";
    /** Default client operating system version. */
    public static final String DEFAULT_OS_VERSION = "14.7.1";

    private static final Pattern MAC_PATTERN = Pattern.compile("[0-9a-fA-F]{2}(:[0-9a-fA-F]{2}){5}");
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Canonical constructor applying default values and validating the MAC address.
     */
    public InfoSettings {
        if (!MAC_PATTERN.matcher(mac).matches()) {
            throw new SettingsError(mac + " is not a valid MAC address");
        }
    }

    /**
     * Creates settings with default identity values and a random {@code rpId}.
     *
     * @return default settings
     */
    public static InfoSettings ofDefaults() {
        return new InfoSettings(DEFAULT_NAME, DEFAULT_MAC, DEFAULT_MODEL, DEFAULT_DEVICE_ID, randomRpId(),
                DEFAULT_OS_NAME, DEFAULT_OS_BUILD, DEFAULT_OS_VERSION);
    }

    private static String randomRpId() {
        byte[] raw = new byte[6];
        RANDOM.nextBytes(raw);
        return HexFormat.of().formatHex(raw);
    }

    /**
     * Returns a copy with another name.
     *
     * @param name new name
     * @return updated copy
     */
    public InfoSettings withName(String name) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another MAC address.
     *
     * @param mac new MAC address
     * @return updated copy
     */
    public InfoSettings withMac(String mac) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another model.
     *
     * @param model new model
     * @return updated copy
     */
    public InfoSettings withModel(String model) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another device id.
     *
     * @param deviceId new device id
     * @return updated copy
     */
    public InfoSettings withDeviceId(String deviceId) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another remote pairing id.
     *
     * @param rpId new remote pairing id
     * @return updated copy
     */
    public InfoSettings withRpId(String rpId) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another operating system name.
     *
     * @param osName new operating system name
     * @return updated copy
     */
    public InfoSettings withOsName(String osName) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another operating system build number.
     *
     * @param osBuild new operating system build number
     * @return updated copy
     */
    public InfoSettings withOsBuild(String osBuild) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }

    /**
     * Returns a copy with another operating system version.
     *
     * @param osVersion new operating system version
     * @return updated copy
     */
    public InfoSettings withOsVersion(String osVersion) {
        return new InfoSettings(name, mac, model, deviceId, rpId, osName, osBuild, osVersion);
    }
}
