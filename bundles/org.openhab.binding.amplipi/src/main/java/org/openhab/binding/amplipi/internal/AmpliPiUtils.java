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
package org.openhab.binding.amplipi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;

/**
 * This class has some commonly used static helper functions.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public class AmpliPiUtils {
    /**
     * The supported volume range in decibels for the AmpliPi (used for the
     * legacy dB-based group volume delta).
     */
    public static final int MIN_VOLUME_DB = -79;
    public static final int MAX_VOLUME_DB = 0;

    /**
     * Converts a normalized AmpliPi volume fraction (0.0 - 1.0) to an openHAB
     * PercentType. Using the firmware-provided fraction avoids assuming a fixed
     * dB range, which differs between firmware versions (e.g. -79 vs -80 dB).
     *
     * @param volumeFraction volume fraction from AmpliPi (0.0 - 1.0)
     * @return according PercentType for openHAB
     */
    public static PercentType volumeFractionToPercentType(double volumeFraction) {
        long percent = Math.round(volumeFraction * 100);
        return new PercentType((int) Math.max(0, Math.min(100, percent)));
    }

    /**
     * Converts an openHAB PercentType to a normalized AmpliPi volume fraction
     * (0.0 - 1.0).
     *
     * @param volume volume as PercentType
     * @return according volume fraction for AmpliPi
     */
    public static double percentTypeToVolumeFraction(PercentType volume) {
        return volume.doubleValue() / 100.0;
    }

    /**
     * Converts a dB volume from AmpliPi to an openHAB PercentType. Only used for
     * the group volume delta; the result is clamped so an out-of-range value can
     * never throw.
     *
     * @param volume volume from AmpliPi in dB
     * @return according PercentType for openHAB
     */
    public static PercentType volumeToPercentType(Integer volume) {
        int percent = (volume + 79) * 100 / 79;
        return new PercentType(Math.max(0, Math.min(100, percent)));
    }

    /**
     * Converts a volume as PercentType from openHAB to a dB integer for AmpliPi.
     *
     * @param volume volume as PercentType
     * @return according volume as int for AmpliPi
     */
    public static int percentTypeToVolume(PercentType volume) {
        // AmpliPi volumes range from -79..0
        return (volume.intValue() * 79 / 100) - 79;
    }
}
