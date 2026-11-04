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

    /**
     * Reports whether Monday is enabled.
     *
     * @return whether Monday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getMo() {
        return mo;
    }

    /**
     * Reports whether Tuesday is enabled.
     *
     * @return whether Tuesday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getTu() {
        return tu;
    }

    /**
     * Reports whether Wednesday is enabled.
     *
     * @return whether Wednesday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getWe() {
        return we;
    }

    /**
     * Reports whether Thursday is enabled.
     *
     * @return whether Thursday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getTh() {
        return th;
    }

    /**
     * Reports whether Friday is enabled.
     *
     * @return whether Friday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getFr() {
        return fr;
    }

    /**
     * Reports whether Saturday is enabled.
     *
     * @return whether Saturday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getSa() {
        return sa;
    }

    /**
     * Reports whether Sunday is enabled.
     *
     * @return whether Sunday is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getSu() {
        return su;
    }
}
