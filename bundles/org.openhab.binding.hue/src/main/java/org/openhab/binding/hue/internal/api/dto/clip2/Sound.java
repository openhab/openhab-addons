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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.SoundValue;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Extended DTO for the state of a chime.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Sound extends SoundBase {
    protected @Nullable SoundBase status; // "status" contains current state
    protected @Nullable Volume volume;
    protected @Nullable Long duration; // in milliseconds

    @Override
    public List<SoundValue> getSoundValues() {
        return status instanceof SoundBase statusValue ? statusValue.getSoundValues() : List.of();
    }

    @Override
    public @Nullable SoundValue getSoundValue() {
        return status instanceof SoundBase statusValue ? statusValue.getSoundValue() : null;
    }

    public Sound setDuration(@Nullable QuantityType<?> duration) {
        this.duration = duration != null && duration.toUnit(Units.SECOND) instanceof QuantityType<?> seconds
                ? seconds.longValue() * 1000 // API specification requires a step size of 1000ms
                : null;
        return this;
    }

    public Sound setVolume(@Nullable PercentType volume) {
        this.volume = volume == null ? null : new Volume().setVolumeLevel(volume);
        return this;
    }

    public Sound setSoundValue(SoundValue soundValue) {
        this.sound = soundValue.name().toLowerCase();
        return this;
    }
}
