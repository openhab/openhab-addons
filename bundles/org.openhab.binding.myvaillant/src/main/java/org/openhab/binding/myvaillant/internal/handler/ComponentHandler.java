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
package org.openhab.binding.myvaillant.internal.handler;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.PROPERTY_COMPONENT_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for the Things of a system's components (zone, circuit, hot water, device). The system bridge reads the whole
 * system and passes it to {@link #updateData(SystemData)}; commands are sent through the system's API client.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public abstract class ComponentHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ComponentHandler.class);

    private volatile boolean active;
    private volatile @Nullable SystemData data;

    protected ComponentHandler(Thing thing) {
        super(thing);
    }

    /**
     * Value of the representation property of zones, circuits and hot water. Their index is only unique within a
     * system, so it cannot identify the Thing.
     */
    public static String componentId(String systemId, int index) {
        return systemId + "-" + index;
    }

    /**
     * Reads the configuration.
     *
     * @return the status description if the configuration is invalid, else {@code null}
     */
    protected abstract @Nullable String readConfiguration();

    /**
     * @return the value of the representation property, {@code null} if the configuration identifies the Thing
     * @see #componentId(String, int)
     */
    protected @Nullable String getComponentId(String systemId) {
        return null;
    }

    /** Whether the system (still) has this component */
    protected abstract boolean isPresent(SystemData systemData);

    /** Updates all channels from the data of the system */
    protected abstract void updateChannels(SystemData systemData);

    /**
     * Sends a command to the API. Implementations update the channel state if the API accepted the command, the
     * system is updated afterwards.
     *
     * @return whether a request was sent
     */
    protected abstract boolean sendCommand(String channelId, Command command, MyVaillantApi api, SystemData systemData,
            SystemHandler systemHandler) throws MyVaillantException;

    @Override
    public void initialize() {
        String error = readConfiguration();
        if (error != null) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, error);
            return;
        }
        active = true;
        Bridge bridge = getBridge();
        SystemHandler systemHandler = getSystemHandler();
        if (bridge == null || systemHandler == null) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "@text/offline.conf-error.no-system-bridge");
            return;
        }
        SystemData systemData = systemHandler.getData();
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        } else if (systemData != null) {
            updateData(systemData);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        active = false;
        data = null;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (!active) {
            return;
        }
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
            return;
        }
        SystemHandler systemHandler = getSystemHandler();
        SystemData systemData = systemHandler == null ? null : systemHandler.getData();
        if (systemData != null) {
            updateData(systemData);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    /** Called by the system bridge with each update of the system */
    void updateData(SystemData systemData) {
        if (!active) {
            return;
        }
        data = systemData;
        if (isPresent(systemData)) {
            String componentId = getComponentId(systemData.getSystemId());
            if (componentId != null) {
                updateProperty(PROPERTY_COMPONENT_ID, componentId);
            }
            updateStatus(ThingStatus.ONLINE);
            updateChannels(systemData);
        } else {
            updateStatus(ThingStatusDetail.OFFLINE.GONE, "@text/offline.gone.not-in-system");
        }
    }

    /** The data of the last update of the system, {@code null} before the first one */
    protected @Nullable SystemData getData() {
        return data;
    }

    /** The system bridge of this component */
    public @Nullable SystemHandler getSystem() {
        return getSystemHandler();
    }

    /** The handler of the system bridge, {@code null} without bridge */
    protected @Nullable SystemHandler getSystemHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof SystemHandler handler ? handler : null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            SystemData systemData = data;
            if (systemData != null && isPresent(systemData)) {
                updateChannels(systemData);
            }
            return;
        }
        // Requests can take long, e.g. if the access token has to be renewed first
        scheduler.execute(() -> sendCommand(channelUID, command));
    }

    private void sendCommand(ChannelUID channelUID, Command command) {
        SystemData systemData = data;
        SystemHandler systemHandler = getSystemHandler();
        MyVaillantApi api = systemHandler == null ? null : systemHandler.getApi();
        if (!active || systemHandler == null || api == null || systemData == null || !isPresent(systemData)) {
            logger.debug("Ignoring command {} to {}, the system is not online", command, channelUID);
            return;
        }
        try {
            if (sendCommand(channelUID.getId(), command, api, systemData, systemHandler)) {
                systemHandler.scheduleRefresh();
            }
        } catch (MyVaillantException e) {
            systemHandler.handleRequestFailure("Command " + command + " to " + channelUID, e);
        }
    }
}
