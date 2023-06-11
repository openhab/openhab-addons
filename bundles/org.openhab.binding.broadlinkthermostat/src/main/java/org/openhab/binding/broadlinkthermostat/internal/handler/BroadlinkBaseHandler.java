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
package org.openhab.binding.broadlinkthermostat.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.BroadlinkConfig;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.BLDevice;

/**
 * The {@link BroadlinkBaseHandler} is the device handler class for a broadlink device.
 *
 * @author Florian Mueller - Initial contribution
 */
@NonNullByDefault
public abstract class BroadlinkBaseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkBaseHandler.class);

    @Nullable
    BLDevice blDevice;
    String host = "";
    String macAddress = "";

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    BroadlinkBaseHandler(Thing thing) {
        super(thing);
    }

    void authenticate(boolean reauth) {
        logger.debug("Authenticating with broadlink device {}...", thing.getLabel());
        try {
            BLDevice blDevice = this.blDevice;
            if (blDevice != null && blDevice.auth(reauth)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error while authenticating broadlink device " + thing.getLabel() + ":" + e.getMessage());
        }
    }

    @Override
    public void initialize() {
        BroadlinkConfig config = getConfigAs(BroadlinkConfig.class);
        host = config.getHost();
        macAddress = config.getMacAddress();
    }
}
