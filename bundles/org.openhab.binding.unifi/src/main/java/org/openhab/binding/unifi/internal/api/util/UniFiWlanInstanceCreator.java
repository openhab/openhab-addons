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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.model.UniFiWlan;

import com.google.gson.InstanceCreator;

/**
 * The {@link UniFiWlanInstanceCreator} creates instances of {@link UniFiWlan}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UniFiWlanInstanceCreator implements InstanceCreator<UniFiWlan> {

    private final UniFiControllerCache cache;

    public UniFiWlanInstanceCreator(final UniFiControllerCache cache) {
        this.cache = cache;
    }

    @Override
    public UniFiWlan createInstance(final @Nullable Type type) {
        return UniFiWlan.class.equals(type) ? new UniFiWlan(cache) : null;
    }
}
