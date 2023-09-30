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
package org.openhab.binding.digitalstrom.internal.discovery;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.handler.BridgeHandler;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZoneTemperatureControlDiscoveryService} discovers all digitalSTROM zones which have temperature control
 * configured. The thing-type has to be given to the
 * {@link #ZoneTemperatureControlDiscoveryService(BridgeHandler, ThingTypeUID)} as
 * {@link org.openhab.core.thing.ThingTypeUID}. The supported {@link org.openhab.core.thing.ThingTypeUID}
 * can be found at
 * {@link org.openhab.binding.digitalstrom.internal.handler.ZoneTemperatureControlHandler#SUPPORTED_THING_TYPES}
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class ZoneTemperatureControlDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(ZoneTemperatureControlDiscoveryService.class);
    BridgeHandler bridgeHandler;
    private final ThingUID bridgeUID;
    private final String thingTypeID;

    public static final int TIMEOUT = 10;

    /**
     * Creates a new {@link ZoneTemperatureControlDiscoveryService}.
     *
     * @param bridgeHandler must not be null
     * @param supportedThingType must not be null
     * @throws IllegalArgumentException see {@link AbstractDiscoveryService#AbstractDiscoveryService(int)}
     */
    public ZoneTemperatureControlDiscoveryService(BridgeHandler bridgeHandler, ThingTypeUID supportedThingType)
            throws IllegalArgumentException {
        super(new HashSet<>(Arrays.asList(supportedThingType)), TIMEOUT, true);
        bridgeUID = bridgeHandler.getThing().getUID();
        this.bridgeHandler = bridgeHandler;
        thingTypeID = supportedThingType.getId();
    }

    @Override
    protected void startScan() {
        for (TemperatureControlStatus tempConStat : bridgeHandler.getTemperatureControlStatusFromAllZones()) {
            internalConfigChanged(tempConStat);
        }
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivate discovery service for zone teperature control type remove thing types {}",
                super.getSupportedThingTypes());
        removeOlderResults(new Date().getTime());
    }

    /**
     * Method for the background discovery
     *
     * @see org.openhab.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener#configChanged(TemperatureControlStatus)
     * @param tempControlStatus can be null
     */
    public void configChanged(TemperatureControlStatus tempControlStatus) {
        if (isBackgroundDiscoveryEnabled()) {
            internalConfigChanged(tempControlStatus);
        }
    }

    private void internalConfigChanged(TemperatureControlStatus tempControlStatus) {
        if (tempControlStatus == null) {
            return;
        }
        if (tempControlStatus.isNotSetOff()) {
            logger.debug("found configured zone TemperatureControlStatus = {}", tempControlStatus);

            ThingUID thingUID = getThingUID(tempControlStatus);
            if (thingUID != null) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(DigitalSTROMBindingConstants.ZONE_ID, tempControlStatus.getZoneID());
                String zoneName = tempControlStatus.getZoneName();
                if (zoneName == null || zoneName.isBlank()) {
                    zoneName = tempControlStatus.getZoneID().toString();
                }
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(zoneName).build();
                thingDiscovered(discoveryResult);

            }
        }
    }

    private ThingUID getThingUID(TemperatureControlStatus tempControlStatus) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, thingTypeID);
        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingID = tempControlStatus.getZoneID().toString();
            return new ThingUID(thingTypeUID, bridgeUID, thingID);
        } else {
            return null;
        }
    }

    /**
     * Returns the ID of this {@link ZoneTemperatureControlDiscoveryService}.
     *
     * @return id of the service
     */
    public String getID() {
        return thingTypeID;
    }
}
