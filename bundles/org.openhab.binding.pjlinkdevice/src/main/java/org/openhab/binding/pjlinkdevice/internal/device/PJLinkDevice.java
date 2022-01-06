/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.CachedCommand;
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
import org.openhab.binding.pjlinkdevice.internal.device.command.lampstatus.LampStatesCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.lampstatus.LampStatesResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.lampstatus.LampStatesResponse.LampState;
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
 * Represents a PJLink device and takes care of managing the TCP connection, executing commands, and authentication.
 *
 * The central interface to get information about and set status on the device.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PJLinkDevice {
    private static final int TIMEOUT = 30000;
    protected int tcpPort;
    protected InetAddress ipAddress;
    protected @Nullable String adminPassword;
    protected boolean authenticationRequired;
    protected @Nullable BufferedReader reader;
    protected @Nullable Socket socket;
    protected int timeout = TIMEOUT;
    private final Logger logger = LoggerFactory.getLogger(PJLinkDevice.class);
    private String prefixForNextCommand = "";
    private @Nullable Instant socketCreatedOn;
    private CachedCommand<LampStatesResponse> cachedLampHoursCommand = new CachedCommand<>(new LampStatesCommand(this));

    public PJLinkDevice(int tcpPort, InetAddress ipAddress, @Nullable String adminPassword, int timeout) {
        this.tcpPort = tcpPort;
        this.ipAddress = ipAddress;
        this.adminPassword = adminPassword;
        this.timeout = timeout;
    }

    public PJLinkDevice(int tcpPort, InetAddress ipAddress, @Nullable String adminPassword) {
        this(tcpPort, ipAddress, adminPassword, TIMEOUT);
    }

    @Override
    public String toString() {
        return "PJLink " + this.ipAddress + ":" + this.tcpPort;
    }

    protected Socket connect() throws IOException, ResponseException, AuthenticationException {
        return connect(false);
    }

    protected BufferedReader getReader() throws IOException, ResponseException, AuthenticationException {
        BufferedReader reader = this.reader;
        if (reader == null) {
            this.reader = reader = new BufferedReader(new InputStreamReader(connect().getInputStream()));
        }
        return reader;
    }

    protected void closeSocket(@Nullable Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // okay then, at least we tried
                logger.trace("closing of socket failed", e);
            }
        }
        this.socket = null;
        this.reader = null;
    }

    protected Socket connect(boolean forceReconnect) throws IOException, ResponseException, AuthenticationException {
        Instant now = Instant.now();
        Socket socket = this.socket;
        boolean connectionTooOld = false;
        Instant socketCreatedOn = this.socketCreatedOn;
        if (socketCreatedOn != null) {
            long millisecondsSinceLastConnect = Duration.between(socketCreatedOn, now).toMillis();
            // according to the PJLink specification, the device closes the connection after 30s idle (without notice),
            // so to be on the safe side we do not reuse sockets older than 20s
            connectionTooOld = millisecondsSinceLastConnect > 20 * 1000;
        }

        if (forceReconnect || connectionTooOld) {
            if (socket != null) {
                closeSocket(socket);
            }
        }

        this.socketCreatedOn = now;
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return socket;
        }

        SocketAddress socketAddress = new InetSocketAddress(ipAddress, tcpPort);

        try {
            this.socket = socket = new Socket();
            socket.connect(socketAddress, timeout);
            socket.setSoTimeout(timeout);
            BufferedReader reader = getReader();
            String rawHeader = reader.readLine();
            if (rawHeader == null) {
                throw new ResponseException("No PJLink header received from the device");
            }
            String header = rawHeader.toUpperCase();
            switch (header.substring(0, "PJLINK x".length())) {
                case "PJLINK 0":
                    logger.debug("Authentication not needed");
                    this.authenticationRequired = false;
                    break;
                case "PJLINK 1":
                    logger.debug("Authentication needed");
                    this.authenticationRequired = true;
                    if (this.adminPassword == null) {
                        closeSocket(socket);
                        throw new AuthenticationException("No password provided, but device requires authentication");
                    } else {
                        try {
                            authenticate(rawHeader.substring("PJLINK 1 ".length()));
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
                    logger.debug("Cannot handle introduction response {}", header);
                    throw new ResponseException("Invalid header: " + header);
            }
            return socket;
        } catch (ConnectException | SocketTimeoutException | NoRouteToHostException e) {
            // these exceptions indicate that there's no device at this address, just throw without logging
            throw e;
        } catch (IOException | ResponseException e) {
            // these exceptions seem to be more interesting in the log during a scan
            // This should not happen and might be a user configuration issue, we log a warning message therefore.
            logger.debug("Could not create a socket connection", e);
            throw e;
        }
    }

    private void authenticate(String challenge) throws ResponseException, IOException, AuthenticationException {
        new AuthenticationCommand<>(this, challenge, new PowerQueryCommand(this)).execute();
    }

    public PowerQueryResponse getPowerStatus() throws ResponseException, IOException, AuthenticationException {
        return new PowerQueryCommand(this).execute();
    }

    public void addPrefixToNextCommand(String cmd) throws IOException, AuthenticationException {
        this.prefixForNextCommand = cmd;
    }

    public static String preprocessResponse(String response) {
        // some devices send leading zero bytes, see https://github.com/openhab/openhab-addons/issues/6725
        return response.replaceAll("^\0*|\0*$", "");
    }

    public synchronized String execute(String command) throws IOException, AuthenticationException, ResponseException {
        String fullCommand = this.prefixForNextCommand + command;
        this.prefixForNextCommand = "";
        for (int numberOfTries = 0; true; numberOfTries++) {
            try {
                Socket socket = connect();
                socket.getOutputStream().write((fullCommand).getBytes());
                socket.getOutputStream().flush();

                // success, no further tries needed
                break;
            } catch (java.net.SocketException e) {
                closeSocket(socket);
                if (numberOfTries >= 2) {
                    // do not retry endlessly
                    throw e;
                }
            }
        }

        String response = null;
        while ((response = getReader().readLine()) != null && preprocessResponse(response).isEmpty()) {
            logger.debug("Got empty string response for request '{}' from {}, waiting for another line", response,
                    fullCommand.replaceAll("\r", "\\\\r"));
        }
        if (response == null) {
            throw new ResponseException(MessageFormat.format("Response to request ''{0}'' was null",
                    fullCommand.replaceAll("\r", "\\\\r")));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got response '{}' ({}) for request '{}' from {}", response,
                    Arrays.toString(response.getBytes()), fullCommand.replaceAll("\r", "\\\\r"), ipAddress);
        }
        return preprocessResponse(response);
    }

    public void checkAvailability() throws IOException, AuthenticationException, ResponseException {
        connect();
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

    public @Nullable String getAdminPassword() {
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

    public List<LampState> getLampStates() throws ResponseException, IOException, AuthenticationException {
        return new LampStatesCommand(this).execute().getResult();
    }

    public List<LampState> getLampStatesCached() throws ResponseException, IOException, AuthenticationException {
        return cachedLampHoursCommand.execute().getResult();
    }

    public String getOtherInformation() throws ResponseException, IOException, AuthenticationException {
        return new IdentificationCommand(this, IdentificationCommand.IdentificationProperty.OTHER_INFORMATION).execute()
                .getResult();
    }

    public Set<Input> getAvailableInputs() throws ResponseException, IOException, AuthenticationException {
        return new InputListQueryCommand(this).execute().getResult();
    }

    public void dispose() {
        final Socket socket = this.socket;
        if (socket != null) {
            closeSocket(socket);
        }
    }
}
