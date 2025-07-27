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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.AttributeListener;
import org.openhab.binding.matter.internal.client.EventTriggeredListener;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic abstract converter for translating Matter clusters to openHAB channels.
 *
 * Converters are responsible for converting Matter cluster commands and attributes into openHAB commands and vice
 * versa.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class GenericConverter<T extends BaseCluster> implements AttributeListener, EventTriggeredListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // This cluster is used for initializing the converter, but is not kept updated as values change over time.
    protected T initializingCluster;
    protected MatterBaseThingHandler handler;
    protected int endpointNumber;
    protected String labelPrefix;
    // used to REFRESH channels
    protected ConcurrentHashMap<String, State> stateCache = new ConcurrentHashMap<>();

    public GenericConverter(T cluster, MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        this.initializingCluster = cluster;
        this.handler = handler;
        this.endpointNumber = endpointNumber;
        this.labelPrefix = labelPrefix;
    }

    public abstract Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID);

    /**
     * Updates all the channel states of a cluster
     */
    public abstract void initState();

    /**
     * Handles a openHAB channel command
     */
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            stateCache.forEach((channelId, state) -> handler.updateState(endpointNumber, channelId, state));
        }
    }

    /**
     * This method is designed to be optionally overridden in subclasses when a cluster need to poll for one or more
     * attributes
     */
    public void pollCluster() {
        // add polling logic here in subclasses
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
    }

    public T getInitializingCluster() {
        return initializingCluster;
    }

    public final void updateState(String channelId, State state) {
        handler.updateState(endpointNumber, channelId, state);
        stateCache.put(channelId, state);
    }

    public final void triggerChannel(String channelId, String event) {
        handler.triggerChannel(endpointNumber, channelId, event);
    }

    protected String formatLabel(String channelLabel) {
        if (labelPrefix.trim().length() > 0) {
            return labelPrefix.trim() + " - " + channelLabel;
        }
        return channelLabel;
    }

    protected void updateThingAttributeProperty(String attributeName, @Nullable Object value) {
        handler.updateClusterAttributeProperty(initializingCluster.name, attributeName, value);
    }
}
