/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * default timeout for socket connections is 1 second
     */
    private static final int CONNECTION_TIMEOUT = 1000;

    /**
     * default timeout for socket responses is 10 seconds
     */
    private static int responseTimeout = 10000;

    /**
     * gets all known values from the SolarMax device addressable at host:portNumber
     * 
     * @param host hostname or ip address of the SolarMax device to be contacted
     * @param portNumber portNumber the SolarMax is listening on (default is 12345)
     * @param deviceAddress
     * @return
     * @throws SolarMaxException if some other exception occurs
     */
    public static SolarMaxData getAllValuesFromSolarMax(final String host, final int portNumber,
            final int deviceAddress) throws SolarMaxException {
        List<SolarMaxCommandKey> commandList = new ArrayList<>();

        for (SolarMaxCommandKey solarMaxCommandKey : SolarMaxCommandKey.values()) {
            if (solarMaxCommandKey != SolarMaxCommandKey.UNKNOWN) {
                commandList.add(solarMaxCommandKey);
            }
        }

        SolarMaxData solarMaxData = new SolarMaxData();

        // get the data from the SolarMax device. If we didn't get as many values back as we asked for, there were
        // communications problems, so set communicationSuccessful appropriately

        Map<SolarMaxCommandKey, @Nullable String> valuesFromSolarMax = getValuesFromSolarMax(host, portNumber,
                deviceAddress, commandList);
        boolean allCommandsAnswered = true;
        for (SolarMaxCommandKey solarMaxCommandKey : commandList) {
            if (!valuesFromSolarMax.containsKey(solarMaxCommandKey)) {
                allCommandsAnswered = false;
                break;
            }
        }
        solarMaxData.setDataDateTime(ZonedDateTime.now());
        solarMaxData.setCommunicationSuccessful(allCommandsAnswered);
        solarMaxData.setData(valuesFromSolarMax);

        return solarMaxData;
    }

    /**
     * gets values from the SolarMax device addressable at host:portNumber
     * 
     * @param host hostname or ip address of the SolarMax device to be contacted
     * @param portNumber portNumber the SolarMax is listening on (default is 12345)
     * @param commandList a list of commands to be sent to the SolarMax device
     * @return
     * @throws UnknownHostException if the host is unknown
     * @throws SolarMaxException if some other exception occurs
     */
    private static Map<SolarMaxCommandKey, @Nullable String> getValuesFromSolarMax(final String host,
            final int portNumber, final int deviceAddress, final List<SolarMaxCommandKey> commandList)
            throws SolarMaxException {
        Socket socket;

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
                    deviceAddress, responseTimeout);

            int firstCommandNumber = requestNumber * maxConcurrentCommands;
            int lastCommandNumber = (requestNumber + 1) * maxConcurrentCommands;
            if (lastCommandNumber > commandList.size()) {
                lastCommandNumber = commandList.size();
            }
            List<SolarMaxCommandKey> commandsToSend = commandList.subList(firstCommandNumber, lastCommandNumber);

            try {
                socket = getSocketConnection(host, portNumber);
            } catch (UnknownHostException e) {
                throw new SolarMaxConnectionException(e);
            }
            returnMap.putAll(getValuesFromSolarMax(socket, deviceAddress, commandsToSend));

            // SolarMax can't deal with requests too close to one another, so just wait a moment
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        return returnMap;
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

    private static Map<SolarMaxCommandKey, @Nullable String> getValuesFromSolarMax(final Socket socket,
            final int deviceAddress, final List<SolarMaxCommandKey> commandList) throws SolarMaxException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            return getValuesFromSolarMax(outputStream, inputStream, deviceAddress, commandList);
        } catch (final SolarMaxException | IOException e) {
            throw new SolarMaxException("Error getting input/output streams from socket", e);
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

    private static Map<SolarMaxCommandKey, @Nullable String> getValuesFromSolarMax(final OutputStream outputStream,
            final InputStream inputStream, final int deviceAddress, final List<SolarMaxCommandKey> commandList)
            throws SolarMaxException {
        Map<SolarMaxCommandKey, @Nullable String> returnedValues;
        String commandString = getCommandString(commandList);
        String request = contructRequest(deviceAddress, commandString);
        try {
            LOGGER.trace("    ==>: {}", request);

            outputStream.write(request.getBytes());

            String response = "";
            byte[] responseByte = new byte[1];

            // get everything from the stream
            while (true) {
                // read one byte from the stream
                int bytesRead = inputStream.read(responseByte);

                // if there was nothing left, break
                if (bytesRead < 1) {
                    break;
                }

                // add the received byte to the response
                final String responseString = new String(responseByte);
                response = response + responseString;

                // if it was the final expected character "}", break
                if ("}".equals(responseString)) {
                    break;
                }
            }

            LOGGER.trace("    <==: {}", response);

            if (!validateResponse(response)) {
                throw new SolarMaxException("Invalid response received: " + response);
            }

            returnedValues = extractValuesFromResponse(response);

            return returnedValues;
        } catch (IOException e) {
            LOGGER.debug("Error communicating via input/output streams: {} ", e.getMessage());
            throw new SolarMaxException(e);
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
        if (response.indexOf("|") == -1) {
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
                // could be split on "=" instead of fixed length or made to respect length of command, but they're all 3
                // characters long (then plus "=")
                String str = entry.substring(0, 3);

                String responseValue = (entry.length() >= 5) ? entry.substring(4) : null;

                SolarMaxCommandKey key = SolarMaxCommandKey.getKeyFromString(str);
                if (key != SolarMaxCommandKey.UNKNOWN) {
                    responseMap.put(key, responseValue);
                }
            }
        }

        return responseMap;
    }

    private static Socket getSocketConnection(final String host, int portNumber)
            throws SolarMaxConnectionException, UnknownHostException {
        portNumber = (portNumber == 0) ? DEFAULT_PORT : portNumber;

        Socket socket;

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, portNumber), CONNECTION_TIMEOUT);
            socket.setSoTimeout(responseTimeout);
        } catch (final UnknownHostException e) {
            throw e;
        } catch (final IOException e) {
            throw new SolarMaxConnectionException(
                    "Error connecting to portNumber '" + portNumber + "' on host '" + host + "'", e);
        }

        return socket;
    }

    public static boolean connectionTest(final String host, final int portNumber) throws UnknownHostException {
        Socket socket = null;

        try {
            socket = getSocketConnection(host, portNumber);
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
     * @return timeout for responses in milliseconds
     */
    public static int getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * @param responseTimeout timeout for responses in milliseconds
     */
    public static void setResponseTimeout(int responseTimeout) {
        SolarMaxConnector.responseTimeout = responseTimeout;
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
        byte[] bytes = str.getBytes();
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

    static boolean validateResponse(final String header) {
        // probably should implement a patter matcher with a patternString like "/\\{([0-9A-F]{2});FB;([0-9A-F]{2})/",
        // but for now...
        return true;
    }
}
