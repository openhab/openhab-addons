/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.fields.HSBK;

/**
 * This class represents LIFX Tile effect
 * 
 * @author Pawel Pieczul - initial contribution
 */
@NonNullByDefault
public class TileEffect {
    private Integer effectType;
    private Long speed;
    private final Long duration;
    private final HSBK[] pallette;
    public static final TileEffect TILE_OFF = new TileEffect(0, 0L, 0L, new HSBK[0]);
    public static final TileEffect TILE_MORPH = new TileEffect(2, 3000L, 0L,
            new HSBK[] { new HSBK(0, 65535, 65535, 3500), new HSBK(7281, 65535, 65535, 3500),
                    new HSBK(10922, 65535, 65535, 3500), new HSBK(22209, 65535, 65535, 3500),
                    new HSBK(43507, 65535, 65535, 3500), new HSBK(49333, 65535, 65535, 3500),
                    new HSBK(53520, 65535, 65535, 3500) });
    public static final TileEffect TILE_FLAME = new TileEffect(3, 4000L, 0L, new HSBK[0]);

    public enum TileEffectType {
        OFF(0),
        RESERVED(1),
        MORPH(2),
        FLAME(3);

        private final Integer type;

        private TileEffectType(Integer type) {
            this.type = type;
        }

        public Integer value() {
            return type;
        }

        public static TileEffectType fromValue(Integer i) {
            switch (i) {
                case 1:
                    return RESERVED;
                case 2:
                    return MORPH;
                case 3:
                    return FLAME;
                default:
                    return OFF;
            }
        }
    }

    public TileEffect(Integer effectType, Long speed, Long duration, HSBK[] pallette) {
        this.effectType = effectType;
        this.speed = speed;
        this.duration = duration;
        this.pallette = pallette;
    }

    public TileEffect(TileEffectType effectType, Long speed, Long duration, HSBK[] pallette) {
        this(effectType.value(), speed, duration, pallette);
    }

    public TileEffect(TileEffect old) {
        this(old.effectType, old.speed, old.duration, old.pallette);
    }

    public TileEffectType getEffectType() {
        return TileEffectType.fromValue(effectType);
    }

    public void setEffectType(Integer effectType) {
        this.effectType = effectType;
    }

    public Long getSpeed() {
        return speed;
    }

    public void setSpeed(Long speed) {
        this.speed = speed;
    }

    public Long getSpeedInSeconds() {
        return getSpeed() / 1000L;
    }

    public void setSpeedInSeconds(Long speed) {
        setSpeed(speed * 1000L);
    }

    public Long getDuration() {
        return duration;
    }

    public HSBK[] getPallette() {
        return pallette;
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
        TileEffect n = (TileEffect) o;
        return n.effectType.equals(this.effectType) && n.duration.equals(this.duration) && n.speed.equals(this.speed)
                && n.pallette == this.pallette;
    }

    @Override
    public int hashCode() {
        Long hash = 1L;
        int prime = 31;
        hash = prime * hash + effectType;
        hash = prime * hash + duration;
        hash = prime * hash + speed;
        hash = prime * hash + pallette.hashCode();
        return hash.intValue();
    }
}
