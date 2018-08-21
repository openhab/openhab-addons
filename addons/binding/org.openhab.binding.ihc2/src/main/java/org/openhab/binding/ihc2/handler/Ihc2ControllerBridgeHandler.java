/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.handler;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ihc2.internal.config.Ihc2ControllerConfig;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client.ConnectionState;
import org.openhab.binding.ihc2.internal.ws.Ihc2EventListener;
import org.openhab.binding.ihc2.internal.ws.Ihc2Execption;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSDate;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSResourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ihc2ControllerBridgeHandler} is responsible for setting up the communication with the LK IHC Controller.
 *
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2ControllerBridgeHandler extends BaseBridgeHandler implements Ihc2EventListener {

    private final Logger logger = LoggerFactory.getLogger(Ihc2ControllerBridgeHandler.class);

    private final Ihc2Client ihc2Client = Ihc2Client.getInstance();

    private Ihc2ControllerConfig config = null;

    public Ihc2ControllerBridgeHandler(Bridge bridge) {
        super(bridge);
        logger.debug("Ihc2BridgeHandler(Bridge bridge)");
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        logger.debug("Ihc2BridgeHandler handleCommand() {} cmd: {}", channelUID.getAsString(), command.toFullString());

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Override
    public void initialize() {
        if (ihc2Client.getConnectionState() == ConnectionState.CONNECTED) {
            try {
                ihc2Client.closeConnection();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } catch (Ihc2Execption e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        logger.debug("Ihc2ControllerBridgeHandler initialize");
        this.config = getThing().getConfiguration().as(Ihc2ControllerConfig.class);
        logger.debug("initialize() {}", config.getIpAddress());

        ihc2Client.addEventListener(this, thing.hashCode());

        ihc2Client.setIp(config.getIpAddress());
        ihc2Client.setUsername(config.getUser());
        ihc2Client.setPassword(config.getPassword());
        ihc2Client.setTimeoutInMillisecods(config.getTimeout());

        ihc2Client.setDiscoveryLevel(config.getDiscoveryLevel());

        ihc2Client.setDumpResourceInformationToFile(config.getResourceFile());
        ihc2Client.setProjectFile(config.getProjectFile());

        Ihc2StartCommunication startCommunication = new Ihc2StartCommunication();
        // ihc2Client.openConnection() takes too long time for the initialize()
        startCommunication.start();
    }

    @Override
    public void statusUpdateReceived(EventObject event, WSControllerState status) {
        logger.debug("statusUpdateReceived() {}", status.getState());
        if (status.getState().equals(WSControllerState.CONTROLLER_STATE_READY)) {
            WSProjectInfo pi = ihc2Client.getProjectInfo();

            logger.debug("ProjectMajorRevision: {}", pi.getProjectMajorRevision());

            WSDate wsdate = pi.getLastmodified();

            getThing().setProperty("Lastmodified", wsdate.toString());
            String oldProjectMajorRevision = getThing().setProperty("ProjectMajorRevision",
                    String.valueOf(pi.getProjectMajorRevision()));
            String oldProjectMinorRevision = getThing().setProperty("ProjectMinorRevision",
                    String.valueOf(pi.getProjectMinorRevision()));
            String oldVisualMajorVersion = getThing().setProperty("VisualMajorVersion",
                    String.valueOf(pi.getVisualMajorVersion()));
            String oldVisualMinorVersion = getThing().setProperty("VisualMinorVersion",
                    String.valueOf(pi.getVisualMinorVersion()));
            String oldCustomerName = getThing().setProperty("CustomerName", pi.getCustomerName());
            String oldInstallerName = getThing().setProperty("InstallerName", pi.getInstallerName());
            return;
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void resourceValueUpdateReceived(EventObject event, WSResourceValue value) {
        logger.debug("resourceValueUpdateReceived()");
    }

    @Override
    public void errorOccured(EventObject event, Ihc2Execption e) {
        logger.debug("errorOccured()");
    }

    private class Ihc2StartCommunication extends Thread {
        @Override
        public void run() {
            logger.debug("Ihc2StartCommunication run()");
            try {
                ihc2Client.openConnection();
                updateStatus(ThingStatus.ONLINE);
            } catch (Ihc2Execption e) {
                logger.error("errorOccured()", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

    }

}
