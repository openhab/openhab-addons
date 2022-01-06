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
package org.openhab.binding.cbus.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusNetworkHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.Application;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;
import com.daveoxley.cbus.Network;

/**
 * The {@link CBusGroupDiscovery} class is used to discover CBus
 * groups that are in the CBus Network
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusGroupDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(CBusGroupDiscovery.class);

    private final CBusNetworkHandler cbusNetworkHandler;

    public CBusGroupDiscovery(CBusNetworkHandler cbusNetworkHandler) {
        super(CBusBindingConstants.SUPPORTED_THING_TYPES_UIDS, 30, false);
        this.cbusNetworkHandler = cbusNetworkHandler;
    }

    @Override
    protected synchronized void startScan() {
        if (cbusNetworkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            ThingUID bridgeUid = cbusNetworkHandler.getThing().getBridgeUID();
            if (bridgeUid == null) {
                scanFinished();
                return;
            }
            try {
                Map<Integer, ThingTypeUID> applications = new HashMap<Integer, ThingTypeUID>();
                applications.put(CBusBindingConstants.CBUS_APPLICATION_LIGHTING, CBusBindingConstants.THING_TYPE_LIGHT);
                applications.put(CBusBindingConstants.CBUS_APPLICATION_DALI, CBusBindingConstants.THING_TYPE_DALI);
                applications.put(CBusBindingConstants.CBUS_APPLICATION_TEMPERATURE,
                        CBusBindingConstants.THING_TYPE_TEMPERATURE);
                applications.put(CBusBindingConstants.CBUS_APPLICATION_TRIGGER,
                        CBusBindingConstants.THING_TYPE_TRIGGER);

                Network network = cbusNetworkHandler.getNetwork();
                if (network == null) {
                    scanFinished();
                    return;
                }
                for (Map.Entry<Integer, ThingTypeUID> applicationItem : applications.entrySet()) {
                    Application application = network.getApplication(applicationItem.getKey());
                    if (application == null) {
                        continue;
                    }
                    ArrayList<Group> groups = application.getGroups(false);
                    for (Group group : groups) {
                        logger.debug("Found group: {} {} {}", application.getName(), group.getGroupID(),
                                group.getName());
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(CBusBindingConstants.PROPERTY_APPLICATION_ID,
                                Integer.toString(applicationItem.getKey()));
                        properties.put(CBusBindingConstants.CONFIG_GROUP_ID, Integer.toString(group.getGroupID()));
                        properties.put(CBusBindingConstants.PROPERTY_GROUP_NAME, group.getName());
                        properties.put(CBusBindingConstants.PROPERTY_NETWORK_ID,
                                Integer.toString(network.getNetworkID()));

                        ThingUID uid = new ThingUID(applicationItem.getValue(), Integer.toString(group.getGroupID()),
                                bridgeUid.getId(), cbusNetworkHandler.getThing().getUID().getId());
                        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                                .withLabel("CBUS " + group.getName() + "(" + group.getGroupID() + ")")
                                .withBridge(cbusNetworkHandler.getThing().getUID()).build();
                        thingDiscovered(result);
                    }
                }
            } catch (CGateException e) {
                logger.debug("Failed to discover groups", e);
            }
        }
        scanFinished();
    }

    private synchronized void scanFinished() {
        stopScan();// this notifies the scan listener that the scan is finished
        abortScan();// this clears the scheduled call to stopScan
    }
}
