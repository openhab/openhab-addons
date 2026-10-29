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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.CURRENTALBUMART;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.TUNEINSTATIONID;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonos.internal.SonosStateDescriptionOptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Tests which values a group coordinator passes on to the players of its group.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ZonePlayerHandlerGroupTest {

    private static final String COORDINATOR_UDN = "RINCON_00000000000C01400";
    private static final String MEMBER_UDN = "RINCON_00000000000M01400";
    private static final String AV_TRANSPORT = "AVTransport";
    private static final String TOPOLOGY = "ZoneGroupTopology";
    private static final String TUNEIN_STREAM = "x-sonosapi-stream:s10637?sid=254&flags=8224&sn=0";
    private static final StringType TUNEIN_STATION = new StringType("10637");

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @Mock @NonNullByDefault({}) UpnpIOService upnpIOService;
    private @Mock @NonNullByDefault({}) SonosStateDescriptionOptionProvider stateDescriptionProvider;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback coordinatorCallback;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback memberCallback;

    private final Map<ThingUID, Thing> things = new HashMap<>();
    private @NonNullByDefault({}) ZonePlayerHandler coordinator;
    private @NonNullByDefault({}) ZonePlayerHandler member;

    @BeforeEach
    @SuppressWarnings("null")
    public void createGroup() {
        when(thingRegistry.get(any(ThingUID.class))).thenAnswer(invocation -> things.get(invocation.getArgument(0)));
        lenient().when(coordinatorCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        lenient().when(memberCallback.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        coordinator = createOnlinePlayer(COORDINATOR_UDN, coordinatorCallback);
        member = createOnlinePlayer(MEMBER_UDN, memberCallback);
    }

    @AfterEach
    public void disposePlayers() {
        coordinator.dispose();
        member.dispose();
    }

    @Test
    public void memberTakesTheTransportStateOfItsCoordinator() {
        groupPlaying();

        assertEquals("PLAYING", member.getTransportState());
    }

    @Test
    public void playerWithoutKnownTransportUriTakesTheTransportStateOfItsCoordinator() {
        coordinator.onValueReceived("ZoneGroupState", zoneGroupState("1", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);

        coordinator.onValueReceived("TransportState", "PLAYING", AV_TRANSPORT);

        assertEquals("PLAYING", member.getTransportState());
    }

    @Test
    public void playerThatLeftKeepsItsOwnTransportStateWhileTheCoordinatorStillListsIt() {
        groupPlaying();

        member.onValueReceived("CurrentURI", "", AV_TRANSPORT);
        member.onValueReceived("TransportState", "STOPPED", AV_TRANSPORT);
        coordinator.onValueReceived("ZoneGroupState", zoneGroupState("2", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);

        assertEquals("STOPPED", member.getTransportState());
    }

    @Test
    @SuppressWarnings("null")
    public void playerThatLeftKeepsItsOwnMediaInformationWhileTheCoordinatorStillListsIt() {
        member.onValueReceived("CurrentURI", "", AV_TRANSPORT);

        coordinator.onValueReceived("CurrentURI", TUNEIN_STREAM, AV_TRANSPORT);
        coordinator.onValueReceived("ZoneGroupState", zoneGroupState("1", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);

        verify(memberCallback, never()).stateUpdated(eq(channel(member, TUNEINSTATIONID)), any());
    }

    @Test
    @SuppressWarnings("null")
    public void playerThatLeftKeepsItsOwnAlbumArtWhileTheCoordinatorStillListsIt() {
        member.onValueReceived("CurrentURI", "", AV_TRANSPORT);
        coordinator.onValueReceived("ZoneGroupState", zoneGroupState("1", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);

        coordinator.onValueReceived("CurrentURI", "", AV_TRANSPORT);
        coordinator.updateMediaInformation();

        verify(coordinatorCallback).stateUpdated(channel(coordinator, CURRENTALBUMART), UnDefType.UNDEF);
        verify(memberCallback, never()).stateUpdated(eq(channel(member, CURRENTALBUMART)), any());
    }

    @Test
    @SuppressWarnings("null")
    public void playerLeavingBeforeItsOwnTopologyChangesResetsOnlyItsOwnAlbumArt() {
        coordinator.onValueReceived("CurrentURI", TUNEIN_STREAM, AV_TRANSPORT);
        member.onValueReceived("ZoneGroupState", zoneGroupState("1", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);
        member.onValueReceived("CurrentURI", "x-rincon:" + COORDINATOR_UDN, AV_TRANSPORT);

        member.onValueReceived("CurrentURI", "", AV_TRANSPORT);
        member.updateMediaInformation();

        verify(memberCallback).stateUpdated(channel(member, CURRENTALBUMART), UnDefType.UNDEF);
        verify(coordinatorCallback, never()).stateUpdated(eq(channel(coordinator, CURRENTALBUMART)), any());
    }

    @Test
    public void standalonePlayerUpdatesItsOwnMediaInformation() {
        coordinator.onValueReceived("CurrentURI", TUNEIN_STREAM, AV_TRANSPORT);

        coordinator.updateMediaInformation();

        verify(coordinatorCallback).stateUpdated(channel(coordinator, TUNEINSTATIONID), TUNEIN_STATION);
    }

    @Test
    public void playerJoiningAfterTheCoordinatorListsItTakesTheStateOfItsCoordinator() {
        member.onValueReceived("CurrentURI", "x-rincon-queue:" + MEMBER_UDN + "#0", AV_TRANSPORT);
        member.onValueReceived("TransportState", "STOPPED", AV_TRANSPORT);
        coordinator.onValueReceived("CurrentURI", TUNEIN_STREAM, AV_TRANSPORT);
        coordinator.onValueReceived("TransportState", "PLAYING", AV_TRANSPORT);
        coordinator.onValueReceived("ZoneGroupState", zoneGroupState("1", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);

        member.onValueReceived("CurrentURI", "x-rincon:" + COORDINATOR_UDN, AV_TRANSPORT);

        assertEquals("PLAYING", member.getTransportState());
        verify(memberCallback).stateUpdated(channel(member, TUNEINSTATIONID), TUNEIN_STATION);
    }

    @Test
    public void playerFollowingAnOfflineCoordinatorDoesNotTakeItsTransportState() {
        coordinator.onValueReceived("TransportState", "PLAYING", AV_TRANSPORT);
        coordinator.getThing()
                .setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, null));

        member.onValueReceived("CurrentURI", "x-rincon:" + COORDINATOR_UDN, AV_TRANSPORT);

        assertNull(member.getTransportState());
    }

    private void groupPlaying() {
        member.onValueReceived("CurrentURI", "x-rincon:" + COORDINATOR_UDN, AV_TRANSPORT);
        coordinator.onValueReceived("ZoneGroupState", zoneGroupState("1", COORDINATOR_UDN, MEMBER_UDN), TOPOLOGY);
        coordinator.onValueReceived("TransportState", "PLAYING", AV_TRANSPORT);
    }

    private ZonePlayerHandler createOnlinePlayer(String udn, ThingHandlerCallback callback) {
        Thing thing = ThingBuilder.create(ZONEPLAYER_THING_TYPE_UID, udn)
                .withConfiguration(new Configuration(Map.of("udn", udn, "refresh", 60))).build();
        ZonePlayerHandler handler = new ZonePlayerHandler(thingRegistry, thing, upnpIOService, null,
                stateDescriptionProvider);
        handler.setCallback(callback);
        handler.initialize();
        thing.setHandler(handler);
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        things.put(thing.getUID(), thing);
        return handler;
    }

    private static ChannelUID channel(ZonePlayerHandler handler, String channelId) {
        return new ChannelUID(handler.getThing().getUID(), channelId);
    }

    private static String zoneGroupState(String groupSequence, String coordinatorUDN, String... memberUDNs) {
        StringBuilder members = new StringBuilder();
        for (String memberUDN : memberUDNs) {
            members.append("<ZoneGroupMember UUID=\"").append(memberUDN).append("\" ZoneName=\"").append(memberUDN)
                    .append("\"/>");
        }
        return "<ZoneGroupState><ZoneGroups><ZoneGroup Coordinator=\"" + coordinatorUDN + "\" ID=\"" + coordinatorUDN
                + ":" + groupSequence + "\">" + members + "</ZoneGroup></ZoneGroups></ZoneGroupState>";
    }
}
