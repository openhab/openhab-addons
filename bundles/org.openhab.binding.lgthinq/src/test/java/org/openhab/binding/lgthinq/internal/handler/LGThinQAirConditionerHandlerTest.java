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
package org.openhab.binding.lgthinq.internal.handler;

import static org.mockito.Mockito.*;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.LG_API_PLATFORM_TYPE_V2;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * Tests air conditioner state publication to Thing channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class LGThinQAirConditionerHandlerTest {
    private final ThingUID thingUID = new ThingUID(THING_TYPE_AIR_CONDITIONER, "test");
    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private LGThinQAirConditionerHandler handler = createHandler();

    @BeforeEach
    void setUp() {
        callback = mock(ThingHandlerCallback.class);
        handler = createHandler();
        handler.setCallback(callback);
    }

    @Test
    void publishesAutoDryStateUsingAutoDryCapabilityValue() throws LGThinqApiException {
        ACCapability capability = new ACCapability();
        capability.setCoolJetModeCommandOn("2");
        capability.setAutoDryModeCommandOn("1");
        handler.thinQCapability = capability;

        ACCanonicalSnapshot snapshot = new ACCanonicalSnapshot();
        snapshot.setAutoDryMode(1.0);

        handler.updateDeviceChannels(snapshot);

        verify(callback).stateUpdated(channel(CHANNEL_AC_AUTO_DRY_ID), OnOffType.ON);
    }

    private LGThinQAirConditionerHandler createHandler() {
        ChannelUID autoDryChannelUID = channel(CHANNEL_AC_AUTO_DRY_ID);
        Thing thing = ThingBuilder.create(THING_TYPE_AIR_CONDITIONER, thingUID)
                .withProperties(Map.of(PROP_INFO_PLATFORM_TYPE, LG_API_PLATFORM_TYPE_V2))
                .withChannel(ChannelBuilder.create(autoDryChannelUID, CoreItemFactory.SWITCH).build()).build();
        return new LGThinQAirConditionerHandler(thing, mock(LGThinQStateDescriptionProvider.class),
                mock(ItemChannelLinkRegistry.class), mock(HttpClientFactory.class));
    }

    private ChannelUID channel(String channelId) {
        return new ChannelUID(thingUID, CHANNEL_DASHBOARD_GRP_ID + "#" + channelId);
    }
}
