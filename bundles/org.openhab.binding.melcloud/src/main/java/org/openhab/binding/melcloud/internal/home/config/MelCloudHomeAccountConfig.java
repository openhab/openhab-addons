/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.melcloud.internal.home.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Config class for the {@code home-account} bridge. The {@link #username}/{@link #password} pair is used to run
 * the headless MELCloud Home login.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAccountConfig {

    public String username = "";
    public String password = "";

    /**
     * Whether to connect to the realtime push channel to narrow the staleness between {@code /context} polls.
     * Best-effort: failures fall back to plain polling. Defaults to {@code true}.
     */
    public boolean enableRealtimeUpdates = true;

    @Override
    public String toString() {
        return "[username=" + (username.isEmpty() ? "<empty>" : username) + ", password="
                + (password.isEmpty() ? "<empty>" : "<redacted>") + ", enableRealtimeUpdates=" + enableRealtimeUpdates
                + "]";
    }
}
