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
package org.openhab.binding.roborock.internal;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.api.enums.RobotCapabilities;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.UnDefType;

/**
 * A Thing configuration change re-initializes the existing handler instance, so field initializers
 * do not run a second time and everything the room resolution is decided from has to be reset in
 * {@code initialize()} explicitly.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault({})
class RoborockVacuumHandlerReinitializationTest {

    /**
     * Pointing the Thing at a different robot must not leave the previous robot's room names in
     * place, because segment ids overlap between maps.
     */
    @Test
    void initializeDropsTheRoomStateOfThePreviousConfiguration() {
        ThingUID thingUID = new ThingUID(RoborockBindingConstants.ROBOROCK_VACUUM, "test");
        ChannelUID currentRoomUID = new ChannelUID(thingUID, RobotCapabilities.CURRENT_ROOM.getChannel());
        Channel currentRoomChannel = ChannelBuilder.create(currentRoomUID, "String").build();

        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getConfiguration()).thenReturn(new Configuration());
        when(thing.getChannel(RobotCapabilities.CURRENT_ROOM.getChannel())).thenReturn(currentRoomChannel);
        // No bridge: initialize() returns right after resetting its state, the part under test.
        when(thing.getBridgeUID()).thenReturn(null);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        RoborockVacuumHandler handler = new RoborockVacuumHandler(thing, mock(ChannelTypeRegistry.class),
                mock(RoborockStateDescriptionOptionProvider.class));
        handler.setCallback(callback);

        handler.initialize();

        // The UNDEF update is the observable half of the segment-name table reset.
        verify(callback, atLeastOnce()).stateUpdated(currentRoomUID, UnDefType.UNDEF);
    }
}
