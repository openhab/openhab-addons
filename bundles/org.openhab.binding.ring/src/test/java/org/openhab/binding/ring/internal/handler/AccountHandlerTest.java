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

package org.openhab.binding.ring.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ring.RingBindingConstants.*;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.ring.internal.RingVideoServlet;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * @author Paul Smedley - Initial contribution
 */

@ExtendWith(MockitoExtension.class)
public class AccountHandlerTest {

    @Mock
    private Bridge bridge;
    @Mock
    private NetworkAddressService networkAddressService;
    @Mock
    private RingVideoServlet ringVideoServlet;
    @Mock
    private HttpClient httpClient;

    @Mock
    private ThingHandlerCallback callback;

    private AccountHandler accountHandler;

    @BeforeEach
    public void setUp() {
        ThingUID thingUID = new ThingUID("ring", "account", "test_account");
        when(bridge.getUID()).thenReturn(thingUID);

        accountHandler = new AccountHandler(bridge, networkAddressService, ringVideoServlet, 8080, httpClient);
        accountHandler.setCallback(callback);
    }

    @Test
    public void testHandlePushEvent_ParsesMotionAndUpdatesChannels() {
        String fcmDataPayload = """
                {
                  "device": {
                    "e2ee_enabled": false,
                    "e2ee_cem": 1,
                    "id": 27802331,
                    "kind": "stickup_cam_lunar",
                    "name": "Henley Side"
                  },
                  "event": {
                    "ding": {
                      "id": "7653728126335728347",
                      "created_at": "2026-06-21T06:14:08Z",
                      "subtype": "human",
                      "detection_type": "human"
                    }
                  },
                  "location": {
                    "id": "r1fdhm-rp72-0"
                  }
                }
                """;

        String fcmAndroidConfig = """
                {
                  "group_key": "r1fdhm-rp72-0",
                  "category": "com.ring.pn.live-event.motion",
                  "channel": "motion_channel_notification27802331",
                  "body": "There is a Person at your Henley Side"
                }
                """;

        accountHandler.handlePushEvent(fcmDataPayload, fcmAndroidConfig, null);

        // Verify the callback received the correct channel and state updates (2 arguments only)
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), CHANNEL_EVENT_KIND)),
                eq(new StringType("motion")));
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), CHANNEL_EVENT_DOORBOT_ID)),
                eq(new StringType("27802331")));
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), CHANNEL_EVENT_DOORBOT_DESCRIPTION)),
                eq(new StringType("Henley Side")));
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), CHANNEL_EVENT_EXTENDED_DESCRIPTION)),
                eq(new StringType("There is a Person at your Henley Side")));
    }

    @Test
    public void testHandlePushEvent_IgnoresDuplicates() {
        String fcmDataPayload = "{\"device\":{\"id\":123},\"event\":{\"ding\":{\"id\":\"999\"}}}";

        accountHandler.handlePushEvent(fcmDataPayload, "", null);
        accountHandler.handlePushEvent(fcmDataPayload, "", null);

        // Ensure the callback was only triggered once for the KIND channel
        verify(callback, org.mockito.Mockito.times(1))
                .stateUpdated(eq(new ChannelUID(bridge.getUID(), CHANNEL_EVENT_KIND)), any(State.class));
    }
}
