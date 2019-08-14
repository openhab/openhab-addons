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
package org.openhab.binding.lgtvserial.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommand;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialCommunicator;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponseListener;
import org.openhab.binding.lgtvserial.internal.protocol.serial.SerialCommunicatorFactory;
import org.openhab.binding.lgtvserial.internal.protocol.serial.commands.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LgTvSerialHandler} contains all the logic of this simple binding. It
 * is responsible for handling commands and sending them to the serial port.
 *
 * @author Marius Bjoernstad - Initial contribution
 * @author Richard Lavoie - Major rework to add many more channels and support daisy chaining
 */
public class LgTvSerialHandler extends BaseThingHandler {

    /**
     * Interval at which to send update refresh commands.
     */
    private static final int EVENT_REFRESH_INTERVAL = 120;

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(LgTvSerialHandler.class);

    /**
     * Serial communicator factory used to retrieve the communicator for a given port.
     */
    private final SerialCommunicatorFactory factory;

    /**
     * Communicator used to send commands to the TV(s).
     */
    private LGSerialCommunicator communicator;

    /**
     * List of linked items used for the refresh polling.
     */
    private Map<ChannelUID, LGSerialCommand> channelCommands = Collections.synchronizedMap(new HashMap<>());

    /**
     * Set ID of this TV.
     */
    private LGSerialResponseListener responseListener;

    /**
     * Polling updater job.
     */
    private ScheduledFuture<?> updateJob;

    /**
     * Create the LG TV hander.
     *
     * @param thing Thing associated to this handler
     * @param factory Factory to retrieve a communicator for a given port
     */
    public LgTvSerialHandler(Thing thing, SerialCommunicatorFactory factory) {
        super(thing);
        this.factory = factory;
    }

    @Override
    public synchronized void initialize() {
        String portName = (String) getThing().getConfiguration().get("port");
        BigDecimal setIdParam = (BigDecimal) getThing().getConfiguration().get("setId");
        int setId = 1;
        if (setIdParam != null) {
            setId = setIdParam.intValue();
        }
        final int set = setId;
        responseListener = new LGSerialResponseListener() {
            @Override
            public int getSetID() {
                return set;
            }

            @Override
            public void onSuccess(ChannelUID channel, LGSerialResponse response) {
                State state = response.getState();
                logger.debug("Updating channel {} with value {}", channel, state);
                updateState(channel, state);
            }

            @Override
            public void onFailure(ChannelUID channel, LGSerialResponse response) {
                logger.debug("Received error response for channel {}: {}", channel, response.getState());
            }

        };

        if (portName != null) {
            communicator = factory.getInstance(portName);
            if (communicator != null) {
                communicator.register(responseListener);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect to serial port " + portName);
                logger.debug("Failed to connect to serial port {}", portName);
                return;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port name not configured");
            logger.debug("Serial port name not configured");
            return;
        }

        if (updateJob == null || updateJob.isCancelled()) {
            updateJob = scheduler.scheduleWithFixedDelay(eventRunnable, 0, EVENT_REFRESH_INTERVAL, TimeUnit.SECONDS);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public synchronized void dispose() {
        if (updateJob != null && !updateJob.isCancelled()) {
            updateJob.cancel(true);
            updateJob = null;
        }
        if (communicator != null) {
            communicator.unregister(responseListener);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        LGSerialCommand command = CommandFactory.createCommandFor(channelUID, responseListener);
        if (command == null) {
            logger.warn(
                    "A command could not be found for channel name '{}'. Please create an issue on the openhab project for the lgtvserial binding. ",
                    channelUID.getId());
            return;
        }
        this.channelCommands.put(channelUID, command);
        handleCommand(channelUID, RefreshType.REFRESH);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        this.channelCommands.remove(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                channelCommands.get(channelUID).execute(channelUID, communicator, null);
            } else {
                channelCommands.get(channelUID).execute(channelUID, communicator, command);
            }
        } catch (IOException e) {
            logger.warn("Serial port write error", e);
        }
    }

    private Runnable eventRunnable = () -> {
        synchronized (channelCommands) {
            for (Map.Entry<ChannelUID, LGSerialCommand> entry : channelCommands.entrySet()) {
                if (Thread.currentThread().isInterrupted()) {
                    logger.debug("Thread interrupted, stopping");
                    break;
                }
                try {
                    entry.getValue().execute(entry.getKey(), communicator, null);
                } catch (IOException e) {
                    logger.warn("An error occured while sending an update command for {}: {}", entry.getKey(),
                            e.getMessage());
                }
            }
        }
    };
}
