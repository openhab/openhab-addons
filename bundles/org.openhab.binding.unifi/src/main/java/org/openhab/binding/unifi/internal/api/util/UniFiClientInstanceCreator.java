/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiUnknownClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWirelessClient;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link UniFiClientInstanceCreator} creates instances of {@link UniFiClient}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiClientInstanceCreator implements InstanceCreator<UniFiClient> {

    private final UniFiControllerCache cache;

    public UniFiClientInstanceCreator(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public UniFiClient createInstance(final @Nullable Type type) {
        if (UniFiUnknownClient.class.equals(type)) {
            return new UniFiUnknownClient(cache);
        }
        if (UniFiWirelessClient.class.equals(type)) {
            return new UniFiWirelessClient(cache);
        }
        if (UniFiWiredClient.class.equals(type)) {
            return new UniFiWiredClient(cache);
        } else {
            throw new JsonSyntaxException("Expected a UniFi Client type, but got " + type);
        }
    }
}
