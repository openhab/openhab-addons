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
package org.openhab.binding.heos.internal.resources;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Telnet} is a Telnet Client which handles the connection
 * to a network via the Telnet interface
 *
 * @author Johannes Einig - Initial contribution
 */
public class Telnet {
    private final Logger logger = LoggerFactory.getLogger(Telnet.class);

    private static final int READ_TIMEOUT = 3000;
    private static final int IS_ALIVE_TIMEOUT = 10000;

    private final HeosStringPropertyChangeListener eolNotifier = new HeosStringPropertyChangeListener();
    private final TelnetClient client = new TelnetClient();
    private ExecutorService timedReaderExecutor;

    private String ip;
    private int port;

    private String readResult = "";

    private InetAddress address;
    private DataOutputStream outStream;
    private BufferedInputStream bufferedStream;

    /**
     * Connects to a host with the specified IP address and port
     *
     * @param ip IP Address of the host
     * @param port where to be connected
     * @return True if connection was successful
     * @throws SocketException
     * @throws IOException
     */
    public boolean connect(String ip, int port) throws SocketException, IOException {
        this.ip = ip;
        this.port = port;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            logger.debug("Unknown Host Exception - Message: {}", e.getMessage());
        }
        timedReaderExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("heos-telnet-reader", true));

        return openConnection();
    }

    private boolean openConnection() throws IOException {
        client.setConnectTimeout(5000);
        client.connect(ip, port);
        outStream = new DataOutputStream(client.getOutputStream());
        bufferedStream = new BufferedInputStream(client.getInputStream());
        return client.isConnected();
    }

    /**
     * Appends \r\n to the command.
     * For clear send use sendClear
     *
     * @param command The command to be send
     * @return true after the command was send
     * @throws IOException
     */
    public boolean send(String command) throws IOException {
        if (client.isConnected()) {
            sendClear(command + "\r\n");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Send command without additional commands
     *
     * @param command The command to be send
     * @throws IOException
     */
    private void sendClear(String command) throws IOException {
        if (!client.isConnected()) {
            return;
        }

        outStream.writeBytes(command);
        outStream.flush();
    }

    /**
     * Read all commands till an End Of Line is detected
     * I more than one line is read every line is an
     * element in the returned {@code ArrayList<>}
     * Reading timed out after 3000 milliseconds. For another timing
     *
     * @return A list with all read commands
     * @throws ReadException
     * @throws IOException
     * @see #readLine(int timeOut)
     */
    public String readLine() throws ReadException, IOException {
        return readLine(READ_TIMEOUT);
    }

    /**
     * Read all commands till an End Of Line is detected
     * I more than one line is read every line is an
     * element in the returned {@code ArrayList<>}
     * Reading time out is defined by parameter in
     * milliseconds.
     *
     * @param timeOut the time in millis after reading times out
     * @return A list with all read commands
     * @throws ReadException
     * @throws IOException
     */
    public @Nullable String readLine(int timeOut) throws ReadException, IOException {
        if (client.isConnected()) {
            try {
                return timedCallable(() -> {
                    BufferedReader reader = new CRLFLineReader(
                            new InputStreamReader(bufferedStream, StandardCharsets.UTF_8));
                    String lastLine;
                    do {
                        lastLine = reader.readLine();
                    } while (reader.ready());
                    return lastLine;
                }, timeOut);
            } catch (InterruptedException | TimeoutException e) {
                throw new ReadException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException exception) {
                    throw exception;
                } else {
                    throw new ReadException(cause);
                }
            }
        }
        return null;
    }

    private String timedCallable(Callable<String> callable, int timeOut)
            throws InterruptedException, ExecutionException, TimeoutException {
        Future<String> future = timedReaderExecutor.submit(callable);
        try {
            return future.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            throw e;
        }
    }

    /**
     * Disconnect Telnet and close all Streams
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        client.disconnect();
        timedReaderExecutor.shutdown();
    }

    /**
     * Input Listener which fires event if input is detected
     */
    public void startInputListener() {
        logger.debug("Starting input listener");
        client.setReaderThread(true);
        client.registerInputListener(this::inputAvailableRead);
    }

    public void stopInputListener() {
        logger.debug("Stopping input listener");
        client.unregisterInputListener();
    }

    /**
     * Reader for InputListenerOnly which only reads the
     * available data without any check
     */
    private void inputAvailableRead() {
        try {
            int i = bufferedStream.available();
            byte[] buffer = new byte[i];
            bufferedStream.read(buffer);
            String str = new String(buffer, StandardCharsets.UTF_8);
            concatReadResult(str);
        } catch (IOException e) {
            logger.debug("IO Exception, message: {}", e.getMessage());
        }
    }

    /**
     * Read values until end of line is reached.
     * Then fires event for change Listener.
     *
     * @return -1 to indicate that end of line is reached
     *         else returns 0
     */
    private int concatReadResult(String value) {
        readResult = readResult.concat(value);
        if (readResult.contains("\r\n")) {
            eolNotifier.setValue(readResult.trim());
            readResult = "";
            return -1;
        }
        return 0;
    }

    /**
     * Checks if the HEOS system is reachable
     * via the network. This does not check if
     * a Telnet connection is open.
     *
     * @return true if HEOS is reachable
     */
    public boolean isHostReachable() {
        try {
            return address != null && address.isReachable(IS_ALIVE_TIMEOUT);
        } catch (IOException e) {
            logger.debug("IO Exception- Message: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "Telnet{" + "ip='" + ip + '\'' + ", port=" + port + '}';
    }

    public HeosStringPropertyChangeListener getReadResultListener() {
        return eolNotifier;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public static class ReadException extends Exception {
        private static final long serialVersionUID = 1L;

        public ReadException() {
            super("Can not read from client");
        }

        public ReadException(Throwable cause) {
            super("Can not read from client", cause);
        }
    }
}
