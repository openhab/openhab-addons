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

import static org.openhab.binding.ring.RingBindingConstants.CHANNEL_STATUS_BATTERY;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.data.RingDeviceTO;
import org.openhab.binding.ring.internal.data.Stickupcam;
import org.openhab.binding.ring.internal.errors.DeviceNotFoundException;
import org.openhab.binding.ring.internal.errors.IllegalDeviceClassException;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

import com.google.gson.Gson;

/**
 * The handler for a Ring Video Stickup Cam.
 *
 * @author Chris Milbert - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 *
 */

@NonNullByDefault
public class StickupcamHandler extends RingDeviceHandler {
    private int lastBattery = -1;

    public StickupcamHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Stickupcam handler");
        super.initialize();

        RingDeviceRegistry registry = RingDeviceRegistry.getInstance();
        String id = getThing().getUID().getId();
        if (registry.isInitialized()) {
            try {
                linkDevice(id, Stickupcam.class);
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
        logger.debug("StickupcamHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
        }
        RingDeviceTO deviceTO = gson.fromJson(device.getJsonObject(), RingDeviceTO.class);
        if ((deviceTO != null) && (deviceTO.health.batteryPercentage != lastBattery)) {
            logger.debug("Battery Level: {}", deviceTO.battery);
            ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_BATTERY);
            updateState(channelUID, new DecimalType(deviceTO.health.batteryPercentage));
            lastBattery = deviceTO.health.batteryPercentage;
        } else if (deviceTO != null) {
            logger.debug("Battery Level Unchanged for {} - {} vs {}", getThing().getUID().getId(),
                    deviceTO.health.batteryPercentage, lastBattery);

        }
    }
}
