/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal.discovery;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Utils;
import org.openhab.binding.km200.internal.handler.KM200GatewayHandler;
import org.openhab.binding.km200.internal.handler.KM200GatewayStatusListener;
import org.openhab.binding.km200.internal.handler.KM200SwitchProgramServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200GatewayDiscoveryService} class discovers things through a gateway
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200GatewayDiscoveryService extends AbstractDiscoveryService implements KM200GatewayStatusListener {

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayDiscoveryService.class);

    private static int timeOut = 120;

    KM200GatewayHandler gateway;

    public KM200GatewayDiscoveryService(KM200GatewayHandler gateway) {
        super(KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS, timeOut, true);
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
    }

    @Override
    public void gatewayStatusChanged(ThingStatus status) {
        if (status.equals(ThingStatus.ONLINE)) {
            discoverDevices();
        }
    }

    @Override
    protected void deactivate() {
        gateway.removeHubStatusListener(this);
        super.deactivate();
    }

    /**
     * Discovers devices connected to a hub
     */
    private void discoverDevices() {
        if (!gateway.getDevice().getInited()) {
            logger.debug("Gateway not configured, scanning postponed.");
            return;
        }
        ThingUID thingUID = null;
        ThingUID bridgeUID = gateway.getThing().getUID();
        logger.debug("Checking bridgeUID: {} ", bridgeUID.getAsString());
        for (KM200ThingType tType : KM200ThingType.values()) {
            String root = tType.getRootPath();
            if (root.isEmpty()) {
                continue;
            }
            logger.debug("typeuid: {}", tType.getThingTypeUID());
            String checkService = tType.getActiveCheckSubPath();
            logger.debug("root: {}", root);
            if (gateway.getDevice().containsService(root)) {
                boolean enumOnly = true;
                KM200ServiceObject object = gateway.getDevice().getServiceObject(root);
                Set<String> keys = object.serviceTreeMap.keySet();
                /* Check whether all sub services are refEnum */
                for (String key : keys) {
                    if (!"refEnum".equals(object.serviceTreeMap.get(key).getServiceType())) {
                        enumOnly = false;
                        break;
                    }
                }
                /* If there are refEnum only, then create for every one an own thing */
                if (enumOnly) {
                    for (String key : keys) {
                        /* Check whether this part of heating system is inactive. If its then ignore it */
                        if (checkService != null) {
                            String checkServicePath = root + "/" + key + "/" + checkService;
                            if (gateway.getDevice().containsService(checkServicePath)) {
                                if ("INACTIVE"
                                        .equals(gateway.getDevice().getServiceObject(checkServicePath).getValue())) {
                                    continue;
                                }
                            }
                        }
                        thingUID = new ThingUID(tType.getThingTypeUID(), bridgeUID, key);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("root", KM200Utils.translatesPathToName(root) + "#" + key);
                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                                .withLabel(key).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        if (object.serviceTreeMap.get(key).serviceTreeMap.containsKey(SWITCH_PROGRAM_PATH_NAME)) {
                            String currentPathName = root + "/" + key + "/" + SWITCH_PROGRAM_CURRENT_PATH_NAME;
                            String currParaRepl = SWITCH_PROGRAM_REPLACEMENT;
                            boolean currExists = object.serviceTreeMap.get(key).serviceTreeMap
                                    .containsKey(SWITCH_PROGRAM_CURRENT_PATH_NAME);

                            KM200ServiceObject switchObject = object.serviceTreeMap.get(key).serviceTreeMap
                                    .get(SWITCH_PROGRAM_PATH_NAME);
                            if (switchObject.serviceTreeMap.isEmpty()) {
                                continue;
                            }
                            /*
                             * if the device has only one switching program then the "activeSwitchProgram" service is
                             * not existing. In this case we are using a fix path to this one service.
                             */
                            if (!currExists) {
                                if (switchObject.serviceTreeMap.keySet().size() == 1) {
                                    currParaRepl = switchObject.serviceTreeMap.entrySet().iterator().next().getKey();
                                }
                            }

                            String posName = ((KM200SwitchProgramServiceHandler) switchObject.serviceTreeMap.entrySet()
                                    .iterator().next().getValue().getValueParameter()).getPositiveSwitch();
                            String negName = ((KM200SwitchProgramServiceHandler) switchObject.serviceTreeMap.entrySet()
                                    .iterator().next().getValue().getValueParameter()).getNegativeSwitch();
                            ThingUID subThingUID = new ThingUID(tType.getThingTypeUID(), bridgeUID,
                                    key + "-switchprogram");
                            Map<String, Object> subProperties = new HashMap<>(4);
                            subProperties.put("root", KM200Utils.translatesPathToName(
                                    root + "/" + key + "/" + SWITCH_PROGRAM_PATH_NAME + "/" + currParaRepl));
                            subProperties.put(SWITCH_PROGRAM_CURRENT_PATH_NAME,
                                    KM200Utils.translatesPathToName(currentPathName));
                            subProperties.put(SWITCH_PROGRAM_POSITIVE, posName);
                            subProperties.put(SWITCH_PROGRAM_NEGATIVE, negName);
                            logger.debug("enum thingUID {} bridgeUID {} withLabel {} root {}", thingUID, bridgeUID, key,
                                    root);
                            DiscoveryResult subDiscoveryResult = DiscoveryResultBuilder.create(subThingUID)
                                    .withBridge(bridgeUID).withLabel(key + " switch program")
                                    .withProperties(subProperties).build();
                            thingDiscovered(subDiscoveryResult);
                        }
                    }
                } else {
                    String[] sParts = root.split("/");
                    String key = sParts[sParts.length - 1];
                    thingUID = new ThingUID(tType.getThingTypeUID(), bridgeUID, key);
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put("root", KM200Utils.translatesPathToName(root));
                    logger.debug("thingUID {} bridgeUID {} withLabel {} root {}", thingUID, bridgeUID, key, root);
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withLabel(key).withProperties(properties).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }
}
