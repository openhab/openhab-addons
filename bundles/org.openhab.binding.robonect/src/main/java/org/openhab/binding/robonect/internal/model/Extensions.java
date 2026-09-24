/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Optional extension state returned by the Robonect API. */
@NonNullByDefault
public class Extensions {
    private @Nullable Extension gpio1;
    private @Nullable Extension gpio2;
    private @Nullable Extension out1;
    private @Nullable Extension out2;

    public @Nullable Extension getGpio1() {
        return gpio1;
    }

    public @Nullable Extension getGpio2() {
        return gpio2;
    }

    public @Nullable Extension getOut1() {
        return out1;
    }

    public @Nullable Extension getOut2() {
        return out2;
    }
}
