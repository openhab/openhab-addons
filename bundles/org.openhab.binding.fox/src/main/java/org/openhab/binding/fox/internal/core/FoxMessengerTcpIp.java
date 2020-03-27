/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FoxMessengerTcpIp} implements communication with Fox system via TCP/IP telnet service.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxMessengerTcpIp implements FoxMessenger {

    private String host = "";
    private int port = 0;
    private int timeout = 250;
    private String password = "";

    private Socket socket = new Socket();
    private @Nullable PrintWriter toServer;
    private @Nullable BufferedReader fromServer;

    public FoxMessengerTcpIp() {
        toServer = null;
        fromServer = null;
    }

    public void setHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        int colon = host.indexOf(":");
        if (colon < 1) {
            setHost(host, 23);
        } else {
            setHost(host.substring(0, colon), Integer.parseInt(host.substring(colon + 1)));
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void open() throws FoxException {
        close();

        PrintWriter toServer = null;
        BufferedReader fromServer = null;

        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(timeout);

            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            boolean promptOk = false;
            StringBuilder sb = new StringBuilder();
            for (;;) {
                int value = fromServer.read();
                if (value < 0) {
                    break;
                }

                sb.append((char) value);
                String prompt = sb.toString();

                if (prompt.equals("pass: ")) {
                    if (password.length() > 0) {
                        toServer.println(password);
                        sb = new StringBuilder();
                    } else {
                        throw new FoxException("Password demand but no password set");
                    }
                } else if (prompt.equals("> Fox terminal")) {
                    promptOk = true;
                    fromServer.readLine();
                    break;
                } else if (prompt.equals("ACCESS DENIED")) {
                    throw new FoxException("Password rejected, access denied");
                }
            }

            if (!promptOk) {
                throw new FoxException("Wrong prompt received");
            }

            this.toServer = toServer;
            this.fromServer = fromServer;
        } catch (FoxException | IOException e) {
            if (toServer != null) {
                toServer.close();
            }
            try {
                if (fromServer != null) {
                    fromServer.close();
                }
            } catch (IOException e1) {
            }
            try {
                socket.close();
            } catch (IOException e1) {
            }
            socket = new Socket();
            throw new FoxException(e.getMessage());
        }
    }

    @Override
    public void write(String text) throws FoxException {
        write(text, "");
    }

    public void write(String text, String echo) throws FoxException {
        PrintWriter toServer = this.toServer;
        if (toServer != null) {
            toServer.println(text);
            if (echo.length() > 0) {
                readEcho(echo);
            }
        } else {
            throw new FoxException("Closed messenger");
        }
    }

    private String purify(String line) {
        String l = line;
        while (l.length() > 0) {
            char start = l.charAt(0);
            if (start < 0x20 || start > 0x7f) {
                if (l.length() > 1) {
                    l = l.substring(1);
                } else {
                    l = "";
                }
            } else {
                return l;
            }
        }
        return l;
    }

    private String readLine() throws FoxException {
        try {
            BufferedReader fromServer = this.fromServer;
            if (fromServer != null) {
                String line = fromServer.readLine();
                if (line != null) {
                    return purify(line);
                } else {
                    throw new FoxException("Reading error: End of stream");
                }
            } else {
                throw new FoxException("Closed messenger");
            }
        } catch (SocketTimeoutException e) {
            return "";
        } catch (IOException e) {
            throw new FoxException("Reading error: " + e.getMessage());
        }
    }

    private void readEcho(String text) throws FoxException {
        String echo = readLine();
        if (!echo.equals(text.trim())) {
            throw new FoxException("Wrong echo received");
        }
    }

    @Override
    public String read() throws FoxException {
        String text = readLine();
        return text;
    }

    @Override
    public void close() {
        try {
            PrintWriter toServer = this.toServer;
            if (toServer != null) {
                toServer.close();
                this.toServer = null;
            }
            BufferedReader fromServer = this.fromServer;
            if (fromServer != null) {
                fromServer.close();
                this.fromServer = null;
            }
            socket.close();
        } catch (IOException e) {
        }
        socket = new Socket();
    }

    @Override
    public void ping() throws FoxException {
        write("hello", "");
    }

    @Override
    public void test() throws FoxException {
        write("hello", "> Fox hello");
    }

}
