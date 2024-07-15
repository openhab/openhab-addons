/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.config.BroadlinkDeviceConfiguration;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocketListener;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This agent exploits the well-known Broadlink device discovery process
 * to attempt to "rediscover" a previously-discovered dynamically-addressed
 * Broadlink device that may have recently changed IP address.
 *
 * This agent has NOTHING TO DO WITH the initial device discovery process.
 * It is explicitly initiated when a dynamically-addressed Broadlink device
 * appears to have dropped off the network.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class DeviceRediscoveryAgent implements BroadlinkSocketListener, DiscoveryFinishedListener {

    private final Logger logger = LoggerFactory.getLogger(DeviceRediscoveryAgent.class);
    private final BroadlinkDeviceConfiguration missingThingConfig;
    private final DeviceRediscoveryListener drl;
    private boolean foundDevice = false;

    public DeviceRediscoveryAgent(BroadlinkDeviceConfiguration missingThingConfig, DeviceRediscoveryListener drl) {
        this.missingThingConfig = missingThingConfig;
        this.drl = drl;
    }

    public void attemptRediscovery() {
        logger.debug("DeviceRediscoveryAgent - Beginning Broadlink device scan for missing {}",
                missingThingConfig.toString());
        DiscoveryProtocol.beginAsync(this, 5000L, this, logger);
    }

    public void onDataReceived(String remoteAddress, int remotePort, String remoteMAC, ThingTypeUID thingTypeUID,
            int model) {
        logger.trace("Data received during Broadlink device rediscovery: from {}:{} [{}]", remoteAddress, remotePort,
                remoteMAC);

        // if this thing matches the missingThingConfig, we've found it!
        logger.trace("Comparing with desired mac: {}", missingThingConfig.getMacAddressAsString());

        if (missingThingConfig.getMacAddressAsString().equals(remoteMAC)) {
            logger.debug("We have a match for target MAC {} at {} - reassociate!", remoteMAC, remoteAddress);
            foundDevice = true;
            this.drl.onDeviceRediscovered(remoteAddress);
        }
    }

    public void onDiscoveryFinished() {
        if (!foundDevice) {
            this.drl.onDeviceRediscoveryFailure();
        }
    }
}
