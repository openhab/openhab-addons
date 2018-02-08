/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.listener.ThingStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all ZigBee thing
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public abstract class OpenWebNetZigBeeThingHandler extends BaseThingHandler implements ThingStatusListener {

    @SuppressWarnings("null")
    private Logger logger = LoggerFactory.getLogger(OpenWebNetZigBeeThingHandler.class);

    private int macAddress;

    private final int numberOfPorts;

    protected int getNumberOfPorts() {
        return numberOfPorts;
    }

    public OpenWebNetZigBeeThingHandler(Thing thing) {
        super(thing);
        macAddress = 0;
        if (thing.getThingTypeUID().equals(OpenWebNetBindingConstants.THING_TYPE_DUAL_LIGHTING)) {
            numberOfPorts = 2;
        } else {
            numberOfPorts = 1;
        }
    }

    @Override
    public void initialize() {
        // Get MAC Address
        String prop = editProperties().get(Thing.PROPERTY_MAC_ADDRESS);
        try {
            this.macAddress = Integer.valueOf(prop);
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "MAC address invalid (not a number: " + prop + ")");
            return;
        }

        setBridgeListener(true);

        updateState();
    }

    /**
     * Return the OpenWebNet where information for given port
     *
     * @param port
     * @return where or 0 if port is invalid
     */
    protected int getWhere(int port) {
        if ((port > 0) && (port <= numberOfPorts)) {
            return macAddress * 100 + port;
        } else {
            return 0;
        }
    }

    protected int getMacAddress() {
        return macAddress;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            setBridgeListener(true);
        }
    }

    @Override
    public void dispose() {
        setBridgeListener(false);
    }

    protected void updateState() {
        @Nullable
        Bridge bridge = getBridge();
        if (bridge != null) {
            switch (bridge.getStatus()) {
                case ONLINE:
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case OFFLINE:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    break;
                case UNINITIALIZED:
                case INITIALIZING:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE);
                    break;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
        }
    }

    protected void setBridgeListener(boolean enable) {
        @Nullable
        Bridge bridge = getBridge();
        @Nullable
        OpenWebNetBridgeHandler bridgeHandler = (bridge == null) ? null : (OpenWebNetBridgeHandler) bridge.getHandler();
        if (bridgeHandler != null) {
            for (int port = 1; port <= this.numberOfPorts; port++) {
                if (enable) {
                    bridgeHandler.addThingStatusListener(getWhere(port), this);
                } else {
                    bridgeHandler.removeThingStatusListener(getWhere(port));
                }
            }
        } else {
            logger.debug("Thing as no bridge");
        }
    }
}
