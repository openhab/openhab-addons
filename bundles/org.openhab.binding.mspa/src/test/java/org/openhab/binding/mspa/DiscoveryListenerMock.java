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
package org.openhab.binding.mspa;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * {@link DiscoveryListenerMock} listen to discovery results
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class DiscoveryListenerMock implements DiscoveryListener {
    List<DiscoveryResult> results = new ArrayList<>();

    public List<DiscoveryResult> getResults() {
        return results;
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        results.add(result);
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
    }

    @Override
    public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, Instant timestamp,
            @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        return null;
    }
}
