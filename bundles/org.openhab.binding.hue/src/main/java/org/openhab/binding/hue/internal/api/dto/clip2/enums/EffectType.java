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
package org.openhab.binding.hue.internal.api.dto.clip2.enums;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enum for 'effect' types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum EffectType {
    // fixed Effects
    PRISM,
    OPAL,
    GLISTEN,
    SPARKLE,
    FIRE,
    CANDLE,
    // timed Effects
    SUNRISE,
    // applies to both
    NO_EFFECT;

    private static final Set<EffectType> FIXED = Set.of(PRISM, OPAL, GLISTEN, SPARKLE, FIRE, CANDLE);
    private static final Set<EffectType> TIMED = Set.of(SUNRISE);

    public static EffectType of(@Nullable String value) {
        if (value != null) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return NO_EFFECT;
    }

    public boolean isFixed() {
        return FIXED.contains(this);
    }

    public boolean isTimed() {
        return TIMED.contains(this);
    }
}
