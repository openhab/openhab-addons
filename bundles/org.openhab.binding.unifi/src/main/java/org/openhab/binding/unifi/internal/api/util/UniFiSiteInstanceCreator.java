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
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;

/**
 *
 * The {@link UniFiSiteInstanceCreator} creates instances of {@link UniFiSite}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiSiteInstanceCreator implements InstanceCreator<UniFiSite> {

    private final UniFiControllerCache cache;

    public UniFiSiteInstanceCreator(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public UniFiSite createInstance(final @Nullable Type type) {
        if (UniFiSite.class.equals(type)) {
            return new UniFiSite(cache);
        } else {
            throw new JsonSyntaxException("Expected a UniFiSite type, but got " + type);
        }
    }
}
