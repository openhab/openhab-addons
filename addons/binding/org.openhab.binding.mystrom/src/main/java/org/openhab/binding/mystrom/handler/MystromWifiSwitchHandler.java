/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mystrom.MystromBindingConstants;
import org.openhab.binding.mystrom.internal.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MystromWifiSwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels for the Wifi Switch device.
 *
 * @author Stéphane Raemy - Initial contribution
 */
public class MystromWifiSwitchHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MystromWifiSwitchHandler.class);
    private Device lastData;

    public MystromWifiSwitchHandler(@NonNull Thing thing) {
        super(thing);
    }

    /**
     * Handle the command to do things to the device, this will change the
     * value of a channel by sending the request to mystrom.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = (String) getThing().getConfiguration().getProperties().get(MystromBindingConstants.ID);
        if (channelUID.getId().equals(MystromBindingConstants.STATE)) {
            // Change the mode.
            if (command instanceof OnOffType) {
                OnOffType cmd = (OnOffType) command;
                boolean state = (cmd == OnOffType.ON ? true : false);
                getBridgeHandler().switchDevice(id, state);
            }
        }
    }

    /**
     * Initialize the system.
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Dispose the handler, cleaning up memory.
     */
    @Override
    public void dispose() {
        this.lastData = null;
        super.dispose();
    }

    /**
     * Handlers an incoming update from mystrom.
     *
     * @param device The device to update
     */
    public void updateDevice(Device device) {
        logger.debug("Updating Wifi Switch device {}", device.id);
        if (lastData == null || !lastData.equals(device)) {
            updateState(MystromBindingConstants.STATE, device.state.equals("on") ? OnOffType.ON : OnOffType.OFF);
            updateState(MystromBindingConstants.CONSUMPTION, new DecimalType(device.power));
            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

            // Setup the properties for this device.
            // updateProperty("id", device.id);
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }

    private MystromBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof MystromBridgeHandler) {
            return (MystromBridgeHandler) handler;
        } else {
            return null;
        }
    }

}
