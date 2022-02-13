/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.DEBOUNCE_DELAY;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NeoHubDebouncer} determines if change events should be forwarded
 * to a channel
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class NeoHubDebouncer {

    private final Map<String, DebounceDelay> channels = new HashMap<>();

    @SuppressWarnings("null")
    @NonNullByDefault
    static class DebounceDelay {

        private long expireTime;

        public DebounceDelay(Boolean enabled) {
            if (enabled) {
                expireTime = new Date().getTime() + (DEBOUNCE_DELAY * 1000);
            }
        }

        public boolean timeExpired() {
            return (expireTime < new Date().getTime());
        }
    }

    public NeoHubDebouncer() {
    }

    public void initialize(String channelId) {
        channels.put(channelId, new DebounceDelay(true));
    }

    @SuppressWarnings("null")
    public boolean timeExpired(String channelId) {
        return (channels.containsKey(channelId) ? channels.get(channelId).timeExpired() : true);
    }
}
