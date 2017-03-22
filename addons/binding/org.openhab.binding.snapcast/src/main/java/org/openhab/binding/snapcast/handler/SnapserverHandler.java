/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.handler;

import static org.openhab.binding.snapcast.SnapcastBindingConstants.CHANNEL_NAME;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.snapcast.SnapcastBindingConstants;
import org.openhab.binding.snapcast.discovery.SnapclientDiscoveryService;
import org.openhab.binding.snapcast.internal.protocol.SnapcastClientController;
import org.openhab.binding.snapcast.internal.protocol.SnapcastController;
import org.openhab.binding.snapcast.internal.rpc.JsonRpcEventClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SnapcastHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Steffen Folman Sørensen - Initial contribution
 */
public class SnapserverHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(SnapserverHandler.class);
    private String host;
    private Integer port = 1705;
    private SnapcastController snapcastController;

    public SnapserverHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_NAME)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    public SnapcastClientController getClient(String mac) {
        return snapcastController.getClient(mac);
    }

    @Override
    public void initialize() {
        host = (String) thing.getConfiguration().get(SnapcastBindingConstants.CONFIG_HOST_NAME);

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname not set!");
            return;
        }
        snapcastController = new SnapcastController(new JsonRpcEventClient(host, port));

        try {
            snapcastController.connect();
            // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
            // Long running initialization should be done asynchronously in background.

            // if (snapcastController.getProtocolVersion() <= 2) {
            updateStatus(ThingStatus.ONLINE);
            SnapclientDiscoveryService discoveryService = new SnapclientDiscoveryService(host, snapcastController,
                    thing.getUID());
            discoveryService.start(bundleContext);
            // } else {
            // updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_REGISTERING_ERROR,
            // "Protocol version 1 is not support please use a never version of snapcast.");
            // logger.error("Protocol version 1 is not support please use a never version of snapcast.");
            // }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_REGISTERING_ERROR, e.getMessage());
            e.printStackTrace();
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

}
