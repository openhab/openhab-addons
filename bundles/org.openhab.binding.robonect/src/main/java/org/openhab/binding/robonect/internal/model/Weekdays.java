/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Days configured for a timer.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Weekdays {
    private @Nullable Boolean mo, tu, we, th, fr, sa, su;

    public @Nullable Boolean getMo() {
        return mo;
    }

    public @Nullable Boolean getTu() {
        return tu;
    }

    public @Nullable Boolean getWe() {
        return we;
    }

    public @Nullable Boolean getTh() {
        return th;
    }

    public @Nullable Boolean getFr() {
        return fr;
    }

    public @Nullable Boolean getSa() {
        return sa;
    }

    public @Nullable Boolean getSu() {
        return su;
    }
}
