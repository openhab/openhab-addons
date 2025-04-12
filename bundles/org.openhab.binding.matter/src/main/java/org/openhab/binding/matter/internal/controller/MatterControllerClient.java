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
package org.openhab.binding.matter.internal.controller;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.client.MatterWebsocketClient;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.PairingCodes;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OperationalCredentialsCluster;
import org.openhab.binding.matter.internal.client.dto.ws.ActiveSessionInformation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MatterControllerClient}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterControllerClient extends MatterWebsocketClient {

    public static final int PAIRING_TIMEOUT_SECONDS = 60 * 3;

    public void connect(MatterWebsocketService wss, BigInteger nodeId, String controllerName, String storagePath) {
        Map<String, String> params = Map.of("nodeId", nodeId.toString(), "controllerName", controllerName,
                "storagePath", storagePath);
        connectWhenReady(wss, params);
    }

    /**
     * Get all nodes the are commissioned / paired to this controller
     *
     * @param onlyConnected filter to nodes that are currently connected
     * @return
     * @throws Exception
     */
    public CompletableFuture<List<BigInteger>> getCommissionedNodeIds() {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "listNodes", new Object[0]);
        return future.thenApply(obj -> {
            List<BigInteger> nodes = gson.fromJson(obj, new TypeToken<List<BigInteger>>() {
            }.getType());
            return nodes != null ? nodes : Collections.emptyList();
        });
    }

    /**
     * Initialize a commissioned node, wait for connectionTimeoutMilliseconds for the node to connect before returning
     * 
     * @param id
     * @param connectionTimeoutMilliseconds
     * @return
     */
    public CompletableFuture<Void> initializeNode(BigInteger id, Integer connectionTimeoutMilliseconds) {
        // add 1 second delay to the message timeout to allow the function to complete
        CompletableFuture<JsonElement> future = sendMessage("nodes", "initializeNode",
                new Object[] { id, connectionTimeoutMilliseconds }, connectionTimeoutMilliseconds / 1000 + 1);
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Request all cluster attribute data for the node from the controller, the actual data will be sent via a
     * NodeDataListener event
     * 
     * @param id
     * @return
     */
    public CompletableFuture<Void> requestAllNodeData(BigInteger id) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "requestAllData", new Object[] { id });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Request all cluster attribute data for a single endpoint and its children
     * 
     * @param id
     * @param endpointId
     * @return
     */
    public CompletableFuture<Void> requestEndpointData(BigInteger id, Integer endpointId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "requestEndpointData",
                new Object[] { id, endpointId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Pair a node using a pairing code, either a manual pairing code or a matter QR code (starts with MT:)
     * 
     * @param code
     * @return
     */
    public CompletableFuture<BigInteger> pairNode(String code) {
        String[] parts = code.trim().split(" ");
        CompletableFuture<JsonElement> future = null;
        if (parts.length == 2) {
            future = sendMessage("nodes", "pairNode", new Object[] { "", parts[0], parts[1] }, PAIRING_TIMEOUT_SECONDS);
        } else {
            // MT is a matter QR code, other wise remove any dashes in a manual pairing code
            String pairCode = parts[0].toUpperCase().indexOf("MT:") == 0 ? parts[0] : parts[0].replaceAll("-", "");
            future = sendMessage("nodes", "pairNode", new Object[] { pairCode }, PAIRING_TIMEOUT_SECONDS);
        }
        return future.thenApply(obj -> {
            return new BigInteger(obj.getAsString());
        });
    }

    /**
     * Remove a node from the controller
     * 
     * @param nodeId
     * @return
     */
    public CompletableFuture<Void> removeNode(BigInteger nodeId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "removeNode", new Object[] { nodeId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Reconnect a node to the controller
     * 
     * @param nodeId
     * @return
     */
    public CompletableFuture<Void> reconnectNode(BigInteger nodeId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "reconnectNode", new Object[] { nodeId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Get the pairing codes for a node
     * 
     * @param id
     * @return
     */
    public CompletableFuture<PairingCodes> enhancedCommissioningWindow(BigInteger id) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "enhancedCommissioningWindow",
                new Object[] { id });
        return future.thenApply(obj -> {
            PairingCodes codes = gson.fromJson(obj, PairingCodes.class);
            if (codes == null) {
                throw new JsonParseException("Could not deserialize pairing codes");
            }
            return codes;
        });
    }

    /**
     * Disconnect a node from the controller
     * 
     * @param nodeId
     * @return
     */
    public CompletableFuture<Void> disconnectNode(BigInteger nodeId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "disconnectNode", new Object[] { nodeId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Get the fabrics for a node, fabrics aer the list of matter networks the node is joined to
     * 
     * @param nodeId
     * @return
     */
    public CompletableFuture<List<OperationalCredentialsCluster.FabricDescriptorStruct>> getFabrics(BigInteger nodeId) {
        Object[] clusterArgs = { String.valueOf(nodeId) };
        CompletableFuture<JsonElement> future = sendMessage("nodes", "getFabrics", clusterArgs);
        return future.thenApply(obj -> {
            Type listType = new TypeToken<List<OperationalCredentialsCluster.FabricDescriptorStruct>>() {
            }.getType();
            List<OperationalCredentialsCluster.FabricDescriptorStruct> list = gson.fromJson(obj, listType);
            if (list == null) {
                throw new JsonParseException("Could not deserialize fabrics");
            }
            return list;
        });
    }

    /**
     * Remove a fabric from a node, fabrics are identified by an index in the matter specification
     * 
     * @param nodeId
     * @param index
     * @return
     */
    public CompletableFuture<Void> removeFabric(BigInteger nodeId, Integer index) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "removeFabric", new Object[] { nodeId, index });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Send a command to a cluster
     * 
     * @param nodeId
     * @param endpointId
     * @param clusterName
     * @param command
     * @return
     */
    public CompletableFuture<JsonElement> clusterCommand(BigInteger nodeId, Integer endpointId, String clusterName,
            ClusterCommand command) {
        Object[] clusterArgs = { String.valueOf(nodeId), endpointId, clusterName, command.commandName, command.args };
        CompletableFuture<JsonElement> future = sendMessage("clusters", "command", clusterArgs);
        return future;
    }

    /**
     * Write an attribute to a cluster
     * 
     * @param nodeId
     * @param endpointId
     * @param clusterName
     * @param attributeName
     * @param value
     * @return
     */
    public CompletableFuture<Void> clusterWriteAttribute(BigInteger nodeId, Integer endpointId, String clusterName,
            String attributeName, String value) {
        Object[] clusterArgs = { String.valueOf(nodeId), endpointId, clusterName, attributeName, value };
        CompletableFuture<JsonElement> future = sendMessage("clusters", "writeAttribute", clusterArgs);
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Read an attribute from a cluster
     * 
     * @param nodeId
     * @param endpointId
     * @param clusterName
     * @param attributeName
     * @return
     */
    public CompletableFuture<String> clusterReadAttribute(BigInteger nodeId, Integer endpointId, String clusterName,
            String attributeName) {
        Object[] clusterArgs = { String.valueOf(nodeId), endpointId, clusterName, attributeName };
        CompletableFuture<JsonElement> future = sendMessage("clusters", "readAttribute", clusterArgs);
        return future.thenApply(obj -> {
            return obj.getAsString();
        });
    }

    /**
     * Get the session information for the controller
     * 
     * @return
     */
    public CompletableFuture<ActiveSessionInformation[]> getSessionInformation() {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "sessionInformation", new Object[0]);
        return future.thenApply(obj -> {
            ActiveSessionInformation[] sessions = gson.fromJson(obj, ActiveSessionInformation[].class);
            return sessions == null ? new ActiveSessionInformation[0] : sessions;
        });
    }
}
