/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.onewire.device;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS2405;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests cases for {@link DS2405}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2405Test extends DeviceTestParent<DS2405> {

    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_BASIC, DS2405.class);

        addChannel(channelName(0), "Switch");
    }

    @Test
    public void digitalChannel() throws OwException {
        digitalChannelTest(OnOffType.ON, 0);
        digitalChannelTest(OnOffType.OFF, 0);
    }

    private void digitalChannelTest(OnOffType state, int channelNo) throws OwException {
        final DS2405 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        BitSet returnValue = new BitSet(8);
        if (state == OnOffType.ON) {
            returnValue.flip(0, 7);
        }

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readBitSet(eq(testSensorId), any())).thenReturn(returnValue);

        testDevice.configureChannels();
        testDevice.refresh(mockBridgeHandler, true);

        inOrder.verify(mockBridgeHandler, times(2)).readBitSet(eq(testSensorId), any());
        inOrder.verify(mockThingHandler).postUpdate(eq(channelName(channelNo)), eq(state));
    }

    private String channelName(int channelNo) {
        return CHANNEL_DIGITAL + channelNo;
    }
}
