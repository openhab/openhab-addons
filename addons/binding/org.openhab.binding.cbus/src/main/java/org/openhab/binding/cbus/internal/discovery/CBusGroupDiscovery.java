/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusNetworkHandler;
import org.openhab.binding.cbus.internal.cgate.Application;
import org.openhab.binding.cbus.internal.cgate.CGateException;
import org.openhab.binding.cbus.internal.cgate.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CBusGroupDiscovery} class is used to discover CBus
 * groups that are in the CBus Network
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusGroupDiscovery extends AbstractDiscoveryService {

    private final static Logger logger = LoggerFactory.getLogger(CBusGroupDiscovery.class);

    private CBusNetworkHandler cbusNetworkHandler;

    public CBusGroupDiscovery(CBusNetworkHandler cbusNetworkHandler) {
        super(CBusBindingConstants.SUPPORTED_THING_TYPES_UIDS, 300, false);
        this.cbusNetworkHandler = cbusNetworkHandler;
    }

    @Override
    protected void startScan() {
        if (cbusNetworkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            try {
                Map<String, ThingTypeUID> applications = new HashMap<String, ThingTypeUID>();
                applications.put(CBusBindingConstants.CBUS_APPLICATION_LIGHTING, CBusBindingConstants.THING_TYPE_LIGHT);
                applications.put(CBusBindingConstants.CBUS_APPLICATION_DALI, CBusBindingConstants.THING_TYPE_DALI);
                applications.put(CBusBindingConstants.CBUS_APPLICATION_TEMPERATURE,
                        CBusBindingConstants.THING_TYPE_TEMPERATURE);
                applications.put(CBusBindingConstants.CBUS_APPLICATION_TRIGGER,
                        CBusBindingConstants.THING_TYPE_TRIGGER);

                for (Map.Entry<String, ThingTypeUID> applicationItem : applications.entrySet()) {
                    Application application = cbusNetworkHandler.getNetwork()
                            .getApplication(Integer.parseInt(applicationItem.getKey()));
                    if (application == null) {
                        continue;
                    }
                    ArrayList<Group> groups = application.getGroups(false);
                    for (Group group : groups) {
                        logger.debug("Found group: {} {} {}", application.getName(), group.getGroupID(),
                                group.getName());
                        Map<String, Object> properties = new HashMap<>(2);
                        properties.put(CBusBindingConstants.CONFIG_GROUP_ID, group.getGroupID());
                        properties.put(CBusBindingConstants.PROPERTY_NAME, group.getName());
                        ThingUID uid = new ThingUID(applicationItem.getValue(), Integer.toString(group.getGroupID()),
                                cbusNetworkHandler.getThing().getBridgeUID().getId(),
                                cbusNetworkHandler.getThing().getUID().getId());
                        if (uid != null) {
                            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                                    .withLabel(group.getGroupID() + " - " + group.getName())
                                    .withBridge(cbusNetworkHandler.getThing().getUID()).build();
                            thingDiscovered(result);
                        }
                    }
                }
            } catch (CGateException e) {
                logger.error("Failed to discover groups", e);
            }
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        // TODO Auto-generated method stub
        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        // TODO Auto-generated method stub
        super.stopBackgroundDiscovery();
    }

    public void activate() {
        // cgateHandler.registerDiscoveryService(this);
    }

    @Override
    public void deactivate() {
        // cgateHandler.unregisterDiscoveryService();
    }

}
