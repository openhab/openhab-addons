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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.github.filosganga.geogson.model.Geometry;
import com.github.filosganga.geogson.model.positions.SinglePosition;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class Position {

    private @Nullable ZonedDateTime updatedAt;
    private @Nullable ZonedDateTime createdAt;
    private @Nullable Geometry<SinglePosition> geometry;
    private @Nullable Properties properties;
    private @Nullable String type;

    public @Nullable ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public @Nullable ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable Geometry<SinglePosition> getGeometry() {
        return geometry;
    }

    public @Nullable Properties getProperties() {
        return properties;
    }

    public @Nullable String getType() {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("updatedAt", updatedAt).append("createdAt", createdAt)
                .append("geometry", geometry).append("properties", properties).append("type", type).toString();
    }
}
