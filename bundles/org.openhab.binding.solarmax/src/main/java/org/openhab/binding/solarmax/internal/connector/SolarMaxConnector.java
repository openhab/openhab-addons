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
package org.openhab.binding.solarmax.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The {@link SolarMaxConnector} class is used to communicated with the SolarMax device (on a binary level)
 *
 * With a little help from https://github.com/sushiguru/solar-pv/blob/master/solmax/pv.php
 * 
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxConnector {

    /**
     * default port number of SolarMax devices is...
     */
    private static final int DEFAULT_PORT = 12345;

    private static final Logger LOGGER = LoggerFactory.getLogger(SolarMaxConnector.class);

    /**
     * gets all known values from the SolarMax device addressable at host:portNumber
     *
     * @param host hostname or ip address of the SolarMax device to be contacted
     * @param portNumber portNumber the SolarMax is listening on (default is 12345)
     * @param deviceAddress
     * @param settings timeout and retry settings
     * @return
     * @throws SolarMaxException if some other exception occurs
     */
    public static SolarMaxData getAllValuesFromSolarMax(final String host, final int portNumber,
            final int deviceAddress, final SolarMaxConnectionSettings settings) throws SolarMaxException {
        List<SolarMaxCommandKey> commandList = new ArrayList<>();

        for (SolarMaxCommandKey solarMaxCommandKey : SolarMaxCommandKey.values()) {
            if (solarMaxCommandKey != SolarMaxCommandKey.UNKNOWN) {
                commandList.add(solarMaxCommandKey);
            }
        }

        SolarMaxData solarMaxData = new SolarMaxData();

        // get the data from the SolarMax device. Not every model supports every command (unsupported commands are
        // silently left out of the response, see MaxComm protocol description), so the communication is considered
        // successful as soon as the device returned any value at all.

        Map<SolarMaxCommandKey, @Nullable String> valuesFromSolarMax = getValuesFromSolarMax(host, portNumber,
                deviceAddress, commandList, settings);
        if (LOGGER.isDebugEnabled()) {
            for (SolarMaxCommandKey solarMaxCommandKey : commandList) {
                if (!valuesFromSolarMax.containsKey(solarMaxCommandKey)) {
                    LOGGER.debug("    Command {} ({}) was not answered by the device", solarMaxCommandKey,
                            solarMaxCommandKey.getCommandKey());
                }
            }
        }
        solarMaxData.setDataDateTime(ZonedDateTime.now());
        solarMaxData.setCommunicationSuccessful(!valuesFromSolarMax.isEmpty());
        solarMaxData.setData(valuesFromSolarMax);

        return solarMaxData;
    }

    /**
     * gets values from the SolarMax device addressable at host:portNumber
     * 
     * @param host hostname or ip address of the SolarMax device to be contacted
     * @param portNumber portNumber the SolarMax is listening on (default is 12345)
     * @param commandList a list of commands to be sent to the SolarMax device
     * @param settings timeout and retry settings
     * @return
     * @throws SolarMaxException if the values could not be read (also after retrying)
     */
    private static Map<SolarMaxCommandKey, @Nullable String> getValuesFromSolarMax(final String host,
            final int portNumber, final int deviceAddress, final List<SolarMaxCommandKey> commandList,
            final SolarMaxConnectionSettings settings) throws SolarMaxException {
        final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(settings.maxDuration());

        Map<SolarMaxCommandKey, @Nullable String> returnMap = new HashMap<>();

        // SolarMax can't answer correclty if too many commands are send in a single request, so limit it to 16 at a
        // time
        int maxConcurrentCommands = 16;
        int requestsRequired = (commandList.size() / maxConcurrentCommands);
        if (commandList.size() % maxConcurrentCommands != 0) {
            requestsRequired = requestsRequired + 1;
        }
        for (int requestNumber = 0; requestNumber < requestsRequired; requestNumber++) {
            LOGGER.debug("    Requesting data from {}:{} (Device Address {}) with timeout of {}ms", host, portNumber,
                    deviceAddress, settings.timeout());

            int firstCommandNumber = requestNumber * maxConcurrentCommands;
            int lastCommandNumber = (requestNumber + 1) * maxConcurrentCommands;
            if (lastCommandNumber > commandList.size()) {
                lastCommandNumber = commandList.size();
            }
            List<SolarMaxCommandKey> commandsToSend = commandList.subList(firstCommandNumber, lastCommandNumber);

            returnMap.putAll(getValuesWithRetries(host, portNumber, deviceAddress, commandsToSend, settings, deadline));

            // SolarMax can't deal with requests too close to one another, so just wait a moment
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return returnMap;
    }

    /**
     * sends a single request (on a new connection) and repeats it if it fails, as long as the retries are not used up
     * and the repetition still fits into the time budget. Only one connection is open at any time (MaxComm: the device
     * handles only one request at a time).
     */
    private static Map<SolarMaxCommandKey, @Nullable String> getValuesWithRetries(final String host,
            final int portNumber, final int deviceAddress, final List<SolarMaxCommandKey> commandsToSend,
            final SolarMaxConnectionSettings settings, final long deadline) throws SolarMaxException {
        int retry = 0;
        while (true) {
            try {
                Socket socket = getSocketConnection(host, portNumber, settings.timeout());
                return getValuesFromSolarMax(socket, deviceAddress, commandsToSend, settings.timeout());
            } catch (UnknownHostException e) {
                // retrying won't help
                throw new SolarMaxConnectionException("Unknown host '" + host + "'", e);
            } catch (SolarMaxException e) {
                if (retry >= settings.retries()) {
                    throw e;
                }
                long remainingMillis = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
                if (remainingMillis < settings.retryDelay() + settings.maxAttemptDuration()) {
                    LOGGER.debug("    Request to {}:{} failed ({}), not retrying: only {}ms left of the time budget",
                            host, portNumber, e.getMessage(), remainingMillis);
                    throw e;
                }
                retry++;
                LOGGER.debug("    Request to {}:{} failed ({}), retry {} of {} in {}ms", host, portNumber,
                        e.getMessage(), retry, settings.retries(), settings.retryDelay());
                try {
                    Thread.sleep(settings.retryDelay());
                } catch (InterruptedException ie) {
                    // the thing handler is being disposed
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    /**
     * @return the type and the message of an exception, e.g. "SocketTimeoutException: connect timed out", so the
     *         cause of a communication error can be told apart in the thing status and the log
     */
    static String describe(Throwable e) {
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }

    static String getCommandString(List<SolarMaxCommandKey> commandList) {
        String commandString = "";
        for (SolarMaxCommandKey command : commandList) {
            if (!commandString.isEmpty()) {
                commandString = commandString + ";";
            }
            commandString = commandString + command.getCommandKey();
        }
        return commandString;
    }

    /**
     * @param timeout time in milliseconds the complete response may take. The socket timeout applies to every single
     *            read and starts again with each byte, so a device sending its response slowly byte by byte could
     *            otherwise keep the request running much longer than the timeout.
     */
    private static Map<SolarMaxCommandKey, @Nullable String> getValuesFromSolarMax(final Socket socket,
            final int deviceAddress, final List<SolarMaxCommandKey> commandList, final int timeout)
            throws SolarMaxException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            return getValuesFromSolarMax(socket, outputStream, inputStream, deviceAddress, commandList, timeout);
        } catch (final IOException e) {
            throw new SolarMaxException("Error getting input/output streams from socket: " + describe(e), e);
        } finally {
            try {
                socket.close();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (final IOException e) {
                // ignore the error, we're dying anyway...
            }
        }
    }

    private static Map<SolarMaxCommandKey, @Nullable String> getValuesFromSolarMax(final Socket socket,
            final OutputStream outputStream, final InputStream inputStream, final int deviceAddress,
            final List<SolarMaxCommandKey> commandList, final int timeout) throws SolarMaxException {
        Map<SolarMaxCommandKey, @Nullable String> returnedValues;
        String commandString = getCommandString(commandList);
        String request = contructRequest(deviceAddress, commandString);
        try {
            LOGGER.trace("    ==>: {}", request);

            final long responseDeadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
            outputStream.write(request.getBytes(StandardCharsets.US_ASCII));

            String response = "";
            byte[] responseByte = new byte[1];

            // get everything from the stream
            while (true) {
                // the complete response has to arrive within the timeout, so limit each read to the time left
                long remainingMillis = TimeUnit.NANOSECONDS.toMillis(responseDeadline - System.nanoTime());
                if (remainingMillis <= 0) {
                    throw new SocketTimeoutException("Response not complete after " + timeout + "ms");
                }
                socket.setSoTimeout((int) remainingMillis);

                // read one byte from the stream
                int bytesRead = inputStream.read(responseByte);

                // if there was nothing left, break
                if (bytesRead < 1) {
                    break;
                }

                // add the received byte to the response
                final String responseString = new String(responseByte, StandardCharsets.US_ASCII);
                response = response + responseString;

                // if it was the final expected character "}", break
                if ("}".equals(responseString)) {
                    break;
                }
            }

            LOGGER.trace("    <==: {}", response);

            if (response.isEmpty()) {
                throw new SolarMaxException("No response received, the device closed the connection");
            }
            if (!validateResponse(response)) {
                throw new SolarMaxException(
                        "Invalid response received (" + response.length() + " characters): " + response);
            }

            returnedValues = extractValuesFromResponse(response);

            return returnedValues;
        } catch (IOException e) {
            LOGGER.debug("Error communicating via input/output streams: {} ", describe(e));
            throw new SolarMaxException("Error communicating with the device: " + describe(e), e);
        }
    }

    /**
     * @param response e.g.
     *            "{01;FB;6D|64:KDY=82;KMT=8F;KYR=23F7;KT0=72F1;TNF=1386;TKK=28;PAC=1F70;PRL=28;IL1=236;UL1=8F9;SYS=4E28,0|19E5}"
     * @return a map of keys and values
     */
    static Map<SolarMaxCommandKey, @Nullable String> extractValuesFromResponse(String response) {
        final Map<SolarMaxCommandKey, @Nullable String> responseMap = new HashMap<>();

        // in case there is no response
        if (response.indexOf("|") == response.lastIndexOf("|")) {
            LOGGER.warn("Response doesn't contain data. Response: {}", response);
            return responseMap;
        }

        // extract the body first
        // start by getting the part of the response between the two pipes
        String body = response.substring(response.indexOf("|") + 1, response.lastIndexOf("|"));

        // the name/value pairs now lie after the ":"
        body = body.substring(body.indexOf(":") + 1);

        // split into an array of name=value pairs
        String[] entries = body.split(";");
        for (String entry : entries) {

            if (entry.length() != 0) {
                // commands are 3 or 4 characters long (e.g. "PAC", "UD01"). A command without "=" means the device
                // knows the command but it is not applicable at the moment, so there is no value.
                int separatorIndex = entry.indexOf("=");
                String str = (separatorIndex == -1) ? entry : entry.substring(0, separatorIndex);

                String responseValue = (separatorIndex != -1 && separatorIndex + 1 < entry.length())
                        ? entry.substring(separatorIndex + 1)
                        : null;

                SolarMaxCommandKey key = SolarMaxCommandKey.getKeyFromString(str);
                if (key != SolarMaxCommandKey.UNKNOWN) {
                    responseMap.put(key, responseValue);
                }
            }
        }

        return responseMap;
    }

    /**
     * @param timeout timeout in milliseconds for connecting (and initial socket timeout, the complete response is
     *            limited to the same time while reading it)
     */
    private static Socket getSocketConnection(final String host, int portNumber, int timeout)
            throws SolarMaxConnectionException, UnknownHostException {
        portNumber = (portNumber == 0) ? DEFAULT_PORT : portNumber;

        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(host, portNumber), timeout);
            socket.setSoTimeout(timeout);
        } catch (final UnknownHostException e) {
            closeQuietly(socket);
            throw e;
        } catch (final IOException e) {
            closeQuietly(socket);
            throw new SolarMaxConnectionException(
                    "Error connecting to portNumber '" + portNumber + "' on host '" + host + "': " + describe(e), e);
        }

        return socket;
    }

    private static void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            // ignore any error while trying to close the socket
        }
    }

    public static boolean connectionTest(final String host, final int portNumber) throws UnknownHostException {
        Socket socket = null;

        try {
            socket = getSocketConnection(host, portNumber, SolarMaxConnectionSettings.DEFAULT_TIMEOUT);
        } catch (SolarMaxConnectionException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore any error while trying to close the socket
                }
            }
        }

        return true;
    }

    /**
     * @param destinationDevice device number - used if devices are daisy-chained. Normally it will be "1"
     * @param questions appears to be able to handle multiple commands. For now, one at a time is good fishing
     * @return the request to be sent to the SolarMax device
     */
    static String contructRequest(final int deviceAddress, final String questions) {
        String src = "FB";
        String dstHex = String.format("%02X", deviceAddress); // destinationDevice defaults to 1
        String len = "00";
        String cs = "0000";
        String msg = "64:" + questions;
        int lenInt = ("{" + src + ";" + dstHex + ";" + len + "|" + msg + "|" + cs + "}").length();

        // given the following, I'd expect problems if the request is longer than 255 characters. Since I'm not sure
        // though, I won't fixe what isn't (yet) broken
        String lenHex = String.format("%02X", lenInt);

        String checksum = calculateChecksum16(src + ";" + dstHex + ";" + lenHex + "|" + msg + "|");

        return "{" + src + ";" + dstHex + ";" + lenHex + "|" + msg + "|" + checksum + "}";
    }

    /**
     * calculates the "checksum16" of the given string argument
     */
    static String calculateChecksum16(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        int sum = 0;

        // loop through each of the bytes and add them together
        for (byte aByte : bytes) {
            sum = sum + aByte;
        }

        // calculate the "checksum16"
        sum = sum % (int) Math.pow(2, 16);

        // return Integer.toHexString(sum);
        return String.format("%04X", sum);
    }

    /**
     * checks the frame of a response, e.g. "{01;FB;34|64:PAC=28C;SYS=4E28,0;UL1=90F;TKK=1F|0BA8}": the length field
     * has to match the length of the whole response (including the braces) and the checksum has to match the sum of
     * all characters from the source address up to and including the "|" in front of the checksum
     */
    static boolean validateResponse(final String response) {
        if (!response.startsWith("{") || !response.endsWith("}")) {
            return false;
        }
        int firstPipe = response.indexOf("|");
        int lastPipe = response.lastIndexOf("|");
        // the checksum is always 4 characters long and followed by "}"
        if (firstPipe == -1 || firstPipe == lastPipe || lastPipe != response.length() - 6) {
            return false;
        }
        String[] header = response.substring(1, firstPipe).split(";");
        if (header.length != 3) {
            return false;
        }
        try {
            if (Integer.parseInt(header[2], 16) != response.length()) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        String checksum = response.substring(lastPipe + 1, response.length() - 1);
        return checksum.equals(calculateChecksum16(response.substring(1, lastPipe + 1)));
    }
}
