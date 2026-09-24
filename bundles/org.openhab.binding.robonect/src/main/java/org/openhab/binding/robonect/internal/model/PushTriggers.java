/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Push-service polling interval returned by Robonect. */
@NonNullByDefault
public class PushTriggers {
    private @Nullable Integer interval;

    public @Nullable Integer getInterval() {
        return interval;
    }
}
