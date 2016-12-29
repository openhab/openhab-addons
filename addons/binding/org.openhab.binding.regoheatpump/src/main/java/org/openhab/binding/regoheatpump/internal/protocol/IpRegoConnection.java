package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpRegoConnection implements RegoConnection {
    /** Connection timeout in milliseconds **/
    private static final int CONNECTION_TIMEOUT = 3000;

    /** Socket read timeout in milliseconds **/
    private static final int SOCKET_READ_TIMEOUT = 1000;

    private final Logger logger = LoggerFactory.getLogger(IpRegoConnection.class);
    private final String address;
    private final int port;
    private Socket clientSocket;

    public IpRegoConnection(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        logger.info("Connecting to '{}', port = {}.", address, port);
        if (clientSocket == null) {
            clientSocket = new Socket();
            clientSocket.setSoTimeout(SOCKET_READ_TIMEOUT);
            clientSocket.setKeepAlive(true);
        }
        clientSocket.connect(new InetSocketAddress(address, port), CONNECTION_TIMEOUT);
        logger.info("Connected to '{}', port = {}.", address, port);
    }

    @Override
    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected();
    }

    @Override
    public void close() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // There is really not much we can do here, ignore the error and continue execution.
            logger.warn("Closing socket failed", e);
        }

        clientSocket = null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return clientSocket.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return clientSocket.getInputStream();
    }
}
