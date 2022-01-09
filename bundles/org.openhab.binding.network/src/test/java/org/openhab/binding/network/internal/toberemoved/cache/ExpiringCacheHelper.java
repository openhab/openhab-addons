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
package org.openhab.binding.network.internal.toberemoved.cache;

/**
 * Helper class to make the package private cacheUpdater field available for tests.
 *
 * @author David Graeff
 */
public class ExpiringCacheHelper {
    public static long expireTime(@SuppressWarnings("rawtypes") ExpiringCacheAsync cache) {
        return cache.expiresAt;
    }
}
