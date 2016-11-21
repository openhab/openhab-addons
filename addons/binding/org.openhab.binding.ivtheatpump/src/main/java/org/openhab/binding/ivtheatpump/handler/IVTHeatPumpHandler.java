/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ivtheatpump.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ivtheatpump.IVTHeatPumpBindingConstants;
import org.openhab.binding.ivtheatpump.internal.protocol.CommandFactory;
import org.openhab.binding.ivtheatpump.internal.protocol.IVRConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IVTHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class IVTHeatPumpHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IVTHeatPumpHandler.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IVRConnection connection;

    public IVTHeatPumpHandler(Thing thing, IVRConnection connection) {
        super(thing);

        this.connection = connection;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("handleCommand for channel {} and command {}", channelUID, command);

        CompletableFuture.supplyAsync(this::executeCommand, executor).thenAccept(temp -> {
            updateState(IVTHeatPumpBindingConstants.CHANNEL_RADIATOR_RETURN_GT1, new DecimalType(temp));
        });

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        super.dispose();

        connection.close();
    }

    private double executeCommand() {
        try {
            if (connection.isConnected() == false) {
                connection.connect();
            }

            connection.write(CommandFactory.createReadFromSystemRegisterCmd((short) 0x020B));

            int value;
            while ((value = connection.read()) != -1) {
                logger.debug("****** {}", Integer.toHexString(value));
            }

            return 34.1;
        } catch (Exception e) {
            return 0;
        }
    }
}
