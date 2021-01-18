/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.handler;

import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyApiHelper;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyBaseMeterPointHandler} is a base class responsible for handling commands, which are sent to
 * one of the channels.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public abstract class OctopusEnergyBaseMeterPointHandler extends BaseThingHandler {

    private static final int CACHE_TIMEOUT_SECOND = 3;

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyBaseMeterPointHandler.class);

    protected final ExpiringCache<Integer> updateThingCache = new ExpiringCache<>(
            Duration.ofSeconds(CACHE_TIMEOUT_SECOND), this::refreshCache);

    protected final OctopusEnergyApiHelper apiHelper;

    public OctopusEnergyBaseMeterPointHandler(Thing thing, OctopusEnergyApiHelper apiHelper) {
        super(thing);
        this.apiHelper = apiHelper;
    }

    @Override
    public void initialize() {
        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            synchronized (updateThingCache) {
                updateThingCache.getValue();
            }
        }
    }

    @Override
    public void updateProperties(Map<String, String> properties) {
        super.updateProperties(properties);
    }

    protected abstract void updateThing();

    private @Nullable Integer refreshCache() {
        logger.debug("cache has expired, we refresh the values in the thing");
        updateThing();
        return Integer.MIN_VALUE;
    }
}
