/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vallox.internal.se.cache;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;

/**
 * Expiring cache object to put into {@link ValloxExpiringChacheMap}.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ExpiringCacheObject {

    private final long expiresAt;
    private final Telegram telegram;

    /**
     * Create a new instance.
     *
     * @param expiry duration of how low until the telegram expires
     * @param telegram the telegram to cache
     */
    public ExpiringCacheObject(long expiry, Telegram telegram) {
        this.expiresAt = System.nanoTime() + expiry;
        this.telegram = telegram;
    }

    /**
     * Check if value has expired
     *
     * @return True if cached value is expired
     */
    public boolean isExpired() {
        return System.nanoTime() > expiresAt;
    }

    /**
     * Get telegram
     *
     * @return
     */
    public Telegram get() {
        return telegram;
    }
}
