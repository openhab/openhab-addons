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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.actions.MatterOTBRActions;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralCommissioningCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadBorderRouterManagementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;

/**
 * The {@link ThreadBorderRouterManagementConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThreadBorderRouterManagementConverter extends GenericConverter<ThreadBorderRouterManagementCluster> {
    private static final int FAIL_SAFE_TIMEOUT_SECONDS = 30;
    private static final int ROOT_ENDPOINT = 0;
    private static final String ACTIVE_DATASET = "activeDataset";
    private static final String PENDING_DATASET = "pendingDataset";
    private final AtomicBoolean activeDatasetPending = new AtomicBoolean(false);
    private final AtomicBoolean pendingDatasetPending = new AtomicBoolean(false);

    public ThreadBorderRouterManagementConverter(ThreadBorderRouterManagementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        handler.registerService(MatterOTBRActions.class);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID thingUID) {
        return Collections.emptyMap();
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        updateThingAttributeProperty(message.path.attributeName, message.value);
        switch (message.path.attributeName) {
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_ACTIVE_DATASET_TIMESTAMP:
                getActiveDataset().thenAccept(result -> {
                    updateThingAttributeProperty(ACTIVE_DATASET, result);
                }).exceptionally(e -> {
                    logger.debug("Error getting active dataset after timestamp update", e);
                    updateThingAttributeProperty(ACTIVE_DATASET, null);
                    throw new CompletionException(e);
                });
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_PENDING_DATASET_TIMESTAMP:
                getPendingDataset().thenAccept(result -> {
                    updateThingAttributeProperty(PENDING_DATASET, result);
                }).exceptionally(e -> {
                    logger.debug("Error getting pending dataset after timestamp update", e);
                    updateThingAttributeProperty(PENDING_DATASET, null);
                    throw new CompletionException(e);
                });
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateThingAttributeProperty(ThreadBorderRouterManagementCluster.ATTRIBUTE_BORDER_AGENT_ID,
                initializingCluster.borderAgentId);
        updateThingAttributeProperty(ThreadBorderRouterManagementCluster.ATTRIBUTE_BORDER_ROUTER_NAME,
                initializingCluster.borderRouterName);
        updateThingAttributeProperty(ThreadBorderRouterManagementCluster.ATTRIBUTE_THREAD_VERSION,
                initializingCluster.threadVersion);
        updateThingAttributeProperty(ThreadBorderRouterManagementCluster.ATTRIBUTE_INTERFACE_ENABLED,
                initializingCluster.interfaceEnabled);
        updateThingAttributeProperty(ThreadBorderRouterManagementCluster.ATTRIBUTE_ACTIVE_DATASET_TIMESTAMP,
                initializingCluster.activeDatasetTimestamp);
        updateThingAttributeProperty(ThreadBorderRouterManagementCluster.ATTRIBUTE_PENDING_DATASET_TIMESTAMP,
                initializingCluster.pendingDatasetTimestamp);
        if (initializingCluster.activeDatasetTimestamp != null) {
            getActiveDataset().thenAccept(result -> {
                updateThingAttributeProperty(ACTIVE_DATASET, result);
            }).exceptionally(e -> {
                logger.debug("Error getting active dataset during init", e);
                updateThingAttributeProperty(ACTIVE_DATASET, null);
                return null;
            });
        } else {
            updateThingAttributeProperty(ACTIVE_DATASET, null);
        }
        if (initializingCluster.pendingDatasetTimestamp != null) {
            getPendingDataset().thenAccept(result -> {
                updateThingAttributeProperty(PENDING_DATASET, result);
            }).exceptionally(e -> {
                logger.debug("Error getting pending dataset during init", e);
                updateThingAttributeProperty(PENDING_DATASET, null);
                return null;
            });
        } else {
            updateThingAttributeProperty(PENDING_DATASET, null);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public synchronized CompletableFuture<String> getActiveDataset() {
        if (activeDatasetPending.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Active dataset request already pending"));
        }
        activeDatasetPending.set(true);
        return handler.sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                ThreadBorderRouterManagementCluster.getActiveDatasetRequest()).thenApply(result -> {
                    return result.getAsJsonObject().get("dataset").getAsString();
                }).exceptionally(e -> {
                    logger.debug("Error getting active dataset", e);
                    throw new CompletionException(e);
                }).whenComplete((r, e) -> {
                    activeDatasetPending.set(false);
                });
    }

    public synchronized CompletableFuture<String> getPendingDataset() {
        if (pendingDatasetPending.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Pending dataset request already pending"));
        }
        pendingDatasetPending.set(true);
        return handler.sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                ThreadBorderRouterManagementCluster.getPendingDatasetRequest()).thenApply(result -> {
                    return result.getAsJsonObject().get("dataset").getAsString();
                }).exceptionally(e -> {
                    logger.debug("Error getting pending dataset", e);
                    throw new CompletionException(e);
                }).whenComplete((r, e) -> {
                    pendingDatasetPending.set(false);
                });
    }

    public synchronized CompletableFuture<Void> setActiveDataset(String dataset) {
        if (activeDatasetPending.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Active dataset request already pending"));
        }
        activeDatasetPending.set(true);

        return handler
                .sendClusterCommand(ROOT_ENDPOINT, GeneralCommissioningCluster.CLUSTER_NAME,
                        GeneralCommissioningCluster.armFailSafe(FAIL_SAFE_TIMEOUT_SECONDS, BigInteger.ZERO))
                .thenCompose(result -> {
                    // When the device acknowledges the arm fail safe we can set the active dataset
                    return handler.sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                            ThreadBorderRouterManagementCluster
                                    .setActiveDatasetRequest(new BaseCluster.OctetString(dataset), null));
                }).thenCompose(result2 -> {
                    logger.debug("Active dataset set");
                    // When the device acknowledges the active dataset we can complete arm fail safe
                    return handler.sendClusterCommand(ROOT_ENDPOINT, GeneralCommissioningCluster.CLUSTER_NAME,
                            GeneralCommissioningCluster.commissioningComplete());
                }).<Void> thenApply(result3 -> {
                    logger.debug("operation dataset set confirmed");
                    return Void.TYPE.cast(null);
                }).exceptionally(e -> {
                    logger.error("Error in setActiveDataset", e);
                    throw new CompletionException(e);
                }).whenComplete((r, e) -> {
                    activeDatasetPending.set(false);
                });
    }

    public synchronized CompletableFuture<Void> setPendingDataset(String dataset) {
        if (pendingDatasetPending.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Pending dataset request already pending"));
        }
        pendingDatasetPending.set(true);

        return handler
                .sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                        ThreadBorderRouterManagementCluster
                                .setPendingDatasetRequest(new BaseCluster.OctetString(dataset)))
                .<Void> thenApply(result -> {
                    logger.debug("Pending dataset set");
                    return Void.TYPE.cast(null);
                }).exceptionally(e -> {
                    logger.error("Error setting pending dataset", e);
                    throw new CompletionException(e);
                }).whenComplete((r, e) -> {
                    pendingDatasetPending.set(false);
                });
    }
}
