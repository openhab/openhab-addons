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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.EventSubType;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.NAError;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * {@link ChannelHelperCapability} give the capability to dispatch incoming data across the channel helpers.
 * This capability is common to all things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ChannelHelperCapability extends Capability {
    private @Nullable ZonedDateTime lastEventTime;
    private final List<ChannelHelper> channelHelpers;

    public ChannelHelperCapability(CommonInterface handler, List<ChannelHelper> channelHelpers) {
        super(handler);
        this.channelHelpers = channelHelpers;
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        if (firstLaunch && !moduleType.isLogical()) {
            properties.put(PROPERTY_MODEL_ID, moduleType.apiName.isBlank() ? moduleType.name() : moduleType.apiName);
            properties.put(PROPERTY_VENDOR, VENDOR);
        }
    }

    @Override
    public void afterNewData(Object newData) {
        channelHelpers.forEach(helper -> helper.setNewData(newData));
        handler.getActiveChannels().forEach(channel -> {
            ChannelUID channelUID = channel.getUID();
            String channelID = channelUID.getIdWithoutGroup();
            String groupId = channelUID.getGroupId();
            Configuration channelConfig = channel.getConfiguration();
            for (ChannelHelper helper : channelHelpers) {
                State state = helper.getChannelState(channelID, groupId, channelConfig);
                if (state != null) {
                    handler.updateState(channelUID, state);
                    break;
                }
            }
        });

        // The channel should get triggered at last (after super and sub methods), because this allows rules to access
        // the new updated data from the other channels.
        // Event is common ancestor to HomeEvent and WebhookEvent
        if (newData instanceof Event event) {
            EventType eventType = event.getEventType();
            ZonedDateTime localLast = lastEventTime;
            ZonedDateTime eventTime = event.getTime();

            if (eventType.validFor(handler.getModuleType()) && (localLast == null || eventTime.isAfter(localLast))) {
                String eventTypeName = eventType.name();
                lastEventTime = eventTime;
                handler.recurseUpToHomeHandler(handler).ifPresent(homeHandler -> homeHandler
                        .triggerChannel(GROUP_SECURITY_EVENT, CHANNEL_HOME_EVENT, eventTypeName));
                handler.triggerChannel(GROUP_SECURITY_EVENT, CHANNEL_HOME_EVENT, Objects
                        .requireNonNull(event.getSubTypeDescription().map(EventSubType::name).orElse(eventTypeName)));
            }
        }

        super.afterNewData(newData);
    }

    @Override
    protected void internalSetNewData(Object newData) {
        super.internalSetNewData(newData);
        if (newData instanceof NAThing naThing) {
            naThing.getFirmware().map(fw -> properties.put(PROPERTY_FIRMWARE_VERSION, fw));
            if (!naThing.isReachable()) {
                statusReason = "@text/device-not-connected";
            }
        }
    }

    @Override
    protected void updateErrors(NAError error) {
        if (error.getId().equals(handler.getId())) {
            statusReason = "@text/%s".formatted(error.getCode().message);
        }
    }
}
