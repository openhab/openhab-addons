/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Push-service settings returned by Robonect. */
@NonNullByDefault
public class Push {
    private @Nullable PushServer server;
    private @Nullable PushTriggers trigger;

    public @Nullable PushServer getServer() {
        return server;
    }

    public @Nullable PushTriggers getTrigger() {
        return trigger;
    }
}
