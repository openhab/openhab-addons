/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
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

    private static final int UPDATE_THING_CACHE_TIMEOUT = 3000; // 3 secs

    private final Logger logger = LoggerFactory.getLogger(SurePetcareBaseObjectHandler.class);

    protected SurePetcareAPIHelper petcareAPI;

    protected ExpiringCache<Integer> updateThingCache = new ExpiringCache<Integer>(UPDATE_THING_CACHE_TIMEOUT,
            this::refreshCache);

    public SurePetcareBaseObjectHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
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
    public void updateProperties(Map<String, String> properties) {
        logger.debug("Updating thing properties");
        super.updateProperties(properties);
    }

    private Integer refreshCache() {
        logger.debug("cache has timed out, we refresh the values in the thing");
        updateThing();
        // we don't care about the cache content, we just return a zero
        return 0;
    }

    /**
     * Updates all channels of a thing.
     */
    protected abstract void updateThing();

}
