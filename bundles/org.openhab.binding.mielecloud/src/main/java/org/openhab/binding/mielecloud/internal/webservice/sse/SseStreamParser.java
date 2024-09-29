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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceDisconnectSseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses events from the SSE event stream and emits them via the given dispatcher.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
class SseStreamParser {
    private static final String SSE_KEY_EVENT = "event:";
    private static final String SSE_KEY_DATA = "data:";

    private final Logger logger = LoggerFactory.getLogger(SseStreamParser.class);

    private final BufferedReader reader;
    private final Consumer<ServerSentEvent> onServerSentEventCallback;
    private final Consumer<@Nullable Throwable> onStreamClosedCallback;

    private @Nullable String event;

    SseStreamParser(InputStream inputStream, Consumer<ServerSentEvent> onServerSentEventCallback,
            Consumer<@Nullable Throwable> onStreamClosedCallback) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        this.onServerSentEventCallback = onServerSentEventCallback;
        this.onStreamClosedCallback = onStreamClosedCallback;
    }

    void parseAndDispatchEvents() {
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                onLineReceived(line);
            }

            silentlyCloseReader();
            logger.debug("SSE stream ended. Closing stream.");
            onStreamClosedCallback.accept(null);
        } catch (IOException exception) {
            silentlyCloseReader();

            if (!(exception.getCause() instanceof MieleWebserviceDisconnectSseException)) {
                logger.warn("SSE connection failed unexpectedly: {}", exception.getMessage());
                onStreamClosedCallback.accept(exception.getCause());
            }
        }
        logger.debug("SSE stream closed.");
    }

    private void silentlyCloseReader() {
        try {
            reader.close();
        } catch (IOException e) {
            logger.warn("Failed to clean up SSE connection resources!", e);
        }
    }

    private void onLineReceived(String line) {
        if (line.isEmpty()) {
            return;
        }

        if (line.startsWith(SSE_KEY_EVENT)) {
            event = line.substring(SSE_KEY_EVENT.length()).trim();
        } else if (line.startsWith(SSE_KEY_DATA)) {
            String event = this.event;
            String data = line.substring(SSE_KEY_DATA.length()).trim();

            if (event == null) {
                logger.warn("Received data payload without prior event payload.");
            } else {
                onServerSentEventCallback.accept(new ServerSentEvent(event, data));
            }
        } else {
            logger.warn("Unable to parse line from SSE stream: {}", line);
        }
    }
}
