/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.dto;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.LifxBindingConstants;
import org.openhab.binding.lifx.internal.fields.HSBK;

/**
 * This class represents LIFX Tile effect
 *
 * @author Pawel Pieczul - Initial contribution
 */
@NonNullByDefault
public class Effect {
    public enum EffectType {
        OFF(0),
        MORPH(2),
        FLAME(3);

        Integer type;

        EffectType(Integer type) {
            this.type = type;
        }

        public static EffectType fromValue(Integer value) {
            switch (value) {
                case 0:
                    return OFF;
                case 2:
                    return MORPH;
                case 3:
                    return FLAME;
                default:
                    throw new IllegalArgumentException("Unknown effect type");
            }
        }

        public static EffectType fromValue(String value) {
            if (LifxBindingConstants.CHANNEL_TYPE_EFFECT_OPTION_OFF.equals(value)) {
                return OFF;
            } else if (LifxBindingConstants.CHANNEL_TYPE_EFFECT_OPTION_MORPH.equals(value)) {
                return MORPH;
            } else if (LifxBindingConstants.CHANNEL_TYPE_EFFECT_OPTION_FLAME.equals(value)) {
                return FLAME;
            }
            throw new IllegalArgumentException("Unknown effect type");
        }

        public Integer intValue() {
            return type;
        }

        public String stringValue() {
            switch (type) {
                case 2:
                    return LifxBindingConstants.CHANNEL_TYPE_EFFECT_OPTION_MORPH;
                case 3:
                    return LifxBindingConstants.CHANNEL_TYPE_EFFECT_OPTION_FLAME;
                default:
                    return LifxBindingConstants.CHANNEL_TYPE_EFFECT_OPTION_OFF;
            }
        }
    }

    final EffectType type;
    final Long speed;
    final Long duration;
    final HSBK[] palette;

    public EffectType getType() {
        return type;
    }

    public Long getSpeed() {
        return speed;
    }

    public Long getDuration() {
        return duration;
    }

    HSBK[] getPalette() {
        return palette;
    }

    public Effect(EffectType type, Long speed, Long duration, HSBK[] palette) {
        this.type = type;
        this.palette = palette;
        this.duration = duration;
        this.speed = speed;
    }

    public Effect(Integer type, Long speed, Long duration, HSBK[] palette) {
        this(EffectType.fromValue(type), speed, duration, palette);
    }

    public Effect() {
        this(EffectType.OFF, 3000L, 0L, new HSBK[0]);
    }

    public static Effect createDefault(String type, @Nullable Long morphSpeed, @Nullable Long flameSpeed) {
        Long speed;
        EffectType effectType = EffectType.fromValue(type);
        switch (effectType) {
            case OFF:
                return new Effect(effectType, 0L, 0L, new HSBK[0]);
            case MORPH:
                if (morphSpeed == null) {
                    speed = 3000L;
                } else {
                    speed = morphSpeed;
                }
                HSBK[] p = { new HSBK(0, 65535, 65535, 3500), new HSBK(7281, 65535, 65535, 3500),
                        new HSBK(10922, 65535, 65535, 3500), new HSBK(22209, 65535, 65535, 3500),
                        new HSBK(43507, 65535, 65535, 3500), new HSBK(49333, 65535, 65535, 3500),
                        new HSBK(53520, 65535, 65535, 3500) };
                return new Effect(effectType, speed, 0L, p);
            case FLAME:
                if (flameSpeed == null) {
                    speed = 4000L;
                } else {
                    speed = flameSpeed;
                }
                return new Effect(effectType, speed, 0L, new HSBK[0]);
            default:
                throw new IllegalArgumentException("Unknown effect type");
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        Effect n = (Effect) o;
        return n.getType().equals(this.getType()) && n.duration.equals(this.duration) && n.speed.equals(this.speed)
                && Arrays.equals(n.palette, this.palette);
    }

    @Override
    public int hashCode() {
        Long hash = 1L;
        int prime = 31;
        hash = prime * hash + type.hashCode();
        hash = prime * hash + duration;
        hash = prime * hash + speed;
        hash = prime * hash + palette.hashCode();
        return hash.intValue();
    }
}
