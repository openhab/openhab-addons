/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.onewire.device;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2423;
import org.eclipse.smarthome.binding.onewire.test.AbstractDeviceTest;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests cases for {@link DS2423}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DS2423Test extends AbstractDeviceTest {

    @Before
    public void setupMocks() {
        setupMocks(THING_TYPE_COUNTER2);
        deviceTestClazz = DS2423.class;

        for (int i = 0; i < 2; i++) {
            addChannel(channelName(i), "Number");
        }
    }

    @Test
    public void presenceTestOn() {
        presenceTest(OnOffType.ON);
    }

    @Test
    public void presenceTestOff() {
        presenceTest(OnOffType.OFF);
    }

    @Test
    public void counterChannelTest() {
        instantiateDevice();

        List<State> returnValue = new ArrayList<>();
        returnValue.add(new DecimalType(1408));
        returnValue.add(new DecimalType(3105));

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalTypeArray(eq(testSensorId), any())).thenReturn(returnValue);

            testDevice.configureChannels();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler, times(1)).readDecimalTypeArray(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(channelName(0)), eq(returnValue.get(0)));
            inOrder.verify(mockThingHandler).postUpdate(eq(channelName(1)), eq(returnValue.get(1)));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    private String channelName(int channelNo) {
        return CHANNEL_COUNTER + String.valueOf(channelNo);
    }
}
