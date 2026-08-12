/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.home.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Thin session handler for the MELCloud Home realtime push channel.
 *
 * <p>
 * Reads only enough of an incoming {@code unitStateChanged} frame to extract the affected unit ID, then hands it to
 * {@code onUnitDelta}. It never applies the frame's payload as state — per ADR-007, {@code GET /context} remains the
 * only source of truth; this class exists purely to trigger an out-of-cycle refresh sooner than the next scheduled
 * poll.
 *
 * <p>
 * Implements the classic Jetty {@link WebSocketListener} contract (as bundled by openHAB core's
 * {@code WebSocketFactory}) rather than the newer {@code Session.Listener} annotated style, which is not available
 * in that bundled version.
 *
 * <p>
 * {@link WebSocketListener}'s parameters carry no null annotations of their own (it is a plain, unannotated Jetty
 * interface), so under this class's {@code @NonNullByDefault} the Eclipse compiler rejects narrowing them to
 * {@code @NonNull} on override ("illegal redefinition"). Every overridden parameter is therefore marked
 * {@code @Nullable} to match the supertype's unconstrained contract, with an explicit null guard where the Jetty
 * contract guarantees non-null values are the only realistic case (e.g. {@code onWebSocketConnect}'s session).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeWebSocketListener implements WebSocketListener {

    private static final String MESSAGE_TYPE_UNIT_STATE_CHANGED = "unitStateChanged";

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeWebSocketListener.class);

    private final Consumer<String> onUnitDelta;
    private final Runnable onOpen;
    private final Runnable onClosed;

    /**
     * @param onUnitDelta called with the unit ID from every {@code unitStateChanged} frame received
     * @param onOpen called once the WebSocket session is established
     * @param onClosed called when the session ends, whether cleanly or due to an error
     */
    public MelCloudHomeWebSocketListener(Consumer<String> onUnitDelta, Runnable onOpen, Runnable onClosed) {
        this.onUnitDelta = onUnitDelta;
        this.onOpen = onOpen;
        this.onClosed = onClosed;
    }

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        logger.debug("MELCloud Home WebSocket connected");
        onOpen.run();
    }

    @Override
    public void onWebSocketText(@Nullable String message) {
        if (message == null) {
            return;
        }
        for (String unitId : extractUnitIds(message)) {
            onUnitDelta.accept(unitId);
        }
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
        // The MELCloud Home push channel only ever sends text frames; nothing to do with a binary frame.
        logger.debug("Ignoring unexpected binary MELCloud Home WebSocket frame ({} bytes)", len);
    }

    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        logger.debug("MELCloud Home WebSocket error: {}", cause == null ? "unknown" : cause.getMessage());
        onClosed.run();
    }

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        logger.debug("MELCloud Home WebSocket closed ({}): {}", statusCode, reason);
        onClosed.run();
    }

    /**
     * Extracts unit IDs from every {@code unitStateChanged} item in a text frame, which may be a single JSON object
     * or an array of them. Malformed JSON or an individual malformed item is skipped rather than propagated, so one
     * bad frame never tears down the session (mirrors the reference implementation's tolerance).
     */
    List<String> extractUnitIds(String message) {
        JsonElement root;
        try {
            root = JsonParser.parseString(message);
        } catch (JsonSyntaxException e) {
            logger.debug("Ignoring malformed MELCloud Home WebSocket frame: {}", e.getMessage());
            return List.of();
        }

        List<JsonElement> items = new ArrayList<>();
        if (root.isJsonArray()) {
            root.getAsJsonArray().forEach(items::add);
        } else {
            items.add(root);
        }

        List<String> unitIds = new ArrayList<>();
        for (JsonElement item : items) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject object = item.getAsJsonObject();
            JsonElement messageType = object.get("messageType");
            if (messageType == null || !messageType.isJsonPrimitive()
                    || !MESSAGE_TYPE_UNIT_STATE_CHANGED.equals(messageType.getAsString())) {
                continue;
            }
            JsonElement data = object.has("Data") ? object.get("Data") : object.get("data");
            if (data == null || !data.isJsonObject()) {
                continue;
            }
            JsonElement id = data.getAsJsonObject().get("id");
            if (id != null && id.isJsonPrimitive()) {
                unitIds.add(id.getAsString());
            }
        }
        return unitIds;
    }
}
