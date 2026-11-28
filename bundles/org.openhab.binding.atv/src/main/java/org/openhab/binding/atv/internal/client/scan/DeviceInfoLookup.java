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
package org.openhab.binding.atv.internal.client.scan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.OperatingSystem;

/**
 * Lookup methods for device data (hardware identifiers, model names, build numbers).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class DeviceInfoLookup {

    /** Hardware model identifier to device model. */
    private static final Map<String, DeviceModel> MODEL_LIST = new LinkedHashMap<>();

    /** Internal Apple model name to device model. */
    private static final Map<String, DeviceModel> INTERNAL_NAME_LIST = new LinkedHashMap<>();

    /** Build number to OS version (Apple TV only). */
    private static final Map<String, String> VERSION_LIST = new LinkedHashMap<>();

    /** Identifier patterns of macOS devices. */
    private static final List<Pattern> OS_IDENTIFIER_FORMATS = List.of(Pattern.compile("MacBookAir\\d+,\\d+"),
            Pattern.compile("iMac\\d+,\\d+"), Pattern.compile("Macmini\\d+,\\d+"),
            Pattern.compile("MacBookPro\\d+,\\d+"), Pattern.compile("Mac\\d+,\\d+"),
            Pattern.compile("MacPro\\d+,\\d+"));

    private static final Pattern BUILD_BASE_PATTERN = Pattern.compile("^(\\d+)[A-Z]");

    static {
        MODEL_LIST.put("AirPort4,107", DeviceModel.AirPortExpress);
        MODEL_LIST.put("AirPort10,115", DeviceModel.AirPortExpressGen2);
        MODEL_LIST.put("AppleTV1,1", DeviceModel.AppleTVGen1);
        MODEL_LIST.put("AppleTV2,1", DeviceModel.Gen2);
        MODEL_LIST.put("AppleTV3,1", DeviceModel.Gen3);
        MODEL_LIST.put("AppleTV3,2", DeviceModel.Gen3);
        MODEL_LIST.put("AppleTV5,3", DeviceModel.Gen4);
        MODEL_LIST.put("AppleTV6,2", DeviceModel.Gen4K);
        MODEL_LIST.put("AppleTV11,1", DeviceModel.AppleTV4KGen2);
        MODEL_LIST.put("AppleTV14,1", DeviceModel.AppleTV4KGen3);
        MODEL_LIST.put("AudioAccessory1,1", DeviceModel.HomePod);
        MODEL_LIST.put("AudioAccessory1,2", DeviceModel.HomePod);
        MODEL_LIST.put("AudioAccessory5,1", DeviceModel.HomePodMini);
        MODEL_LIST.put("AudioAccessorySingle5,1", DeviceModel.HomePodMini);
        MODEL_LIST.put("AudioAccessory6,1", DeviceModel.HomePodGen2);

        INTERNAL_NAME_LIST.put("K66AP", DeviceModel.Gen2);
        INTERNAL_NAME_LIST.put("J33AP", DeviceModel.Gen3);
        INTERNAL_NAME_LIST.put("J33IAP", DeviceModel.Gen3);
        INTERNAL_NAME_LIST.put("J42dAP", DeviceModel.Gen4);
        INTERNAL_NAME_LIST.put("J105aAP", DeviceModel.Gen4K);
        INTERNAL_NAME_LIST.put("J305AP", DeviceModel.AppleTV4KGen2);
        INTERNAL_NAME_LIST.put("J255AP", DeviceModel.AppleTV4KGen3);

        VERSION_LIST.put("17J586", "13.0");
        VERSION_LIST.put("17K82", "13.2");
        VERSION_LIST.put("17K449", "13.3");
        VERSION_LIST.put("17K795", "13.3.1");
        VERSION_LIST.put("17L256", "13.4");
        VERSION_LIST.put("17L562", "13.4.5");
        VERSION_LIST.put("17L570", "13.4.6");
        VERSION_LIST.put("17M61", "13.4.8");
        VERSION_LIST.put("18J386", "14.0");
        VERSION_LIST.put("18J400", "14.0.1");
        VERSION_LIST.put("18J411", "14.0.2");
        VERSION_LIST.put("18K57", "14.2");
        VERSION_LIST.put("18K561", "14.3");
        VERSION_LIST.put("18K802", "14.4");
        VERSION_LIST.put("18L204", "14.5");
        VERSION_LIST.put("18L569", "14.6");
        VERSION_LIST.put("18M60", "14.7");
        VERSION_LIST.put("19J346", "15.0");
        VERSION_LIST.put("19J572", "15.1");
        VERSION_LIST.put("19J581", "15.1.1");
        VERSION_LIST.put("19K53", "15.2");
        VERSION_LIST.put("19K547", "15.3");
        VERSION_LIST.put("19L440", "15.4");
        VERSION_LIST.put("19L452", "15.4.1");
        VERSION_LIST.put("19L570", "15.5");
        VERSION_LIST.put("19L580", "15.5.1");
        VERSION_LIST.put("19M65", "15.6");
        VERSION_LIST.put("20J373", "16.0");
        VERSION_LIST.put("20K71", "16.1");
        VERSION_LIST.put("20K80", "16.1.1");
        VERSION_LIST.put("20K362", "16.2");
        VERSION_LIST.put("20K650", "16.3");
        VERSION_LIST.put("20K661", "16.3.1");
        VERSION_LIST.put("20K672", "16.3.2");
        VERSION_LIST.put("20K680", "16.3.3");
        VERSION_LIST.put("20L497", "16.4");
        VERSION_LIST.put("20L498", "16.4.1");
        VERSION_LIST.put("20L563", "16.5");
        VERSION_LIST.put("20M73", "16.6");
        VERSION_LIST.put("22J354", "17.0");
        VERSION_LIST.put("21K69", "17.1");
        VERSION_LIST.put("21K365", "17.2");
        VERSION_LIST.put("21K646", "17.3");
        VERSION_LIST.put("21L227", "17.4");
        VERSION_LIST.put("21L569", "17.5");
        VERSION_LIST.put("21L580", "17.5.1");
        VERSION_LIST.put("21M71", "17.6");
        VERSION_LIST.put("21M80", "17.6.1");
        VERSION_LIST.put("22J357", "18.0");
        VERSION_LIST.put("22J580", "18.1");
    }

    private DeviceInfoLookup() {
    }

    /**
     * Looks up a device model from a hardware identifier.
     *
     * @param identifier hardware identifier, e.g. {@code AppleTV6,2} (may be {@code null})
     * @return device model, {@link DeviceModel#Unknown} when not found
     */
    public static DeviceModel lookupModel(@Nullable String identifier) {
        return MODEL_LIST.getOrDefault(identifier == null ? "" : identifier, DeviceModel.Unknown);
    }

    /**
     * Looks up a device model from an internal Apple model name.
     *
     * @param name internal name, e.g. {@code J105aAP} (may be {@code null})
     * @return device model, {@link DeviceModel#Unknown} when not found
     */
    public static DeviceModel lookupInternalName(@Nullable String name) {
        return INTERNAL_NAME_LIST.getOrDefault(name == null ? "" : name, DeviceModel.Unknown);
    }

    /**
     * Looks up an OS version from a build number. Falls back to deriving the major
     * version from the build base (17A123 corresponds to tvOS 13.x, 16A123 to tvOS
     * 12.x and so on).
     *
     * @param build build number, e.g. {@code 18M60} (may be {@code null})
     * @return version string or {@code null} when unknown
     */
    public static @Nullable String lookupVersion(@Nullable String build) {
        if (build == null || build.isEmpty()) {
            return null;
        }
        String version = VERSION_LIST.get(build);
        if (version != null) {
            return version;
        }
        Matcher match = BUILD_BASE_PATTERN.matcher(build);
        if (match.find()) {
            int base = Integer.parseInt(match.group(1));
            return (base - 4) + ".x";
        }
        return null;
    }

    /**
     * Looks up the operating system based on a hardware identifier string, e.g.
     * {@code MacBookAir10,1}.
     *
     * @param identifier hardware identifier
     * @return {@link OperatingSystem#MacOS} for known macOS identifiers, otherwise
     *         {@link OperatingSystem#Unknown}
     */
    public static OperatingSystem lookupOs(String identifier) {
        for (Pattern format : OS_IDENTIFIER_FORMATS) {
            // lookingAt() only anchors at the start, unlike matches()
            if (format.matcher(identifier).lookingAt()) {
                return OperatingSystem.MacOS;
            }
        }
        return OperatingSystem.Unknown;
    }

    /**
     * Looks up the operating system based on a device model.
     *
     * @param model device model
     * @return the operating system, {@link OperatingSystem#Unknown} when not derivable
     */
    public static OperatingSystem lookupOs(DeviceModel model) {
        switch (model) {
            case AirPortExpress:
            case AirPortExpressGen2:
                return OperatingSystem.AirPortOS;
            case HomePod:
            case HomePodMini:
            case HomePodGen2:
                return OperatingSystem.TvOS;
            case AppleTVGen1:
            case Gen2:
            case Gen3:
                return OperatingSystem.Legacy;
            case Gen4:
            case Gen4K:
            case AppleTV4KGen2:
            case AppleTV4KGen3:
                return OperatingSystem.TvOS;
            default:
                return OperatingSystem.Unknown;
        }
    }
}
