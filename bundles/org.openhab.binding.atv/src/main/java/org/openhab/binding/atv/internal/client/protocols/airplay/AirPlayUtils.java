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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.util.EnumSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.AuthenticationType;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.settings.RaopSettings.AirPlayVersion;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;

/**
 * Helpers for interpreting announced AirPlay features and TXT properties.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayUtils {

    /** Status flag: PIN required for pairing (bit {@code 0x8} of {@code sf}/{@code flags}). */
    public static final long PIN_REQUIRED = 0x8;

    /** Status flag: password protected (bit {@code 0x80} of {@code sf}/{@code flags}). */
    public static final long PASSWORD_BIT = 0x80;

    /** Status flag: legacy pairing required (bit {@code 0x200} of {@code sf}/{@code flags}). */
    public static final long LEGACY_PAIRING_BIT = 0x200;

    private static final double DBFS_MIN = -30.0;
    private static final double DBFS_MAX = 0.0;
    private static final double PERCENTAGE_MIN = 0.0;
    private static final double PERCENTAGE_MAX = 100.0;

    private static final Pattern[] UNSUPPORTED_MODELS = { Pattern.compile("^Mac\\d+,\\d+$") };

    private AirPlayUtils() {
    }

    /** Flags are either present via {@code sf} or {@code flags}. */
    private static long getFlags(Map<String, String> properties) {
        String flags = properties.get("sf");
        if (flags == null || flags.isEmpty()) {
            flags = properties.get("flags");
        }
        if (flags == null || flags.isEmpty()) {
            flags = "0x0";
        }
        return parseHex(flags);
    }

    /** Parses a hex string with optional {@code 0x} prefix. */
    private static long parseHex(String value) {
        String stripped = value.startsWith("0x") || value.startsWith("0X") ? value.substring(2) : value;
        return Long.parseUnsignedLong(stripped, 16);
    }

    /**
     * Returns if a password is required by an AirPlay service.
     *
     * <p>
     * A password is required under these conditions:
     * <ul>
     * <li>{@code pw} is true</li>
     * <li>{@code sf} or {@code flags} has bit {@code 0x80} set</li>
     * </ul>
     *
     * @param service the AirPlay (or RAOP) service
     * @return true if a password is required
     */
    public static boolean isPasswordRequired(BaseService service) {
        // "pw" flag
        if ("true".equalsIgnoreCase(service.properties().getOrDefault("pw", "false"))) {
            return true;
        }

        // Legacy "flags" property
        return (getFlags(service.properties()) & PASSWORD_BIT) != 0;
    }

    /**
     * Returns the pairing requirement for a service.
     *
     * <p>
     * Pairing requirement is {@code Mandatory} if bit {@code 0x200} or bit {@code 0x8} is set
     * in {@code sf} (AirPlay v1) / {@code flags} (AirPlay v2). An {@code act} (Access Control
     * Type) value of {@code "2"} seems to correspond to "Current User" which is not supported,
     * other cases are optimistically treated as {@code NotNeeded}.
     *
     * @param service the AirPlay service
     * @return the pairing requirement
     */
    public static PairingRequirement getPairingRequirement(BaseService service) {
        if ((getFlags(service.properties()) & (LEGACY_PAIRING_BIT | PIN_REQUIRED)) != 0) {
            return PairingRequirement.Mandatory;
        }

        // "Current User" access control is not supported
        if ("2".equals(service.properties().getOrDefault("act", "0"))) {
            return PairingRequirement.Unsupported;
        }
        return PairingRequirement.NotNeeded;
    }

    /**
     * Returns if a device supports remote control tunneling.
     *
     * <p>
     * This is a guess: HomePods (model {@code AudioAccessory*}) support it with transient
     * credentials, Apple TVs (model {@code AppleTV*}) running tvOS 13+ support it
     * with HAP credentials.
     *
     * @param service the AirPlay service
     * @param credentials the credentials that will be used
     * @return true if remote control tunneling is supported
     */
    public static boolean isRemoteControlSupported(BaseService service, HapCredentials credentials) {
        String model = service.properties().getOrDefault("model", "");

        // HomePod supports remote control but only with transient credentials
        if (model.startsWith("AudioAccessory")) {
            return credentials.equals(HapCredentials.TRANSIENT_CREDENTIALS);
        }

        if (!model.startsWith("AppleTV")) {
            return false;
        }

        // tvOS must be at least version 13 and HAP credentials are required by Apple TV
        String version = service.properties().getOrDefault("osvers", "0.0").split("\\.", 2)[0];
        return Double.parseDouble(version) >= 13.0 && credentials.type() == AuthenticationType.HAP;
    }

    /**
     * Encodes a binary plist payload.
     *
     * @param data the object graph to encode
     * @return {@code bplist00} bytes
     */
    public static byte[] encodePlistBody(Object data) {
        return BinaryPlist.dump(data);
    }

    /**
     * Decodes a binary plist payload.
     *
     * @param body the payload ({@code byte[]}, {@link String} or already decoded {@link Map})
     * @return the decoded object, or {@code null} if the payload is not a valid plist
     */
    public static @Nullable Object decodePlistBody(Object body) {
        try {
            if (body instanceof Map) {
                return body;
            }
            byte[] bytes = body instanceof byte[] raw ? raw
                    : body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return BinaryPlist.parse(bytes);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Returns the major AirPlay version supported by a service.
     *
     * <p>
     * This is a guess: with {@code Auto}, the service is considered AirPlay 2 when the
     * {@code ft}/{@code features} flags contain {@code SupportsUnifiedMediaControl} (bit
     * 38) or {@code SupportsCoreUtilsPairingAndEncryption} (bit 48).
     *
     * @param service the AirPlay service
     * @param preferredVersion the version preference from settings
     * @return the major AirPlay version to use
     */
    public static AirPlayMajorVersion getProtocolVersion(BaseService service, AirPlayVersion preferredVersion) {
        if (preferredVersion == AirPlayVersion.Auto) {
            String features = service.properties().get("ft");
            if (features == null || features.isEmpty()) {
                features = service.properties().getOrDefault("features", "0x0");
            }

            EnumSet<AirPlayFlags> parsedFeatures = AirPlayFlags.parse(features);
            if (parsedFeatures.contains(AirPlayFlags.SupportsUnifiedMediaControl)
                    || parsedFeatures.contains(AirPlayFlags.SupportsCoreUtilsPairingAndEncryption)) {
                return AirPlayMajorVersion.AirPlayV2;
            }
            return AirPlayMajorVersion.AirPlayV1;
        }
        if (preferredVersion == AirPlayVersion.V2) {
            return AirPlayMajorVersion.AirPlayV2;
        }
        return AirPlayMajorVersion.AirPlayV1;
    }

    /**
     * Updates an AirPlay service according to what it supports: sets the password
     * requirement and the pairing requirement ({@code Disabled} when access control only
     * allows same-home devices, {@code Unsupported} for known-unsupported models).
     *
     * @param service the mutable service to update
     */
    public static void updateServiceDetails(Service service) {
        service.setRequiresPassword(isPasswordRequired(service));

        if ("1".equals(service.properties().getOrDefault("acl", "0"))) {
            // Access control might say that pairing is not possible, e.g. only devices
            // belonging to the same home
            service.setPairing(PairingRequirement.Disabled);
        } else if (isUnsupportedModel(service.properties().getOrDefault("model", ""))) {
            // Set as "unsupported" for devices known to not (yet) be supported
            service.setPairing(PairingRequirement.Unsupported);
        } else {
            service.setPairing(getPairingRequirement(service));
        }
    }

    private static boolean isUnsupportedModel(String model) {
        for (Pattern pattern : UNSUPPORTED_MODELS) {
            if (pattern.matcher(model).lookingAt()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a percentage level to dBFS, used for volume levels in AirPlay.
     *
     * @param level percentage in [0.0, 100.0]
     * @return dBFS value ({@code -144.0} for muted)
     */
    public static double pctToDbfs(double level) {
        // AirPlay uses -144.0 as muted volume, so re-map 0.0 to that
        if (level == 0.0) {
            return -144.0;
        }

        // Map percentage to dBFS
        return mapRange(level, PERCENTAGE_MIN, PERCENTAGE_MAX, DBFS_MIN, DBFS_MAX);
    }

    /**
     * Converts dBFS to a percentage.
     *
     * @param level dBFS value
     * @return percentage in [0.0, 100.0]
     */
    public static double dbfsToPct(double level) {
        // AirPlay uses -144.0 as "muted", but we treat everything below -30.0 as
        // muted to be a bit defensive
        if (level < DBFS_MIN) {
            return PERCENTAGE_MIN;
        }

        // Map dBFS to percentage
        return mapRange(level, DBFS_MIN, DBFS_MAX, PERCENTAGE_MIN, PERCENTAGE_MAX);
    }

    /** Maps a value in one range to another range. */
    private static double mapRange(double value, double inMin, double inMax, double outMin, double outMax) {
        if (inMax - inMin <= 0.0) {
            throw new IllegalArgumentException("invalid input range");
        }
        if (outMax - outMin <= 0.0) {
            throw new IllegalArgumentException("invalid output range");
        }
        if (value < inMin || value > inMax) {
            throw new IllegalArgumentException("input value out of range");
        }
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
