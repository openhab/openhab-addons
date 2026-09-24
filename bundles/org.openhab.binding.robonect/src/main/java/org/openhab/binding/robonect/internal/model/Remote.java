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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A configured remote-start point returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Remote {
    private @Nullable String name;
    private @Nullable Boolean visible;
    private @Nullable Integer path;
    private @Nullable Integer proportion;
    private @Nullable Integer distance;
    private @Nullable Boolean corridorcut;

    public @Nullable String getName() {
        return name;
    }

    public @Nullable Boolean getVisible() {
        return visible;
    }

    public @Nullable Integer getPath() {
        return path;
    }

    public @Nullable Integer getProportion() {
        return proportion;
    }

    public @Nullable Integer getDistance() {
        return distance;
    }

    public @Nullable Boolean getCorridorcut() {
        return corridorcut;
    }
}
