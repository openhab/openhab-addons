/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.handler;

import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.mac.Mac;
import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

/**
 * The {@link Rm2Handler} is responsible for handling RM2 devices.
 *
 * @author Florian Mueller - Initial contribution
 */
public class Rm2Handler extends BroadlinkHandler {

    private final Logger logger = LoggerFactory.getLogger(Rm2Handler.class);
    private RM2Device rm2Device;

    /**
     * Creates a new instance of this class for the {@link Rm2Handler}.
     *
     * @param thing the thing that should be handled, not null
     */
    public Rm2Handler(Thing thing) {
        super(thing);
        try {
            blDevice = new RM2Device(host, new Mac(mac));
            this.rm2Device = (RM2Device) blDevice;
        } catch (IOException e) {
            logger.error("Could not find broadlink device at Host {} with MAC {} ", host, mac, e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command class: {}", command.getClass());
        logger.debug("Command: {}", command.toFullString());

        if(command == RefreshType.REFRESH){
            refreshData();
            return;
        }

        switch (channelUID.getIdWithoutGroup()) {
            case IR_RF_COMMAND:
                handleIrRfCommand(channelUID,command);
                break;
            default:
                logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    private void handleIrRfCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            try {
                logger.info("TODO: Implement this channel");
            } catch (Exception e) {
                logger.error("Error while setting remote lock of {} to {}", thing.getUID(), command, e);
            }
        } else {
            logger.warn("Channel {} does not support command {}", channelUID, command);
        }
    }

    @Override
    protected void refreshData() {
        try {
            updateState(ROOM_TEMPERATURE, new DecimalType(rm2Device.getTemp()));
        } catch (Exception e) {
            logger.error("Error while retrieving data for {}", thing.getUID(), e);
        }
    }

}
