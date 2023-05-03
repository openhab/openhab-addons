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
package org.openhab.binding.rainsoft.handler;

import static org.openhab.binding.rainsoft.RainSoftBindingConstants.*;

import java.math.BigDecimal;

import org.openhab.binding.rainsoft.internal.RainSoftDeviceRegistry;
import org.openhab.binding.rainsoft.internal.data.WCS;
import org.openhab.binding.rainsoft.internal.errors.DeviceNotFoundException;
import org.openhab.binding.rainsoft.internal.errors.IllegalDeviceClassException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The handler for a Water Conditioning System.
 *
 * @author Ben Rosenblum - Initial contribution
 *
 */

public class WCSHandler extends RainSoftDeviceHandler {

    public WCSHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WCS handler");
        super.initialize();

        RainSoftDeviceRegistry registry = RainSoftDeviceRegistry.getInstance();
        String id = getThing().getUID().getId();
        if (registry.isInitialized()) {
            try {
                linkDevice(id, WCS.class);
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
                    "Waiting for RainSoftAccount to initialize");
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        if (this.refreshJob == null) {
            Configuration config = getThing().getConfiguration();
            Integer refreshInterval = ((BigDecimal) config.get("refreshInterval")).intValueExact();
            startAutomaticRefresh(refreshInterval);
        }
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
        if (device == null) {
            initialize();
        }
    }
}
