/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.samsungtv.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerLegacy} is responsible for sending key codes to the
 * Samsung TV.
 *
 * @see <a
 *      href="http://sc0ty.pl/2012/02/samsung-tv-network-remote-control-protocol/">http://sc0ty.pl/2012/02/samsung-tv-
 *      network-remote-control-protocol/</a>
 *
 *
 * @author Pauli Anttila - Initial contribution
 * @author Arjan Mels - Renamed and reworked to use RemoteController base class, to allow different protocols
 * @author Nick Waterton - moved Sendkeys to RemoteController, reworked sendkey, sendKeyData
 */
@NonNullByDefault
public class RemoteControllerLegacy extends RemoteController {

    private static final int CONNECTION_TIMEOUT = 500;

    private final Logger logger = LoggerFactory.getLogger(RemoteControllerLegacy.class);

    // Access granted response
    private static final char[] ACCESS_GRANTED_RESP = new char[] { 0x64, 0x00, 0x01, 0x00 };

    // User rejected your network remote controller response
    private static final char[] ACCESS_DENIED_RESP = new char[] { 0x64, 0x00, 0x00, 0x00 };

    // waiting for user to grant or deny access response
    private static final char[] WAITING_USER_GRANT_RESP = new char[] { 0x0A, 0x00, 0x02, 0x00, 0x00, 0x00 };

    // timeout or cancelled by user response
    private static final char[] ACCESS_TIMEOUT_RESP = new char[] { 0x65, 0x00 };

    private static final String APP_STRING = "iphone.iapp.samsung";

    private @Nullable Socket socket;
    private @Nullable InputStreamReader reader;
    private @Nullable BufferedWriter writer;

    /**
     * Create and initialize remote controller instance.
     *
     * @param host Host name of the Samsung TV.
     * @param port TCP port of the remote controller protocol.
     * @param appName Application name used to send key codes.
     * @param uniqueId Unique Id used to send key codes.
     */
    public RemoteControllerLegacy(String host, int port, @Nullable String appName, @Nullable String uniqueId) {
        super(host, port, appName, uniqueId);
    }

    /**
     * Open Connection to Samsung TV.
     *
     * @throws RemoteControllerException
     */
    public void openConnection() throws RemoteControllerException {
        if (isConnected()) {
            return;
        }
        logger.debug("{}: Open connection to host '{}:{}'", host, host, port);

        Socket localsocket = new Socket();
        socket = localsocket;
        try {
            if (socket != null) {
                socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
            } else {
                throw new IOException("no Socket");
            }
        } catch (IOException e) {
            logger.debug("{}: Cannot connect to Legacy Remote Controller: {}", host, e.getMessage());
            throw new RemoteControllerException("Connection failed", e);
        }

        InputStream inputStream;
        try {
            BufferedWriter localwriter = new BufferedWriter(new OutputStreamWriter(localsocket.getOutputStream()));
            writer = localwriter;
            inputStream = localsocket.getInputStream();
            InputStreamReader localreader = new InputStreamReader(inputStream);
            reader = localreader;

            logger.debug("{}: Connection successfully opened...querying access", host);
            writeInitialInfo(localwriter, localsocket);
            readInitialInfo(localreader);

            int i;
            while ((i = inputStream.available()) > 0) {
                inputStream.skip(i);
            }
        } catch (IOException e) {
            throw new RemoteControllerException(e);
        }
    }

    private void writeInitialInfo(Writer writer, Socket socket) throws RemoteControllerException {
        try {
            /* @formatter:off
            *
            * offset value and description
            * ------ ---------------------
            * 0x00   0x00 - datagram type?
            * 0x01   0x0013 - string length (little endian)
            * 0x03   "iphone.iapp.samsung" - string content
            * 0x16   0x0038 - payload size (little endian)
            * 0x18   payload
            *
            * Payload starts with 2 bytes: 0x64 and 0x00, then comes 3 strings
            * encoded with base64 algorithm. Every string is preceded by
            * 2-bytes field containing encoded string length.
            *
            * These three strings are as follow:
            *
            * remote control device IP, unique ID – value to distinguish
            * controllers, name – it will be displayed as controller name.
            *
            * @formatter:on
            */

            writer.append((char) 0x00);
            writeString(writer, APP_STRING);
            writeString(writer, createRegistrationPayload(socket.getLocalAddress().getHostAddress()));
            writer.flush();
        } catch (IOException e) {
            throw new RemoteControllerException(e);
        }
    }

    private void readInitialInfo(Reader reader) throws RemoteControllerException {
        try {
            /* @formatter:off
            *
            * offset value and description
            * ------ ---------------------
            * 0x00   don't know, it it always 0x00 or 0x02
            * 0x01   0x000c - string length (little endian)
            * 0x03   "iapp.samsung" - string content
            * 0x0f   0x0006 - payload size (little endian)
            * 0x11   payload
            *
            * @formatter:on
            */

            reader.skip(1);
            readString(reader);
            char[] result = readCharArray(reader);

            if (Arrays.equals(result, ACCESS_GRANTED_RESP)) {
                logger.debug("{}: Access granted", host);
            } else if (Arrays.equals(result, ACCESS_DENIED_RESP)) {
                throw new RemoteControllerException("Access denied");
            } else if (Arrays.equals(result, ACCESS_TIMEOUT_RESP)) {
                throw new RemoteControllerException("Registration timed out");
            } else if (Arrays.equals(result, WAITING_USER_GRANT_RESP)) {
                throw new RemoteControllerException("Waiting for user to grant access");
            } else {
                throw new RemoteControllerException("Unknown response received for access query");
            }
        } catch (IOException e) {
            throw new RemoteControllerException(e);
        }
    }

    /**
     * Close connection to Samsung TV.
     *
     */
    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore error
        }
    }

    public void sendUrl(String command) {
        logger.warn("{}: Remote control legacy: unsupported command: {}", host, command);
    }

    public void sendSourceApp(String command) {
        logger.warn("{}: Remote control legacy: unsupported command: {}", host, command);
    }

    public void updateCurrentApp() {
    }

    public void getArtmodeStatus(String... optionalRequests) {
    }

    public boolean closeApp() {
        return false;
    }

    public void getAppStatus(String id) {
    }

    public boolean noApps() {
        return false;
    }

    private void logResult(String msg, Throwable cause) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}: {}: ", host, msg, cause);
        } else {
            logger.debug("{}: {}: {}", host, msg, cause.getMessage());
        }
    }

    /**
     * Send key code to Samsung TV.
     *
     * @param key Key code to send.
     */
    public void sendKey(Object key) {
        if (!(key instanceof KeyCode)) {
            logger.warn("{}: Remote control legacy: unsupported command: {}", host, key);
            return;
        }
        logger.trace("{}: Try to send command: {}", host, key);
        for (int i = 0; i < 2; i++) {
            try {
                openConnection();
                if (sendKeyData((KeyCode) key)) {
                    logger.trace("{}: Command successfully sent", host);
                    return;
                }
            } catch (RemoteControllerException e) {
                logResult("Couldn't send command", e);
            }
            closeConnection();
            logger.debug("{}: Retry send command {} attempt {}...", host, key, i);
        }
        logger.warn("{}: Command Retrys failed", host);
    }

    public void sendKeyPress(KeyCode key, int duration) {
        sendKey(key);
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    private String createRegistrationPayload(String ip) throws IOException {
        /*
         * Payload starts with 2 bytes: 0x64 and 0x00, then comes 3 strings
         * encoded with base64 algorithm. Every string is preceded by 2-bytes
         * field containing encoded string length.
         *
         * These three strings are as follow:
         *
         * remote control device IP, unique ID – value to distinguish
         * controllers, name – it will be displayed as controller name.
         */

        StringWriter w = new StringWriter();
        w.append((char) 0x64);
        w.append((char) 0x00);
        writeBase64String(w, ip);
        writeBase64String(w, uniqueId);
        writeBase64String(w, appName);
        w.flush();
        return w.toString();
    }

    private void writeString(Writer writer, String str) throws IOException {
        int len = str.length();
        byte low = (byte) (len & 0xFF);
        byte high = (byte) ((len >> 8) & 0xFF);

        writer.append((char) (low));
        writer.append((char) (high));
        writer.append(str);
    }

    private void writeBase64String(Writer writer, String str) throws IOException {
        writeString(writer, Utils.b64encode(str));
    }

    private String readString(Reader reader) throws IOException {
        return new String(readCharArray(reader));
    }

    private char[] readCharArray(Reader reader) throws IOException {
        byte low = (byte) reader.read();
        byte high = (byte) reader.read();
        int len = (high << 8) + low;

        if (len > 0) {
            char[] buffer = new char[len];
            reader.read(buffer);
            return buffer;
        } else {
            return new char[] {};
        }
    }

    private boolean sendKeyData(KeyCode key) {
        logger.debug("{}: Sending key code {}", host, key.getValue());

        Writer localwriter = writer;
        Reader localreader = reader;
        if (localwriter == null || localreader == null) {
            return false;
        }
        /* @formatter:off
         *
         * offset value and description
         * ------ ---------------------
         * 0x00   always 0x00
         * 0x01   0x0013 - string length (little endian)
         * 0x03   "iphone.iapp.samsung" - string content
         * 0x16   0x0011 - payload size (little endian)
         * 0x18   payload
         *
         * @formatter:on
         */
        try {
            localwriter.append((char) 0x00);
            writeString(localwriter, APP_STRING);
            writeString(localwriter, createKeyDataPayload(key));
            localwriter.flush();

            /*
             * Read response. Response is pretty useless, because TV seems to
             * send same response in both ok and error situation.
             */
            localreader.skip(1);
            readString(localreader);
            readCharArray(localreader);
        } catch (IOException e) {
            logResult("Couldn't send command", e);
            return false;
        }
        return true;
    }

    private String createKeyDataPayload(KeyCode key) throws IOException {
        /* @formatter:off
        *
        * Payload:
        *
        * offset value and description
        * ------ ---------------------
        * 0x18   three 0x00 bytes
        * 0x1b   0x000c - key code size (little endian)
        * 0x1d   key code encoded as base64 string
        *
        * @formatter:on
        */

        StringWriter writer = new StringWriter();
        writer.append((char) 0x00);
        writer.append((char) 0x00);
        writer.append((char) 0x00);
        writeBase64String(writer, key.getValue());
        writer.flush();
        return writer.toString();
    }

    @Override
    public void close() throws RemoteControllerException {
        if (isConnected()) {
            closeConnection();
        }
    }
}
