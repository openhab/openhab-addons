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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.MatterRequestException;
import org.openhab.binding.matter.internal.client.MatterWebsocketClient;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.PairingCodes;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OperationalCredentialsCluster;
import org.openhab.binding.matter.internal.client.dto.ws.ActiveSessionInformation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MatterControllerClient} is a client for the Matter Controller.
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
     * Get all nodes that are commissioned / paired to this controller
     *
     * @return a future that returns a list of node IDs
     * @throws MatterRequestException if the request fails
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
     * @param nodeId the node ID to initialize
     * @param connectionTimeoutMilliseconds the timeout in milliseconds to wait for the node to connect
     * @return a future that completes when the node is initialized
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<Void> initializeNode(BigInteger nodeId, Integer connectionTimeoutMilliseconds) {
        // add 1 second delay to the message timeout to allow the function to complete
        CompletableFuture<JsonElement> future = sendMessage("nodes", "initializeNode",
                new Object[] { nodeId, connectionTimeoutMilliseconds }, connectionTimeoutMilliseconds / 1000 + 1);
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Request all cluster attribute data for the node from the controller, the actual data will be sent via a
     * NodeDataListener event
     * 
     * @param nodeId the node ID to request data for
     * @return a future that completes when the data is requested
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<Void> requestAllNodeData(BigInteger nodeId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "requestAllData", new Object[] { nodeId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Request all cluster attribute data for all nodes for debugging purposes
     * 
     * @return a future that completes when the data is requested
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<String> getAllDataForAllNodes() {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "getAllDataForAllNodes", new Object[0]);
        return future.thenApply(jsonElement -> {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(jsonElement);
        });
    }

    /**
     * Request all cluster attribute data for a single endpoint and its children
     * 
     * @param nodeId the node ID to request data for
     * @param endpointId the endpoint ID to request data for
     * @return a future that completes when the data is requested
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<Void> requestEndpointData(BigInteger nodeId, Integer endpointId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "requestEndpointData",
                new Object[] { nodeId, endpointId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Pair a node using a pairing code, either a manual pairing code or a matter QR code (starts with MT:)
     * 
     * @param code the pairing code to pair with
     * @return a future that completes when the node is paired (or fails)
     * @throws MatterRequestException if the request fails
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
     * @param nodeId the node ID to remove
     * @return a future that completes when the node is removed
     * @throws MatterRequestException if the request fails
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
     * @param nodeId the node ID to reconnect
     * @return a future that completes when the node is reconnected
     * @throws MatterRequestException if the request fails
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
     * @param nodeId the node ID to get the pairing codes for
     * @return a future that completes when the pairing codes are retrieved
     * @throws JsonParseException when completing the future if the pairing codes cannot be deserialized
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<PairingCodes> enhancedCommissioningWindow(BigInteger nodeId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "enhancedCommissioningWindow",
                new Object[] { nodeId });
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
     * @param nodeId the node ID to disconnect
     * @return a future that completes when the node is disconnected
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<Void> disconnectNode(BigInteger nodeId) {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "disconnectNode", new Object[] { nodeId });
        return future.thenAccept(obj -> {
            // Do nothing, just to complete the future
        });
    }

    /**
     * Get the fabrics for a node, fabrics are the list of matter networks the node is joined to
     * 
     * @param nodeId the node ID to get the fabrics for
     * @return a future that completes when the fabrics are retrieved or an exception is thrown
     * @throws JsonParseException when completing the future if the fabrics cannot be deserialized
     * @throws MatterRequestException if the request fails
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
     * @param nodeId the node ID to remove the fabric from
     * @param index the index of the fabric to remove
     * @return a future that completes when the fabric is removed
     * @throws MatterRequestException if the request fails
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
     * @param nodeId the node ID to send the command to
     * @param endpointId the endpoint ID to send the command to
     * @param clusterName the cluster name to send the command to
     * @param command the command to send
     * @return a future that completes when the command is sent
     * @throws MatterRequestException if the request fails
     * @throws JsonParseException when completing the future if the command cannot be deserialized
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
     * @param nodeId the node ID to write the attribute to
     * @param endpointId the endpoint ID to write the attribute to
     * @param clusterName the cluster name to write the attribute to
     * @param attributeName the attribute name to write
     * @param value the value to write
     * @return a future that completes when the attribute is written
     * @throws MatterRequestException if the request fails
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
     * Read all attributes from a cluster
     * 
     * @param type the class type to deserialize the cluster to
     * @param nodeId the node ID to read the cluster from
     * @param endpointId the endpoint ID to read the cluster from
     * @param clusterId the cluster ID to read the cluster from
     * @return a future that completes when the cluster is read
     * @throws JsonParseException when completing the future if the cluster cannot be deserialized
     * @throws MatterRequestException if the request fails
     */
    public <T extends BaseCluster> CompletableFuture<T> readCluster(Class<T> type, BigInteger nodeId,
            Integer endpointId, Integer clusterId) {
        Object[] clusterArgs = { String.valueOf(nodeId), endpointId, clusterId };
        CompletableFuture<JsonElement> future = sendMessage("clusters", "readCluster", clusterArgs);
        return future.thenApply(obj -> {
            @Nullable
            T result = gson.fromJson(obj, type);
            if (result == null) {
                throw new JsonParseException("Could not deserialize cluster data");
            }
            return result;
        });
    }

    /**
     * Read an attribute from a cluster
     * 
     * @param nodeId the node ID to read the attribute from
     * @param endpointId the endpoint ID to read the attribute from
     * @param clusterName the cluster name to read the attribute from
     * @param attributeName the attribute name to read
     * @return a future that completes when the attribute is read
     * @throws MatterRequestException if the request fails
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
     * @return a future that completes when the session information is retrieved
     * @throws JsonParseException when completing the future if the session information cannot be deserialized
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<ActiveSessionInformation[]> getSessionInformation() {
        CompletableFuture<JsonElement> future = sendMessage("nodes", "sessionInformation", new Object[0]);
        return future.thenApply(obj -> {
            ActiveSessionInformation[] sessions = gson.fromJson(obj, ActiveSessionInformation[].class);
            return sessions == null ? new ActiveSessionInformation[0] : sessions;
        });
    }
}
