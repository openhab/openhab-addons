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
package org.openhab.binding.eltako.internal.handler;

import static org.openhab.binding.eltako.internal.misc.EltakoBindingConstants.GENERIC_DEVICE_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.eltako.internal.misc.EltakoTelegramListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EltakoGenericHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Wenske - Initial contribution
 */
@NonNullByDefault
public class EltakoGenericHandler extends BaseThingHandler implements EltakoTelegramListener {

    /*
     * Logger instance to create log entries
     */
    private final Logger logger = LoggerFactory.getLogger(EltakoGenericHandler.class);

    /**
     * Channel variables
     */
    private int deviceId;

    /**
     * Initializer method
     */
    public EltakoGenericHandler(Thing thing) {
        super(thing);
        deviceId = 0;
    }

    /**
     * Called by framework after creation of thing
     */
    @Override
    public void initialize() {

        // Acquire device ID from thing configuration (set by the user)
        // this.deviceId = Integer.parseInt(getThing().getConfiguration().get(GENERIC_DEVICE_ID).toString(), 16);
        // Update thing property
        // updateProperty(GENERIC_HARDWARE_VERSION, "Unknown");

        // Set thing status to UNKNOWN
        this.updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = this.getBridge();
        if (bridge != null) {
            EltakoGenericBridgeHandler bridgeHandle = (EltakoGenericBridgeHandler) bridge.getHandler();
            if (bridgeHandle != null) {
                // Listen for a specific ID so received telegrams are forwarded to thing (Ignore 4th byte)
                bridgeHandle.addPacketListener(this,
                        Integer.parseInt(getThing().getConfiguration().get(GENERIC_DEVICE_ID).toString()) & 0xFFFFFF);
                if (bridge.getStatus() != ThingStatus.ONLINE) {
                    // Set thing status to offline
                    this.updateStatus(ThingStatus.OFFLINE);
                } else {
                    // Set thing status to offline
                    this.updateStatus(ThingStatus.ONLINE);
                    // TODO: Check communication to thing first before setting it to ONLINE
                }
            }
        }

        logger.debug("Finished initializing of thing handler");
    }

    /**
     * Is called in case the bridge is changing its state
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Log event to console
        logger.debug("bridgeStatusChanged => {}", bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            // Set thing status to unknown
            this.updateStatus(ThingStatus.ONLINE);
        }
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            // Set thing status to unknown
            this.updateStatus(ThingStatus.UNKNOWN);
        }
    }

    /**
     * Event handler is called in case a channel has received a command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Called by Bridge when a new telegram has been received
     */
    @Override
    public void telegramReceived(int[] packet) {
    }

    /**
     * Last method called before thing will be destroyed
     */
    @Override
    public void dispose() {
        logger.debug("Dispose thing instance");

        Bridge bridge = this.getBridge();
        if (bridge != null) {
            EltakoGenericBridgeHandler bridgeHandle = (EltakoGenericBridgeHandler) bridge.getHandler();
            if (bridgeHandle != null) {
                // Listen for a specific ID so received telegrams are forwarded to thing
                bridgeHandle.removePacketListener(this, deviceId);
            }
        }
    }

}
