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
package org.openhab.binding.lirc.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.lirc.internal.LIRCBindingConstants;
import org.openhab.binding.lirc.internal.LIRCMessageListener;
import org.openhab.binding.lirc.internal.handler.LIRCBridgeHandler;
import org.openhab.binding.lirc.internal.messages.LIRCButtonEvent;
import org.openhab.binding.lirc.internal.messages.LIRCResponse;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCRemoteDiscoveryService extends AbstractDiscoveryService implements LIRCMessageListener {

    private final Logger logger = LoggerFactory.getLogger(LIRCRemoteDiscoveryService.class);

    private LIRCBridgeHandler bridgeHandler;

    public LIRCRemoteDiscoveryService(LIRCBridgeHandler lircBridgeHandler) {
        super(LIRCBindingConstants.SUPPORTED_DEVICE_TYPES, LIRCBindingConstants.DISCOVERY_TIMOUT, true);
        this.bridgeHandler = lircBridgeHandler;
        bridgeHandler.registerMessageListener(this);
    }

    @Override
    protected void startScan() {
        logger.debug("Discovery service scan started");
        bridgeHandler.startDeviceDiscovery();
    }

    @Override
    public void onButtonPressed(ThingUID bridge, LIRCButtonEvent buttonEvent) {
        addRemote(bridge, buttonEvent.getRemote());
    }

    @Override
    public void onMessageReceived(ThingUID bridge, LIRCResponse message) {
        LIRCResponse response = message;
        String command = response.getCommand();
        if ("LIST".equals(command) && response.isSuccess()) {
            for (String remoteID : response.getData()) {
                addRemote(bridge, remoteID);
            }
        }
    }

    private void addRemote(ThingUID bridge, String remote) {
        ThingTypeUID uid = LIRCBindingConstants.THING_TYPE_REMOTE;
        ThingUID thingUID = new ThingUID(uid, bridge, remote);

        logger.trace("Remote {}: Discovered new remote.", remote);
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(LIRCBindingConstants.PROPERTY_REMOTE, remote);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(remote).withBridge(bridge)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }
}
