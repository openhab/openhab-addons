package org.openhab.binding.coolmasternet.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoolMasterNetClient {
    private static final Logger logger = LoggerFactory.getLogger(CoolMasterNetClient.class);
    private String host;
    private int port;
    private Socket socket;
    private final Object lock = new Object();

    public CoolMasterNetClient(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            connect();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public boolean isConnected() {
        synchronized (this.lock) {
            logger.debug(String.format("Testing %s is connected? %s %s %s", host, socket, socket.isConnected(),
                    socket.isClosed()));
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }

    public String sendCommand(String command) {
        synchronized (this.lock) {
            if (!checkConnection()) {
                return null;
            }

            try {
                logger.debug(String.format("Sending command '%s'", command));
                OutputStream out = socket.getOutputStream();
                out.write(command.getBytes());
                out.write("\r\n".getBytes());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder result = new StringBuilder();
                while (true) {
                    String line = in.readLine();
                    logger.debug(String.format("Read result '%s'", line));
                    if ("OK".equals(line)) {
                        return result.toString();
                    }
                    /* TODO: look for ERR responses here */
                    result.append(line);
                }

            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                return null;
            }
        }
    }

    private Boolean checkConnection() {
        synchronized (this.lock) {
            try {
                if (!isConnected()) {
                    connect();
                    if (!isConnected()) {
                        return false;
                    }
                }

                java.io.InputStream in = socket.getInputStream();
                /* Flush anything pending in the input stream */
                while (in.available() > 0) {
                    in.read();
                }
                /* Send a CRLF, expect a > prompt (and a CRLF) back */
                OutputStream out = socket.getOutputStream();
                out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
                /*
                 * this will time out with IOException if it doesn't see that prompt
                 * with no other data following it, within 1 second (socket timeout)
                 */
                final byte PROMPT = ">".getBytes(StandardCharsets.US_ASCII)[0];
                while (in.read() != PROMPT || in.available() > 3) {
                }
                return true;
            } catch (IOException e) {
                disconnect();
                logger.error(e.getLocalizedMessage(), e);
                return false;
            }
        }
    }

    private void connect() throws IOException {
        synchronized (this.lock) {
            try {
                socket = new Socket(host, port);
                socket.setSoTimeout(1000);
            } catch (UnknownHostException e) {
                logger.error("unknown socket host " + host);
            } catch (SocketException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e1) {
            logger.error(e1.getLocalizedMessage(), e1);
        }
        socket = null;
    }

}
