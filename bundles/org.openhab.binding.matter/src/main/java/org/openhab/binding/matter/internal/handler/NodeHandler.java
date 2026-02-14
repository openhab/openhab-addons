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
package org.openhab.binding.matter.internal.handler;

import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_TYPE_ENDPOINT;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.actions.MatterNodeActions;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.Node;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BridgedDeviceBasicInformationCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OtaSoftwareUpdateRequestorCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OtaSoftwareUpdateRequestorCluster.UpdateStateEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.OtaUpdateAvailableMessage;
import org.openhab.binding.matter.internal.client.dto.ws.OtaUpdateInfo;
import org.openhab.binding.matter.internal.config.NodeConfiguration;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.controller.devices.converter.OtaSoftwareUpdateRequestorConverter;
import org.openhab.binding.matter.internal.controller.devices.types.DeviceType;
import org.openhab.binding.matter.internal.discovery.MatterDiscoveryService;
import org.openhab.binding.matter.internal.util.MatterUIDUtils;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.firmware.Firmware;
import org.openhab.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.openhab.core.thing.binding.firmware.ProgressCallback;
import org.openhab.core.thing.binding.firmware.ProgressStep;

/**
 * The {@link NodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * This class is used to handle a Matter Node, an IPV6 based device which is made up of a collection of endpoints.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class NodeHandler extends MatterBaseThingHandler implements BridgeHandler, FirmwareUpdateHandler {
    protected BigInteger nodeId = BigInteger.valueOf(0);
    private Integer pollInterval = 0;
    private Map<Integer, EndpointHandler> bridgedEndpoints = new ConcurrentHashMap<>();
    private @Nullable ProgressCallback progressCallback;

    public NodeHandler(Bridge bridge, BaseThingHandlerFactory thingHandlerFactory,
            MatterStateDescriptionOptionProvider stateDescriptionProvider,
            MatterChannelTypeProvider channelGroupTypeProvider,
            MatterConfigDescriptionProvider configDescriptionProvider, TranslationService translationService) {
        super(bridge, thingHandlerFactory, stateDescriptionProvider, channelGroupTypeProvider,
                configDescriptionProvider, translationService);
    }

    @Override
    public void initialize() {
        NodeConfiguration config = getConfigAs(NodeConfiguration.class);
        nodeId = new BigInteger(config.nodeId);
        pollInterval = config.pollInterval;
        logger.debug("initialize node {}", nodeId);
        super.initialize();
    }

    @Override
    public BigInteger getNodeId() {
        return nodeId;
    }

    @Override
    public ThingTypeUID getDynamicThingTypeUID() {
        return MatterUIDUtils.nodeThingTypeUID(getNodeId());
    }

    @Override
    public boolean isBridgeType() {
        return true;
    }

    @Override
    public Integer getPollInterval() {
        return pollInterval;
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    @Override
    protected ThingBuilder editThing() {
        return BridgeBuilder.create(getDynamicThingTypeUID(), getThing().getUID()).withBridge(getThing().getBridgeUID())
                .withChannels(getThing().getChannels()).withConfiguration(getThing().getConfiguration())
                .withLabel(getThing().getLabel()).withLocation(getThing().getLocation())
                .withProperties(getThing().getProperties());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MatterNodeActions.class);
    }

    @Override
    protected boolean shouldAddEndpoint(Endpoint endpoint) {
        if (endpoint.clusters.containsKey(BridgedDeviceBasicInformationCluster.CLUSTER_NAME)) {
            updateBridgeEndpoint(endpoint);
            return false;
        }
        return true;
    }

    @Override
    public void handleRemoval() {
        ControllerHandler bridge = controllerHandler();
        if (bridge != null) {
            bridge.removeNode(nodeId);
        }
        super.handleRemoval();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("childHandlerInitialized ready {}", childHandler);
        if (childHandler instanceof EndpointHandler handler) {
            bridgedEndpoints.put(handler.getEndpointId(), handler);
            // if the node is already online, update the endpoint, otherwise wait for the node to come online which will
            // update it.
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                ControllerHandler ch = controllerHandler();
                if (ch != null) {
                    ch.updateEndpoint(getNodeId(), handler.getEndpointId());
                }
            }
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof EndpointHandler handler) {
            bridgedEndpoints.entrySet().removeIf(entry -> entry.getValue().equals(handler));
        }
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        EndpointHandler endpointHandler = bridgedEndpoints.get(message.path.endpointId);
        if (endpointHandler != null) {
            endpointHandler.onEvent(message);
        } else {
            super.onEvent(message);
            if (message.path.clusterId == OtaSoftwareUpdateRequestorCluster.CLUSTER_ID) {
                updateOTAStatus(false);
            }
        }
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
        EndpointHandler endpointHandler = bridgedEndpoints.get(message.path.endpointId);
        if (endpointHandler != null) {
            endpointHandler.onEvent(message);
        } else {
            super.onEvent(message);
        }
    }

    @Override
    protected synchronized void updateBaseEndpoint(Endpoint endpoint) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Setting Online {}", getNodeId());
            updateStatus(ThingStatus.ONLINE);
        }
        super.updateBaseEndpoint(endpoint);
        updateOTAStatus(false);
    }

    @Override
    protected void handleOtaUpdateAvailable(OtaUpdateAvailableMessage message) {
        logger.debug("OtaUpdateAvailableMessage onEvent: node {} is {}", message.nodeId, message);
        handleOtaUpdateAvailable(true);
    }

    @Override
    public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
        MatterControllerClient client = getClient();
        if (client != null) {
            try {
                // Start the firmware update, we will be called back when this starts and set the thing status to
                // firmware updating
                client.otaStartUpdate(getNodeId()).get();
                progressCallback.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.UPDATING);
                this.progressCallback = progressCallback;
            } catch (InterruptedException | ExecutionException e) {
                logger.debug("Failed to start firmware update for device {}", getNodeId(), e);
                progressCallback.failed(MatterBindingConstants.OTA_FIRMWARE_UPDATE_FAILED, e.getLocalizedMessage());
            }
        } else {
            progressCallback.failed(MatterBindingConstants.OTA_FIRMWARE_UPDATE_FAILED);
        }
    }

    /**
     * Cancels a previous started firmware update.
     */
    @Override
    public void cancel() {
        try {
            cancelOTAUpdate().get();
            ProgressCallback progressCallback = this.progressCallback;
            if (progressCallback != null) {
                progressCallback.canceled();
            }
            progressCallback = null;
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Failed to cancel firmware update for device {}", getNodeId(), e);
        }
    }

    /**
     * Returns true, if this firmware update handler is in a state in which the firmware update can be executed,
     * otherwise false (e.g. the thing is {@link ThingStatus#OFFLINE} or its status detail is already
     * {@link ThingStatusDetail#FIRMWARE_UPDATING}.)
     *
     * @return true, if this firmware update handler is in a state in which the firmware update can be executed,
     *         otherwise false
     */
    @Override
    public boolean isUpdateExecutable() {
        return getThing().getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.FIRMWARE_UPDATING;
    }

    public void updateNode(Node node) {
        updateRootProperties(node.rootEndpoint);
        updateBaseEndpoint(node.rootEndpoint);
        checkForOTAUpdate();
    }

    public boolean hasBridgedEndpoints() {
        return !bridgedEndpoints.isEmpty();
    }

    private void updateBridgeEndpoint(Endpoint endpoint) {
        EndpointHandler handler = bridgedEndpoints.get(endpoint.number);
        if (handler != null) {
            updateBridgeEndpointMap(endpoint, handler);
            handler.updateBaseEndpoint(endpoint);
        } else {
            discoverChildBridge(endpoint);
        }
    }

    private void updateBridgeEndpointMap(Endpoint endpoint, final EndpointHandler handler) {
        bridgedEndpoints.put(endpoint.number, handler);
        endpoint.children.forEach(e -> updateBridgeEndpointMap(e, handler));
    }

    private void discoverChildBridge(Endpoint endpoint) {
        logger.debug("discoverChildBridge {}", endpoint.number);
        ControllerHandler controller = controllerHandler();
        if (controller != null) {
            MatterDiscoveryService discoveryService = controller.getDiscoveryService();
            if (discoveryService != null) {
                ThingUID bridgeUID = getThing().getUID();
                ThingUID thingUID = new ThingUID(THING_TYPE_ENDPOINT, bridgeUID, endpoint.number.toString());
                discoveryService.discoverBridgeEndpoint(thingUID, bridgeUID, endpoint);
            }
        }
    }

    /**
     * Check for OTA updates for the node
     * 
     * @return a future that completes with the update information, or null if no update is available
     * @throws JsonParseException when completing the future if the update info cannot be deserialized
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<@Nullable OtaUpdateInfo> checkForOTAUpdate() {
        MatterControllerClient client = getClient();
        if (client != null) {
            return client.otaQueryForUpdates(getNodeId(), true).whenComplete((updateInfo, e) -> {
                if (e != null) {
                    logger.debug("Failed to check for firmware update for device {}", getNodeId(), e);
                } else {
                    handleOtaUpdateAvailable(updateInfo != null);
                }
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Cancel the OTA update for the node
     * 
     * @return a future that completes when the update is cancelled
     * @throws MatterRequestException if the request fails
     */
    public CompletableFuture<Void> cancelOTAUpdate() {
        MatterControllerClient client = getClient();
        if (client != null) {
            return client.otaCancelUpdate(getNodeId()).whenComplete((result, e) -> {
                if (e != null) {
                    logger.debug("Failed to cancel firmware update for device {}", getNodeId(), e);
                } else {
                    updateOTAStatus(true);
                }
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    private void updateOTAStatus(boolean canceled) {
        DeviceType deviceType = devices.get(0);
        if (deviceType != null && deviceType.getClusterConverters().get(
                OtaSoftwareUpdateRequestorCluster.CLUSTER_ID) instanceof OtaSoftwareUpdateRequestorConverter converter) {
            if (canceled) {
                converter.resetUpdateState();
            }
            handleOtaUpdateAvailable(converter.isUpdateAvailable());
            handleOtaStateTransition(converter.getLastUpdateState());
            handleOtaUpdateStateProgress(converter.getLastUpdateStateProgress(), converter.getLastUpdateState());
        }
    }

    private void handleOtaUpdateAvailable(Boolean updateAvailable) {
        logger.debug("Update Available: {} for node {}", updateAvailable, getNodeId());

        // Update the OTA converter channel
        DeviceType deviceType = devices.get(0);
        if (deviceType != null && deviceType.getClusterConverters().get(
                OtaSoftwareUpdateRequestorCluster.CLUSTER_ID) instanceof OtaSoftwareUpdateRequestorConverter converter) {
            converter.setUpdateAvailable(updateAvailable);
        }

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    updateAvailable
                            ? translationService.getTranslation(
                                    MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_UPDATE_AVAILABLE)
                            : null);
        }
    }

    private void handleOtaStateTransition(UpdateStateEnum stateTransition) {
        logger.debug("OTA State Transition: {} for node {}", stateTransition, getNodeId());
        ThingStatus status = getThing().getStatus();
        ProgressCallback progressCallback = this.progressCallback;
        switch (stateTransition) {
            case UNKNOWN:
            case IDLE:
                // we are idle, so the update is complete if we were updating
                if (progressCallback != null) {
                    progressCallback.success();
                    this.progressCallback = null;
                }
            case QUERYING:
            case DELAYED_ON_QUERY:
                break;
            case DOWNLOADING:
                updateStatus(status, ThingStatusDetail.FIRMWARE_UPDATING, translationService
                        .getTranslation(MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_DOWNLOADING));
                if (progressCallback != null) {
                    // DOWNLOADING
                    progressCallback.next();
                }
                break;
            case APPLYING:
                // we are applying, go offline as the device will reboot if it succeeds or rolls back, and come back
                // online when it is done
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.FIRMWARE_UPDATING, translationService
                        .getTranslation(MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_APPLYING));
                if (progressCallback != null) {
                    // UPDATING
                    progressCallback.update(100);
                    progressCallback.next();
                }
                break;
            case DELAYED_ON_APPLY:
                updateStatus(status, ThingStatusDetail.FIRMWARE_UPDATING, translationService
                        .getTranslation(MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_DELAYED_ON_APPLY));
                break;
            case ROLLING_BACK:
                updateStatus(status, ThingStatusDetail.FIRMWARE_UPDATING, translationService
                        .getTranslation(MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_ROLLING_BACK));
                if (progressCallback != null) {
                    progressCallback.failed(MatterBindingConstants.OTA_FIRMWARE_UPDATE_FAILED_ROLLING_BACK);
                }
                break;
            case DELAYED_ON_USER_CONSENT:
                updateStatus(status, ThingStatusDetail.FIRMWARE_UPDATING, translationService
                        .getTranslation(MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_DELAYED_ON_USER_CONSENT));
                if (progressCallback != null) {
                    progressCallback.failed(MatterBindingConstants.OTA_FIRMWARE_UPDATE_FAILED_DELAYED_ON_USER_CONSENT);
                }
                break;
        }
    }

    private void handleOtaUpdateStateProgress(int progress, UpdateStateEnum stateTransition) {
        logger.debug("OTA Update State Progress: {} for node {}", progress, getNodeId());
        ThingStatus status = getThing().getStatus();
        switch (stateTransition) {
            case DOWNLOADING:
                updateStatus(status, ThingStatusDetail.FIRMWARE_UPDATING, translationService.getTranslation(
                        MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_DOWNLOADING_WITH_PROGRESS, progress));
                if (progressCallback != null) {
                    progressCallback.update(progress);
                }
                break;
            case APPLYING:
                updateStatus(status, ThingStatusDetail.FIRMWARE_UPDATING, translationService
                        .getTranslation(MatterBindingConstants.THING_STATUS_DETAIL_FIRMWARE_APPLYING));
                break;
            default:
                break;
        }
    }
}
