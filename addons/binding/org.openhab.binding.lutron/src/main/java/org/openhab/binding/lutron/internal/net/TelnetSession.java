/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * A single telnet session.
 *
 * @author Allan Tong - Initial contribution
 */
public class TelnetSession implements Closeable {

    private static final int BUFSIZE = 8192;

    private TelnetClient telnetClient;
    private BufferedReader reader;
    private PrintStream outstream;

    private CharBuffer charBuffer;
    private List<TelnetSessionListener> listeners = new ArrayList<>();

    private TelnetOptionHandler suppressGAOptionHandler;

    public TelnetSession() {
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
            this.telnetClient.connect(host, port);
            this.telnetClient.setKeepAlive(true);

            if (this.suppressGAOptionHandler == null) {
                // Only do this once.
                this.suppressGAOptionHandler = new SuppressGAOptionHandler(true, true, true, true);

                try {
                    this.telnetClient.addOptionHandler(this.suppressGAOptionHandler);
                } catch (InvalidTelnetOptionException e) {
                    // Should never happen. Wrap it inside IOException so as not to declare another throwable.
                    throw new IOException(e);
                }
            }

            this.reader = new BufferedReader(new InputStreamReader(this.telnetClient.getInputStream()));
            this.outstream = new PrintStream(this.telnetClient.getOutputStream());
        }
    }

    @Override
    public void close() throws IOException {
        if (this.telnetClient.isConnected()) {
            this.telnetClient.disconnect();
        }
    }

    public boolean isConnected() {
        return this.telnetClient.isConnected();
    }

    private void readInput() throws IOException {
        synchronized (this.charBuffer) {
            this.reader.read(this.charBuffer);
            this.charBuffer.notifyAll();

            if (this.charBuffer.position() > 0) {
                notifyInputAvailable();
            }
        }
    }

    public MatchResult waitFor(String prompt) throws InterruptedException {
        return waitFor(prompt, 0);
    }

    public MatchResult waitFor(String prompt, long timeout) throws InterruptedException {
        Pattern regex = Pattern.compile(prompt);
        long startTime = timeout > 0 ? System.currentTimeMillis() : 0;

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
        this.outstream.print(line + "\r\n");

        if (this.outstream.checkError()) {
            throw new IOException("Could not write to stream");
        }
    }
}
