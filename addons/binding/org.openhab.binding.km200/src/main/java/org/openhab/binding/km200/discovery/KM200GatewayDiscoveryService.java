/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.discovery;

import static org.openhab.binding.km200.KM200BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.km200.KM200ThingTypes;
import org.openhab.binding.km200.handler.KM200GatewayHandler;
import org.openhab.binding.km200.handler.KM200GatewayStatusListener;
import org.openhab.binding.km200.internal.KM200CommObject;
import org.openhab.binding.km200.internal.KM200SwitchProgramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200GatewayDiscoveryService} class discovers things through a gateway
 *
 * @author Markus Eckhardt
 *
 */
public class KM200GatewayDiscoveryService extends AbstractDiscoveryService implements KM200GatewayStatusListener {

    private Logger logger = LoggerFactory.getLogger(KM200GatewayDiscoveryService.class);

    private static int TIMEOUT = 120;

    KM200GatewayHandler gateway;

    public KM200GatewayDiscoveryService(KM200GatewayHandler gateway) {
        super(KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        logger.debug("KM200GatewayDiscoveryService {}", gateway);
        this.gateway = gateway;
        this.gateway.addGatewayStatusListener(this);
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverDevices();
    };

    @Override
    public void gatewayStatusChanged(ThingStatus status) {
        if (status.equals(ThingStatus.ONLINE)) {
            discoverDevices();
        }
    }

    @Override
    protected void deactivate() {
        super.deactivate();
        gateway.removeHubStatusListener(this);
    }

    /**
     * Discovers devices connected to a hub
     */
    private void discoverDevices() {
        if (!gateway.device.getInited()) {
            logger.info("Gateway not configured, scanning postponed.");
            return;
        }
        ThingUID thingUID = null;
        ThingUID bridgeUID = gateway.getThing().getUID();
        logger.debug("Checking bridgeUID: {} ", bridgeUID.getAsString());

        for (KM200ThingTypes tType : KM200ThingTypes.values()) {
            String root = tType.getRootPath();
            if (root.isEmpty()) {
                continue;
            }
            String checkService = tType.getActiveCheckSubPath();
            logger.debug("root: {}", root);
            if (gateway.device.containsService(root)) {
                Boolean enumOnly = true;
                KM200CommObject object = gateway.device.getServiceObject(root);
                Set<String> keys = object.serviceTreeMap.keySet();
                /* Check whether all sub services are refEnum */
                for (String key : keys) {
                    if (!object.serviceTreeMap.get(key).getServiceType().equals("refEnum")) {
                        enumOnly = false;
                        break;
                    }
                }
                /* If there are refEnum only, then create for every one an own thing */
                if (enumOnly) {
                    for (String key : keys) {
                        /* Check whether this part of heating system is inactive. If its then ignore it */
                        if (checkService != null) {
                            if (gateway.device.containsService(root + "/" + key + "/" + checkService)) {
                                if (gateway.device.getServiceObject(root + "/" + key + "/" + checkService).getValue()
                                        .equals("INACTIVE")) {
                                    continue;
                                }
                            }
                        }
                        thingUID = new ThingUID(tType.getThingTypeUID(), key);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("root", KM200GatewayHandler.translatesPathToName(root) + "#" + key);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                                .withLabel(key).withProperties(properties).build();

                        thingDiscovered(discoveryResult);
                        if (object.serviceTreeMap.get(key).serviceTreeMap.containsKey(SWITCH_PROGRAM_PATH_NAME)) {
                            String currentPathName = root + "/" + key + "/" + SWITCH_PROGRAM_CURRENT_PATH_NAME;
                            KM200CommObject switchObject = object.serviceTreeMap.get(key).serviceTreeMap
                                    .get(SWITCH_PROGRAM_PATH_NAME);
                            if (switchObject.serviceTreeMap.isEmpty()) {
                                continue;
                            }
                            String posName = ((KM200SwitchProgramService) switchObject.serviceTreeMap.entrySet()
                                    .iterator().next().getValue().getValueParameter()).getPositiveSwitch();
                            String negName = ((KM200SwitchProgramService) switchObject.serviceTreeMap.entrySet()
                                    .iterator().next().getValue().getValueParameter()).getNegativeSwitch();

                            ThingUID subThingUID = new ThingUID(tType.getThingTypeUID(), key + "-switchprogram");
                            Map<String, Object> subProperties = new HashMap<>(4);
                            subProperties.put("root", KM200GatewayHandler.translatesPathToName(root + "/" + key + "/"
                                    + SWITCH_PROGRAM_PATH_NAME + "/" + SWITCH_PROGRAM_REPLACEMENT));
                            subProperties.put(SWITCH_PROGRAM_CURRENT_PATH_NAME,
                                    KM200GatewayHandler.translatesPathToName(currentPathName));
                            subProperties.put(SWITCH_PROGRAM_POSITIVE, posName);
                            subProperties.put(SWITCH_PROGRAM_NEGATIVE, negName);
                            DiscoveryResult subDiscoveryResult = DiscoveryResultBuilder.create(subThingUID)
                                    .withBridge(bridgeUID).withLabel(key + " switch program")
                                    .withProperties(subProperties).build();

                            thingDiscovered(subDiscoveryResult);
                        }
                    }
                } else {
                    String[] sParts = root.split("/");
                    String key = sParts[sParts.length - 1];
                    thingUID = new ThingUID(tType.getThingTypeUID(), key);
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put("root", KM200GatewayHandler.translatesPathToName(root));

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withLabel(key).withProperties(properties).build();

                    thingDiscovered(discoveryResult);
                }
            }
        }
    }
}
