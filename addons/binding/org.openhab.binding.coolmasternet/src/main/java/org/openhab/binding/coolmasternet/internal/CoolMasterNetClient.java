package org.openhab.binding.coolmasternet.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Class to access CoolMasterNet ASCII protocol via TCP socket.
 *
 * Abstracts protocol access in a way that allows a single client to be shared across multiple Things
 * (because a single CoolMasterNet unit can contain multiple independent UIDs representing different
 * HVAC units.)
 */
public class CoolMasterNetClient {
    private static final int SOCKET_TIMEOUT = 2000;
    private static final Logger logger = LoggerFactory.getLogger(CoolMasterNetClient.class);
    private static final Map<String, WeakReference<CoolMasterNetClient>> clientCache = new HashMap<>();
    private String host;
    private int port;
    private Socket socket;
    private final Object lock = new Object();

    /*
     * Returns a client for this host/port combination. Reuses an existing
     * client if one exists, otherwise creates a new client.
     *
     * This allows for sharing a single TCP connection among multiple
     * CoolMasterNet UIDs on the same device.
     */
    public static CoolMasterNetClient getClient(String host, int port) {
        String key = String.format("%s:%d", host, port);
        WeakReference<CoolMasterNetClient> ref = clientCache.getOrDefault(key, null);
        CoolMasterNetClient result = (ref != null) ? ref.get() : null;
        if (result == null) {
            result = new CoolMasterNetClient(host, port);
            clientCache.put(key, new WeakReference<>(result));
        }
        return result;
    }

    private CoolMasterNetClient(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            connect();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /*
     * Return true if the client socket is connected.
     *
     * Use checkConnection() to probe if the coolmasternet is responding correctly,
     * and try to re-establish the connection if possible.
     */
    public boolean isConnected() {
        synchronized (this.lock) {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }

    /*
     * Send a particular ASCII command to the CoolMasterNet, and return the successful response as a string.
     *
     * If the "OK" prompt is not received then a CoolMasterClientError is thrown that contains whatever
     * error message was printed by the CoolMasterNet.
     */
    public String sendCommand(String command) throws CoolMasterClientError {
        synchronized (this.lock) {
            checkConnection();

            StringBuilder response = new StringBuilder();
            try {
                logger.trace(String.format("Sending command '%s'", command));
                OutputStream out = socket.getOutputStream();
                out.write(command.getBytes());
                out.write("\r\n".getBytes());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    String line = in.readLine();
                    logger.debug(String.format("Read result '%s'", line));
                    if ("OK".equals(line)) {
                        return response.toString();
                    }
                    response.append(line);
                    if (response.length() > 100) {
                        /*
                         * Usually this loop times out on errors, but in the case that we just keep getting
                         * data we should also fail with an error.
                         */
                        throw new CoolMasterClientError(String.format("Got gibberish response to command %s", command));
                    }
                }

            } catch (SocketTimeoutException e) {
                if (response.length() == 0) {
                    throw new CoolMasterClientError(String.format("No response to command %s", command));
                }
                throw new CoolMasterClientError(String.format("Command '%s' got error '%s'", command, response));
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                return null;
            }
        }
    }

    /*
     * Verify that the client socket is connected and responding, and try to reconnect if possible.
     * May block for 1-2 seconds.
     *
     * Throws CoolMasterNetClientError if there is a conection problem.
     *
     */
    public void checkConnection() throws CoolMasterClientError {
        synchronized (this.lock) {
            try {
                if (!isConnected()) {
                    connect();
                    if (!isConnected()) {
                        throw new CoolMasterClientError(String.format("Failed to connect to %s:%s", host, port));
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
            } catch (IOException e) {
                disconnect();
                logger.error(e.getLocalizedMessage(), e);
                throw new CoolMasterClientError(String.format("No response from CoolMasterNet unit %s:%s", host, port));
            }
        }
    }

    private void connect() throws IOException {
        synchronized (this.lock) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
                socket.setSoTimeout(SOCKET_TIMEOUT);
            } catch (UnknownHostException e) {
                logger.error("unknown socket host " + host);
                socket = null;
            } catch (SocketException e) {
                logger.error(e.getLocalizedMessage(), e);
                socket = null;
            }
        }
    }

    public void disconnect() {
        synchronized (this.lock) {
            try {
                socket.close();
            } catch (IOException e1) {
                logger.error(e1.getLocalizedMessage(), e1);
            }
            socket = null;
        }
    }

    public class CoolMasterClientError extends Exception {
        private static final long serialVersionUID = 1L;

        public CoolMasterClientError(String message) {
            super(message);
        }
    }
}
