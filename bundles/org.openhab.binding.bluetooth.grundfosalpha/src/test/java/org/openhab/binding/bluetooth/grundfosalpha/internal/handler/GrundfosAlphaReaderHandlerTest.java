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
package org.openhab.binding.bluetooth.grundfosalpha.internal.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bluetooth.grundfosalpha.internal.GrundfosAlphaBindingConstants;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.util.HexUtils;

/**
 * Test the {@link GrundfosAlphaReaderHandler}.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class GrundfosAlphaReaderHandlerTest {

    private @NonNullByDefault({}) @Mock Thing thingMock;
    private @NonNullByDefault({}) @Mock ThingHandlerCallback callback;

    @Test
    public void testMessageType0xf1() {
        byte[] data = HexUtils.hexToBytes("15f130017a5113030300994109589916613003004005");
        final BluetoothScanNotification scanNotification = new BluetoothScanNotification();
        scanNotification.setManufacturerData(data);
        final GrundfosAlphaReaderHandler handler = new GrundfosAlphaReaderHandler(thingMock);
        handler.setCallback(callback);
        handler.onScanRecordReceived(scanNotification);

        verify(callback).statusUpdated(thingMock,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verifyNoMoreInteractions(callback);
    }

    @Test
    public void testMessageType0xf2() {
        when(thingMock.getUID()).thenReturn(new ThingUID(GrundfosAlphaBindingConstants.THING_TYPE_MI401, "dummy"));

        byte[] data = HexUtils.hexToBytes("14f23001650305065419b9180f011f007c1878170d");
        final BluetoothScanNotification scanNotification = new BluetoothScanNotification();
        scanNotification.setManufacturerData(data);
        final GrundfosAlphaReaderHandler handler = new GrundfosAlphaReaderHandler(thingMock);
        handler.setCallback(callback);
        handler.onScanRecordReceived(scanNotification);

        verify(callback).statusUpdated(thingMock,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verify(callback).stateUpdated(
                new ChannelUID(thingMock.getUID(), GrundfosAlphaBindingConstants.CHANNEL_BATTERY_LEVEL),
                new QuantityType<>(75, Units.PERCENT));
        verify(callback).stateUpdated(
                new ChannelUID(thingMock.getUID(), GrundfosAlphaBindingConstants.CHANNEL_FLOW_RATE),
                new QuantityType<>(0.98939496, Units.CUBICMETRE_PER_HOUR));
        verify(callback).stateUpdated(
                new ChannelUID(thingMock.getUID(), GrundfosAlphaBindingConstants.CHANNEL_PUMP_HEAD),
                new QuantityType<>(1.9315165, SIUnits.METRE));
        verify(callback).stateUpdated(
                new ChannelUID(thingMock.getUID(), GrundfosAlphaBindingConstants.CHANNEL_PUMP_TEMPERATURE),
                new QuantityType<>(31, SIUnits.CELSIUS));

        verifyNoMoreInteractions(callback);
    }
}
