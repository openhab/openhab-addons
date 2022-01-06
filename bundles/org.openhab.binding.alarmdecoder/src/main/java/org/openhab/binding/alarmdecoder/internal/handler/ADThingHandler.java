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
package org.openhab.binding.alarmdecoder.internal.handler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
import org.openhab.binding.alarmdecoder.internal.protocol.ADMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ADThingHandler} is the abstract base class for all AD thing handlers.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public abstract class ADThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ADThingHandler.class);
    protected final AtomicBoolean firstUpdateReceived = new AtomicBoolean(false);

    public ADThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Initialize device state and set status for handler. Should be called at the end of initialize(). Also called by
     * bridgeStatusChanged() when bridge status changes from OFFLINE to ONLINE. Calls initChannelState() to initialize
     * channels if setting status to ONLINE.
     */
    protected void initDeviceState() {
        logger.trace("Initializing device state");
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            initChannelState();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Initialize channel states if necessary
     */
    public abstract void initChannelState();

    /**
     * Notify handler that panel is in ready state so that any un-updated contact channels can be set to default
     * (closed).
     */
    public abstract void notifyPanelReady();

    /**
     * Notify handler of a message from the AD via the bridge
     *
     * @param msg The ADMessage to handle
     */
    public abstract void handleUpdate(ADMessage msg);

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        logger.debug("Bridge status changed to {} for AD handler", bridgeStatus);

        if (bridgeStatus == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();

        } else if (bridgeStatus == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Send a command via the bridge
     *
     * @param command command to send
     */
    protected void sendCommand(ADCommand command) {
        Bridge bridge = getBridge();
        ADBridgeHandler bridgeHandler = bridge == null ? null : (ADBridgeHandler) bridge.getHandler();

        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
        } else {
            bridgeHandler.sendADCommand(command);
        }
    }
}
