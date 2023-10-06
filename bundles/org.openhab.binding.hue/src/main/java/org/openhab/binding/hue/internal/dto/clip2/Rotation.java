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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.DirectionType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * DTO for rotation element of a tap dial switch.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Rotation {
    private @Nullable String direction;
    private @Nullable Integer duration;
    private @Nullable Integer steps;

    public @Nullable DirectionType getDirection() {
        String direction = this.direction;
        return Objects.nonNull(direction) ? DirectionType.valueOf(direction.toUpperCase()) : null;
    }

    public int getDuration() {
        Integer duration = this.duration;
        return Objects.nonNull(duration) ? duration.intValue() : 0;
    }

    public int getSteps() {
        Integer steps = this.steps;
        return Objects.nonNull(steps) ? steps.intValue() : 0;
    }

    /**
     * Get the state corresponding to a relative rotary dial's last steps value. Clockwise rotations are positive, and
     * counter clockwise rotations negative.
     *
     * @return the state or UNDEF.
     */
    public State getStepsState() {
        DirectionType direction = getDirection();
        Integer steps = this.steps;
        if (Objects.nonNull(direction) && Objects.nonNull(steps)) {
            return new DecimalType(DirectionType.CLOCK_WISE.equals(direction) ? steps.intValue() : -steps.intValue());
        }
        return UnDefType.NULL;
    }
}
