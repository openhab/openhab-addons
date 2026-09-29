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
 * Listener interface for device refresh completion events.
 * Allows discovery and other components to react when new data is available in the cache.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public interface RefreshListener {

    /**
     * Called after a device completes a full refresh cycle and the cache is updated.
     *
     * @param device the device that completed refresh
     */
    void onRefreshComplete(DDWRTBaseDevice device);
}
