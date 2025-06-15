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
package org.openhab.binding.ring.handler;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.data.Chime;
import org.openhab.binding.ring.internal.errors.DeviceNotFoundException;
import org.openhab.binding.ring.internal.errors.IllegalDeviceClassException;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

import com.google.gson.Gson;

/**
 * The handler for a Ring Chime.
 *
 * @author Ben Rosenblum - Initial contribution
 *
 */

@NonNullByDefault
public class ChimeHandler extends RingDeviceHandler {
    public ChimeHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Chime handler");
        super.initialize();

        RingDeviceRegistry registry = getDeviceRegistry();
        String id = getThing().getUID().getId();
        if (registry != null && registry.isInitialized()) {
            try {
                linkDevice(id, Chime.class);
                updateStatus(ThingStatus.ONLINE);
            } catch (DeviceNotFoundException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device with id '" + id + "' not found");
            } catch (IllegalDeviceClassException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device with id '" + id + "' of wrong type");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Waiting for RingAccount to initialize");
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        if (this.refreshJob == null) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = ConfigParser
                    .valueAsOrElse(config.get("refreshInterval"), BigDecimal.class, BigDecimal.valueOf(500)).intValue();
            startAutomaticRefresh(refreshInterval);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Do Nothing
    }

    @Override
    protected void refreshState() {
        // Do Nothing
    }

    @Override
    protected void minuteTick() {
        logger.debug("ChimeHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
        }
    }
}
