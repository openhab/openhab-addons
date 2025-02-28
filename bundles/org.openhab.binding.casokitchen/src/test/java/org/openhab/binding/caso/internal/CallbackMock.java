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
package org.openhab.binding.caso.internal;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * {@link CallbackMock} listener for handler updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CallbackMock implements ThingHandlerCallback {
    public Map<String, State> states = new HashMap<>();
    public ThingStatus thingStatus = ThingStatus.UNINITIALIZED;

    @Override
    public void stateUpdated(ChannelUID channelUID, State state) {
        states.put(channelUID.toString(), state);
    }

    public void waitForFullUpdate(int stateCount) {
        Instant startWaiting = Instant.now();
        while (states.size() < stateCount && startWaiting.plus(5, ChronoUnit.SECONDS).isAfter(Instant.now())) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        }
        if (!ThingStatus.ONLINE.equals(thingStatus)) {
            fail(thingStatus.toString());
        }
    }

    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
    }

    public void waitForOnline() {
        synchronized (this) {
            Instant startWaiting = Instant.now();
            while (!ThingStatus.ONLINE.equals(thingStatus)
                    && startWaiting.plus(5, ChronoUnit.SECONDS).isAfter(Instant.now())) {
                try {
                    wait(250);
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
            if (!ThingStatus.ONLINE.equals(thingStatus)) {
                fail(thingStatus.toString());
            }
        }
    }

    @Override
    public void statusUpdated(Thing thing, ThingStatusInfo thingStatusInfo) {
        synchronized (this) {
            thing.setStatusInfo(thingStatusInfo);
            thingStatus = thingStatusInfo.getStatus();
            notifyAll();
        }
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
    }

    @Override
    public ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
        return ChannelBuilder.create(new ChannelUID("test"), null);
    }

    @Override
    public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
        return ChannelBuilder.create(new ChannelUID("test"), null);
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
        return null;
    }
}
