/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.modbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.thing.ThingStatus;

/**
 * Lazy updating thing status
 *
 * This class was introduced the limit to calls BaseThingHandler.updateStatus in case there's not really anything to
 * update. During very high frequency polling, it turns out that updateStatus starts to show up in CPU usage.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class LazyThingStatusUpdater {

    public static final long CACHE_MILLIS = 1000L;

    @Nullable
    private volatile ExpiringCache<ThingStatus> cache;

    @Nullable
    private volatile ThingStatus lastStatus;

    /**
     * Notify that status has updated
     *
     * The behaviour depends on previous update:
     * - If the this is the first time status has been updated: the action is run immediately.
     * - If the status has not been changed from the previous: run action immediately if the previous update is older
     * than CACHE_MILLIS, otherwise, no action is taken.
     * - If the status has changed from the previous: the action is run immediately.
     *
     * @param status new update
     * @param action runnable to call in cases where update is needed
     */
    public synchronized void statusUpdated(ThingStatus status, Runnable action) {
        ExpiringCache<ThingStatus> cache = this.cache;
        if (cache == null || status != lastStatus) {
            // status changed, update immediately
            cache = new ExpiringCache<>(CACHE_MILLIS, () -> {
                action.run();
                return status;
            });
        }
        lastStatus = status;
        cache.refreshValue();

    }

    /**
     * Reset this updater to its initial state
     */
    public synchronized void invalidate() {
        cache = null;
        lastStatus = null;
    }

}
