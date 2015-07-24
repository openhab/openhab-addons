/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.discovery;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.NetatmoBindingConstants;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.messages.AbstractDevice;
import org.openhab.binding.netatmo.internal.messages.NetatmoModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoModuleDiscoveryService} tracks for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(NetatmoModuleDiscoveryService.class);

    private NetatmoBridgeHandler netatmoBridgeHandler;
    private final static int SEARCH_TIME = 2;

    public NetatmoModuleDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        super(SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return NetatmoBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        List<AbstractDevice> equipments = netatmoBridgeHandler.deviceList.getAllEquipments();
        if (equipments != null) {
            for (AbstractDevice d : equipments) {
                onModuleAddedInternal(d);
            }
        }
        stopScan();
    }

    private void onModuleAddedInternal(AbstractDevice d) {
        ThingUID thingUID = getThingUID(d);
        if (thingUID != null) {
            ThingUID bridgeUID = netatmoBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);

            if (d instanceof NetatmoModule) {
                properties.put(MODULE_ID, d.getId());
                properties.put(DEVICE_ID, ((NetatmoModule) d).getMainDevice());
            } else {
                properties.put(DEVICE_ID, d.getId());
            }
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(d.getModuleName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported device of type '{}' with id {}", d.getType(), d.getId());
        }
    }

    private ThingUID getThingUID(AbstractDevice d) {
        ThingUID bridgeUID = netatmoBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, d.getType());

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, d.getId().replaceAll("[^a-zA-Z0-9_]", ""));
            return thingUID;
        } else {
            return null;
        }
    }

}
