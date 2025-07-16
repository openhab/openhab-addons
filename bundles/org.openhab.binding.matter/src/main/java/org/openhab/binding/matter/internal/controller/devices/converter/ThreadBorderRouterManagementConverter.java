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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.actions.MatterOTBRActions;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralCommissioningCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadBorderRouterManagementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.ThreadDataset;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.config.core.ConfigDescriptionParameterGroupBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;

/**
 * A converter for translating {@link ThreadBorderRouterManagementCluster} events and attributes to openHAB channels and
 * back again.
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
        updateConfigDescription();
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        return Map.of();
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        logger.debug("onEvent: {} {}", message.path.attributeName, message.value);
        updateThingAttributeProperty(message.path.attributeName, message.value);
        switch (message.path.attributeName) {
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_ACTIVE_DATASET_TIMESTAMP:
                if (message.value == null || message.value.equals("null")) {
                    updateThingAttributeProperty(ACTIVE_DATASET, null);
                } else {
                    getActiveDataset().thenAccept(result -> {
                        updateThingAttributeProperty(ACTIVE_DATASET, result);
                        updateThreadConfiguration(result);
                    }).exceptionally(e -> {
                        logger.debug("Error getting active dataset after timestamp update", e);
                        updateThingAttributeProperty(ACTIVE_DATASET, null);
                        throw new CompletionException(e);
                    });
                }
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_PENDING_DATASET_TIMESTAMP:
                if (message.value == null || message.value.equals("null")) {
                    updateThingAttributeProperty(PENDING_DATASET, null);
                    // If the pending dataset timestamp is null, we need to get the active dataset as the pending moved
                    // to active
                    getActiveDataset().thenAccept(result -> {
                        updateThingAttributeProperty(ACTIVE_DATASET, result);
                        updateThreadConfiguration(result);
                    }).exceptionally(e -> {
                        logger.debug("Error getting active dataset after timestamp update", e);
                        updateThingAttributeProperty(ACTIVE_DATASET, null);
                        throw new CompletionException(e);
                    });
                } else {
                    getPendingDataset().thenAccept(result -> {
                        updateThingAttributeProperty(PENDING_DATASET, result);
                    }).exceptionally(e -> {
                        logger.debug("Error getting pending dataset after timestamp update", e);
                        updateThingAttributeProperty(PENDING_DATASET, null);
                        throw new CompletionException(e);
                    });
                }
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
                updateThreadConfiguration(result);
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

    private void updateConfigDescription() {
        List<ConfigDescriptionParameter> params = new ArrayList<>();
        List<ConfigDescriptionParameterGroup> groups = new ArrayList<>();

        groups.add(ConfigDescriptionParameterGroupBuilder
                .create(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE)
                .withLabel(handler
                        .getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_BORDER_ROUTER_OPERATIONAL_DATASET))
                .withDescription(handler
                        .getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_BORDER_ROUTER_OPERATIONAL_DATASET))
                .build());

        ConfigDescriptionParameterBuilder builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_THREAD_CHANNEL, Type.INTEGER);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_NETWORK_CHANNEL_NUMBER));
        builder.withDefault("0");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_NETWORK_CHANNEL_NUMBER));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_ALLOWED_CHANNELS,
                Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_NETWORK_ALLOWED_CHANNELS));
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_NETWORK_ALLOWED_CHANNELS));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_EXTENDED_PAN_ID,
                Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_EXTENDED_PAN_ID));
        builder.withDefault("");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_EXTENDED_PAN_ID));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_MESH_LOCAL_PREFIX,
                Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_MESH_LOCAL_PREFIX));
        builder.withDefault("0");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_MESH_LOCAL_PREFIX));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_NETWORK_NAME,
                Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_NETWORK_NAME));
        builder.withDefault("0");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_NETWORK_NAME));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_NETWORK_KEY, Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_NETWORK_KEY));
        builder.withDefault("");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_NETWORK_KEY));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_PAN_ID, Type.INTEGER);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_PAN_ID));
        builder.withDefault("0");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_PAN_ID));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_PSKC, Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_PSKC));
        builder.withDefault("0");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_PSKC));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_SECONDS, Type.TEXT);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_SECONDS));
        builder.withDefault("1");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_SECONDS));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_TICKS,
                Type.INTEGER);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_TICKS));
        builder.withDefault("0");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_TICKS));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_AUTHORITATIVE, Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_ACTIVE_TIMESTAMP_IS_AUTHORITATIVE));
        builder.withDefault("false");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_ACTIVE_TIMESTAMP_IS_AUTHORITATIVE));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_THREAD_BORDER_ROUTER_CORE);
        builder.withAdvanced(true);
        params.add(builder.build());

        groups.add(ConfigDescriptionParameterGroupBuilder.create(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY)
                .withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_THREAD_DATASET_SECURITY_POLICY))
                .withDescription(
                        handler.getTranslation(MatterBindingConstants.CONFIG_DESC_THREAD_DATASET_SECURITY_POLICY))
                .withAdvanced(true).build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_ROTATION_TIME,
                Type.INTEGER);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_ROTATION_TIME));
        builder.withDefault("672");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_ROTATION_TIME));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_OBTAIN_NETWORK_KEY,
                Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_OBTAIN_NETWORK_KEY));
        builder.withDefault("true");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_OBTAIN_NETWORK_KEY));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_NATIVE_COMMISSIONING,
                Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_NATIVE_COMMISSIONING));
        builder.withDefault("true");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_NATIVE_COMMISSIONING));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_ROUTERS, Type.BOOLEAN);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_ROUTERS));
        builder.withDefault("true");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_ROUTERS));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_EXTERNAL_COMMISSIONING,
                Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_EXTERNAL_COMMISSIONING));
        builder.withDefault("true");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_EXTERNAL_COMMISSIONING));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_THREAD_COMMERCIAL_COMMISSIONING, Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_COMMERCIAL_COMMISSIONING));
        builder.withDefault("false");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_COMMERCIAL_COMMISSIONING));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_AUTONOMOUS_ENROLLMENT,
                Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_AUTONOMOUS_ENROLLMENT));
        builder.withDefault("true");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_AUTONOMOUS_ENROLLMENT));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder
                .create(MatterBindingConstants.CONFIG_THREAD_NETWORK_KEY_PROVISIONING, Type.BOOLEAN);
        builder.withLabel(
                handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_NETWORK_KEY_PROVISIONING));
        builder.withDefault("true");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_NETWORK_KEY_PROVISIONING));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_TOBLE_LINK,
                Type.BOOLEAN);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_TO_BLE_LINK));
        builder.withDefault("true");
        builder.withDescription(handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_TO_BLE_LINK));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        builder = ConfigDescriptionParameterBuilder.create(MatterBindingConstants.CONFIG_THREAD_NON_CCM_ROUTERS,
                Type.BOOLEAN);
        builder.withLabel(handler.getTranslation(MatterBindingConstants.CONFIG_LABEL_SECURITY_POLICY_NON_CCM_ROUTERS));
        builder.withDefault("false");
        builder.withDescription(
                handler.getTranslation(MatterBindingConstants.CONFIG_DESC_SECURITY_POLICY_NON_CCM_ROUTERS));
        builder.withGroupName(MatterBindingConstants.CONFIG_GROUP_SECURITY_POLICY);
        builder.withAdvanced(true);
        params.add(builder.build());

        handler.addConfigDescription((ConfigDescriptionBuilder.create(handler.getConfigDescriptionURI())
                .withParameters(params).withParameterGroups(groups).build()));
    }

    public void updateThreadConfiguration(String hexDataset) {
        ThreadDataset dataset = ThreadDataset.fromHex(hexDataset);

        Map<String, Object> entries = new HashMap<>();
        dataset.getChannel().ifPresent(c -> entries.put(MatterBindingConstants.CONFIG_THREAD_CHANNEL, c));
        dataset.getChannelSet().ifPresent(cm -> entries.put(MatterBindingConstants.CONFIG_THREAD_ALLOWED_CHANNELS,
                cm.stream().map(Object::toString).collect(Collectors.joining(","))));
        dataset.getExtPanIdHex().ifPresent(ep -> entries.put(MatterBindingConstants.CONFIG_THREAD_EXTENDED_PAN_ID, ep));
        dataset.getMeshLocalPrefixFormatted()
                .ifPresent(mlp -> entries.put(MatterBindingConstants.CONFIG_THREAD_MESH_LOCAL_PREFIX, mlp));
        dataset.getNetworkName().ifPresent(nn -> entries.put(MatterBindingConstants.CONFIG_THREAD_NETWORK_NAME, nn));
        dataset.getNetworkKeyHex().ifPresent(nk -> entries.put(MatterBindingConstants.CONFIG_THREAD_NETWORK_KEY, nk));
        dataset.getPanId().ifPresent(pid -> entries.put(MatterBindingConstants.CONFIG_THREAD_PAN_ID, pid));
        dataset.getPskcHex().ifPresent(pskc -> entries.put(MatterBindingConstants.CONFIG_THREAD_PSKC, pskc));
        dataset.getSecurityPolicyRotation()
                .ifPresent(rt -> entries.put(MatterBindingConstants.CONFIG_THREAD_ROTATION_TIME, rt));
        dataset.getActiveTimestampObject().ifPresent(ts -> {
            entries.put(MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_SECONDS, ts.getSeconds());
            entries.put(MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_TICKS, ts.getTicks());
            entries.put(MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_AUTHORITATIVE, ts.isAuthoritative());
        });
        entries.put(MatterBindingConstants.CONFIG_THREAD_OBTAIN_NETWORK_KEY, dataset.isObtainNetworkKey());
        entries.put(MatterBindingConstants.CONFIG_THREAD_NATIVE_COMMISSIONING, dataset.isNativeCommissioning());
        entries.put(MatterBindingConstants.CONFIG_THREAD_ROUTERS, dataset.isRoutersEnabled());
        entries.put(MatterBindingConstants.CONFIG_THREAD_EXTERNAL_COMMISSIONING, dataset.isExternalCommissioning());
        entries.put(MatterBindingConstants.CONFIG_THREAD_COMMERCIAL_COMMISSIONING, dataset.isCommercialCommissioning());
        entries.put(MatterBindingConstants.CONFIG_THREAD_AUTONOMOUS_ENROLLMENT, dataset.isAutonomousEnrollment());
        entries.put(MatterBindingConstants.CONFIG_THREAD_NETWORK_KEY_PROVISIONING, dataset.isNetworkKeyProvisioning());
        entries.put(MatterBindingConstants.CONFIG_THREAD_TOBLE_LINK, dataset.isToBleLink());
        entries.put(MatterBindingConstants.CONFIG_THREAD_NON_CCM_ROUTERS, dataset.isNonCcmRouters());
        handler.updateConfiguration(entries);
        logger.debug("Updated thread configuration: {}", dataset.toJson());
    }

    public ThreadDataset datasetFromConfiguration() {
        Configuration config = handler.getThing().getConfiguration();
        ThreadDataset dataset = new ThreadDataset();
        ThreadDataset.ThreadTimestamp ts = new ThreadDataset.ThreadTimestamp(1, 0, false);
        config.getProperties().forEach((key, value) -> {
            switch (key) {
                case MatterBindingConstants.CONFIG_THREAD_CHANNEL ->
                    dataset.setChannel(new BigDecimal(value.toString()).intValue());
                case MatterBindingConstants.CONFIG_THREAD_ALLOWED_CHANNELS ->
                    dataset.setChannelSet(new java.util.HashSet<>(
                            Arrays.stream(value.toString().split(",")).map(Integer::parseInt).toList()));
                case MatterBindingConstants.CONFIG_THREAD_EXTENDED_PAN_ID -> dataset.setExtPanId(value.toString());
                case MatterBindingConstants.CONFIG_THREAD_MESH_LOCAL_PREFIX ->
                    dataset.setMeshLocalPrefixFormatted(value.toString());
                case MatterBindingConstants.CONFIG_THREAD_NETWORK_NAME -> dataset.setNetworkName(value.toString());
                case MatterBindingConstants.CONFIG_THREAD_NETWORK_KEY -> dataset.setNetworkKey(value.toString());
                case MatterBindingConstants.CONFIG_THREAD_PAN_ID ->
                    dataset.setPanId(new BigDecimal(value.toString()).intValue());
                case MatterBindingConstants.CONFIG_THREAD_PSKC -> dataset.setPskc(value.toString());
                case MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_SECONDS ->
                    ts.setSeconds(new BigDecimal(value.toString()).longValue());
                case MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_TICKS ->
                    ts.setTicks(new BigDecimal(value.toString()).intValue());
                case MatterBindingConstants.CONFIG_THREAD_ACTIVE_TIMESTAMP_AUTHORITATIVE ->
                    ts.setAuthoritative(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_DELAY_TIMER ->
                    dataset.setDelayTimer(new BigDecimal(value.toString()).longValue());
                case MatterBindingConstants.CONFIG_THREAD_ROTATION_TIME ->
                    dataset.setSecurityPolicyRotation(new BigDecimal(value.toString()).intValue());
                case MatterBindingConstants.CONFIG_THREAD_OBTAIN_NETWORK_KEY ->
                    dataset.setObtainNetworkKey(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_NATIVE_COMMISSIONING ->
                    dataset.setNativeCommissioning(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_ROUTERS ->
                    dataset.setRoutersEnabled(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_EXTERNAL_COMMISSIONING ->
                    dataset.setExternalCommissioning(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_COMMERCIAL_COMMISSIONING ->
                    dataset.setCommercialCommissioning(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_AUTONOMOUS_ENROLLMENT ->
                    dataset.setAutonomousEnrollment(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_NETWORK_KEY_PROVISIONING ->
                    dataset.setNetworkKeyProvisioning(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_TOBLE_LINK ->
                    dataset.setToBleLink(Boolean.parseBoolean(value.toString()));
                case MatterBindingConstants.CONFIG_THREAD_NON_CCM_ROUTERS ->
                    dataset.setNonCcmRouters(Boolean.parseBoolean(value.toString()));
                default -> logger.debug("Unknown configuration property: {}", key);
            }
            dataset.setActiveTimestamp(ts);
        });
        return dataset;
    }
}
