/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lgtvserial.LgTvSerialBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

/**
 * The {@link LgTvSerialHandler} contains all the logic of this simple binding. It
 * is responsible for handling commands and sending them to the serial port.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
public class LgTvSerialHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(LgTvSerialHandler.class);
    private static final int BAUD = 9600;
    private NRSerialPort serialPort;
    private OutputStreamWriter output;
    private String portName;
    long lastCommandTime = System.currentTimeMillis();
    boolean lastCommandWasPower = false;
    private static final long POWER_COMMAND_DELAY_MS = 500;

    public LgTvSerialHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        portName = (String) getThing().getConfiguration().get("port");
        if (portName != null) {
            serialPort = new NRSerialPort(portName, BAUD);
            if (serialPort.connect()) {
                updateStatus(ThingStatus.ONLINE);
                output = new OutputStreamWriter(serialPort.getOutputStream());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect to serial port " + portName);
                logger.debug("Failed to connect to serial port {}", portName);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port name not configured");
            logger.debug("Serial port name not configured");
        }
    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            serialPort.disconnect();
            serialPort = null;
        }
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return; // Protocol doesn't support refreshing
        }
        try {
            long now = System.currentTimeMillis();

            if (lastCommandWasPower && now - lastCommandTime < POWER_COMMAND_DELAY_MS) {
                try {
                    Thread.sleep(POWER_COMMAND_DELAY_MS - (now - lastCommandTime));
                    now += POWER_COMMAND_DELAY_MS - (now - lastCommandTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
            lastCommandWasPower = false;
            if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_POWER) && (command instanceof OnOffType)) {

                if (now - lastCommandTime < POWER_COMMAND_DELAY_MS) {
                    try {
                        Thread.sleep(POWER_COMMAND_DELAY_MS - (now - lastCommandTime));
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                if (command == OnOffType.ON) {
                    output.write("ka 0 1\r");
                } else if (command == OnOffType.OFF) {
                    output.write("ka 0 0\r");
                }
                lastCommandWasPower = true;
                updateState(channelUID, (OnOffType) command);
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_INPUT)) {
                output.write(String.format("xb 0 %x\r", Integer.parseInt(command.toString())));
                updateState(channelUID, new StringType(command.toString()));
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_VOLUME)
                    && command instanceof PercentType) {
                PercentType vol = (PercentType) command;
                output.write(String.format("kf 0 %x\r", vol.intValue()));
                updateState(channelUID, (PercentType) command);
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_MUTE)
                    && (command instanceof OnOffType)) {
                if (command == OnOffType.ON) {
                    output.write("ke 0 0\r");
                } else if (command == OnOffType.OFF) {
                    output.write("ke 0 1\r");
                }
                updateState(channelUID, (OnOffType) command);
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_BACKLIGHT)
                    && (command instanceof PercentType)) {
                PercentType level = (PercentType) command;
                output.write(String.format("mg 0 %x\r", level.intValue()));
                updateState(channelUID, (PercentType) command);
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_COLOR_TEMPERATURE)) {
                output.write(String.format("ku 0 %x\r", Integer.parseInt(command.toString())));
                updateState(channelUID, new StringType(command.toString()));
            }
            output.flush();
            lastCommandTime = now;
        } catch (IOException e) {
            logger.error("Serial port write error", e);
        }
    }
}
