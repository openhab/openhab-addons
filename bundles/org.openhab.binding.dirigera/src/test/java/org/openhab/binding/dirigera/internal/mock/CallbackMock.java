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
package org.openhab.binding.dirigera.internal.mock;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The {@link CallbackMock} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CallbackMock implements ThingHandlerCallback {
    private final int STATUS_DURATION_TIMEOUT_SEC = 10;
    private ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED).build();
    private @Nullable Bridge bridge;
    public Map<String, State> stateMap = new HashMap<>();
    public Map<String, String> triggerMap = new HashMap<>();

    @Override
    public void statusUpdated(Thing thing, ThingStatusInfo thingStatusInfo) {
        synchronized (this) {
            thing.setStatusInfo(thingStatusInfo);
            statusInfo = thingStatusInfo;
            this.notifyAll();
        }
    }

    public void waitForStatus(ThingStatus expectedStatus) {
        synchronized (this) {
            Instant start = Instant.now();
            Instant check = Instant.now();
            while (!expectedStatus.equals(statusInfo.getStatus())
                    && Duration.between(start, check).getSeconds() < STATUS_DURATION_TIMEOUT_SEC) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    fail("Interruppted waiting for ONLINE");
                }
                check = Instant.now();
            }
        }
        // if method is exited without reaching ONLINE e.g. through timeout fail
        if (!expectedStatus.equals(statusInfo.getStatus())) {
            fail("Wait for status " + expectedStatus + " reached just " + statusInfo);
        }
    }

    public void waitForOnline() {
        waitForStatus(ThingStatus.ONLINE);
    }

    public ThingStatusInfo getStatus() {
        return statusInfo;
    }

    public void clear() {
        stateMap.clear();
    }

    public @Nullable State getState(String channel) {
        synchronized (stateMap) {
            Instant start = Instant.now();
            Instant check = Instant.now();
            while (stateMap.get(channel) == null
                    && Duration.between(start, check).getSeconds() < STATUS_DURATION_TIMEOUT_SEC) {
                try {
                    stateMap.wait(100);
                } catch (InterruptedException e) {
                    fail("Interruppted waiting for ONLINE");
                }
                check = Instant.now();
            }
        }
        return stateMap.get(channel);
    }

    @Override
    public void stateUpdated(ChannelUID channelUID, State state) {
        synchronized (stateMap) {
            stateMap.put(channelUID.getAsString(), state);
            stateMap.notifyAll();
        }
    }

    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
    }

    @Override
    public void thingUpdated(Thing thing) {
    }

    @Override
    public void validateConfigurationParameters(Thing thing, Map<String, Object> configurationParameters) {
    }

    @Override
    public void validateConfigurationParameters(Channel channel, Map<String, Object> configurationParameters) {
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(ChannelTypeUID channelTypeUID) {
        return null;
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(ThingTypeUID thingTypeUID) {
        return null;
    }

    @Override
    public void configurationUpdated(Thing thing) {
    }

    @Override
    public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration) {
    }

    @Override
    public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
        triggerMap.put(channelUID.getAsString(), event);
    }

    @Override
    public ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
        return ChannelBuilder.create(new ChannelUID("handler:test"));
    }

    @Override
    public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
        return ChannelBuilder.create(new ChannelUID("handler:test"));
    }

    @Override
    public List<ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
            ChannelGroupTypeUID channelGroupTypeUID) {
        return List.of();
    }

    @Override
    public boolean isChannelLinked(ChannelUID channelUID) {
        return false;
    }

    @Override
    public @Nullable Bridge getBridge(ThingUID bridgeUID) {
        return bridge;
    }

    public void setBridge(Bridge bridge) {
        this.bridge = bridge;
    }
}
