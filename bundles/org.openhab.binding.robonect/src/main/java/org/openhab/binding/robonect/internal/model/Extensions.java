/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Optional extension state returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Extensions {
    private @Nullable Extension gpio1;
    private @Nullable Extension gpio2;
    private @Nullable Extension out1;
    private @Nullable Extension out2;

    /**
     * Returns the first GPIO extension.
     *
     * @return the first GPIO extension, or {@code null} if absent
     */
    public @Nullable Extension getGpio1() {
        return gpio1;
    }

    /**
     * Returns the second GPIO extension.
     *
     * @return the second GPIO extension, or {@code null} if absent
     */
    public @Nullable Extension getGpio2() {
        return gpio2;
    }

    /**
     * Returns the first output extension.
     *
     * @return the first output extension, or {@code null} if absent
     */
    public @Nullable Extension getOut1() {
        return out1;
    }

    /**
     * Returns the second output extension.
     *
     * @return the second output extension, or {@code null} if absent
     */
    public @Nullable Extension getOut2() {
        return out2;
    }
}
