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
package org.openhab.binding.km200.internal.discovery;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Utils;
import org.openhab.binding.km200.internal.handler.KM200GatewayHandler;
import org.openhab.binding.km200.internal.handler.KM200GatewayStatusListener;
import org.openhab.binding.km200.internal.handler.KM200SwitchProgramServiceHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KM200GatewayDiscoveryService} class discovers things through a gateway
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200GatewayDiscoveryService extends AbstractDiscoveryService implements KM200GatewayStatusListener {

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayDiscoveryService.class);

    private static int timeOut = 120;

    KM200GatewayHandler gateway;

    public KM200GatewayDiscoveryService(KM200GatewayHandler gateway) {
        super(KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS, timeOut, true);
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
        for (KM200ThingType tType : KM200ThingType.values()) {
            String root = tType.getRootPath();
            if (root.isEmpty()) {
                continue;
            }
            String checkService = tType.getActiveCheckSubPath();
            if (gateway.getDevice().containsService(root)) {
                boolean enumOnly = true;
                KM200ServiceObject object = gateway.getDevice().getServiceObject(root);
                if (null == object) {
                    logger.warn("No root service object found");
                    return;
                }
                Set<String> keys = object.serviceTreeMap.keySet();
                /* Check whether all sub services are refEnum */
                for (String key : keys) {
                    if (!DATA_TYPE_REF_ENUM.equals(object.serviceTreeMap.get(key).getServiceType())) {
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
                                KM200ServiceObject serviceObject = gateway.getDevice()
                                        .getServiceObject(checkServicePath);
                                if (null != serviceObject) {
                                    Object val = serviceObject.getValue();
                                    if (null != val && "INACTIVE".equals(val)) {
                                        continue;
                                    }
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
                            if (switchObject != null) {
                                if (switchObject.serviceTreeMap.isEmpty()) {
                                    continue;
                                }
                                /*
                                 * if the device has only one switching program then the "activeSwitchProgram" service
                                 * is
                                 * not existing. In this case we are using a fix path to this one service.
                                 */
                                if (!currExists) {
                                    if (switchObject.serviceTreeMap.keySet().size() == 1) {
                                        currParaRepl = switchObject.serviceTreeMap.entrySet().iterator().next()
                                                .getKey();
                                    }
                                }
                                KM200SwitchProgramServiceHandler valPara = (KM200SwitchProgramServiceHandler) switchObject.serviceTreeMap
                                        .entrySet().iterator().next().getValue().getValueParameter();
                                if (null != valPara) {
                                    String posName = valPara.getPositiveSwitch();
                                    String negName = valPara.getNegativeSwitch();
                                    if (null == posName || null == negName) {
                                        logger.warn("Service switches not found!");
                                        return;
                                    }
                                    ThingUID subThingUID = new ThingUID(tType.getThingTypeUID(), bridgeUID,
                                            key + "-switchprogram");
                                    Map<String, Object> subProperties = new HashMap<>(4);
                                    subProperties.put("root", KM200Utils.translatesPathToName(
                                            root + "/" + key + "/" + SWITCH_PROGRAM_PATH_NAME + "/" + currParaRepl));
                                    subProperties.put(SWITCH_PROGRAM_CURRENT_PATH_NAME,
                                            KM200Utils.translatesPathToName(currentPathName));
                                    subProperties.put(SWITCH_PROGRAM_POSITIVE, posName);
                                    subProperties.put(SWITCH_PROGRAM_NEGATIVE, negName);
                                    DiscoveryResult subDiscoveryResult = DiscoveryResultBuilder.create(subThingUID)
                                            .withBridge(bridgeUID).withLabel(key + " switch program")
                                            .withProperties(subProperties).build();
                                    thingDiscovered(subDiscoveryResult);
                                }
                            }
                        }
                    }
                } else {
                    String[] sParts = root.split("/");
                    String key = sParts[sParts.length - 1];
                    thingUID = new ThingUID(tType.getThingTypeUID(), bridgeUID, key);
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put("root", KM200Utils.translatesPathToName(root));
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withLabel(key).withProperties(properties).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }
}
