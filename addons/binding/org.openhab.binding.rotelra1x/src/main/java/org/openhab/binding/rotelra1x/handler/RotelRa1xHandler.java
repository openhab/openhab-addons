/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rotelra1x.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
import org.eclipse.smarthome.core.types.UnDefType;
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
public class RotelRa1xHandler extends BaseThingHandler implements Runnable {

    private static final int BAUD = 115200;
    private int maximumVolume;
    private RXTXPort serialPort;

    private boolean exit;
    private volatile boolean power;

    private Logger logger = LoggerFactory.getLogger(RotelRa1xHandler.class);

    public RotelRa1xHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        maximumVolume = ((BigDecimal) getThing().getConfiguration().get("maximum-volume")).intValue();
        exit = false;
        try {
            connect();
            updateStatus(ThingStatus.ONLINE);
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
            updateState(getThing().getChannel("mute").getUID(), OnOffType.OFF);
            updateState(getThing().getChannel("dimmer").getUID(), new PercentType(100));
            // Seems we need to wait a bit after initialization for the channels to
            // be ready to accept updates, so deferring input loop by 1 sec.
            scheduler.schedule(this, 1, TimeUnit.SECONDS);
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

    public PercentType readVolume() throws IOException {
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

    public PercentType readDimmer() throws IOException {
        String dimmerString = readUntil('!');
        int dimmer = Integer.parseInt(dimmerString, 10);
        // Let's make 100 % the brightest, makes more sense
        double dimmerPct = 100.0 - (dimmer * 100.0 / 6.0);
        return new PercentType(BigDecimal.valueOf(Math.round(dimmerPct)));
    }

    public DecimalType readFrequency() throws IOException {
        String freqString = readUntil('!');
        double freq;
        if ("off".equals(freqString)) {
            freq = 0.0;
        } else {
            freq = Double.parseDouble(freqString);
        }
        return new DecimalType(freq);
    }

    void powerOnRefresh() {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    send("get_volume!");
                    send("get_current_source!");
                } catch (IOException | ConfigurationError e) {
                    logger.info("Failed to request volume and source after powering on.", e);
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        while (serialPort != null && !exit) {
            try {
                String command = readCommand();
                if ("volume".equals(command)) {
                    PercentType vol = readVolume();
                    updateState(getThing().getChannel("volume").getUID(), vol);
                } else if ("mute".equals(command)) {
                    String muteState = readUntil('!');
                    updateState(getThing().getChannel("mute").getUID(),
                            "on".equals(muteState) ? OnOffType.ON : OnOffType.OFF);
                } else if ("power_off".equals(command)) {
                    power = false;
                    updateState(getThing().getChannel("mute").getUID(), OnOffType.OFF);
                    updateState(getThing().getChannel("power").getUID(), OnOffType.OFF);
                    updateState(getThing().getChannel("volume").getUID(), UnDefType.NULL);
                    updateState(getThing().getChannel("source").getUID(), UnDefType.NULL);
                } else if ("power_on".equals(command)) {
                    power = true;
                    updateState(getThing().getChannel("power").getUID(), OnOffType.ON);
                    powerOnRefresh();
                } else if ("power".equals(command)) {
                    String state = readUntil('!');
                    if ("on".equals(state)) {
                        power = true;
                        updateState(getThing().getChannel("power").getUID(), OnOffType.ON);
                        powerOnRefresh();
                    } else if ("standby".equals(state)) {
                        updateState(getThing().getChannel("mute").getUID(), OnOffType.OFF);
                        updateState(getThing().getChannel("power").getUID(), OnOffType.OFF);
                        power = false;
                    }
                } else if ("dimmer".equals(command)) {
                    updateState(getThing().getChannel("brightness").getUID(), readDimmer());
                } else if ("freq".equals(command)) {
                    updateState(getThing().getChannel("frequency").getUID(), readFrequency());
                } else if ("source".equals(command)) {
                    updateState(getThing().getChannel("source").getUID(), new StringType(readUntil('!')));
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
                    logger.info("Input error while receiving data from amplifier", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    disconnect();
                }
            } catch (Exception e) {
                if (serialPort != null) { // If serial port is closed, it's set to null,
                                          // there is no message here,
                    logger.info("Unexpected error", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Unknown error while processing input: " + e.getMessage());
                    disconnect();
                }
            }
        }
    }

    void send(String text) throws IOException, ConfigurationError {
        if (power) {
            connect();
            serialPort.getOutputStream().write(text.getBytes(StandardCharsets.US_ASCII));
        }
    }

    void sendForce(String text) throws IOException, ConfigurationError {
        connect();
        serialPort.getOutputStream().write(text.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if ("power".equals(channelUID.getId())) {
                if (command == OnOffType.ON) {
                    sendForce("power_on!");
                } else if (command == OnOffType.OFF) {
                    sendForce("power_off!");
                } else if (command instanceof RefreshType) {
                    sendForce("get_current_power!");
                }
            } else if ("mute".equals(channelUID.getId())) {
                if (command == OnOffType.ON) {
                    send("mute_on!");
                } else {
                    send("mute_off!");
                }
            } else if ("volume".equals(channelUID.getId())) {
                handleVolume(command);
            } else if ("brightness".equals(channelUID.getId())) {
                handleBrightness(command);
            } else if ("source".equals(channelUID.getId())) {
                if (command instanceof StringType) {
                    send(command.toString() + "!");
                } else {
                    send("get_current_source!");
                }
            }
        } catch (IOException e) {
            logger.info("An I/O error occurred while processing the command {}.", command, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
        } catch (ConfigurationError e) {
            logger.info("There is an error in the configuration of the thing.", e);
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
