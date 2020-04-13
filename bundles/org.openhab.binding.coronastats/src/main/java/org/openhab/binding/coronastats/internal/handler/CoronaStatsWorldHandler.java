/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.coronastats.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.coronastats.internal.dto.CoronaStats;
import org.openhab.binding.coronastats.internal.dto.CoronaStatsWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CoronaStatsCountryHandler} is the handler for country thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsWorldHandler extends CoronaStatsThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CoronaStatsWorldHandler.class);

    public CoronaStatsWorldHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Corona Stats world handler");

        CoronaStatsBridgeHandler handler = getBridgeHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge handler missing");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("CoronaStats world handler disposes.");
    }

    public void notifyOnUpdate(CoronaStats coronaStats) {
        CoronaStatsWorld world = coronaStats.getWorld();
        if (world == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "World stats not found");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        world.getChannelsStateMap().forEach(this::updateState);
    }
}
