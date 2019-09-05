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
package org.openhab.binding.heos.internal.resources;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Telnet} is an Telnet Client which handles the connection
 * to a network via the Telnet interface
 *
 * @author Johannes Einig - Initial contribution
 */
public class Telnet {
    private final Logger logger = LoggerFactory.getLogger(Telnet.class);

    private static final int READ_TIMEOUT = 3000;
    private static final int IS_ALIVE_TIMEOUT = 10000;

    private String ip;
    private int port;

    private String readResult;
    private String readLineResult;
    private List<String> readResultList = new ArrayList<>(5);

    private InetAddress address;
    private TelnetClient client;
    private DataOutputStream outStream;
    private InputStream inputStream;
    private BufferedInputStream bufferedStream;

    private HeosStringPropertyChangeListener eolNotifyer = new HeosStringPropertyChangeListener();

    private TelnetInputListener inputListener;

    public Telnet() {
        client = new TelnetClient();
        readResult = ""; // Has to bin initialized because value is used later with readResult.concat() function
    }

    /**
     * Connects to a host with the specified IP address and port
     *
     * @param ip   IP Address of the host
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
        return openConnection();
    }

    private boolean openConnection() throws SocketException, IOException {
        client.setConnectTimeout(5000);
        client.connect(ip, port);
        outStream = new DataOutputStream(client.getOutputStream());
        inputStream = client.getInputStream();
        bufferedStream = new BufferedInputStream(inputStream);
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
     * @return true after the command was send
     * @throws IOException
     */
    public boolean sendClear(String command) throws IOException {
        if (client.isConnected()) {
            outStream.writeBytes(command);
            outStream.flush();
            return true;
        } else {
            return false;
        }
    }

    /**
     * The read function reads the input of the Telnet connection
     * it determine the amount of bytes to read.
     * If no bytes available i is 0, if End of Line is detected i=-1
     * Bytes are read into buffer and changed to String
     * Then the single values are merged by function concatReadResult
     *
     * @throws IOException
     */
    public boolean read() throws IOException {
        if (client.isConnected()) {
            int i = 1;
            while (i != -1) {
                i = bufferedStream.available();
                byte[] buffer = new byte[i];
                bufferedStream.read(buffer);
                String str = new String(buffer, "UTF-8");
                i = concatReadResult(str);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Read all commands till an End Of Line is detected
     * I more than one line is read every line is an
     * element in the returned {@code ArrayList<>}
     * Reading timed out after 3000 milliseconds. For another timing
     * @see readLine(int timeOut).
     *
     * @return A list with all read commands
     * @throws ReadException
     * @throws IOException
     */
    public List<String> readLine() throws ReadException, IOException {
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
    public List<String> readLine(int timeOut) throws ReadException, IOException {
        readResultList.clear();
        long timeZero = System.currentTimeMillis();
        long timeAfterTry = 0;
        long timeTryiedToRead = 0;
        if (client.isConnected()) {
            readLineResult = "";
            int i = 1;
            while (i != -1) {
                i = bufferedStream.available();
                byte[] buffer = new byte[i];
                bufferedStream.read(buffer);
                String str = new String(buffer, "UTF-8");
                i = concatReadLineResult(str);
                timeAfterTry = System.currentTimeMillis();
                timeTryiedToRead = timeAfterTry - timeZero;
                if (timeTryiedToRead >= timeOut) {
                    throw new ReadException();
                }
            }
        } else {
            readResultList.add(null);
        }
        return readResultList;
    }

    /*
     * It seems to be that sometime a command is still
     * in the reading line without being read out. This
     * shall be prevented with an Map which reads until no
     * End of line is detected. Each element of the list
     * should be a JSON Element
     */
    private int concatReadLineResult(String value) {
        readLineResult = readLineResult.concat(value);
        if (readLineResult.endsWith("\r\n")) {
            readLineResult = readLineResult.trim();
            while (readLineResult.contains("\r\n")) {
                int indexFirstElement = readLineResult.indexOf("\r\n");
                readResultList.add(readLineResult.substring(0, indexFirstElement));
                readLineResult = readLineResult.substring(indexFirstElement);
                readLineResult = readLineResult.trim();
            }
            readResultList.add(readLineResult);
            return -1;
        }
        return 0;
    }

    /**
     * Disconnect Telnet and close all Streams
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        inputStream.close();
        outStream.close();
        client.disconnect();
    }

    /**
     * Input Listener which fires event if input is detected
     */
    public void startInputListener() {
        inputListener = new TelnetInputListener() {
            @Override
            public void telnetInputAvailable() {
                inputAvailableRead();
            }
        };
        client.registerInputListener(inputListener);
    }

    public void stopInputListener() {
        client.unregisterInputListener();
    }

    /**
     * Reader for InputListenerOnly which only reads the
     * available data without any check
     *
     */
    private void inputAvailableRead() {
        try {
            int i = bufferedStream.available();
            byte[] buffer = new byte[i];
            bufferedStream.read(buffer);
            String str = new String(buffer, "UTF-8");
            i = concatReadResult(str);
        } catch (IOException e) {
            logger.debug("IO Exception- Message: {}", e.getMessage());
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
            eolNotifyer.setValue(readResult.trim());
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
    public boolean isConnectionAlive() {
        try {
            return address.isReachable(IS_ALIVE_TIMEOUT);
        } catch (IOException e) {
            logger.debug("IO Exception- Message: {}", e.getMessage());
            return false;
        }
    }

    public HeosStringPropertyChangeListener getReadResultListener() {
        return eolNotifyer;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public class ReadException extends Exception {
        private static final long serialVersionUID = 1L;

        public ReadException() {
            super("Can not read from client");
        }
    }
}
