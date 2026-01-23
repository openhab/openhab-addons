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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.openhab.binding.matter.internal.MatterBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OtaSoftwareUpdateRequestorCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OtaSoftwareUpdateRequestorCluster.UpdateStateEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;

/**
 * A converter for managing OTA software update state.
 * This converter maintains local state for update status and progress, and provides a channel
 * to indicate update availability which is set out of band from the cluster.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OtaSoftwareUpdateRequestorConverter extends GenericConverter<OtaSoftwareUpdateRequestorCluster> {

    private UpdateStateEnum lastUpdateState = UpdateStateEnum.UNKNOWN;
    private int lastUpdateStateProgress = 0;
    private boolean updateAvailable = false;
    private boolean updatePossible = false;

    public OtaSoftwareUpdateRequestorConverter(OtaSoftwareUpdateRequestorCluster cluster,
            MatterBaseThingHandler handler, int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        // Update Available - indicates if a firmware update is available
        Channel channel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_OTASOFTWAREUPDATEREQUESTOR_UPDATEAVAILABLE),
                        CoreItemFactory.SWITCH)
                .withType(CHANNEL_OTASOFTWAREUPDATEREQUESTOR_UPDATEAVAILABLE).build();
        channels.put(channel, null);

        return channels;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case OtaSoftwareUpdateRequestorCluster.ATTRIBUTE_UPDATE_STATE:
                if (message.value instanceof UpdateStateEnum updateState) {
                    lastUpdateState = updateState;
                }
                break;
            case OtaSoftwareUpdateRequestorCluster.ATTRIBUTE_UPDATE_STATE_PROGRESS:
                if (message.value instanceof Number progress) {
                    lastUpdateStateProgress = progress.intValue();
                }
                break;
            default:
                logger.debug("Unknown attribute {}", message.path.attributeName);
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        lastUpdateState = initializingCluster.updateState != null ? initializingCluster.updateState
                : UpdateStateEnum.UNKNOWN;
        lastUpdateStateProgress = initializingCluster.updateStateProgress != null
                ? initializingCluster.updateStateProgress
                : 0;
        updatePossible = initializingCluster.updatePossible != null ? initializingCluster.updatePossible : false;
        // Initialize update available channel to false
        setUpdateAvailable(false);
    }

    /**
     * Sets the update available state and updates the channel.
     * 
     * @param available true if a firmware update is available, false otherwise
     */
    public void setUpdateAvailable(boolean available) {
        updateAvailable = available;
        updateState(CHANNEL_ID_OTASOFTWAREUPDATEREQUESTOR_UPDATEAVAILABLE, OnOffType.from(available));
    }

    /**
     * Gets the current update available state.
     * 
     * @return true if a firmware update is available, false otherwise
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Gets the current update possible state.
     * 
     * @return true if a firmware update is possible, false otherwise
     */
    public boolean isUpdatePossible() {
        return updatePossible;
    }

    /**
     * Gets the last update state.
     * 
     * @return the last update state
     */
    public UpdateStateEnum getLastUpdateState() {
        return lastUpdateState;
    }

    /**
     * Gets the last update state progress.
     * 
     * @return the last update state progress
     */
    public int getLastUpdateStateProgress() {
        return lastUpdateStateProgress;
    }

    /**
     * If the update is canceled, this method should be called to reset the update state.
     * Resets the update state to unknown and update state progress to 0.
     */
    public void resetUpdateState() {
        lastUpdateState = UpdateStateEnum.UNKNOWN;
        lastUpdateStateProgress = 0;
    }
}
