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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Kinetic {

    private @Nullable ZonedDateTime createdAt;
    private @Nullable BigDecimal acceleration;
    private @Nullable Boolean moving;
    private @Nullable BigDecimal pace;
    private @Nullable BigDecimal speed;

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable BigDecimal getAcceleration() {
        return acceleration;
    }

    public @Nullable Boolean isMoving() {
        return moving;
    }

    public @Nullable BigDecimal getPace() {
        return pace;
    }

    public @Nullable BigDecimal getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("createdAt", createdAt).append("acceleration", acceleration)
                .append("moving", moving).append("pace", pace).append("speed", speed).toString();
    }
}
