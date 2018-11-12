/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
  * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.evohome.EvohomeBindingConstants;
import org.openhab.binding.evohome.handler.AccountStatusListener;
import org.openhab.binding.evohome.handler.EvohomeAccountBridgeHandler;
import org.openhab.binding.evohome.internal.api.models.v2.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.response.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvohomeDiscoveryService} class is capable of discovering the available data from Evohome
 *
 * @author Neil Renaud - Initial contribution
 * @author Jasper van Zuijlen - Background discovery
 *
 */
public class EvohomeDiscoveryService extends AbstractDiscoveryService implements AccountStatusListener {
    private final Logger logger = LoggerFactory.getLogger(EvohomeDiscoveryService.class);
    private static final int TIMEOUT = 5;

    private EvohomeAccountBridgeHandler bridge;
    private ThingUID bridgeUID;

    public EvohomeDiscoveryService(EvohomeAccountBridgeHandler bridge) {
        super(EvohomeBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT);

        this.bridge = bridge;
        this.bridgeUID = this.bridge.getThing().getUID();
        this.bridge.addAccountStatusListener(this);
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverDevices();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void accountStatusChanged(ThingStatus status) {
        if (status == ThingStatus.ONLINE) {
            discoverDevices();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bridge.removeAccountStatusListener(this);
    }

    private void discoverDevices() {
        if (bridge.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Evohome Gateway not online, scanning postponed");
            return;
        }

        for (Location location : bridge.getEvohomeConfig()) {
            for (Gateway gateway : location.getGateways()) {
                for (TemperatureControlSystem tcs : gateway.getTemperatureControlSystems()) {
                    addDisplayDiscoveryResult(location, tcs);
                    for (Zone zone : tcs.getZones()) {
                        addZoneDiscoveryResult(location, zone);
                    }
                }
            }
        }

        stopScan();
    }

    private void addDisplayDiscoveryResult(Location location, TemperatureControlSystem tcs) {
        String id = tcs.getSystemId();
        String name = location.getLocationInfo().getName();
        ThingUID thingUID = new ThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_DISPLAY, bridgeUID, id);

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(EvohomeBindingConstants.PROPERTY_ID, id);
        properties.put(EvohomeBindingConstants.PROPERTY_NAME, name);

        addDiscoveredThing(thingUID, properties, name);
    }

    private void addZoneDiscoveryResult(Location location, Zone zone) {
        String id = zone.getZoneId();
        String name = zone.getName() + " (" + location.getLocationInfo().getName() + ")";
        ThingUID thingUID = new ThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_HEATING_ZONE, bridgeUID, id);

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(EvohomeBindingConstants.PROPERTY_ID, id);
        properties.put(EvohomeBindingConstants.PROPERTY_NAME, name);

        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeUID).withLabel(displayLabel).build();
        thingDiscovered(discoveryResult);
    };

}
