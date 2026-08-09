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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Features supported by AirPlay, announced through the {@code features}/{@code ft} TXT property.
 *
 * <p>
 * The flags are imported from
 * <a href="https://emanuelecozzi.net/docs/airplay2/features/">emanuelecozzi.net</a>.
 *
 * <p>
 * Every constant carries its bit position, and feature strings parse to an {@link EnumSet}.
 * Bits without a defined constant are ignored.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum AirPlayFlags {

    SupportsAirPlayVideoV1(0),
    SupportsAirPlayPhoto(1),
    SupportsAirPlaySlideShow(5),
    SupportsAirPlayScreen(7),
    SupportsAirPlayAudio(9),
    AudioRedundant(11),
    Authentication_4(14),
    MetadataFeatures_0(15),
    MetadataFeatures_1(16),
    MetadataFeatures_2(17),
    AudioFormats_0(18),
    AudioFormats_1(19),
    AudioFormats_2(20),
    AudioFormats_3(21),
    Authentication_1(23),
    Authentication_8(26),
    SupportsLegacyPairing(27),
    HasUnifiedAdvertiserInfo(30),
    IsCarPlay(32), // SupportsVolume?
    SupportsAirPlayVideoPlayQueue(33),
    SupportsAirPlayFromCloud(34),
    SupportsTLS_PSK(35),
    SupportsUnifiedMediaControl(38),
    SupportsBufferedAudio(40),
    SupportsPTP(41),
    SupportsScreenMultiCodec(42),
    SupportsSystemPairing(43),
    IsAPValeriaScreenSender(44),
    SupportsHKPairingAndAccessControl(46),
    SupportsCoreUtilsPairingAndEncryption(48),
    SupportsAirPlayVideoV2(49),
    MetadataFeatures_3(50),
    SupportsUnifiedPairSetupandMFi(51),
    SupportsSetPeersExtendedMessage(52),
    SupportsAPSync(54),
    SupportsWoL(55),
    SupportsWoL2(56),
    SupportsHangdogRemoteControl(58),
    SupportsAudioStreamConnectionSetup(59),
    SupportsAudioMetadataControl(60),
    SupportsRFC2198Redundancy(61);

    private static final Pattern FEATURES_PATTERN = Pattern.compile("^0x([0-9A-Fa-f]{1,8})(?:,0x([0-9A-Fa-f]{1,8})|)$");

    private final int bit;

    AirPlayFlags(int bit) {
        this.bit = bit;
    }

    /** Returns the bit position of this flag in the 64-bit feature value. */
    public int bit() {
        return bit;
    }

    /** Returns the numeric value ({@code 1 << bit}) of this flag. */
    public long value() {
        return 1L << bit;
    }

    /**
     * Parses an AirPlay feature string and returns what is supported.
     *
     * <p>
     * A feature string has one of the following formats:
     * <ul>
     * <li>{@code 0x12345678}</li>
     * <li>{@code 0x12345678,0xabcdef12} =&gt; {@code 0xabcdef1212345678}</li>
     * </ul>
     *
     * @param features the feature string
     * @return set of supported features
     * @throws IllegalArgumentException if the feature string is invalid
     */
    public static EnumSet<AirPlayFlags> parse(String features) {
        Matcher matcher = FEATURES_PATTERN.matcher(features);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("invalid feature string: " + features);
        }

        String value = matcher.group(1);
        String upper = matcher.group(2);
        if (upper != null) {
            value = upper + value;
        }
        return fromValue(Long.parseUnsignedLong(value, 16));
    }

    /**
     * Converts a raw 64-bit feature value to the set of known flags.
     *
     * @param value the raw feature bits
     * @return set of known features present in the value
     */
    public static EnumSet<AirPlayFlags> fromValue(long value) {
        EnumSet<AirPlayFlags> result = EnumSet.noneOf(AirPlayFlags.class);
        for (AirPlayFlags flag : values()) {
            if ((value & flag.value()) != 0) {
                result.add(flag);
            }
        }
        return result;
    }

    /**
     * Converts a set of flags back to the raw 64-bit feature value.
     *
     * @param flags the flags
     * @return the combined bit value
     */
    public static long toValue(Set<AirPlayFlags> flags) {
        long value = 0;
        for (AirPlayFlags flag : flags) {
            value |= flag.value();
        }
        return value;
    }
}
