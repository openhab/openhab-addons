/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.radiora.RS232Connection;
import org.openhab.binding.lutron.internal.radiora.RadioRAConnection;
import org.openhab.binding.lutron.internal.radiora.RadioRAFeedbackListener;
import org.openhab.binding.lutron.internal.radiora.config.RS232Config;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRACommand;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapInquiryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RS232Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeff Lauterbach - Initial contribution
 */
public class RS232Handler extends BaseBridgeHandler implements RadioRAFeedbackListener {

    private Logger logger = LoggerFactory.getLogger(RS232Handler.class);

    private RadioRAConnection chronosConnection;

    private ScheduledFuture<?> zoneMapScheduledTask;

    public RS232Handler(Bridge bridge) {
        super(bridge);

        this.chronosConnection = new RS232Connection();
        this.chronosConnection.setListener(this);
    }

    @Override
    public void dispose() {
        if (zoneMapScheduledTask != null) {
            zoneMapScheduledTask.cancel(true);
        }

        if (chronosConnection != null) {
            chronosConnection.disconnect();
        }
    }

    @Override
    public void initialize() {
        connectToChronos();

        scheduleZoneMapQuery();
    }

    protected void connectToChronos() {
        RS232Config config = getConfigAs(RS232Config.class);
        String portName = config.getPortName();
        int baud = config.getBaud();

        logger.debug("Attempting to connect to Chronos on port {}", portName);

        if (!chronosConnection.open(portName, baud)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Error initializing - Failed to connect to chronos");
            return;
        }

        logger.debug("Connected successfully");

        updateStatus(ThingStatus.ONLINE);
    }

    protected void scheduleZoneMapQuery() {
        RS232Config config = getConfigAs(RS232Config.class);
        logger.debug("Scheduling zone map query at {} second inverval", config.getZoneMapQueryInterval());

        Runnable task = () -> sendCommand(new ZoneMapInquiryCommand());

        zoneMapScheduledTask = this.scheduler.scheduleWithFixedDelay(task, 3, config.getZoneMapQueryInterval(),
                TimeUnit.SECONDS);
    }

    public void sendCommand(RadioRACommand command) {
        chronosConnection.write(command.toString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void handleRadioRAFeedback(RadioRAFeedback feedback) {
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler instanceof LutronHandler) {
                ((LutronHandler) handler).handleFeedback(feedback);
            } else {
                logger.debug("Unexpected - Thing {} is not a LutronHandler", thing.getClass());
            }
        }
    }
}
