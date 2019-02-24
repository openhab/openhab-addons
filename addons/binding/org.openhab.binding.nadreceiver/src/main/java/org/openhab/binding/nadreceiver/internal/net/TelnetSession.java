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
package org.openhab.binding.nadreceiver.internal.net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetInputListener;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class copied from binding project Lutron and their authors Allan Tong and Bob Adair
 *
 * A single Telnet session connected to NAD Receiver server. Please note that NAD Receiver supports multiple connections
 * in the same time. In such cases, when a status update occurs, all listening Telnet client are notified via a message
 * with new status
 *
 * @author Marc Chételat - Initial contribution
 */
public class TelnetSession implements Closeable {

    private static final int BUFFER_SIZE = 8192;

    private final Logger logger = LoggerFactory.getLogger(TelnetSession.class);

    private TelnetClient telnetClient;
    private BufferedReader bufferedReader;
    private PrintStream outStream;

    private CharBuffer charBuffer;
    private List<TelnetSessionListener> telnetListeners = new ArrayList<>();

    private TelnetOptionHandler suppressGAOptionHandler;

    /**
     * Creating new session
     */
    public TelnetSession() {
        logger.trace("Creating new TelnetSession");
        this.telnetClient = new TelnetClient();
        this.charBuffer = CharBuffer.allocate(BUFFER_SIZE);
        this.telnetClient.setReaderThread(true);
        this.telnetClient.registerInputListener(new TelnetInputListener() {
            @Override
            public void telnetInputAvailable() {
                try {
                    readInput();
                } catch (IOException e) {
                    notifyListenersForNewError(e);
                }
            }
        });
    }

    /**
     * Adding a new listener. Will be notified later
     *
     * @param listener
     */
    public void addListener(TelnetSessionListener listener) {
        this.telnetListeners.add(listener);
    }

    public void clearListeners() {
        this.telnetListeners.clear();
    }

    private void notifyListenersForNewMessages() {
        for (TelnetSessionListener listener : this.telnetListeners) {
            listener.incomingMessageAvailable();
        }
    }

    private void notifyListenersForNewError(IOException exception) {
        logger.debug("TelnetSession notifyListenersForNewError: {}", exception.getMessage());
        for (TelnetSessionListener listener : this.telnetListeners) {
            listener.errorHandling(exception);
        }
    }

    public void open(String host) throws IOException {
        open(host, 23);
    }

    public void open(String host, int port) throws IOException {
        // Synchronized block prevents listener thread from attempting to read input before we're ready.
        synchronized (this.charBuffer) {
            try {
                logger.trace("Opening TelnetSession...");
                telnetClient.connect(host, port);
                telnetClient.setKeepAlive(true);
                logger.trace("... TelnetSession opened.");
            } catch (IOException e) {
                logger.debug("TelnetSession error during opening: {}", e.getMessage());
                throw (e);
            }

            if (this.suppressGAOptionHandler == null) {
                // Only do this once.
                this.suppressGAOptionHandler = new SuppressGAOptionHandler(true, true, true, true);

                try {
                    this.telnetClient.addOptionHandler(this.suppressGAOptionHandler);
                } catch (InvalidTelnetOptionException e) {
                    // Should never happen. Wrap it inside IOException so as not to declare another throwable.
                    logger.debug("TelnetSession open: error adding telnet option handler: {}", e.getMessage());
                    throw new IOException(e);
                }
            }

            this.bufferedReader = new BufferedReader(new InputStreamReader(this.telnetClient.getInputStream()));
            this.outStream = new PrintStream(this.telnetClient.getOutputStream());
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (charBuffer) {
            logger.trace("TelnetSession close called");
            try {
                if (telnetClient.isConnected()) {
                    telnetClient.disconnect();
                }
            } catch (IOException e) {
                logger.debug("TelnetSession close: error disconnecting: {}", e.getMessage());
                throw (e);
            } finally {
                bufferedReader = null;
                outStream = null;
            }
        }
    }

    public boolean isConnected() {
        synchronized (charBuffer) {
            return bufferedReader != null;
        }
    }

    private void readInput() throws IOException {
        synchronized (charBuffer) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.read(charBuffer);
                } catch (IOException e) {
                    logger.debug("TelnetSession readInput: error reading: {}", e.getMessage());
                    throw (e);
                }
                charBuffer.notifyAll();

                if (charBuffer.position() > 0) {
                    notifyListenersForNewMessages();
                }
            } else {
                logger.debug("TelnetSession readInput: reader is null - session is closed");
                throw new IOException("Session is closed");
            }
        }
    }

    public MatchResult waitFor(String prompt) throws InterruptedException {
        return waitFor(prompt, 0);
    }

    public MatchResult waitFor(String prompt, long timeout) throws InterruptedException {
        Pattern regex = Pattern.compile(prompt);
        long startTime = timeout > 0 ? System.currentTimeMillis() : 0;

        logger.trace("TelnetSession waitFor called with {} {}", prompt, timeout);
        synchronized (this.charBuffer) {
            this.charBuffer.flip();

            String bufdata = this.charBuffer.toString();
            int n = bufdata.lastIndexOf('\n');
            String lastLine;

            if (n != -1) {
                lastLine = bufdata.substring(n + 1);
            } else {
                lastLine = bufdata;
            }

            Matcher matcher = regex.matcher(lastLine);

            while (!matcher.find()) {
                long elapsed = timeout > 0 ? (System.currentTimeMillis() - startTime) : 0;

                if (timeout > 0 && elapsed >= timeout) {
                    break;
                }

                this.charBuffer.clear();
                this.charBuffer.put(lastLine);

                this.charBuffer.wait(timeout - elapsed);
                this.charBuffer.flip();

                bufdata = this.charBuffer.toString();
                n = bufdata.lastIndexOf('\n');

                if (n != -1) {
                    lastLine = bufdata.substring(n + 1);
                } else {
                    lastLine = bufdata;
                }
                matcher = regex.matcher(lastLine);
            }

            this.charBuffer.clear();
            return matcher.toMatchResult();
        }
    }

    public Iterable<String> readLines() {
        synchronized (this.charBuffer) {
            this.charBuffer.flip();

            String bufdata = this.charBuffer.toString();
            int n = bufdata.lastIndexOf('\n');
            String leftover;
            String[] lines = null;

            if (n != -1) {
                leftover = bufdata.substring(n + 1);
                bufdata = bufdata.substring(0, n).trim();

                lines = bufdata.split("\r\n");
            } else {
                leftover = bufdata;
            }

            this.charBuffer.clear();
            this.charBuffer.put(leftover);

            return lines == null ? Collections.<String> emptyList() : Arrays.asList(lines);
        }
    }

    public void writeLine(String line) throws IOException {
        synchronized (charBuffer) {
            logger.trace("TelnetSession writeLine called with {}", line);
            if (outStream == null) {
                logger.debug("TelnetSession writeLine: outstream is null - session is closed");
                throw new IOException("Session is closed");
            }

            // For some reasons, if the NAD receiver is receiving messages the new line requires first a new line before
            // writing the new command
            outStream.print("\r\n" + line + "\r\n");
            if (outStream.checkError()) {
                logger.debug("TelnetSession writeLine: error writing to outstream");
                throw new IOException("Could not write to stream");
            }
        }
    }
}
