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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_THREADVERSION;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_THREADBORDERROUTERMANAGEMENT_THREADVERSION;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_NUMBER;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_STRING;
import static org.openhab.binding.matter.internal.MatterBindingConstants.ITEM_TYPE_SWITCH;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.actions.MatterOTBRActions;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralCommissioningCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThreadBorderRouterManagementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 * The {@link ThreadBorderRouterManagementConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThreadBorderRouterManagementConverter extends GenericConverter<ThreadBorderRouterManagementCluster> {
    private static final int FAIL_SAFE_TIMEOUT_SECONDS = 30;
    private final AtomicBoolean activeDatasetPending = new AtomicBoolean(false);
    private final AtomicBoolean pendingDatasetPending = new AtomicBoolean(false);

    public ThreadBorderRouterManagementConverter(ThreadBorderRouterManagementCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
        handler.registerService(MatterOTBRActions.class);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID thingUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        Channel channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME),
                        ITEM_TYPE_STRING)
                .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID),
                        ITEM_TYPE_STRING)
                .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION),
                        ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_THREADVERSION)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_THREADVERSION)).build();
        StateDescription threadVersionDesc = StateDescriptionFragmentBuilder.create().withPattern("%d").build()
                .toStateDescription();
        channels.put(channel, threadVersionDesc);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED),
                        ITEM_TYPE_SWITCH)
                .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED)).build();
        channels.put(channel, null);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP),
                        ITEM_TYPE_NUMBER)
                .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP)).build();
        StateDescription timestampDesc = StateDescriptionFragmentBuilder.create().withPattern("%d").build()
                .toStateDescription();
        channels.put(channel, timestampDesc);

        channel = ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET),
                        ITEM_TYPE_STRING)
                .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET)
                .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET)).build();
        channels.put(channel, null);

        if (initializingCluster.featureMap.panChange) {
            channel = ChannelBuilder
                    .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP),
                            ITEM_TYPE_NUMBER)
                    .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP)
                    .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP)).build();
            channels.put(channel, timestampDesc);

            channel = ChannelBuilder
                    .create(new ChannelUID(thingUID, CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET),
                            ITEM_TYPE_STRING)
                    .withType(CHANNEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET)
                    .withLabel(formatLabel(CHANNEL_LABEL_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET)).build();
            channels.put(channel, null);
        }
        return channels;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_BORDER_ROUTER_NAME:
                if (message.value instanceof String string) {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME, new StringType(string));
                }
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_BORDER_AGENT_ID:
                if (message.value instanceof String string) {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID, new StringType(string));
                }
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_THREAD_VERSION:
                if (message.value instanceof Number number) {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION, new DecimalType(number));
                }
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_INTERFACE_ENABLED:
                if (message.value instanceof Boolean bool) {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED,
                            bool ? OnOffType.ON : OnOffType.OFF);
                }
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_ACTIVE_DATASET_TIMESTAMP:
                if (message.value instanceof BigInteger bigInt) {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP,
                            new DecimalType(bigInt));
                    getActiveDataset();
                } else {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET, UnDefType.NULL);
                }
                break;
            case ThreadBorderRouterManagementCluster.ATTRIBUTE_PENDING_DATASET_TIMESTAMP:
                if (message.value instanceof BigInteger bigInt) {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP,
                            new DecimalType(bigInt));
                    getPendingDataset();
                } else {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET, UnDefType.NULL);
                }
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERROUTERNAME,
                initializingCluster.borderRouterName != null ? new StringType(initializingCluster.borderRouterName)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_BORDERAGENTID,
                initializingCluster.borderAgentId != null ? new StringType(initializingCluster.borderAgentId.toString())
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_THREADVERSION,
                initializingCluster.threadVersion != null ? new DecimalType(initializingCluster.threadVersion)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_INTERFACEENABLED,
                initializingCluster.interfaceEnabled != null
                        ? (initializingCluster.interfaceEnabled ? OnOffType.ON : OnOffType.OFF)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASETTIMESTAMP,
                initializingCluster.activeDatasetTimestamp != null
                        ? new DecimalType(initializingCluster.activeDatasetTimestamp)
                        : UnDefType.NULL);
        updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASETTIMESTAMP,
                initializingCluster.pendingDatasetTimestamp != null
                        ? new DecimalType(initializingCluster.pendingDatasetTimestamp)
                        : UnDefType.NULL);

        if (initializingCluster.activeDatasetTimestamp != null) {
            getActiveDataset();
        } else {
            updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET, UnDefType.NULL);
        }
        if (initializingCluster.pendingDatasetTimestamp != null) {
            getPendingDataset();
        } else {
            updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET, UnDefType.NULL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final int rootEndpoint = 0;
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET:
                if (command instanceof StringType stringCommand) {
                    // first we need to arm the fail safe on the ROOT endpoint which tells the device a destructive
                    // change is being made
                    activeDatasetPending.set(true);
                    handler.sendClusterCommand(rootEndpoint, GeneralCommissioningCluster.CLUSTER_NAME,
                            GeneralCommissioningCluster.armFailSafe(FAIL_SAFE_TIMEOUT_SECONDS, BigInteger.ZERO))
                            .thenAccept(result -> {
                                // When the device acknowledges the arm fail safe we can set the active dataset
                                handler.sendClusterCommand(endpointNumber,
                                        ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                                        ThreadBorderRouterManagementCluster.setActiveDatasetRequest(
                                                new BaseCluster.OctetString(stringCommand.toString()), null))
                                        .thenAccept(result2 -> {
                                            logger.debug("Active dataset set");
                                            // When the device acknowledges the active dataset we can complete the
                                            // commissioning process
                                            handler.sendClusterCommand(rootEndpoint,
                                                    GeneralCommissioningCluster.CLUSTER_NAME,
                                                    GeneralCommissioningCluster.commissioningComplete())
                                                    .thenAccept(result3 -> {
                                                        logger.debug("Commissioning complete");
                                                    }).exceptionally(e -> {
                                                        logger.error("Error commissioning complete", e);
                                                        return null;
                                                    }).whenComplete((r, e) -> {
                                                        activeDatasetPending.set(false);
                                                    });
                                        }).exceptionally(e -> {
                                            logger.error("Error setting active dataset", e);
                                            activeDatasetPending.set(false);
                                            return null;
                                        });
                            }).exceptionally(e -> {
                                logger.debug("Error sending arm fail safe", e);
                                activeDatasetPending.set(false);
                                return null;
                            });
                }
                break;
            case CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET:
                if (command instanceof StringType stringCommand) {
                    pendingDatasetPending.set(true);
                    handler.sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                            ThreadBorderRouterManagementCluster
                                    .setPendingDatasetRequest(new BaseCluster.OctetString(stringCommand.toString())))
                            .exceptionally(e -> {
                                logger.error("Error setting pending dataset", e);
                                return new JsonObject();
                            }).whenComplete((r, e) -> {
                                pendingDatasetPending.set(false);
                            });
                }
                break;
        }
    }

    private void getActiveDataset() {
        if (activeDatasetPending.get()) {
            return;
        }
        activeDatasetPending.set(true);
        handler.sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                ThreadBorderRouterManagementCluster.getActiveDatasetRequest()).thenAccept(result -> {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_ACTIVEDATASET,
                            new StringType(result.getAsJsonObject().get("dataset").getAsString()));
                }).exceptionally(e -> {
                    logger.debug("Error getting active dataset", e);
                    return null;
                }).whenComplete((r, e) -> {
                    activeDatasetPending.set(false);
                });
    }

    private void getPendingDataset() {
        if (pendingDatasetPending.get()) {
            return;
        }
        pendingDatasetPending.set(true);
        handler.sendClusterCommand(endpointNumber, ThreadBorderRouterManagementCluster.CLUSTER_NAME,
                ThreadBorderRouterManagementCluster.getPendingDatasetRequest()).thenAccept(result -> {
                    updateState(CHANNEL_ID_THREADBORDERROUTERMANAGEMENT_PENDINGDATASET,
                            new StringType(result.getAsJsonObject().get("dataset").getAsString()));
                }).exceptionally(e -> {
                    logger.debug("Error getting pending dataset", e);
                    return null;
                }).whenComplete((r, e) -> {
                    pendingDatasetPending.set(false);
                });
    }
}
