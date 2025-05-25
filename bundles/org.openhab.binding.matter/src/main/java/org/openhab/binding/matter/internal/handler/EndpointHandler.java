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
package org.openhab.binding.matter.internal.handler;

import static org.openhab.binding.matter.internal.MatterBindingConstants.THING_STATUS_DETAIL_ENDPOINT_THING_NOT_REACHABLE;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.client.dto.Endpoint;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BridgedDeviceBasicInformationCluster;
import org.openhab.binding.matter.internal.config.EndpointConfiguration;
import org.openhab.binding.matter.internal.util.MatterUIDUtils;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * The {@link EndpointHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * This class is used for handling endpoints that are "bridged" devices, although it could be used to handle any
 * endpoint(s) if needed, otherwise Endpoints are generally managed as channel groups on a Node Thing
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class EndpointHandler extends MatterBaseThingHandler {
    private Integer endpointId = 0;
    private Integer pollInterval = 0;

    public EndpointHandler(Thing thing, BaseThingHandlerFactory thingHandlerFactory,
            MatterStateDescriptionOptionProvider stateDescriptionProvider,
            MatterChannelTypeProvider channelGroupTypeProvider,
            MatterConfigDescriptionProvider configDescriptionProvider, TranslationService translationService) {
        super(thing, thingHandlerFactory, stateDescriptionProvider, channelGroupTypeProvider, configDescriptionProvider,
                translationService);
    }

    @Override
    public void initialize() {
        EndpointConfiguration config = getConfigAs(EndpointConfiguration.class);
        endpointId = config.endpointId;
        pollInterval = config.pollInterval;
        logger.debug("initialize bridge endpoint {}", config.endpointId);
        super.initialize();
    }

    @Override
    public BigInteger getNodeId() {
        NodeHandler nodeHandler = nodeHandler();
        if (nodeHandler != null) {
            return nodeHandler.getNodeId();
        }
        throw new IllegalStateException("Could not access parent bridge!");
    }

    @Override
    public ThingTypeUID getDynamicThingTypeUID() {
        return MatterUIDUtils.endpointThingTypeUID(getNodeId(), endpointId);
    }

    @Override
    public boolean isBridgeType() {
        return false;
    }

    @Override
    public Integer getPollInterval() {
        return pollInterval;
    }

    @Override
    protected void updateBaseEndpoint(Endpoint endpoint) {
        boolean reachable = true;
        BaseCluster basicInfoObject = endpoint.clusters.get(BridgedDeviceBasicInformationCluster.CLUSTER_NAME);
        if (basicInfoObject != null) {
            BridgedDeviceBasicInformationCluster basicInfo = (BridgedDeviceBasicInformationCluster) basicInfoObject;
            if (basicInfo.reachable != null) {
                reachable = basicInfo.reachable;
            }
        }
        if (reachable) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                logger.debug("Setting Online {}", endpointId);
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    getTranslation(THING_STATUS_DETAIL_ENDPOINT_THING_NOT_REACHABLE));
        }
        updateRootProperties(endpoint);
        super.updateBaseEndpoint(endpoint);
    }

    public Integer getEndpointId() {
        return endpointId;
    }

    private @Nullable NodeHandler nodeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof NodeHandler nodeHandler) {
                return nodeHandler;
            }
        }
        return null;
    }
}
