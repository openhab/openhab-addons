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

import java.util.List;
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

    /**
     * Add an endpoint to the bridge.
     * 
     * @param be the bridged endpoint
     * @return a future that completes when the endpoint is added
     */
    public CompletableFuture<String> addEndpoint(BridgedEndpoint be) {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "addEndpoint", new Object[] { be.deviceType,
                be.id, be.nodeLabel, be.productName, be.productLabel, be.serialNumber, be.attributeMap });
        return future.thenApply(obj -> obj.toString());
    }

    /**
     * Set the state of an attribute of the endpoint.
     * 
     * @param endpointId the endpoint id
     * @param clusterName the cluster name
     * @param attributeName the attribute name
     * @param state the state
     * @return a future that completes when the state is set
     */
    public CompletableFuture<Void> setEndpointState(String endpointId, String clusterName, String attributeName,
            Object state) {
        return setEndpointStates(endpointId, List.of(new AttributeState(clusterName, attributeName, state)));
    }

    /**
     * Set the states of the endpoint in a single transaction.
     * 
     * @param endpointId the endpoint id
     * @param states the states to set
     * @return a future that completes when the states are set
     */
    public CompletableFuture<Void> setEndpointStates(String endpointId, List<AttributeState> states) {
        CompletableFuture<JsonElement> future = sendMessage("bridge", "setEndpointStates",
                new Object[] { endpointId, states });
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
