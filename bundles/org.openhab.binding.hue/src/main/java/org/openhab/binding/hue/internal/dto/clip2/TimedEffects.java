/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2;

import java.time.Duration;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * DTO for 'timed_effects' of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class TimedEffects extends Effects {
    private @Nullable Long duration;

    public @Nullable Duration getDuration() {
        Long duration = this.duration;
        return Objects.nonNull(duration) ? Duration.ofMillis(duration) : Duration.ZERO;
    }

    public TimedEffects setDuration(Duration duration) {
        this.duration = duration.toMillis();
        return this;
    }
}
