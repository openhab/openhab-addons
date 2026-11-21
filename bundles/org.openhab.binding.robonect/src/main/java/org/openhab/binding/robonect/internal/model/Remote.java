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

    /**
     * Returns the remote-start point name.
     *
     * @return the remote-start point name, or {@code null} if not reported
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Reports whether the point is visible.
     *
     * @return whether the point is visible, or {@code null} if not reported
     */
    public @Nullable Boolean getVisible() {
        return visible;
    }

    /**
     * Returns the remote-start path setting.
     *
     * @return the remote-start path setting, or {@code null} if not reported
     */
    public @Nullable Integer getPath() {
        return path;
    }

    /**
     * Returns the point selection proportion in percent.
     *
     * @return the point selection proportion in percent, or {@code null} if not reported
     */
    public @Nullable Integer getProportion() {
        return proportion;
    }

    /**
     * Returns the distance to the point in metres.
     *
     * @return the distance to the point in metres, or {@code null} if not reported
     */
    public @Nullable Integer getDistance() {
        return distance;
    }

    /**
     * Reports whether corridor cutting is enabled.
     *
     * @return whether corridor cutting is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getCorridorcut() {
        return corridorcut;
    }
}
