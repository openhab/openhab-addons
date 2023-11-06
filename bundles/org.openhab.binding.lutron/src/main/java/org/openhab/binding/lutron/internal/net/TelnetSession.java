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
package org.openhab.binding.lutron.internal.net;

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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single telnet session.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Fix to readInput and added debug logging
 */
@NonNullByDefault
public class TelnetSession implements Closeable {

    private static final int BUFSIZE = 8192;

    private final Logger logger = LoggerFactory.getLogger(TelnetSession.class);

    private TelnetClient telnetClient;
    private @Nullable BufferedReader reader;
    private @Nullable PrintStream outstream;

    private CharBuffer charBuffer;
    private List<TelnetSessionListener> listeners = new ArrayList<>();

    private @Nullable TelnetOptionHandler suppressGAOptionHandler;

    public TelnetSession() {
        logger.trace("Creating new TelnetSession");
        this.telnetClient = new TelnetClient();
        this.charBuffer = CharBuffer.allocate(BUFSIZE);

        this.telnetClient.setReaderThread(true);
        this.telnetClient.registerInputListener(new TelnetInputListener() {
            @Override
            public void telnetInputAvailable() {
                try {
                    readInput();
                } catch (IOException e) {
                    notifyInputError(e);
                }
            }
        });
    }

    public void addListener(TelnetSessionListener listener) {
        this.listeners.add(listener);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    private void notifyInputAvailable() {
        for (TelnetSessionListener listener : this.listeners) {
            listener.inputAvailable();
        }
    }

    private void notifyInputError(IOException exception) {
        logger.debug("TelnetSession notifyInputError: {}", exception.getMessage());
        for (TelnetSessionListener listener : this.listeners) {
            listener.error(exception);
        }
    }

    public void open(String host) throws IOException {
        open(host, 23);
    }

    public void open(String host, int port) throws IOException {
        // Synchronized block prevents listener thread from attempting to read input before we're ready.
        synchronized (this.charBuffer) {
            logger.trace("TelnetSession open called");
            try {
                telnetClient.connect(host, port);
                telnetClient.setKeepAlive(true);
            } catch (IOException e) {
                logger.debug("TelnetSession open: error connecting: {}", e.getMessage());
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

            this.reader = new BufferedReader(new InputStreamReader(this.telnetClient.getInputStream()));
            this.outstream = new PrintStream(this.telnetClient.getOutputStream());
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
                reader = null;
                outstream = null;
            }
        }
    }

    public boolean isConnected() {
        synchronized (charBuffer) {
            return reader != null;
        }
    }

    private void readInput() throws IOException {
        synchronized (charBuffer) {
            if (reader != null) {
                try {
                    reader.read(charBuffer);
                } catch (IOException e) {
                    logger.debug("TelnetSession readInput: error reading: {}", e.getMessage());
                    throw (e);
                }
                charBuffer.notifyAll();

                if (charBuffer.position() > 0) {
                    notifyInputAvailable();
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
            PrintStream out = outstream;
            if (out == null) {
                logger.debug("TelnetSession writeLine: outstream is null - session is closed");
                throw new IOException("Session is closed");
            }
            out.print(line + "\r\n");

            if (out.checkError()) {
                logger.debug("TelnetSession writeLine: error writing to outstream");
                throw new IOException("Could not write to stream");
            }
        }
    }
}
