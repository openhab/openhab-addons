package org.openhab.binding.solarmax.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolarmaxConnectorFindCommands {

    private static final Logger logger = LoggerFactory.getLogger(SolarMaxConnector.class);

    static final String host = "192.168.1.151";
    static final int port = 12345;
    static final int deviceId = 1;
    static final int connectionTimeout = 1000; // ms

    @Test
    public void testForCommands() throws UnknownHostException, SolarMaxException {

        List<String> validCommands = new ArrayList<>();
        List<String> commandsToCheck = new ArrayList<String>();
        List<String> failedCommands = new ArrayList<>();
        int failedCommandRetry = 0;
        String lastFailedCommand = "";

        for (String first : getCharacters()) {
            for (String second : getCharacters()) {
                for (String third : getCharacters()) {
                    commandsToCheck.add(first + second + third);

                    // specifically searching for "E" errors with 4 characters (I know now that they don't exist ;-)
                    // commandsToCheck.add("E" + first + second + third);
                }
            }
        }

        // if you only want to try specific commands, perhaps because they failed in the big run, comment out the above
        // and use this instead
        // commandsToCheck.addAll(Arrays.asList("RH1", "RH2", "RH3", "TP1", "TP2", "TP3", "UL1", "UL2", "UL3", "UMX",
        // "UM1", "UM2", "UM3", "UPD", "TCP"));

        while (!commandsToCheck.isEmpty()) {
            if (commandsToCheck.size() % 100 == 0) {
                System.out.println(commandsToCheck.size() + " left to check");
            }
            try {
                if (checkIsValidCommand(commandsToCheck.get(0))) {
                    validCommands.add(commandsToCheck.get(0));
                    commandsToCheck.remove(0);
                } else {
                    commandsToCheck.remove(0);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println("Sleeping after Exception: " + e.getLocalizedMessage());
                if (lastFailedCommand.equals(commandsToCheck.get(0))) {
                    failedCommandRetry = failedCommandRetry + 1;
                    if (failedCommandRetry >= 5) {
                        failedCommands.add(commandsToCheck.get(0));
                        commandsToCheck.remove(0);
                    }
                } else {
                    failedCommandRetry = 0;
                    lastFailedCommand = commandsToCheck.get(0);
                }
                try {
                    // Backoff somewhat nicely
                    Thread.sleep(2 * failedCommandRetry * failedCommandRetry * failedCommandRetry);
                } catch (InterruptedException e1) {
                    // do nothing
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e1) {
                // do nothing
            }

        }

        System.out.println();
        System.out.println("Valid commands:");

        for (String validCommand : validCommands) {
            System.out.println(validCommand);
        }

        System.out.println();
        System.out.println("Failed commands:");

        for (String failedCommand : failedCommands) {
            System.out.print(failedCommand + "\", \"");
        }
    }

    private boolean checkIsValidCommand(String command)
            throws InterruptedException, UnknownHostException, SolarMaxException {

        List<String> commands = new ArrayList<String>();
        commands.add(command);

        Map<String, String> responseMap = null;

        responseMap = getValuesFromSolarMax(host, port, commands);

        if (responseMap.containsKey(command)) {
            System.out.println("Request: " + command + " Valid Response: " + responseMap.get(command));
            return true;
        }
        return false;
    }

    /**
     * based on SolarMaxConnector.getValuesFromSolarMax
     */
    private static Map<String, String> getValuesFromSolarMax(final String host, int port,
            final List<String> commandList) throws SolarMaxException {

        Socket socket;

        Map<String, String> returnMap = new HashMap<>();

        // SolarMax can't answer correclty if too many commands are send in a single request, so limit it to 16 at a
        // time
        int maxConcurrentCommands = 16;
        int requestsRequired = (commandList.size() / maxConcurrentCommands);
        if (commandList.size() % maxConcurrentCommands != 0) {
            requestsRequired = requestsRequired + 1;
        }
        for (int requestNumber = 0; requestNumber < requestsRequired; requestNumber++) {
            logger.debug("    Requesting data from {}:{} with timeout of {}ms", host, port, connectionTimeout);

            int firstCommandNumber = requestNumber * maxConcurrentCommands;
            int lastCommandNumber = (requestNumber + 1) * maxConcurrentCommands;
            if (lastCommandNumber > commandList.size()) {
                lastCommandNumber = commandList.size();
            }
            List<String> commandsToSend = commandList.subList(firstCommandNumber, lastCommandNumber);

            try {
                socket = getSocketConnection(host, port);
            } catch (UnknownHostException e) {
                throw new SolarMaxConnectionException(e);
            }
            returnMap.putAll(getValuesFromSolarMax(socket, commandsToSend));

            // SolarMax can't deal with requests too close to one another, so just wait a moment
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        return returnMap;
    }

    private static Map<String, String> getValuesFromSolarMax(final Socket socket, final List<String> commandList)
            throws SolarMaxException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            return getValuesFromSolarMax(outputStream, inputStream, commandList);

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

    private List<String> getCharacters() {
        List<String> characters = new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) {
            characters.add(Character.toString(c));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            characters.add(Character.toString(c));
        }
        characters.add("0");
        characters.add("1");
        characters.add("2");
        characters.add("3");
        characters.add("4");
        characters.add("5");
        characters.add("6");
        characters.add("7");
        characters.add("8");
        characters.add("9");

        characters.add(".");
        characters.add("-");
        characters.add("_");

        return characters;
    }

    private static Socket getSocketConnection(final String host, int port)
            throws SolarMaxConnectionException, UnknownHostException {

        port = (port == 0) ? SolarmaxConnectorFindCommands.port : port;

        Socket socket;

        try {
            socket = new Socket();
            logger.debug("    Connecting to " + host + ":" + port + " with a timeout of " + connectionTimeout);
            socket.connect(new InetSocketAddress(host, port), connectionTimeout);
            logger.debug("    Connected.");
            socket.setSoTimeout(connectionTimeout);
        } catch (final UnknownHostException e) {
            throw e;
        } catch (final IOException e) {
            throw new SolarMaxConnectionException("Error connecting to port '" + port + "' on host '" + host + "'", e);
        }

        return socket;
    }

    private static Map<String, String> getValuesFromSolarMax(final OutputStream outputStream,
            final InputStream inputStream, final List<String> commandList) throws SolarMaxException {

        Map<String, String> returnedValues;
        String commandString = getCommandString(commandList);
        String request = SolarMaxConnector.contructRequest(commandString);
        try {

            logger.trace("    ==>: {}", request);

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

            logger.trace("    <==: {}", response);

            // if (!validateResponse(response)) {
            // throw new SolarMaxException("Invalid response received: " + response);
            // }

            returnedValues = extractValuesFromResponse(response);

            return returnedValues;

        } catch (IOException e) {
            logger.debug("Error communicating via input/output streams: {} ", e.getMessage());
            throw new SolarMaxException(e);
        }
    }

    static String getCommandString(List<String> commandList) {
        String commandString = "";
        for (String command : commandList) {
            if (commandString != "") {
                commandString = commandString + ";";
            }
            commandString = commandString + command;
        }
        return commandString;
    }

    /**
     * @param response e.g.
     *            "{01;FB;6D|64:KDY=82;KMT=8F;KYR=23F7;KT0=72F1;TNF=1386;TKK=28;PAC=1F70;PRL=28;IL1=236;UL1=8F9;SYS=4E28,0|19E5}"
     * @return a map of keys and values
     */
    static Map<String, String> extractValuesFromResponse(String response) {

        final Map<String, String> responseMap = new HashMap<>();

        // in case there is no response
        if (response.indexOf("|") == -1) {
            logger.warn("Response doesn't contain data. Response: {}", response);
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
                String responseKey = entry.substring(0, 3);

                String responseValue = (entry.length() >= 5) ? entry.substring(4) : null;

                responseMap.put(responseKey, responseValue);
            }
        }

        return responseMap;
    }
}
