/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Response to a push-service request. */
@NonNullByDefault
public class PushInfo extends RobonectAnswer {
    private @Nullable Push push;

    public @Nullable Push getPush() {
        return push;
    }
}
