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
public class Effect {
    /*
     * OFF(0),
     * RESERVED(1),
     * MORPH(2),
     * FLAME(3);
     */
    final Integer type;
    final Long speed;
    final Long duration;
    final HSBK[] pallette;

    public Integer getType() {
        return type;
    }

    public Long getSpeed() {
        return speed;
    }

    public Long getDuration() {
        return duration;
    }

    HSBK[] getPallette() {
        return pallette;
    }

    public Effect(Integer type, Long speed, Long duration, HSBK[] pallette) {
        this.type = type;
        this.pallette = pallette;
        this.duration = duration;
        this.speed = speed;
    }

    public Effect() {
        this(0, 3000L, 0L, new HSBK[0]);
    }

    public static @Nullable Effect createDefault(Integer type, @Nullable Long morphSpeed, @Nullable Long flameSpeed) {
        Long speed;
        switch (type) {
            case 0:
                return new Effect(0, 0L, 0L, new HSBK[0]);
            case 2:
                if (morphSpeed == null) {
                    speed = 3000L;
                } else {
                    speed = morphSpeed;
                }
                HSBK[] p = { new HSBK(0, 65535, 65535, 3500), new HSBK(7281, 65535, 65535, 3500),
                        new HSBK(10922, 65535, 65535, 3500), new HSBK(22209, 65535, 65535, 3500),
                        new HSBK(43507, 65535, 65535, 3500), new HSBK(49333, 65535, 65535, 3500),
                        new HSBK(53520, 65535, 65535, 3500) };
                return new Effect(type, speed, 0L, p);
            case 3:
                if (flameSpeed == null) {
                    speed = 4000L;
                } else {
                    speed = flameSpeed;
                }
                return new Effect(type, speed, 0L, new HSBK[0]);
            default:
                return null;
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
                && n.pallette == this.pallette;
    }

    @Override
    public int hashCode() {
        Long hash = 1L;
        int prime = 31;
        hash = prime * hash + getType();
        hash = prime * hash + duration;
        hash = prime * hash + speed;
        hash = prime * hash + pallette.hashCode();
        return hash.intValue();
    }
}
