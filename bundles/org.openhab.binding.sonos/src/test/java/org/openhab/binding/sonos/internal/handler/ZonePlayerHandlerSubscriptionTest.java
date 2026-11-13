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
package org.openhab.binding.sonos.internal.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ONE_SL_THING_TYPE_UID;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonos.internal.SonosStateDescriptionOptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests which event subscriptions a player sets up.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ZonePlayerHandlerSubscriptionTest {

    private static final String SURROUND_UDN = "RINCON_00000000000301400";
    private static final String TOPOLOGY = "ZoneGroupTopology";
    private static final String RENDERING_CONTROL = "RenderingControl";
    private static final String LAST_SUBSCRIBED_SERVICE = "ContentDirectory";
    private static final String STANDALONE_SURROUND = "<ZoneGroupState><ZoneGroups><ZoneGroup Coordinator=\""
            + SURROUND_UDN + "\" ID=\"" + SURROUND_UDN + ":1\"><ZoneGroupMember UUID=\"" + SURROUND_UDN
            + "\" ZoneName=\"Office\"/></ZoneGroup></ZoneGroups></ZoneGroupState>";

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @Mock @NonNullByDefault({}) UpnpIOService upnpIOService;
    private @Mock @NonNullByDefault({}) SonosStateDescriptionOptionProvider stateDescriptionProvider;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private final AtomicBoolean registered = new AtomicBoolean(false);
    private @NonNullByDefault({}) ZonePlayerHandler surround;

    @BeforeEach
    public void createSurround() {
        when(upnpIOService.isRegistered(any(UpnpIOParticipant.class))).thenAnswer(invocation -> registered.get());
        when(upnpIOService.invokeAction(any(UpnpIOParticipant.class), anyString(), anyString(), any()))
                .thenReturn(Map.of("DisplaySoftwareVersion", "18.8"));
        Thing thing = ThingBuilder.create(ONE_SL_THING_TYPE_UID, "surround")
                .withConfiguration(new Configuration(Map.of("udn", SURROUND_UDN, "refresh", 1))).build();
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        surround = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, null, stateDescriptionProvider);
        surround.setCallback(callback);
        surround.initialize();
    }

    @AfterEach
    public void disposeSurround() {
        surround.dispose();
    }

    @Test
    public void surroundOfAHomeTheaterDoesNotSubscribeToRenderingControl() throws IOException {
        surround.onValueReceived("ZoneGroupState", homeTheater(), TOPOLOGY);

        registered.set(true);

        verify(upnpIOService, timeout(5000)).addSubscription(surround, LAST_SUBSCRIBED_SERVICE,
                ZonePlayerHandler.SUBSCRIPTION_DURATION);
        verify(upnpIOService, never()).addSubscription(surround, RENDERING_CONTROL,
                ZonePlayerHandler.SUBSCRIPTION_DURATION);
    }

    @Test
    public void surroundThatLeftTheHomeTheaterSubscribesToRenderingControlAgain() throws IOException {
        surround.onValueReceived("ZoneGroupState", homeTheater(), TOPOLOGY);
        registered.set(true);
        verify(upnpIOService, timeout(5000)).addSubscription(surround, LAST_SUBSCRIBED_SERVICE,
                ZonePlayerHandler.SUBSCRIPTION_DURATION);
        verify(upnpIOService, never()).addSubscription(surround, RENDERING_CONTROL,
                ZonePlayerHandler.SUBSCRIPTION_DURATION);

        surround.onValueReceived("ZoneGroupState", STANDALONE_SURROUND, TOPOLOGY);

        verify(upnpIOService, timeout(5000)).addSubscription(surround, RENDERING_CONTROL,
                ZonePlayerHandler.SUBSCRIPTION_DURATION);
    }

    private String homeTheater() throws IOException {
        InputStream resourceStream = getClass().getResourceAsStream("/ZoneGroupState-home-theater.xml");
        assertNotNull(resourceStream);
        return new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
