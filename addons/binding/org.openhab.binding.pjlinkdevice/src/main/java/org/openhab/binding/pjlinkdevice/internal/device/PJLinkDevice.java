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
package org.openhab.binding.pjlinkdevice.internal.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;
import org.openhab.binding.pjlinkdevice.internal.device.command.authentication.AuthenticationCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.errorstatus.ErrorStatusQueryCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.errorstatus.ErrorStatusQueryResponse.ErrorStatusDevicePart;
import org.openhab.binding.pjlinkdevice.internal.device.command.errorstatus.ErrorStatusQueryResponse.ErrorStatusQueryResponseState;
import org.openhab.binding.pjlinkdevice.internal.device.command.identification.IdentificationCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.Input;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.InputInstructionCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.InputListQueryCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.InputQueryCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.InputQueryResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteInstructionCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteInstructionCommand.MuteInstructionChannel;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteInstructionCommand.MuteInstructionState;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteQueryCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteQueryResponse.MuteQueryResponseValue;
import org.openhab.binding.pjlinkdevice.internal.device.command.power.PowerInstructionCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.power.PowerQueryCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.power.PowerQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class PJLinkDevice {
    private static final int TIMEOUT = 30000;
    protected int tcpPort;
    protected InetAddress ipAddress;
    protected String adminPassword;
    protected boolean authenticationRequired;
    protected BufferedReader reader;
    protected Socket socket;
    protected int timeout = TIMEOUT;
    static private Logger logger = LoggerFactory.getLogger(PJLinkDevice.class);
    private String prefixForNextCommand;
    private Instant socketCreatedOn;

    public PJLinkDevice(int tcpPort, InetAddress ipAddress, String adminPassword, int timeout) {
        super();

        this.tcpPort = tcpPort;
        this.ipAddress = ipAddress;
        this.adminPassword = adminPassword;
        this.timeout = timeout;
    }

    public PJLinkDevice(int tcpPort, InetAddress ipAddress, String adminPassword) {
        this(tcpPort, ipAddress, adminPassword, TIMEOUT);
    }

    @Override
    public String toString() {
        return "PJLink " + this.ipAddress + ":" + this.tcpPort;
    }

    protected void connect() throws IOException, ResponseException, AuthenticationException {
        this.connect(false);
    }

    protected void connect(boolean forceReconnect) throws IOException, ResponseException, AuthenticationException {
        Instant now = Instant.now();
        boolean connectionTooOld = false;
        if (this.socketCreatedOn != null) {
            long milliseondsSinceLastConnect = Duration.between(this.socketCreatedOn, now).toMillis();
            // according to the PJLink specification, the device closes the connection after 30s idle (without notice),
            // so to be on the safe side we do not reuse sockets older than 20s
            connectionTooOld = milliseondsSinceLastConnect > 20 * 1000;
        }

        if (forceReconnect || connectionTooOld) {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        }

        this.socketCreatedOn = now;
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        SocketAddress socketAddress = new InetSocketAddress(ipAddress, tcpPort);

        try {
            this.socket = new Socket();
            socket.connect(socketAddress, timeout);
            socket.setSoTimeout(timeout);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String header = reader.readLine();
            if (header == null) {
                throw new ResponseException("No PJLink header received from the device");
            }
            switch (header.substring(0, "PJLINK x".length())) {
                case "PJLINK 0":
                    logger.info("Authentication not needed");
                    this.authenticationRequired = false;
                    break;
                case "PJLINK 1":
                    logger.warn("Authentication needed");
                    this.authenticationRequired = true;
                    if (this.adminPassword == null) {
                        this.socket.close();
                        throw new AuthenticationException("No password provided, but device requires authentication");
                    } else {
                        try {
                            this.authenticate(header.substring("PJLINK 1 ".length()));
                        } catch (AuthenticationException e) {
                            // propagate AuthenticationException
                            throw e;
                        } catch (ResponseException e) {
                            // maybe only the test command is broken on the device
                            // as long as we don't get an AuthenticationException, we'll just ignore it for now
                        }
                    }
                    break;
                default:
                    logger.warn("Cannot handle introduction response {}", header);
                    throw new ResponseException("Invalid header: " + header);
            }
        } catch (ConnectException | SocketTimeoutException | NoRouteToHostException e) {
            throw e;
        } catch (IOException | ResponseException e) {
            // This should not happen and might be a user configuration issue, we log a warning message therefore.
            logger.warn("Could not create a socket connection", e);
            throw e;
        }
    }

    private void authenticate(String challenge) throws ResponseException, IOException, AuthenticationException {
        new AuthenticationCommand(this, challenge, new PowerQueryCommand(this)).execute();
    }

    public PowerQueryResponse getPowerStatus() throws ResponseException, IOException, AuthenticationException {
        return new PowerQueryCommand(this).execute();
    }

    public void addPrefixToNextCommand(String cmd) throws IOException, AuthenticationException {
        this.prefixForNextCommand = cmd;
    }

    public synchronized String execute(String command) throws IOException, AuthenticationException, ResponseException {
        this.connect();
        String fullCommand = command;
        if (this.prefixForNextCommand != null) {
            fullCommand = this.prefixForNextCommand + fullCommand;
            this.prefixForNextCommand = null;
        }
        int numberOfTries = 0;
        while (true) {
            numberOfTries++;
            try {
                socket.getOutputStream().write((fullCommand).getBytes());
                socket.getOutputStream().flush();

                break;
            } catch (java.net.SocketException e) {
                if (numberOfTries < 2) {
                    this.connect(true);
                } else {
                    throw e;
                }
            }
        }
        String response = null;
        while ((response = reader.readLine()) != null && response.isEmpty()) {
            logger.info("Got empty string response for request '{}' from {}, waiting for another line", response,
                    fullCommand.replaceAll("\r", "\\\\r"), ipAddress.toString());
        }
        logger.info("Got response '{}' for request '{}' from {}", response, fullCommand.replaceAll("\r", "\\\\r"),
                ipAddress.toString());
        return response;
    }

    public void checkAvailability() throws IOException, AuthenticationException, ResponseException {
        this.connect();
    }

    public String getName() throws IOException, ResponseException, AuthenticationException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.NAME).execute().getResult();
    }

    public String getManufacturer() throws IOException, ResponseException, AuthenticationException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.MANUFACTURER).execute()
                .getResult();
    }

    public String getModel() throws IOException, ResponseException, AuthenticationException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.MODEL).execute()
                .getResult();
    }

    public String getFullDescription() throws AuthenticationException, ResponseException {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(getManufacturer());
            sb.append(" ");
        } catch (ResponseException | IOException e) {
            // okay, we'll try the other identification commands
        }

        try {
            sb.append(getModel());
        } catch (ResponseException | IOException e1) {
            // okay, we'll try the other identification commands
        }

        try {
            String name = getName();
            if (!name.isEmpty()) {
                sb.append(": ");
                sb.append(name);
            }
        } catch (ResponseException | IOException e2) {
            // okay, we'll try the other identification commands
        }

        if (sb.length() == 0) {
            throw new ResponseException("None of the identification commands worked");
        }

        return sb.toString();
    }

    public String getPJLinkClass() throws IOException, AuthenticationException, ResponseException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.CLASS).execute()
                .getResult();
    }

    public void powerOn() throws ResponseException, IOException, AuthenticationException {
        new PowerInstructionCommand(this, PowerInstructionCommand.PowerInstructionState.ON).execute();
    }

    public void powerOff() throws IOException, ResponseException, AuthenticationException {
        new PowerInstructionCommand(this, PowerInstructionCommand.PowerInstructionState.OFF).execute();
    }

    public String getAdminPassword() {
        return this.adminPassword;
    }

    public Boolean getAuthenticationRequired() {
        return this.authenticationRequired;
    }

    public InputQueryResponse getInputStatus() throws ResponseException, IOException, AuthenticationException {
        return new InputQueryCommand(this).execute();
    }

    public void setInput(Input input) throws ResponseException, IOException, AuthenticationException {
        new InputInstructionCommand(this, input).execute();
    }

    public MuteQueryResponseValue getMuteStatus() throws ResponseException, IOException, AuthenticationException {
        return new MuteQueryCommand(this).execute().getResult();
    }

    public void setMute(MuteInstructionChannel channel, boolean muteOn)
            throws ResponseException, IOException, AuthenticationException {
        new MuteInstructionCommand(this, muteOn ? MuteInstructionState.ON : MuteInstructionState.OFF, channel)
                .execute();
    }

    public Map<ErrorStatusDevicePart, ErrorStatusQueryResponseState> getErrorStatus()
            throws ResponseException, IOException, AuthenticationException {
        return new ErrorStatusQueryCommand(this).execute().getResult();
    }

    public String getLampHours() throws ResponseException, IOException, AuthenticationException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.LAMP_HOURS).execute()
                .getResult();
    }

    public String getOtherInformation() throws ResponseException, IOException, AuthenticationException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.OTHER_INFORMATION).execute()
                .getResult();
    }

    public Set<Input> getAvailableInputs() throws ResponseException, IOException, AuthenticationException {
        return new InputListQueryCommand(this).execute().getResult();

    }
}
