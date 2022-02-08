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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareBaseObjectHandler} is responsible for handling the things created to represent Sure Petcare
 * objects.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public abstract class SurePetcareBaseObjectHandler extends BaseThingHandler {

    private static final int CACHE_TIMEOUT_SECOND = 3;

    private final Logger logger = LoggerFactory.getLogger(SurePetcareBaseObjectHandler.class);

    protected final SurePetcareAPIHelper petcareAPI;

    protected ExpiringCache<Integer> updateThingCache = new ExpiringCache<>(Duration.ofSeconds(CACHE_TIMEOUT_SECOND),
            this::refreshCache);

    public SurePetcareBaseObjectHandler(Thing thing, final SurePetcareAPIHelper petcareAPI) {
        super(thing);
        this.petcareAPI = petcareAPI;
    }

    @Override
    public void initialize() {
        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        }
    }

    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        super.updateProperties(properties);
    }

    private @Nullable Integer refreshCache() {
        logger.debug("cache has timed out, we refresh the values in the thing");
        updateThing();
        // we don't care about the cache content, we just return an empty object
        return Integer.MIN_VALUE;
    }

    /**
     * Updates all channels of a thing.
     */
    protected abstract void updateThing();
}
