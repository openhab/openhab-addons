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
package org.openhab.binding.broadlinkthermostat.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatBindingConstants;
import org.openhab.binding.broadlinkthermostat.internal.BroadlinkThermostatConfig;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.BLDevice;

/**
 * The {@link BroadlinkThermostatHandler} is the device handler class for a broadlinkthermostat device.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public abstract class BroadlinkThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkThermostatHandler.class);

    @Nullable
    BLDevice blDevice;
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingHandler");
    private @Nullable ScheduledFuture<?> scanJob;
    protected @Nullable String host;
    @Nullable
    String mac;
    private String deviceDescription;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    BroadlinkThermostatHandler(Thing thing) {
        super(thing);
        String deviceDescription = thing.getProperties().get(BroadlinkThermostatBindingConstants.DESCRIPTION);
        this.deviceDescription = deviceDescription == null ? "" : deviceDescription;
    }

    void authenticate() {
        logger.debug("Authenticating with broadlinkthermostat device {}...", thing.getLabel());
        try {
            if (blDevice.auth()) {
                logger.debug("Authentication for device {} successful", thing.getLabel());
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error while authenticating broadlinkthermostat device " + thing.getLabel() + ":" + e.getMessage());
        }
    }

    @Override
    public void initialize() {
        BroadlinkThermostatConfig config = getConfigAs(BroadlinkThermostatConfig.class);
        host = config.getHost();
        mac = config.getMac();

        // schedule a new scan every minute
        scanJob = scheduler.scheduleWithFixedDelay(this::refreshData, 0, 1, TimeUnit.MINUTES);
    }

    protected abstract void refreshData();

    @Override
    public void dispose() {
        if (scanJob != null) {
            scanJob.cancel(true);
        }
    }
}
