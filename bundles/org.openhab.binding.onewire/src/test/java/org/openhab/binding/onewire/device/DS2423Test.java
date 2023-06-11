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

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS2423;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * Tests cases for {@link DS2423}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2423Test extends DeviceTestParent<DS2423> {

    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_BASIC, DS2423.class);

        for (int i = 0; i < 2; i++) {
            addChannel(channelName(i), "Number");
        }
    }

    @Test
    public void counterChannelTest() {
        List<State> returnValue = new ArrayList<>();
        returnValue.add(new DecimalType(1408));
        returnValue.add(new DecimalType(3105));

        final DS2423 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalTypeArray(eq(testSensorId), any())).thenReturn(returnValue);

            testDevice.configureChannels();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler, times(1)).readDecimalTypeArray(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(channelName(0)), eq(returnValue.get(0)));
            inOrder.verify(mockThingHandler).postUpdate(eq(channelName(1)), eq(returnValue.get(1)));
        } catch (OwException e) {
            fail("caught unexpected OwException");
        }
    }

    private String channelName(int channelNo) {
        return CHANNEL_COUNTER + channelNo;
    }
}
