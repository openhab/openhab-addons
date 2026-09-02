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
package org.openhab.binding.hue.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ContentType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.UpdateStatusV2;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ZigbeeStatus;
import org.openhab.binding.hue.internal.api.serialization.InstantDeserializer;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for the Zigbee connectivity handling of {@link Clip2ThingHandler}.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null") // Mockito is not designed with null type annotations in mind
class Clip2ThingHandlerConnectivityTest {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
            .create();
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_DEVICE, "test");

    /**
     * A handler under test, its mocked callback, and a mirror of the thing status: whatever the handler publishes
     * through the callback is written back into the thing mock, so the handler observes its own status writes the
     * way it would at runtime. {@link #setThingStatus} simulates a status written by anybody else.
     */
    private static class Fixture {
        private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        private final AtomicReference<ThingStatusInfo> statusInfo = new AtomicReference<>(
                ThingStatusInfoBuilder.create(ThingStatus.UNKNOWN, ThingStatusDetail.NONE).build());
        private final Clip2ThingHandler handler;

        private Fixture(Map<String, String> properties) {
            Thing thing = mock(Thing.class);
            when(thing.getThingTypeUID()).thenReturn(THING_TYPE_DEVICE);
            when(thing.getUID()).thenReturn(THING_UID);
            when(thing.getProperties()).thenReturn(properties);
            when(thing.getStatusInfo()).thenAnswer(invocation -> statusInfo.get());
            when(thing.getStatus()).thenAnswer(invocation -> statusInfo.get().getStatus());
            doAnswer(invocation -> {
                statusInfo.set(invocation.getArgument(1));
                return null;
            }).when(callback).statusUpdated(any(), any());
            handler = new Clip2ThingHandler(thing, mock(Clip2StateDescriptionProvider.class), mock(ThingRegistry.class),
                    mock(ItemChannelLinkRegistry.class));
            handler.setCallback(callback);
        }

        private void setThingStatus(ThingStatus thingStatus, ThingStatusDetail detail, @Nullable String description) {
            statusInfo.set(ThingStatusInfoBuilder.create(thingStatus, detail).withDescription(description).build());
        }
    }

    private Fixture fixture() {
        return new Fixture(Map.of());
    }

    /**
     * Load a test JSON payload string from a file.
     */
    private String load(String fileName) {
        try {
            return Files.readString(Path.of("src/test/resources", fileName + ".json"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail(e.getMessage());
            return "";
        }
    }

    private List<Resource> loadResources(ResourceType resourceType) {
        Resources resources = GSON.fromJson(load(resourceType.name().toLowerCase(Locale.ROOT)), Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        return list;
    }

    /**
     * Get the first resource from the 'zigbee_connectivity' fixture that reports the given status.
     */
    private Resource zigbeeResource(ZigbeeStatus zigbeeStatus) {
        Resource resource = loadResources(ResourceType.ZIGBEE_CONNECTIVITY).stream()
                .filter(r -> zigbeeStatus == r.getZigbeeStatus()).findFirst().orElse(null);
        assertNotNull(resource);
        return resource;
    }

    /**
     * Build a 'zigbee_connectivity' resource carrying a status value that the fixture does not contain.
     */
    private Resource zigbeeResource(String statusValue) {
        Resource resource = GSON.fromJson(
                "{\"type\":\"zigbee_connectivity\",\"id\":\"test\",\"status\":\"" + statusValue + "\"}",
                Resource.class);
        assertNotNull(resource);
        return resource;
    }

    /**
     * Get a full state 'device_power' resource. Feeding it to the handler makes the handler adopt supported channels,
     * which is what makes the UNDEF sweep on a connectivity issue observable.
     */
    private Resource devicePowerResource() {
        return loadResources(ResourceType.DEVICE_POWER).get(0).setContentType(ContentType.FULL_STATE);
    }

    private ThingStatusInfo captureSingleStatus(ThingHandlerCallback callback) {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(any(), captor.capture());
        return Objects.requireNonNull(captor.getValue());
    }

    @Test
    void testDisconnectedIsReportedAsDisconnected() {
        Fixture fixture = fixture();

        fixture.handler.updateChannels(zigbeeResource("disconnected"));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, statusInfo.getStatusDetail());
        assertEquals(TEXT_OFFLINE_ZIGBEE_DISCONNECTED, statusInfo.getDescription());
    }

    @Test
    void testConnectivityIssueIsReportedAsConnectivityIssue() {
        Fixture fixture = fixture();

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, statusInfo.getStatusDetail());
        assertEquals(TEXT_OFFLINE_ZIGBEE_CONNECTIVITY_ISSUE, statusInfo.getDescription());
    }

    @Test
    void testUnrecognisedStatusIsNotReportedAsDisconnected() {
        Fixture fixture = fixture();

        // ZigbeeStatus.of() maps this to DISCONNECTED, but the bridge did not report a disconnect
        fixture.handler.updateChannels(zigbeeResource("a_future_zigbee_status"));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(TEXT_OFFLINE_ZIGBEE_CONNECTIVITY_ISSUE, statusInfo.getDescription());
    }

    @Test
    void testPendingDiscoveryIsNotAConnectivityIssue() {
        Fixture fixture = fixture();

        // documented for 'zgp_connectivity' resources, which are routed through the same handling
        fixture.handler.updateChannels(zigbeeResource("pending_discovery"));

        assertEquals(ThingStatus.ONLINE, fixture.statusInfo.get().getStatus());
    }

    @Test
    void testUnchangedStatusIsPublishedOnlyOnce() {
        Fixture fixture = fixture();
        fixture.handler.updateChannels(devicePowerResource());

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        verify(fixture.callback, times(1)).statusUpdated(any(), any());
        verify(fixture.callback, atLeastOnce()).stateUpdated(any(), eq(UnDefType.UNDEF));

        clearInvocations(fixture.callback);
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        verify(fixture.callback, never()).statusUpdated(any(), any());
        verify(fixture.callback, never()).stateUpdated(any(), eq(UnDefType.UNDEF));
    }

    @Test
    void testChangeBetweenIssueStatesIsPublished() {
        Fixture fixture = fixture();
        fixture.handler.updateChannels(devicePowerResource());

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));
        clearInvocations(fixture.callback);
        fixture.handler.updateChannels(zigbeeResource("disconnected"));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(TEXT_OFFLINE_ZIGBEE_DISCONNECTED, statusInfo.getDescription());
        verify(fixture.callback, atLeastOnce()).stateUpdated(any(), eq(UnDefType.UNDEF));
    }

    @Test
    void testIssueIsPublishedAgainAfterRecovery() {
        Fixture fixture = fixture();

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTED));
        assertEquals(ThingStatus.ONLINE, fixture.statusInfo.get().getStatus());

        clearInvocations(fixture.callback);
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(TEXT_OFFLINE_ZIGBEE_CONNECTIVITY_ISSUE, statusInfo.getDescription());
    }

    @Test
    void testIssueIsPublishedAgainAfterRecoveryIntoFirmwareUpdate() {
        // a recovery that does not end ONLINE: the handler keeps the thing OFFLINE while a firmware update installs
        Fixture fixture = new Fixture(Map.of(PROPERTY_FIRMWARE_UPDATE_STATE, UpdateStatusV2.INSTALLING.toString()));

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTED));
        assertEquals(ThingStatusDetail.FIRMWARE_UPDATING, fixture.statusInfo.get().getStatusDetail());

        clearInvocations(fixture.callback);
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, statusInfo.getStatusDetail());
    }

    @Test
    void testIssueIsPublishedAgainAfterAnotherWriterSetTheThingOnline() {
        Fixture fixture = fixture();

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        // e.g. the default bridge status handling puts the thing back ONLINE once the bridge has returned; the
        // status description may well be carried over, as refreshSoftwareStatusUI() does elsewhere in this binding
        fixture.setThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, TEXT_OFFLINE_ZIGBEE_CONNECTIVITY_ISSUE);
        clearInvocations(fixture.callback);
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, statusInfo.getStatusDetail());
    }

    @Test
    void testIssueIsPublishedAgainAfterAnotherWriterSetTheThingGone() {
        Fixture fixture = fixture();

        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        // onResourcesList() sets this when the resource id is transiently missing from a full resource list
        fixture.setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                "@text/offline.api2.gone.resource-id-unknown");
        clearInvocations(fixture.callback);
        fixture.handler.updateChannels(zigbeeResource(ZigbeeStatus.CONNECTIVITY_ISSUE));

        ThingStatusInfo statusInfo = captureSingleStatus(fixture.callback);
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, statusInfo.getStatusDetail());
        assertEquals(TEXT_OFFLINE_ZIGBEE_CONNECTIVITY_ISSUE, statusInfo.getDescription());
    }
}
