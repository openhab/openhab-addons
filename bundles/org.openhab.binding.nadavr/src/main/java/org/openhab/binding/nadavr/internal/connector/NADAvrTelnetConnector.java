/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nadavr.internal.connector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.nadavr.internal.NADAvrConfiguration;
import org.openhab.binding.nadavr.internal.NADAvrState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NADAvrTelnetConnector.java} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J Schoepel - Initial contribution
 */
public class NADAvrTelnetConnector extends NADAvrConnector implements NADAvrTelnetListener {

    private final Logger logger = LoggerFactory.getLogger(NADAvrTelnetConnector.class);

    // All regular commands. Example: Main.Power=on, Main.Speaker.Front.Config=Small, Source2.Name=Apple TV
    private static final Pattern COMMAND_PATTERN = Pattern.compile("([\\w\\[.\\]\\\\]+)=(.*)");
    private NADAvrTelnetClientThread telnetClientThread;

    protected boolean disposing = false;

    private Future<?> telnetStateRequest;

    // protected HashMap<String, String> avrCommandStates = new HashMap<String, String>();

    private CommandStates avrCommandStates = new CommandStates();
    private static final BigDecimal NINETYNINE = new BigDecimal("99");

    private String thingUID;

    /**
     *
     */
    public NADAvrTelnetConnector(NADAvrConfiguration config, NADAvrState state, ScheduledExecutorService scheduler,
            String thingUID) {
        this.config = config;
        this.scheduler = scheduler;
        this.state = state;
        this.thingUID = thingUID;
    }

    @Override
    public void receivedLine(String line) {

        if (COMMAND_PATTERN.matcher(line).matches()) {
            Matcher matcher = COMMAND_PATTERN.matcher(line);
            matcher.matches();
            String group1 = matcher.group(1);
            String group2 = matcher.group(2);
            logger.debug("Found Command {} and Value {}", group1, group2);

            String command = getCommand(line);
            String value = getCommandState(line);

            logger.debug("Received Command: {}, value: {}", command, value);
            if (avrCommandStates != null) {
                avrCommandStates.setCommandState(command, value);
            }

            // avrCommandStates.put(command, value);

            // use received command (event) from telnet to update state
            switch (command) {
                case "Main.Power":
                    if (value.equals("On") || value.equals("Off")) {
                        state.setPower(value.equals("On")); // Boolean if not on (true) its off(false)
                    }
                    break;
                case "Main.ListeningMode":
                    state.setListeningMode(value);
                    break;
                case "Main.Mute":
                    if (value.equals("On") || value.equals("Off")) {
                        state.setMute(value.equals("On")); // Boolean if not on (true) its off(false)
                    }
                case "Main.Volume":
                    String dB = StringUtils.substring(value, -2);
                    if (StringUtils.isNumeric(dB)) {
                        state.setMainVolume(fromNADValue(value));
                        ; // Convert numeric string to number
                    }
                default:
                    return;
            }
        } else {
            logger.debug("Ignoring received line: '{}'", line);
        }

    }

    @Override
    public void telnetClientConnected(boolean connected) {
        if (!connected) {
            if (!disposing) {
                logger.debug("Telnet client disconnected.");
                state.connectionError("Error connecting to the telnet port.");
            }
        } else {
            refreshState();
        }
    }

    @Override
    public void connect() {
        telnetClientThread = new NADAvrTelnetClientThread(config, this);
        telnetClientThread.setName("OH-binding-" + thingUID);
        telnetClientThread.start();

    }

    @Override
    public void dispose() {
        logger.debug("disposing connector");
        disposing = true;

        if (telnetStateRequest != null) {
            telnetStateRequest.cancel(true);
            telnetStateRequest = null;
        }

        if (telnetClientThread != null) {
            telnetClientThread.interrupt();
            // Invoke a shutdown after interrupting the thread to close the socket immediately,
            // otherwise the client keeps running until a line was received from the telnet connection
            telnetClientThread.shutdown();
            telnetClientThread = null;
        }

    }

    private void refreshState() {
        // Sends a series of state query commands over the telnet connection
        telnetStateRequest = scheduler.submit(() -> {
            List<String> cmds = new ArrayList<>(Arrays.asList("Main", "Source1?", "Source2?", "Source3?", "Source4?",
                    "Source5?", "Source6?", "Source7?", "Source8?", "Source9?", "Source10?"));
            // List<String> cmds = new ArrayList<>(
            // Arrays.asList("Main?", "Zone2?", "Zone3?", "Zone4", "Source1", "Source2?", "Source3?", "Source4?",
            // "Source5?", "Source6?", "Source7?", "Source8?", "Source9?", "Source10?"));

            // if (config.getZoneCount() > 1) {
            // cmds.add("Z2?");
            // cmds.add("Z2MU?");
            // }
            // if (config.getZoneCount() > 2) {
            // cmds.add("Z3?");
            // cmds.add("Z3MU?");
            // }
            for (String cmd : cmds) {
                internalSendCommand(cmd);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.trace("requestStateOverTelnet() - Interrupted while requesting state.");
                    Thread.currentThread().interrupt();
                }
            }

        });
    }

    @Override
    protected void internalSendCommand(String command) {
        logger.debug("Sending command '{}'", command);
        if (command == null || command.isBlank()) {
            logger.warn("Trying to send empty command");
            return;
        }
        telnetClientThread.sendCommand(command);
    }

    private String getCommand(String line) {
        int equalSignPosition = line.indexOf("=");

        String command = line.substring(0, equalSignPosition);
        return command;
    }

    private String getCommandState(String line) {
        int equalSignPosition = line.indexOf("=");

        String commandState = line.substring(equalSignPosition + 1);
        return commandState;
    }

    private BigDecimal fromNADValue(String string) {
        /*
         * 450 = 45.0
         * 45 = 45
         * 045 = 4.5
         * 04 = 4
         */
        BigDecimal value = new BigDecimal(string);
        if (value.compareTo(NINETYNINE) == 1 || (string.startsWith("0") && string.length() > 2)) {
            value = value.divide(BigDecimal.TEN);
        }
        return value;
    }
}
