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
package org.openhab.binding.ddwrt.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener for cache entity changes. Handlers register to be notified
 * when their specific entity is modified in the cache.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public interface CacheChangeListener {

    /**
     * Called when the entity this listener is interested in has changed.
     * Implementations should update their channel states from the cache.
     * This is called on the refresh thread; implementations must be lightweight.
     */
    void onCacheChanged();
}
