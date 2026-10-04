/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal.connector.telnet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes the connection to the receiver and manages it.
 * It is also responsible for sending commands to the receiver.
 *
 * @author Jeroen Idserda - Initial Contribution (1.x Binding)
 * @author Jan-Willem Veldhuis - Refactored for 2.x
 */
@NonNullByDefault
public class DenonMarantzTelnetConnector extends DenonMarantzConnector implements DenonMarantzTelnetListener {

    private final Logger logger = LoggerFactory.getLogger(DenonMarantzTelnetConnector.class);

    // All regular commands. Example: PW, SICD, SITV, Z2MU
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^([A-Z0-9]{2})(.+)$");

    // Example: E2Counting Crows
    private static final Pattern DISPLAY_PATTERN = Pattern.compile("^(E|A)([0-9]{1})(.+)$");

    private static final BigDecimal NINETYNINE = new BigDecimal("99");

    private static final Map<String, String> CHANNEL_VOLUME_COMMANDS = Map.ofEntries(
            Map.entry("CVFL", DenonMarantzBindingConstants.CHANNEL_CVFL),
            Map.entry("CVFR", DenonMarantzBindingConstants.CHANNEL_CVFR),
            Map.entry("CVC", DenonMarantzBindingConstants.CHANNEL_CVC),
            Map.entry("CVSW", DenonMarantzBindingConstants.CHANNEL_CVSW),
            Map.entry("CVSW2", DenonMarantzBindingConstants.CHANNEL_CVSW2),
            Map.entry("CVSW3", DenonMarantzBindingConstants.CHANNEL_CVSW3),
            Map.entry("CVSW4", DenonMarantzBindingConstants.CHANNEL_CVSW4),
            Map.entry("CVSL", DenonMarantzBindingConstants.CHANNEL_CVSL),
            Map.entry("CVSR", DenonMarantzBindingConstants.CHANNEL_CVSR),
            Map.entry("CVSBL", DenonMarantzBindingConstants.CHANNEL_CVSBL),
            Map.entry("CVSBR", DenonMarantzBindingConstants.CHANNEL_CVSBR),
            Map.entry("CVSB", DenonMarantzBindingConstants.CHANNEL_CVSB),
            Map.entry("CVFHL", DenonMarantzBindingConstants.CHANNEL_CVFHL),
            Map.entry("CVFHR", DenonMarantzBindingConstants.CHANNEL_CVFHR),
            Map.entry("CVFWL", DenonMarantzBindingConstants.CHANNEL_CVFWL),
            Map.entry("CVFWR", DenonMarantzBindingConstants.CHANNEL_CVFWR),
            Map.entry("CVTFL", DenonMarantzBindingConstants.CHANNEL_CVTFL),
            Map.entry("CVTFR", DenonMarantzBindingConstants.CHANNEL_CVTFR),
            Map.entry("CVTML", DenonMarantzBindingConstants.CHANNEL_CVTML),
            Map.entry("CVTMR", DenonMarantzBindingConstants.CHANNEL_CVTMR),
            Map.entry("CVTRL", DenonMarantzBindingConstants.CHANNEL_CVTRL),
            Map.entry("CVTRR", DenonMarantzBindingConstants.CHANNEL_CVTRR),
            Map.entry("CVRHL", DenonMarantzBindingConstants.CHANNEL_CVRHL),
            Map.entry("CVRHR", DenonMarantzBindingConstants.CHANNEL_CVRHR),
            Map.entry("CVFDL", DenonMarantzBindingConstants.CHANNEL_CVFDL),
            Map.entry("CVFDR", DenonMarantzBindingConstants.CHANNEL_CVFDR),
            Map.entry("CVSDL", DenonMarantzBindingConstants.CHANNEL_CVSDL),
            Map.entry("CVSDR", DenonMarantzBindingConstants.CHANNEL_CVSDR),
            Map.entry("CVBDL", DenonMarantzBindingConstants.CHANNEL_CVBDL),
            Map.entry("CVBDR", DenonMarantzBindingConstants.CHANNEL_CVBDR),
            Map.entry("CVSHL", DenonMarantzBindingConstants.CHANNEL_CVSHL),
            Map.entry("CVSHR", DenonMarantzBindingConstants.CHANNEL_CVSHR),
            Map.entry("CVTS", DenonMarantzBindingConstants.CHANNEL_CVTS),
            Map.entry("CVCH", DenonMarantzBindingConstants.CHANNEL_CVCH),
            Map.entry("CVTTR", DenonMarantzBindingConstants.CHANNEL_CVTTR));

    private @Nullable DenonMarantzTelnetClientThread telnetClientThread;

    private boolean displayNowplaying = false;

    protected boolean disposing = false;

    private @Nullable Future<?> telnetStateRequest;

    private String thingUID;

    public DenonMarantzTelnetConnector(DenonMarantzConfiguration config, DenonMarantzState state,
            ScheduledExecutorService scheduler, String thingUID) {
        super(config, scheduler, state);
        this.thingUID = thingUID;
    }

    /**
     * Set up the connection to the receiver. Either using Telnet or by polling the HTTP API.
     */
    @Override
    public void connect() {
        DenonMarantzTelnetClientThread telnetClientThread = this.telnetClientThread = new DenonMarantzTelnetClientThread(
                config, this);
        telnetClientThread.setName("OH-binding-" + thingUID);
        telnetClientThread.start();
    }

    @Override
    public void telnetClientConnected(boolean connected) {
        if (!connected) {
            Boolean isTelnet = config.isTelnet();
            if (isTelnet != null && isTelnet && !disposing) {
                logger.debug("Telnet client disconnected.");
                state.connectionError(
                        "Error connecting to the telnet port. Consider disabling telnet in this Thing's configuration to use HTTP polling instead.");
            }
        } else {
            refreshState();
        }
    }

    /**
     * Shutdown the telnet client (if initialized) and the http client
     */
    @Override
    public void dispose() {
        logger.debug("disposing connector");
        disposing = true;

        Future<?> telnetStateRequest = this.telnetStateRequest;
        if (telnetStateRequest != null) {
            telnetStateRequest.cancel(true);
            this.telnetStateRequest = null;
        }

        DenonMarantzTelnetClientThread telnetClientThread = this.telnetClientThread;
        if (telnetClientThread != null) {
            telnetClientThread.interrupt();
            // Invoke a shutdown after interrupting the thread to close the socket immediately,
            // otherwise the client keeps running until a line was received from the telnet connection
            telnetClientThread.shutdown();
            this.telnetClientThread = null;
        }
    }

    private void refreshState() {
        // Sends a series of state query commands over the telnet connection
        telnetStateRequest = scheduler.submit(() -> {
            List<String> cmds = new ArrayList<>(
                    Arrays.asList("PW?", "MS?", "MV?", "ZM?", "MU?", "SI?", "CV?", "MNZST?", "CVTTR ?", "SPPR ?"));
            if (config.getZoneCount() > 1) {
                cmds.add("Z2?");
                cmds.add("Z2MU?");
            }
            if (config.getZoneCount() > 2) {
                cmds.add("Z3?");
                cmds.add("Z3MU?");
            }
            for (String cmd : cmds) {
                internalSendCommand(cmd);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    logger.trace("requestStateOverTelnet() - Interrupted while requesting state.");
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * This method tries to parse information received over the telnet connection.
     * It can be quite unreliable. Some chars go missing or turn into other chars. That's
     * why each command is validated using a regex.
     *
     * @param line The received command (one line)
     */
    @Override
    public void receivedLine(String line) {
        if (COMMAND_PATTERN.matcher(line).matches()) {
            /*
             * This splits the commandString into the command and the parameter. SICD
             * for example has SI as the command and CD as the parameter.
             */
            String command;
            String value;

            int spaceIndex = line.indexOf(' ');
            if (spaceIndex > 0) {
                command = line.substring(0, spaceIndex);
                value = line.substring(spaceIndex + 1).trim();
            } else {
                command = line.substring(0, 2);
                value = line.substring(2).trim();
            }

            logger.debug("Received Command: {}, value: {}", command, value);

            String channel = CHANNEL_VOLUME_COMMANDS.get(command);
            if (channel != null) {
                processChannelVolume(value, channel);
                return;
            }

            // use received command (event) from telnet to update state
            switch (command) {
                case "SI": // Switch Input
                    state.setInput(value);
                    break;
                case "PW": // Power
                    if ("ON".equals(value) || "STANDBY".equals(value)) {
                        state.setPower("ON".equals(value));
                    }
                    break;
                case "MS": // Main zone surround program
                    state.setSurroundProgram(value);
                    break;
                case "MNZST": // All Zone Stereo
                    if ("ON".equals(value) || "OFF".equals(value)) {
                        state.setAllZoneStereo("ON".equals(value));
                    }
                    break;
                case "SPPR":
                    if ("1".equals(value) || "2".equals(value)) {
                        state.setSpeakerPreset(new BigDecimal(value));
                    }
                    break;
                case "MV": // Main zone volume
                    if (value.chars().allMatch(Character::isDigit)) {
                        state.setMainVolume(fromDenonValue(value));
                    }
                    break;
                case "MU": // Main zone mute
                    if ("ON".equals(value) || "OFF".equals(value)) {
                        state.setMute("ON".equals(value));
                    }
                    break;
                case "NS": // Now playing information
                    processTitleCommand(value);
                    break;
                case "Z2": // Zone 2
                    if ("ON".equals(value) || "OFF".equals(value)) {
                        state.setZone2Power("ON".equals(value));
                    } else if ("MUON".equals(value) || "MUOFF".equals(value)) {
                        state.setZone2Mute("MUON".equals(value));
                    } else if (value.chars().allMatch(Character::isDigit)) {
                        state.setZone2Volume(fromDenonValue(value));
                    } else {
                        state.setZone2Input(value);
                    }
                    break;
                case "Z3": // Zone 3
                    if ("ON".equals(value) || "OFF".equals(value)) {
                        state.setZone3Power("ON".equals(value));
                    } else if ("MUON".equals(value) || "MUOFF".equals(value)) {
                        state.setZone3Mute("MUON".equals(value));
                    } else if (value.chars().allMatch(Character::isDigit)) {
                        state.setZone3Volume(fromDenonValue(value));
                    } else {
                        state.setZone3Input(value);
                    }
                    break;
                case "Z4": // Zone 4
                    if ("ON".equals(value) || "OFF".equals(value)) {
                        state.setZone4Power("ON".equals(value));
                    } else if ("MUON".equals(value) || "MUOFF".equals(value)) {
                        state.setZone4Mute("MUON".equals(value));
                    } else if (value.chars().allMatch(Character::isDigit)) {
                        state.setZone4Volume(fromDenonValue(value));
                    } else {
                        state.setZone4Input(value);
                    }
                    break;
                case "ZM": // Main zone
                    if ("ON".equals(value) || "OFF".equals(value)) {
                        state.setMainZonePower("ON".equals(value));
                    }
                    break;
            }
        } else {
            logger.trace("Ignoring received line: '{}'", line);
        }
    }

    private void processChannelVolume(String value, String channel) {
        if (value.chars().allMatch(Character::isDigit)) {
            BigDecimal denonValue = fromDenonValue(value);
            BigDecimal db = denonValue.subtract(DenonMarantzBindingConstants.CHANNEL_VOLUME_DB_OFFSET);

            state.setChannelVolume(channel, db);
        }
    }

    private BigDecimal fromDenonValue(String string) {
        /*
         * 455 = 45,5
         * 45 = 45
         * 045 = 4,5
         * 04 = 4
         */
        BigDecimal value = new BigDecimal(string);
        if (value.compareTo(NINETYNINE) == 1 || (string.startsWith("0") && string.length() > 2)) {
            value = value.divide(BigDecimal.TEN);
        }
        return value;
    }

    private void processTitleCommand(String value) {
        if (DISPLAY_PATTERN.matcher(value).matches()) {
            Integer commandNo = Integer.valueOf(value.substring(1, 2));
            String titleValue = value.substring(2);

            if (commandNo == 0) {
                displayNowplaying = titleValue.contains("Now Playing");
            }

            String nowPlaying = displayNowplaying ? cleanupDisplayInfo(titleValue) : "";

            switch (commandNo) {
                case 1:
                    state.setNowPlayingTrack(nowPlaying);
                    break;
                case 2:
                    state.setNowPlayingArtist(nowPlaying);
                    break;
                case 4:
                    state.setNowPlayingAlbum(nowPlaying);
                    break;
            }
        }
    }

    @Override
    protected void internalSendCommand(String command) {
        logger.debug("Sending command '{}'", command);
        if (command.isBlank()) {
            logger.warn("Trying to send empty command");
            return;
        }
        DenonMarantzTelnetClientThread telnetClientThread = this.telnetClientThread;
        if (telnetClientThread != null) {
            telnetClientThread.sendCommand(command);
        }
    }

    /**
     * Display info could contain some garbled text, attempt to clean it up.
     */
    private String cleanupDisplayInfo(String titleValue) {
        byte[] firstByteRemoved = Arrays.copyOfRange(titleValue.getBytes(), 1, titleValue.getBytes().length);
        return new String(firstByteRemoved).replaceAll("[\u0000-\u001f]", "");
    }
}
