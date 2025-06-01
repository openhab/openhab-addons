/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.bridge;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.client.MatterWebsocketClient;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeCommissionState;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link MatterBridgeClient} is a client for the Matter Bridge service.
 * 
 * It is responsible for sending messages to the Matter Bridge websocket server and receiving responses.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterBridgeClient extends MatterWebsocketClient {

    public CompletableFuture<String> addEndpoint(String deviceType, String id, String nodeLabel, String productName,
            String productLabel, String serialNumber, Map<String, Map<String, Object>> attributeMap) {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "addEndpoint",
                new Object[] { deviceType, id, nodeLabel, productName, productLabel, serialNumber, attributeMap });
        return future.thenApply(obj -> obj.toString());
    }

    public CompletableFuture<Void> setEndpointState(String endpointId, String clusterName, String attributeName,
            Object state) {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "setEndpointState",
                new Object[] { endpointId, clusterName, attributeName, state });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    public CompletableFuture<Void> initializeBridge(boolean resetStorage) {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "initializeBridge",
                new Object[] { resetStorage });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    public CompletableFuture<Void> startBridge() {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "startBridge", new Object[0]);
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    public CompletableFuture<BridgeCommissionState> getCommissioningState() {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "getCommissioningState", new Object[0]);
        return future.thenApply(obj -> {
            BridgeCommissionState state = gson.fromJson(obj, BridgeCommissionState.class);
            if (state == null) {
                throw new JsonParseException("Could not deserialize commissioning state");
            }
            return state;
        });
    }

    public CompletableFuture<Void> openCommissioningWindow() {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "openCommissioningWindow", new Object[0]);
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    public CompletableFuture<Void> closeCommissioningWindow() {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "closeCommissioningWindow", new Object[0]);
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    public CompletableFuture<Void> removeFabric(int fabricIndex) {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "removeFabric", new Object[] { fabricIndex });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    public CompletableFuture<String> getFabrics() {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "getFabrics", new Object[0]);
        return future.thenApply(obj -> {
            return obj.toString();
        });
    }
}
