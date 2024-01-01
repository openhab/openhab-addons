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
package org.openhab.binding.dscalarm.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.dscalarm.internal.DSCAlarmBindingConstants;
import org.openhab.binding.dscalarm.internal.config.EnvisalinkBridgeConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the EyezOn Envisalink 3/2DS Ethernet interface.
 *
 * @author Russell Stephens - Initial Contribution
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.dscalarm")
public class DSCAlarmBridgeDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(DSCAlarmBridgeDiscovery.class);

    private EnvisalinkBridgeDiscovery envisalinkBridgeDiscovery = new EnvisalinkBridgeDiscovery(this);

    public DSCAlarmBridgeDiscovery() {
        super(DSCAlarmBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15, true);
    }

    @Override
    protected void startScan() {
        logger.trace("Start DSC Alarm Bridge discovery.");
        scheduler.execute(envisalinkBridgeDiscoveryRunnable);
    }

    private Runnable envisalinkBridgeDiscoveryRunnable = () -> {
        envisalinkBridgeDiscovery.discoverBridge();
    };

    /**
     * Method to add an Envisalink Bridge to the Inbox.
     *
     * @param ipAddress
     */
    public void addEnvisalinkBridge(String ipAddress) {
        logger.trace("addBridge(): Adding new Envisalink Bridge on {} to inbox", ipAddress);

        String bridgeID = ipAddress.replace('.', '_');
        Map<String, Object> properties = new HashMap<>(0);
        properties.put(EnvisalinkBridgeConfiguration.IP_ADDRESS, ipAddress);

        try {
            ThingUID thingUID = new ThingUID(DSCAlarmBindingConstants.ENVISALINKBRIDGE_THING_TYPE, bridgeID);

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("EyezOn Envisalink Bridge - " + ipAddress).build());

            logger.trace("addBridge(): '{}' was added to inbox.", thingUID);
        } catch (Exception e) {
            logger.error("addBridge(): Error", e);
        }
    }
}
