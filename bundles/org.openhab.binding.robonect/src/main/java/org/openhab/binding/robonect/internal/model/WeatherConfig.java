/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Mowing restrictions configured for the Robonect weather service. */
@NonNullByDefault
public class WeatherConfig {
    private @Nullable Integer maxrain;
    private @Nullable Integer mintemp;
    private @Nullable Integer maxtemp;
    private @Nullable Integer minhumidity;
    private @Nullable Integer maxhumidity;
    private @Nullable Boolean dontmowduringday;
    private @Nullable Boolean dontmowduringnight;

    public @Nullable Integer getMaxrain() {
        return maxrain;
    }

    public @Nullable Integer getMintemp() {
        return mintemp;
    }

    public @Nullable Integer getMaxtemp() {
        return maxtemp;
    }

    public @Nullable Integer getMinhumidity() {
        return minhumidity;
    }

    public @Nullable Integer getMaxhumidity() {
        return maxhumidity;
    }

    public @Nullable Boolean getDontmowduringday() {
        return dontmowduringday;
    }

    public @Nullable Boolean getDontmowduringnight() {
        return dontmowduringnight;
    }
}
