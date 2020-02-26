/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler.strategy;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.Eui64;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.ZigbeeDeviceFeature;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class ZigbeeDeviceHandlerStrategyTest {
    @NonNullByDefault({})
    @Mock
    private Channel averageLQIChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID averageLQIChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel lastKnownLQIChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID lastKnownLQIChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel averageRSSIChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID averageRSSIChannelUid;

    @NonNullByDefault({})
    @Mock
    private Channel lastKnownRSSIChannel;

    @NonNullByDefault({})
    @Mock
    private ChannelUID lastKnownRSSIChannelUid;

    @NonNullByDefault({})
    @Mock
    private Thing thing;

    @NonNullByDefault({})
    @Mock
    private ThingHandlerCallback thingHandlerCallback;

    @Before
    public void setUp() {
        initMocks(this);

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_RADIO_LQI_AVERAGE,
                this.averageLQIChannel,
                this.averageLQIChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_RADIO_LQI_LAST_KNOWN,
                this.lastKnownLQIChannel,
                this.lastKnownLQIChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_RADIO_RSSI_AVERAGE,
                this.averageRSSIChannel,
                this.averageRSSIChannelUid
        );

        TestUtil.initMockChannel(
                this.thing,
                HiveBindingConstants.CHANNEL_RADIO_RSSI_LAST_KNOWN,
                this.lastKnownRSSIChannel,
                this.lastKnownRSSIChannelUid
        );
    }

    @Test
    public void testUpdate() {
        /* Given */
        final String eui64 = "DEADBEEFDEADBEEF";
        final int averageLQI = 99;
        final int lastKnownLQI = 98;
        final int averageRSSI = 97;
        final int lastKnownRSSI = 96;

        final ZigbeeDeviceHandlerStrategy strategy = new ZigbeeDeviceHandlerStrategy();

        // Create the test node
        final ZigbeeDeviceFeature zigbeeDeviceFeature = ZigbeeDeviceFeature.builder()
                .eui64(TestUtil.createSimpleFeatureAttribute(new Eui64(eui64)))
                .averageLQI(TestUtil.createSimpleFeatureAttribute(averageLQI))
                .lastKnownLQI(TestUtil.createSimpleFeatureAttribute(lastKnownLQI))
                .averageRSSI(TestUtil.createSimpleFeatureAttribute(averageRSSI))
                .lastKnownRSSI(TestUtil.createSimpleFeatureAttribute(lastKnownRSSI))
                .build();

        final Node node = TestUtil.getTestNodeWithFeatures(Collections.singletonMap(
                ZigbeeDeviceFeature.class,
                zigbeeDeviceFeature
        ));


        /* When */
        strategy.handleUpdate(
                this.thing,
                this.thingHandlerCallback,
                node
        );


        /* Then */
        verify(this.thing).setProperty(
                eq(HiveBindingConstants.PROPERTY_EUI64),
                eq(eui64)
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.averageLQIChannelUid),
                eq(new DecimalType(averageLQI))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.lastKnownLQIChannelUid),
                eq(new DecimalType(lastKnownLQI))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.averageRSSIChannelUid),
                eq(new DecimalType(averageRSSI))
        );

        verify(this.thingHandlerCallback).stateUpdated(
                eq(this.lastKnownRSSIChannelUid),
                eq(new DecimalType(lastKnownRSSI))
        );
    }
}
