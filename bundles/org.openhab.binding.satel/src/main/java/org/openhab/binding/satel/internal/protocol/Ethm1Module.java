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
package org.openhab.binding.satel.internal.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents Satel ETHM-1 module. Implements method required to connect and
 * communicate with that module over TCP/IP protocol. The module must have
 * integration protocol enable in DLOADX configuration options.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class Ethm1Module extends SatelModule {

    private static final ByteArrayInputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    private final Logger logger = LoggerFactory.getLogger(Ethm1Module.class);

    private final String host;
    private final int port;
    private final String encryptionKey;

    /**
     * Creates new instance with host, port, timeout and encryption key set to
     * specified values.
     *
     * @param host host name or IP of ETHM-1 module
     * @param port TCP port the module listens on
     * @param timeout timeout value in milliseconds for connect/read/write operations
     * @param encryptionKey encryption key for encrypted communication
     * @param extPayloadSupport if <code>true</code>, the module supports extended command payload for reading
     *            INTEGRA 256 state
     */
    public Ethm1Module(String host, int port, int timeout, String encryptionKey, boolean extPayloadSupport) {
        super(timeout, extPayloadSupport);

        this.host = host;
        this.port = port;
        this.encryptionKey = encryptionKey;
    }

    @Override
    protected CommunicationChannel connect() throws ConnectionFailureException {
        logger.info("Connecting to ETHM-1 module at {}:{}", host, port);

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), this.getTimeout());
            logger.info("ETHM-1 module connected successfully");

            if (encryptionKey.isBlank()) {
                return new TCPCommunicationChannel(socket);
            } else {
                return new EncryptedCommunicationChannel(socket, encryptionKey);
            }
        } catch (SocketTimeoutException e) {
            throw new ConnectionFailureException("Connection timeout", e);
        } catch (IOException e) {
            throw new ConnectionFailureException("IO error occurred while connecting socket", e);
        }
    }

    private class TCPCommunicationChannel implements CommunicationChannel {

        private Socket socket;

        public TCPCommunicationChannel(Socket socket) {
            this.socket = socket;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.socket.getOutputStream();
        }

        @Override
        public void disconnect() {
            logger.info("Closing connection to ETHM-1 module");
            try {
                this.socket.close();
            } catch (IOException e) {
                logger.error("IO error occurred during closing socket", e);
            }
        }
    }

    private class EncryptedCommunicationChannel extends TCPCommunicationChannel {

        private EncryptionHelper aesHelper;
        private Random rand;
        private byte idS;
        private byte idR;
        private int rollingCounter;
        private InputStream inputStream;
        private OutputStream outputStream;

        public EncryptedCommunicationChannel(final Socket socket, String encryptionKey) throws IOException {
            super(socket);

            try {
                this.aesHelper = new EncryptionHelper(encryptionKey);
            } catch (Exception e) {
                throw new IOException("General encryption failure", e);
            }
            this.rand = new Random();
            this.idS = 0;
            this.idR = 0;
            this.rollingCounter = 0;

            this.inputStream = new InputStream() {
                private ByteArrayInputStream inputBuffer = EMPTY_INPUT_STREAM;

                @Override
                public int read() throws IOException {
                    if (inputBuffer.available() == 0) {
                        // read message and decrypt it
                        byte[] data = readMessage(socket.getInputStream());
                        // create new buffer
                        inputBuffer = new ByteArrayInputStream(data, 6, data.length - 6);
                    }
                    return inputBuffer.read();
                }
            };

            this.outputStream = new OutputStream() {
                private ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(256);

                @Override
                public void write(int b) throws IOException {
                    outputBuffer.write(b);
                }

                @Override
                public void flush() throws IOException {
                    writeMessage(outputBuffer.toByteArray(), socket.getOutputStream());
                    outputBuffer.reset();
                }
            };
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.outputStream;
        }

        private synchronized byte[] readMessage(InputStream is) throws IOException {
            logger.trace("Receiving data from ETHM-1");
            // read number of bytes
            int bytesCount = is.read();
            logger.trace("Read count of bytes: {}", bytesCount);
            if (bytesCount == -1) {
                throw new IOException("End of input stream reached");
            }
            byte[] data = new byte[bytesCount];
            // read encrypted data
            int bytesRead = is.read(data);
            if (bytesCount != bytesRead) {
                throw new IOException(
                        String.format("Too few bytes read. Read: %d, expected: %d", bytesRead, bytesCount));
            }
            // decrypt data
            if (logger.isTraceEnabled()) {
                logger.trace("Decrypting data: {}", HexUtils.bytesToHex(data));
            }

            try {
                this.aesHelper.decrypt(data);
            } catch (Exception e) {
                throw new IOException("Decryption exception", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Decrypted data: {}", HexUtils.bytesToHex(data));
            }

            // validate message
            this.idR = data[4];
            if (this.idS != data[5]) {
                throw new IOException(String.format("Invalid 'idS' value. Got: %d, expected: %d", data[5], this.idS));
            }

            return data;
        }

        private synchronized void writeMessage(byte[] message, OutputStream os) throws IOException {
            // prepare data for encryption
            int bytesCount = 6 + message.length;
            if (bytesCount < 16) {
                bytesCount = 16;
            }
            byte[] data = new byte[bytesCount];
            int randomValue = this.rand.nextInt();
            data[0] = (byte) (randomValue >> 8);
            data[1] = (byte) (randomValue & 0xff);
            data[2] = (byte) (this.rollingCounter >> 8);
            data[3] = (byte) (this.rollingCounter & 0xff);
            data[4] = this.idS = (byte) this.rand.nextInt();
            data[5] = this.idR;
            ++this.rollingCounter;
            System.arraycopy(message, 0, data, 6, message.length);

            // encrypt data
            if (logger.isDebugEnabled()) {
                logger.debug("Encrypting data: {}", HexUtils.bytesToHex(data));
            }

            try {
                this.aesHelper.encrypt(data);
            } catch (Exception e) {
                throw new IOException("Encryption exception", e);
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Encrypted data: {}", HexUtils.bytesToHex(data));
            }

            // write encrypted data to output stream
            os.write(bytesCount);
            os.write(data);
            os.flush();
        }
    }
}
