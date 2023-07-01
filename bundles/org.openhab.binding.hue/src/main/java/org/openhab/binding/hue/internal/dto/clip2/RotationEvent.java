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
import org.openhab.binding.hue.internal.dto.clip2.enums.RotationEventType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * DTO for rotation event of a dial switch.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class RotationEvent {
    private @Nullable String action;
    private @Nullable Rotation rotation;

    public @Nullable RotationEventType getAction() {
        String action = this.action;
        return Objects.nonNull(action) ? RotationEventType.valueOf(action.toUpperCase()) : null;
    }

    public State getActionState() {
        RotationEventType action = getAction();
        return Objects.nonNull(action) ? new StringType(action.name()) : UnDefType.NULL;
    }

    public @Nullable Rotation getRotation() {
        return rotation;
    }

    public State getStepsState() {
        Rotation rotation = this.rotation;
        return Objects.nonNull(rotation) ? rotation.getStepsState() : UnDefType.NULL;
    }
}
