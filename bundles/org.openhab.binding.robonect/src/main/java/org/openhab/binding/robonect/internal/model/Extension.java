/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** State of one optional GPIO or output extension. */
@NonNullByDefault
public class Extension {
    private @Nullable Boolean flashonerror;
    private @Nullable Boolean inverted;
    private @Nullable Boolean status;

    public @Nullable Boolean getFlashonerror() { return flashonerror; }
    public @Nullable Boolean getInverted() { return inverted; }
    public @Nullable Boolean getStatus() { return status; }
}
