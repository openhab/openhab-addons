/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
     * The supported volume range in decibels for the AmpliPi
     */
    public static final int MIN_VOLUME_DB = -79;
    public static final int MAX_VOLUME_DB = 0;

    /**
     * Converts a volume from AmpliPi to an openHAB PercentType
     *
     * @param volume volume from AmpliPi
     * @return according PercentType for openHAB
     */
    public static PercentType volumeToPercentType(Integer volume) {
        // AmpliPi volumes range from -79..0
        return new PercentType((volume + 79) * 100 / 79);
    }

    /**
     * Converts a volume as PercentType from openHAB to an integer for AmpliPi
     * 
     * @param volume volume as PercentType
     * @return according volume as int for AmpliPi
     */
    public static int percentTypeToVolume(PercentType volume) {
        // AmpliPi volumes range from -79..0
        return (volume.intValue() * 79 / 100) - 79;
    }
}
