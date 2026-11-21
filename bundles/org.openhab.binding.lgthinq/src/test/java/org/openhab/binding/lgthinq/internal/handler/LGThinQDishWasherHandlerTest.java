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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.LG_API_PLATFORM_TYPE_V1;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiExhaustionException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;

/**
 * Tests the dishwasher handler state publication.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class LGThinQDishWasherHandlerTest {
    private final ThingUID thingUID = new ThingUID(THING_TYPE_DISHWASHER, "test");
    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private LGThinQDishWasherHandler handler = createHandler();

    @BeforeEach
    void setUp() {
        callback = mock(ThingHandlerCallback.class);
        handler = createHandler();
        handler.setCallback(callback);
    }

    @Test
    void keepsStatusAndStateWhenV1MonitorReturnsNoData() throws LGThinqException {
        LGThinQDishWasherHandler spiedHandler = spy(handler);
        doThrow(new LGThinqApiExhaustionException("Exhausted trying to get monitor data")).when(spiedHandler)
                .getSnapshotDeviceAdapter(anyString());

        for (int i = 0; i < 10; i++) {
            spiedHandler.updateThingStateFromLG();
        }

        verify(callback, never()).statusUpdated(any(), any());
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void clearsDashboardChannelsWhenDeviceDisconnects() {
        handler.onDeviceDisconnected();

        verify(callback).stateUpdated(channel(CHANNEL_AC_POWER_ID), OnOffType.OFF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_STATE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PROCESS_STATE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_COURSE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_DOOR_LOCK_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_REMAIN_TIME_ID), UnDefType.UNDEF);
    }

    private LGThinQDishWasherHandler createHandler() {
        Thing thing = ThingBuilder.create(THING_TYPE_DISHWASHER, thingUID)
                .withProperties(Map.of(PROP_INFO_PLATFORM_TYPE, LG_API_PLATFORM_TYPE_V1)).build();
        return new LGThinQDishWasherHandler(thing, mock(LGThinQStateDescriptionProvider.class),
                mock(ItemChannelLinkRegistry.class), mock(HttpClientFactory.class));
    }

    private ChannelUID channel(String channelId) {
        return new ChannelUID(thingUID, CHANNEL_DASHBOARD_GRP_ID + "#" + channelId);
    }
}
