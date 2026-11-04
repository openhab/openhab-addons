/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Non-sensitive push-server settings returned by Robonect. */
@NonNullByDefault
public class PushServer {
    private @Nullable String url;
    private @Nullable String name;

    /**
     * Returns the URL.
     *
     * @return the URL, or {@code null} if not reported
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Returns the push server user name.
     *
     * @return the push server user name, or {@code null} if not reported
     */
    public @Nullable String getName() {
        return name;
    }
}
