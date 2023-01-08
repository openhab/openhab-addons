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
package org.openhab.binding.adorne.internal.hub;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

/**
 * The {@link AdorneHubConnection} manages basic connectivity with the Adorne hub.
 *
 * @author Mark Theiding - Initial Contribution
 */
@NonNullByDefault
public class AdorneHubConnection {
    private final Logger logger = LoggerFactory.getLogger(AdorneHubConnection.class);

    private final Socket hubSocket;
    private final PrintStream hubOut;
    private final InputStreamReader hubInReader;
    private final JsonStreamParser hubIn;

    public AdorneHubConnection(String hubHost, int hubPort, int timeout) throws IOException {
        hubSocket = new Socket(hubHost, hubPort);
        hubSocket.setSoTimeout(timeout);
        hubOut = new PrintStream(hubSocket.getOutputStream());
        hubInReader = new InputStreamReader(hubSocket.getInputStream());
        hubIn = new JsonStreamParser(hubInReader);
    }

    public void close() {
        try {
            hubInReader.close(); // Closes underlying input stream as well
        } catch (IOException e) {
            logger.warn("Closing hub input reader failed ({})", e.getMessage());
        }
        hubOut.close(); // Closes underlying output stream as well
        try {
            hubSocket.close();
        } catch (IOException e) {
            logger.warn("Closing hub controller socket failed ({})", e.getMessage());
        }
    }

    public void cancel() {
        try {
            hubSocket.shutdownInput();
        } catch (IOException e) {
            logger.debug("Couldn't shutdown hub socket");
        }
    }

    public void putMsg(String cmd) {
        hubOut.print(cmd);
    }

    public @Nullable JsonObject getMsg() throws JsonParseException {
        JsonElement msg = null;
        JsonObject msgJsonObject = null;

        msg = hubIn.next();

        if (msg == null || (msg instanceof JsonPrimitive && msg.getAsCharacter() == 0)) {
            return null; // Eat empty messages
        }
        logger.debug("Received message {}", msg);
        if (msg instanceof JsonObject) {
            msgJsonObject = (JsonObject) msg;
        }
        return msgJsonObject;
    }
}
