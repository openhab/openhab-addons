/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.alarmdecoder.internal.protocol.ADCommand;
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
    protected AtomicBoolean firstUpdateReceived = new AtomicBoolean(false);

    public ADThingHandler(Thing thing) {
        super(thing);
    }

    protected abstract void initDeviceState();

    public abstract void initChannelState();

    /**
     * Notify handler that panel is in ready state so that any un-updated contact channels can be set to default
     * (closed).
     */
    public abstract void notifyPanelReady();

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for AD handler", bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();

        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            // thingOfflineNotify();
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
