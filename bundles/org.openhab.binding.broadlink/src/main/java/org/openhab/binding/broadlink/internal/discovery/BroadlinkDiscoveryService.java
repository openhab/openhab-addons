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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocketListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Broadlink discovery implementation.
 *
 * @author Cato Sognen - Initial contribution
 * @author John Marshall - Rewrite for V2 and V3
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.broadlink")
public class BroadlinkDiscoveryService extends AbstractDiscoveryService
        implements BroadlinkSocketListener, DiscoveryFinishedListener {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkDiscoveryService.class);
    private int foundCount = 0;

    public BroadlinkDiscoveryService() {
        super(BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.keySet(), 10, true);
        logger.debug("BroadlinkDiscoveryService - Constructed");
    }

    @Override
    public void startScan() {
        foundCount = 0;
        DiscoveryProtocol.beginAsync(this, 10000L, this, logger);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onDataReceived(String remoteAddress, int remotePort, String remoteMAC, ThingTypeUID thingTypeUID,
            int model) {
        logger.trace("Data received during Broadlink device discovery: from {}:{} [{}]", remoteAddress, remotePort,
                remoteMAC);
        foundCount++;
        discoveryResultSubmission(remoteAddress, remotePort, remoteMAC, thingTypeUID, model);
    }

    private void discoveryResultSubmission(String remoteAddress, int remotePort, String remoteMAC,
            ThingTypeUID thingTypeUID, int model) {
        String modelAsHexString = String.format("%x", model);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding new Broadlink device ({} => {}) at {} with mac '{}' to Smarthome inbox",
                    modelAsHexString, thingTypeUID, remoteAddress, remoteMAC);
        }
        Map<String, Object> properties = new HashMap<String, Object>(6);
        properties.put("ipAddress", remoteAddress);
        properties.put("port", Integer.valueOf(remotePort));
        properties.put(Thing.PROPERTY_MAC_ADDRESS, remoteMAC);
        properties.put("deviceType", modelAsHexString);
        ThingUID thingUID = new ThingUID(thingTypeUID, remoteMAC.replace(":", "-"));
        if (logger.isDebugEnabled()) {
            logger.debug("Device '{}' discovered at '{}'.", thingUID, remoteAddress);
        }

        if (BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.containsKey(thingTypeUID)) {
            notifyThingDiscovered(thingTypeUID, thingUID, remoteAddress, properties);
        } else {
            logger.warn("Discovered a {} but do not know how to support it at this time, please report!", thingTypeUID);
        }
    }

    private void notifyThingDiscovered(ThingTypeUID thingTypeUID, ThingUID thingUID, String remoteAddress,
            Map<String, Object> properties) {
        String deviceHumanName = BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.get(thingTypeUID);
        String label = deviceHumanName + " [" + remoteAddress + "]";
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withLabel(label).withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                .build();

        thingDiscovered(result);
    }

    @Override
    public void onDiscoveryFinished() {
        logger.info("Discovery complete. Found {} Broadlink devices", foundCount);
    }
}
