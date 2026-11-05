/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Push-service settings returned by Robonect.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Push {
    private @Nullable PushServer server;
    private @Nullable PushTriggers trigger;

    /**
     * Returns the push server settings.
     *
     * @return the push server settings, or {@code null} if not reported
     */
    public @Nullable PushServer getServer() {
        return server;
    }

    /**
     * Returns the push trigger settings.
     *
     * @return the push trigger settings, or {@code null} if not reported
     */
    public @Nullable PushTriggers getTrigger() {
        return trigger;
    }
}
