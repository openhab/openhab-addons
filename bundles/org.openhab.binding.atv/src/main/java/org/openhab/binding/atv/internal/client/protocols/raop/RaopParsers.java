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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;

/**
 * Utility methods for parsing various kinds of RAOP data.
 *
 * <p>
 * Bit flag values are modelled as {@link EnumSet}s; an unknown/unsupported zero-flag
 * corresponds to the empty set.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopParsers {

    /** Default sample rate in Hz. */
    public static final int DEFAULT_SAMPLE_RATE = 44100;
    /** Default sample size in bits. */
    public static final int DEFAULT_SAMPLE_SIZE = 16;
    /** Default number of channels. */
    public static final int DEFAULT_CHANNELS = 2;

    /** Encryptions supported by a receiver. */
    public enum EncryptionType {
        UNENCRYPTED,
        RSA,
        FAIRPLAY,
        MFISAP,
        FAIRPLAY_SAPV25
    }

    /** Metadata types supported by a receiver. */
    public enum MetadataType {
        TEXT,
        ARTWORK,
        PROGRESS
    }

    /** Encryption types supported by the RAOP sender. */
    public static final Set<EncryptionType> SUPPORTED_ENCRYPTIONS = Set.of(EncryptionType.UNENCRYPTED,
            EncryptionType.MFISAP);

    /** Audio properties advertised by a receiver via Zeroconf. */
    public record AudioProperties(int sampleRate, int channels, int sampleSize) {
    }

    private RaopParsers() {
    }

    /**
     * Parses Zeroconf properties and returns sample rate ({@code sr}), channels
     * ({@code ch}) and sample size in bytes ({@code ss}, advertised in bits).
     *
     * @throws ProtocolError if a property has an invalid (non-numeric) value
     */
    public static AudioProperties getAudioProperties(Map<String, String> properties) {
        try {
            int sampleRate = Integer.parseInt(properties.getOrDefault("sr", String.valueOf(DEFAULT_SAMPLE_RATE)));
            int channels = Integer.parseInt(properties.getOrDefault("ch", String.valueOf(DEFAULT_CHANNELS)));
            int sampleSize = Integer.parseInt(properties.getOrDefault("ss", String.valueOf(DEFAULT_SAMPLE_SIZE))) / 8;
            return new AudioProperties(sampleRate, channels, sampleSize);
        } catch (NumberFormatException e) {
            throw new ProtocolError("invalid audio property", e);
        }
    }

    /**
     * Returns encryption types supported by a receiver.
     *
     * <p>
     * Input format from Zeroconf is a comma separated list, e.g. {@code et=0,1,3}
     * where 0=unencrypted, 1=RSA, 3=FairPlay, 4=MFiSAP, 5=FairPlay SAPv2.5. Missing,
     * empty or unparsable values yield the empty set; unknown numeric types are ignored.
     */
    public static EnumSet<EncryptionType> getEncryptionTypes(Map<String, String> properties) {
        EnumSet<EncryptionType> output = EnumSet.noneOf(EncryptionType.class);
        String raw = properties.get("et");
        if (raw == null) {
            return output;
        }

        for (String part : raw.split(",", -1)) {
            int encType;
            try {
                encType = Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                return EnumSet.noneOf(EncryptionType.class);
            }
            switch (encType) {
                case 0 -> output.add(EncryptionType.UNENCRYPTED);
                case 1 -> output.add(EncryptionType.RSA);
                case 3 -> output.add(EncryptionType.FAIRPLAY);
                case 4 -> output.add(EncryptionType.MFISAP);
                case 5 -> output.add(EncryptionType.FAIRPLAY_SAPV25);
                default -> {
                    // Unknown type: ignored
                }
            }
        }
        return output;
    }

    /**
     * Returns metadata types supported by a receiver.
     *
     * <p>
     * Input format from Zeroconf is a comma separated list, e.g. {@code md=0,1,2}
     * where 0=text, 1=artwork, 2=progress. Missing, empty or unparsable values yield the
     * empty set.
     */
    public static EnumSet<MetadataType> getMetadataTypes(Map<String, String> properties) {
        EnumSet<MetadataType> output = EnumSet.noneOf(MetadataType.class);
        String raw = properties.get("md");
        if (raw == null) {
            return output;
        }

        for (String part : raw.split(",", -1)) {
            int mdType;
            try {
                mdType = Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                return EnumSet.noneOf(MetadataType.class);
            }
            switch (mdType) {
                case 0 -> output.add(MetadataType.TEXT);
                case 1 -> output.add(MetadataType.ARTWORK);
                case 2 -> output.add(MetadataType.PROGRESS);
                default -> {
                    // Unknown type: ignored
                }
            }
        }
        return output;
    }
}
