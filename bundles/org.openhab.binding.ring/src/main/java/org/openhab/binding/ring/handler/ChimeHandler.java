/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ring.handler;

import java.math.BigDecimal;

import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;

/**
 * This clock calculates the sunrise and sunset times based on the
 * location and current date and timezone.
 *
 * @author Wim Vissers - Initial contribution
 *
 */

public class ChimeHandler extends AbstractRingHandler {

    public ChimeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Chime handler");
        super.initialize();
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        Configuration config = getThing().getConfiguration();
        Integer refreshInterval = ((BigDecimal) config.get("refreshInterval")).intValueExact();
        startAutomaticRefresh(refreshInterval);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void refreshState() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void minuteTick() {
        // TODO Auto-generated method stub
    }
}
