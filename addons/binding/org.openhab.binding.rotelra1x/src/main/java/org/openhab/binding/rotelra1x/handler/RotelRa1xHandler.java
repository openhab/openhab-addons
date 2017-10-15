/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rotelra1x.handler;

import static org.openhab.binding.rotelra1x.RotelRa1xBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
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
import org.openhab.binding.rotelra1x.internal.ConfigurationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link RotelRa1xHandler} handles commands and state changes, and communicates
 * with the amplifier over the serial port interface..
 *
 * @author Marius Bjørnstad - Initial contribution
 */
public class RotelRa1xHandler extends BaseThingHandler {

    private static final int BAUD = 115200;
    private static final long ERROR_RETRY_DELAY_MS = 60000;
    private int maximumVolume;
    private RXTXPort serialPort;

    private boolean exit;
    private volatile boolean power;

    private final Logger logger = LoggerFactory.getLogger(RotelRa1xHandler.class);

    public RotelRa1xHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        maximumVolume = ((BigDecimal) getThing().getConfiguration().get("maximum-volume")).intValue();
        exit = false;
        try {
            connect();
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                inputLoop();
            }, 4, TimeUnit.SECONDS);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
        } catch (ConfigurationError e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            disconnect();
        }
    }

    @Override
    public void dispose() {
        exit = true;
        disconnect();
    }

    private void connect() throws IOException, ConfigurationError {
        // Note: connect may leave the port open even if it throws an exception.
        if (serialPort == null) {
            String portName = (String) getThing().getConfiguration().get("port");
            if (portName == null) {
                throw new ConfigurationError("Serial port name not configured");
            }
            try {
                serialPort = new RXTXPort(portName);
            } catch (PortInUseException e) {
                throw new IOException(e);
            }
            try {
                serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                serialPort.close();
                throw new IOException(e);
            }

            // Don't need continuous updates of the display, we still get updates when
            // the volume, etc., changes
            serialPort.getOutputStream().write("display_update_manual!".getBytes(StandardCharsets.US_ASCII));
            updateStatus(ThingStatus.ONLINE);
            serialPort.getOutputStream().write("get_current_power!".getBytes(StandardCharsets.US_ASCII));
            updateState(CHANNEL_MUTE, OnOffType.OFF);
            updateState(CHANNEL_BRIGHTNESS, new PercentType(100));
        }
    }

    private void disconnect() {
        if (serialPort != null) {
            serialPort.close();
        }
        serialPort = null;
    }

    private String readCommand() throws IOException {
        return readUntil('!', '=');
    }

    private String readUntil(char terminator) throws IOException {
        return readUntil(terminator, terminator);
    }

    private String readUntil(char terminator1, char terminator2) throws IOException {
        StringBuilder result = new StringBuilder();
        while (true) {
            int b = serialPort.getInputStream().read();
            if (b == -1) {
                throw new IOException("Connection unexpectedly closed");
            } else if (b == terminator1 || b == terminator2) {
                break;
            } else {
                result.append((char) b);
            }
        }
        return result.toString();
    }

    private PercentType readVolume() throws IOException {
        String volumeString = readUntil('!');
        int volume;
        if ("min".equals(volumeString)) {
            volume = 0;
        } else if ("max".equals(volumeString)) {
            volume = maximumVolume;
        } else {
            volume = Integer.parseInt(volumeString, 10);
        }
        double volumePct = volume * 100.0 / maximumVolume;
        return new PercentType(BigDecimal.valueOf(Math.round(volumePct)));
    }

    private PercentType readDimmer() throws IOException {
        String dimmerString = readUntil('!');
        int dimmer = Integer.parseInt(dimmerString, 10);
        // Let's make 100 % the brightest, makes more sense
        double dimmerPct = 100.0 - (dimmer * 100.0 / 6.0);
        return new PercentType(BigDecimal.valueOf(Math.round(dimmerPct)));
    }

    private DecimalType readFrequency() throws IOException {
        String freqString = readUntil('!');
        double freq;
        if ("off".equals(freqString)) {
            freq = 0.0;
        } else {
            freq = Double.parseDouble(freqString);
        }
        return new DecimalType(freq);
    }

    private void powerOnRefresh() {
        scheduler.submit(() -> {
            try {
                send("get_volume!");
                send("get_current_source!");
            } catch (IOException | ConfigurationError e) {
                logger.warn("Failed to request volume and source after powering on.", e);
            }
        });
    }

    private void inputLoop() {
        while (!exit && !Thread.currentThread().isInterrupted()) {
            try {
                if (serialPort == null) {
                    connect();
                }
                String command = readCommand();
                if ("volume".equals(command)) {
                    PercentType vol = readVolume();
                    updateState(CHANNEL_VOLUME, vol);
                } else if ("mute".equals(command)) {
                    String muteState = readUntil('!');
                    updateState(CHANNEL_MUTE, "on".equals(muteState) ? OnOffType.ON : OnOffType.OFF);
                } else if ("power_off".equals(command)) {
                    power = false;
                    updateState(CHANNEL_MUTE, OnOffType.OFF);
                    updateState(CHANNEL_POWER, OnOffType.OFF);
                } else if ("power_on".equals(command)) {
                    power = true;
                    updateState(CHANNEL_POWER, OnOffType.ON);
                    powerOnRefresh();
                } else if ("power".equals(command)) {
                    String state = readUntil('!');
                    if ("on".equals(state)) {
                        power = true;
                        updateState(CHANNEL_POWER, OnOffType.ON);
                        powerOnRefresh();
                    } else if ("standby".equals(state)) {
                        updateState(CHANNEL_MUTE, OnOffType.OFF);
                        updateState(CHANNEL_POWER, OnOffType.OFF);
                        power = false;
                    }
                } else if ("dimmer".equals(command)) {
                    updateState(CHANNEL_BRIGHTNESS, readDimmer());
                } else if ("freq".equals(command)) {
                    updateState(CHANNEL_FREQUENCY, readFrequency());
                } else if ("source".equals(command)) {
                    updateState(CHANNEL_SOURCE, new StringType(readUntil('!')));
                } else if ("display".equals(command)) {
                    String stringLength = readUntil(',');
                    int length = Integer.parseInt(stringLength);
                    byte[] data = new byte[length];
                    int off = 0;
                    while (off != length) {
                        int r = serialPort.getInputStream().read(data, off, length - off);
                        if (r == -1) {
                            throw new IOException("Connection closed while reading display content");
                        } else {
                            off += r;
                        }
                    }
                    // We don't do anything with display content, could add a channel if this is useful
                } else {
                    readUntil('!'); // discard
                }

            } catch (IOException e) {
                if (serialPort != null) {
                    logger.debug("Input error while receiving data from amplifier, waiting...", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    disconnect();
                    try {
                        Thread.sleep(ERROR_RETRY_DELAY_MS);
                    } catch (InterruptedException e1) {
                        return;
                    }
                }
            } catch (RuntimeException e) {
                if (serialPort != null) { // If serial port is closed, it's set to null,
                                          // there is no message here,
                    logger.warn("Unexpected error", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Unknown error while processing input: " + e.getMessage());
                    disconnect();
                    try {
                        Thread.sleep(ERROR_RETRY_DELAY_MS);
                    } catch (InterruptedException e1) {
                        return;
                    }
                }
            }
        }
    }

    private void send(String text) throws IOException, ConfigurationError {
        if (power) {
            connect();
            serialPort.getOutputStream().write(text.getBytes(StandardCharsets.US_ASCII));
        }
    }

    private void sendForce(String text) throws IOException, ConfigurationError {
        connect();
        serialPort.getOutputStream().write(text.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_POWER.equals(channelUID.getId())) {
                if (command == OnOffType.ON) {
                    sendForce("power_on!");
                } else if (command == OnOffType.OFF) {
                    sendForce("power_off!");
                } else if (command instanceof RefreshType) {
                    sendForce("get_current_power!");
                }
            } else if (CHANNEL_MUTE.equals(channelUID.getId())) {
                if (command == OnOffType.ON) {
                    send("mute_on!");
                } else {
                    send("mute_off!");
                }
            } else if (CHANNEL_VOLUME.equals(channelUID.getId())) {
                handleVolume(command);
            } else if (CHANNEL_BRIGHTNESS.equals(channelUID.getId())) {
                handleBrightness(command);
            } else if (CHANNEL_SOURCE.equals(channelUID.getId())) {
                if (command instanceof StringType) {
                    send(command.toString() + "!");
                } else {
                    send("get_current_source!");
                }
            }
        } catch (IOException e) {
            logger.debug("An I/O error occurred while processing the command {}.", command, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
        } catch (ConfigurationError e) {
            logger.debug("There is an error in the configuration of the thing.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            disconnect();
        }
    }

    private void handleVolume(Command command) throws IOException, ConfigurationError {
        if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
            send("volume_up!");
        } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
            send("volume_down!");
        } else if (command instanceof DecimalType) {
            double value = Double.parseDouble(command.toString()) * maximumVolume / 100.0;
            send("volume_" + Integer.toString((int) value) + "!");
        } else if (command instanceof RefreshType) {
            send("get_current_volume!");
        }
    }

    private void handleBrightness(Command command) throws IOException, ConfigurationError {
        // Invert the scale so 100% is brightest
        if (command instanceof PercentType) {
            double value = 6 - Math.floor(((PercentType) command).doubleValue() * 6 / 100.0);
            send("dimmer_" + Integer.toString((int) value) + "!");
        }
    }
}
