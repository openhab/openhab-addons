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
package org.openhab.binding.yamahamusiccast.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link YamahaMusiccastConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Lennert Coopman - Initial contribution
 * @author Florian Hotze - Add volume in decibel
 */
@NonNullByDefault
public class YamahaMusiccastConfiguration {

    public @Nullable String host;
    public @Nullable Boolean syncVolume;
    public @Nullable String defaultAfterMCLink;

    /**
     * Minimum allowed volume in dB.
     */
    public float volumeDbMin = -80f; // -80.0 dB
    /**
     * Maximum allowed volume in dB.
     */
    public float volumeDbMax = 12f; // 12.0 dB
}
