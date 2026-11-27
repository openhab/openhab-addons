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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.IcdManagementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.IcdState;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for IcdManagementConverter
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class IcdManagementConverterTest extends BaseMatterConverterTest {

    private static final ChannelGroupUID GROUP_UID = new ChannelGroupUID("matter:node:test:12345:0");

    @Mock
    @NonNullByDefault({})
    private IcdManagementCluster mockCluster;
    @NonNullByDefault({})
    private IcdManagementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        converter = new IcdManagementConverter(mockCluster, mockHandler, 0, "TestLabel");
    }

    @Test
    void testCheckInChannelsForLongIdleTimeDevice() {
        mockCluster.featureMap = new IcdManagementCluster.FeatureMap(true, false, true, false);
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(GROUP_UID);
        Set<String> ids = channels.keySet().stream().map(c -> c.getUID().getIdWithoutGroup())
                .collect(Collectors.toSet());
        assertEquals(Set.of("icdmanagement-checkinregistered", "icdmanagement-lastcheckin"), ids);
    }

    @Test
    void testNoChannelsForShortIdleTimeDevice() {
        mockCluster.featureMap = new IcdManagementCluster.FeatureMap(false, false, false, false);
        assertTrue(converter.createChannels(GROUP_UID).isEmpty());
    }

    @Test
    void testUpdateIcdState() {
        mockCluster.featureMap = new IcdManagementCluster.FeatureMap(true, false, true, false);
        IcdState state = new IcdState();
        state.litSupported = true;
        state.registered = true;
        state.lastCheckIn = 1_760_000_000_000L;

        converter.updateIcdState(state);

        verify(mockHandler).updateState(eq(0), eq("icdmanagement-checkinregistered"), eq(OnOffType.ON));
        verify(mockHandler).updateState(eq(0), eq("icdmanagement-lastcheckin"),
                eq(new DateTimeType(Instant.ofEpochMilli(1_760_000_000_000L))));
    }

    @Test
    void testUnknownLastCheckIn() {
        mockCluster.featureMap = new IcdManagementCluster.FeatureMap(true, false, true, false);
        IcdState state = new IcdState();
        state.litSupported = true;

        converter.updateIcdState(state);

        verify(mockHandler).updateState(eq(0), eq("icdmanagement-checkinregistered"), eq(OnOffType.OFF));
        verify(mockHandler).updateState(eq(0), eq("icdmanagement-lastcheckin"), eq(UnDefType.UNDEF));
    }

    @Test
    void testShortIdleTimeDeviceIgnoresIcdState() {
        mockCluster.featureMap = new IcdManagementCluster.FeatureMap(false, false, false, false);

        converter.updateIcdState(new IcdState());

        verify(mockHandler, never()).updateState(anyInt(), anyString(), any());
    }
}
