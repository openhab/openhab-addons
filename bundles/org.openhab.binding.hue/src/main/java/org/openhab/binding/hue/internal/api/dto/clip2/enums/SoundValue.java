/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.api.dto.clip2.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum for sound value resources.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum SoundValue {
    NO_SOUND,
    ALERT,
    BLEEP,
    BOUNCE,
    BRIGHT,
    BRIGHT_MODERN,
    DING_DONG_CLASSIC,
    DING_DONG_MODERN,
    DING_DONG_XYLO,
    ECHO,
    FAIRY,
    GALAXY,
    GLOW,
    HUE_DEFAULT,
    REVEAL,
    RISE,
    SIREN,
    SONAR,
    SWING,
    WELCOME,
    WESTMINSTER_CLASSIC,
    WESTMINSTER_MODERN;

    public static SoundValue of(@Nullable String value) {
        if (value != null) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return NO_SOUND;
    }
}
