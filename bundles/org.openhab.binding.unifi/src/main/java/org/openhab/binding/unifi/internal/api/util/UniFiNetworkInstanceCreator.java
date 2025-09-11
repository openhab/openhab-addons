/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.unifi.internal.api.dto.UniFiNetwork;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link UniFiNetworkInstanceCreator} creates instances of {@link UniFiNetwork}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class UniFiNetworkInstanceCreator implements InstanceCreator<UniFiNetwork> {

    private final UniFiControllerCache cache;

    public UniFiNetworkInstanceCreator(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public UniFiNetwork createInstance(final @Nullable Type type) {
        if (UniFiNetwork.class.equals(type)) {
            return new UniFiNetwork(cache);
        } else {
            throw new JsonSyntaxException("Expected a UniFiNetwork type, but got " + type);
        }
    }
}
