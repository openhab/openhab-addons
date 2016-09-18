/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rotelra1x.handler;

import java.io.IOException;
import java.math.BigDecimal;

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

import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link RotelRa1xHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author FaMaKe - Initial contribution
 */
public class RotelRa1xHandler extends BaseThingHandler implements Runnable {

    private final static int BAUD = 115200;
    private int max_vol = 0;
    private RXTXPort serialPort;

    private boolean connected, exit = false;
    private volatile boolean power = false;

    public RotelRa1xHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        max_vol = ((BigDecimal) getThing().getConfiguration().get("max-vol")).intValue();
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        exit = true;
        disconnect();
    }

    private void connect() throws IOException {
        if (!connected) {
            if (serialPort != null) {
                disconnect();
            }
            String portName = (String) getThing().getConfiguration().get("port");
            if (portName == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                try {
                    serialPort = new RXTXPort(portName);
                    serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                } catch (PortInUseException | UnsupportedCommOperationException e) {
                    serialPort = null;
                    throw new IOException(e);
                }
            }
            connected = true;
            // Don't need continuous updates of the display, we still get updates when
            // the volume, etc., changes
            serialPort.getOutputStream().write("display_update_manual!".getBytes("ascii"));
            Thread receiver = new Thread(this);
            receiver.start();
            updateStatus(ThingStatus.ONLINE);
            sendForce("get_current_power!");
            updateState(getThing().getChannel("mute").getUID(), OnOffType.OFF);
            updateState(getThing().getChannel("dimmer").getUID(), new PercentType(100));
        }
    }

    private void disconnect() {
        if (connected && serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        connected = false;
        if (!exit) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private String readCommand() throws IOException {
        int b;
        StringBuilder commandPart = new StringBuilder();
        do {
            b = serialPort.getInputStream().read();
            if (b == -1) {
                throw new IOException("Connection unexpectedly closed");
            } else {
                commandPart.append((char) b);
            }
        } while (b != '!' && b != '=');
        return commandPart.toString().substring(0, commandPart.length() - 1);
    }

    private String readUntil(char terminator) throws IOException {
        int b;
        StringBuilder commandPart = new StringBuilder();
        do {
            b = serialPort.getInputStream().read();
            if (b == -1) {
                throw new IOException("Connection unexpectedly closed");
            } else {
                commandPart.append((char) b);
            }
        } while (b != terminator);
        return commandPart.toString().substring(0, commandPart.length() - 1);
    }

    public PercentType readVolume() throws IOException {
        String volumeString = readUntil('!');
        int volume;
        if (volumeString.equals("min")) {
            volume = 0;
        } else if (volumeString.equals("max")) {
            volume = max_vol;
        } else {
            volume = Integer.parseInt(volumeString);
        }
        double volumePct = volume * 100.0 / max_vol;
        return new PercentType(BigDecimal.valueOf(Math.round(volumePct)));
    }

    public PercentType readDimmer() throws IOException {
        String dimmerString = readUntil('!');
        int dimmer = Integer.parseInt(dimmerString);
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
        new Thread() {
            @Override
            public void run() {
                try {
                    send("get_volume!");
                    send("get_current_source!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000); // Seems we need to wait a bit after initialization or channels won't
                                // be updated. (making run() and initialize() synchronized doesn't work)
        } catch (InterruptedException e1) {
        }
        while (connected && !exit) {
            try {
                String command = readCommand();
                if (command.equals("volume")) {
                    PercentType vol = readVolume();
                    updateState(getThing().getChannel("volume").getUID(), vol);
                } else if (command.equals("mute")) {
                    String muteState = readUntil('!');
                    updateState(getThing().getChannel("mute").getUID(),
                            "on".equals(muteState) ? OnOffType.ON : OnOffType.OFF);
                } else if (command.equals("power_off")) {
                    power = false;
                    updateState(getThing().getChannel("mute").getUID(), OnOffType.OFF);
                    updateState(getThing().getChannel("power").getUID(), OnOffType.OFF);
                    updateState(getThing().getChannel("volume").getUID(), UnDefType.NULL);
                    updateState(getThing().getChannel("source").getUID(), UnDefType.NULL);
                } else if (command.equals("power_on")) {
                    power = true;
                    updateState(getThing().getChannel("power").getUID(), OnOffType.ON);
                    powerOnRefresh();
                } else if (command.equals("power")) {
                    String state = readUntil('!');
                    if (state.equals("on")) {
                        power = true;
                        updateState(getThing().getChannel("power").getUID(), OnOffType.ON);
                        powerOnRefresh();
                    } else if (state.equals("standby")) {
                        updateState(getThing().getChannel("mute").getUID(), OnOffType.OFF);
                        updateState(getThing().getChannel("power").getUID(), OnOffType.OFF);
                        power = false;
                    }
                } else if (command.equals("dimmer")) {
                    updateState(getThing().getChannel("dimmer").getUID(), readDimmer());
                } else if (command.equals("freq")) {
                    updateState(getThing().getChannel("frequency").getUID(), readFrequency());
                } else if (command.equals("source")) {
                    updateState(getThing().getChannel("source").getUID(), new StringType(readUntil('!')));
                } else if (command.equals("display")) {
                    String str_length = readUntil(',');
                    int length = Integer.parseInt(str_length);
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
                e.printStackTrace();
                disconnect();
            }
        }
    }

    void send(String text) throws IOException {
        if (power) {
            connect();
            serialPort.getOutputStream().write(text.getBytes());
        }
    }

    void sendForce(String text) throws IOException {
        connect();
        serialPort.getOutputStream().write(text.getBytes());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals("power")) {
                if (command == OnOffType.ON) {
                    sendForce("power_on!");
                } else if (command == OnOffType.OFF) {
                    sendForce("power_off!");
                } else if (command instanceof RefreshType) {
                    sendForce("get_current_power!");
                }
            } else if (channelUID.getId().equals("mute")) {
                if (command == OnOffType.ON) {
                    send("mute_on!");
                } else {
                    send("mute_off!");
                }
            } else if (channelUID.getId().equals("volume")) {
                handleVolume(command);
            } else if (channelUID.getId().equals("dimmer")) {
                // Invert the scale so 100% is brightest
                if (command instanceof PercentType) {
                    double value = 6 - Math.floor(((PercentType) command).doubleValue() * 6 / 100.0);
                    send("dimmer_" + Integer.toString((int) value) + "!");
                }
            } else if (channelUID.getId().equals("source")) {
                if (command instanceof StringType) {
                    send(command.toString() + "!");
                } else {
                    send("get_current_source!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    private void handleVolume(Command command) throws IOException {
        if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
            send("volume_up!");
        } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
            send("volume_down!");
        } else if (command instanceof DecimalType) {
            double value = Double.parseDouble(command.toString()) * max_vol / 100.0;
            send("volume_" + Integer.toString((int) value) + "!");
        } else if (command instanceof RefreshType) {
            send("get_current_volume!");
        }
    }
}
