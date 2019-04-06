/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.BAE0910;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * Tests cases for {@link BAE0910}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class BAE0910Test extends DeviceTestParent {

    @Before
    public void setupMocks() {
        setupMocks(THING_TYPE_BAE091X);
        deviceTestClazz = BAE0910.class;
    }

    @Test
    public void pwmChannel1() {
        pwmBaseChannel(CHANNEL_PWM_FREQ1, CHANNEL_PWM_DUTY1, "/period1", "/duty1", 3);
    }

    @Test
    public void pwmChannel2() {
        pwmBaseChannel(CHANNEL_PWM_FREQ2, CHANNEL_PWM_DUTY2, "/period2", "/duty2", 4);
    }

    @Test
    public void pwmChannel3() {
        pwmBaseChannel(CHANNEL_PWM_FREQ1, CHANNEL_PWM_DUTY3, "/period1", "/duty3", 3);
    }

    @Test
    public void pwmChannel4() {
        pwmBaseChannel(CHANNEL_PWM_FREQ2, CHANNEL_PWM_DUTY4, "/period2", "/duty4", 4);
    }

    private void pwmBaseChannel(String freqChannel, String dutyChannel, String freqParam, String dutyParam, int index) {
        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put("prescaler", 5);

        addChannel(freqChannel, "Number:Frequency", new Configuration(channelConfig));
        addChannel(dutyChannel, "Number:Dimensionless");
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(
                    mockBridgeHandler.readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(freqParam))))
                    .thenReturn(new DecimalType(32768));
            Mockito.when(
                    mockBridgeHandler.readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(dutyParam))))
                    .thenReturn(new DecimalType(16384));

            testDevice.enableChannel(freqChannel);
            testDevice.enableChannel(dutyChannel);
            ((BAE0910) testDevice).configureChannels(mockBridgeHandler);

            // test configuration
            ArgumentCaptor<BitSet> configArgumentCaptor = ArgumentCaptor.forClass(BitSet.class);
            BitSet expected = new BitSet(8);
            expected.set(0);
            expected.set(2);
            inOrder.verify(mockBridgeHandler).writeBitSet(eq(testSensorId), eq(new OwserverDeviceParameter("/outc")),
                    configArgumentCaptor.capture());
            inOrder.verify(mockBridgeHandler).writeBitSet(eq(testSensorId), eq(new OwserverDeviceParameter("/pioc")),
                    configArgumentCaptor.capture());
            inOrder.verify(mockBridgeHandler).writeBitSet(eq(testSensorId), eq(new OwserverDeviceParameter("/adcc")),
                    configArgumentCaptor.capture());
            inOrder.verify(mockBridgeHandler).writeBitSet(eq(testSensorId), eq(new OwserverDeviceParameter("/tpm1c")),
                    configArgumentCaptor.capture());
            inOrder.verify(mockBridgeHandler).writeBitSet(eq(testSensorId), eq(new OwserverDeviceParameter("/tpm2c")),
                    configArgumentCaptor.capture());
            assertEquals(expected, configArgumentCaptor.getAllValues().get(index));

            // refresh
            ArgumentCaptor<State> stateArgumentCaptor = ArgumentCaptor.forClass(State.class);
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId),
                    eq(new OwserverDeviceParameter(freqParam)));
            inOrder.verify(mockThingHandler).postUpdate(eq(freqChannel), stateArgumentCaptor.capture());
            assertEquals(new QuantityType<>("15.2587890625 Hz"), stateArgumentCaptor.getValue());
            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId),
                    eq(new OwserverDeviceParameter(dutyParam)));
            inOrder.verify(mockThingHandler).postUpdate(eq(dutyChannel), stateArgumentCaptor.capture());
            assertEquals(new QuantityType<>("50 %"), stateArgumentCaptor.getValue());

            // write
            ArgumentCaptor<DecimalType> decimalTypeArgumentCaptor = ArgumentCaptor.forClass(DecimalType.class);
            ((BAE0910) testDevice).writeChannel(mockBridgeHandler, freqChannel, new QuantityType<>("50000 Hz"));
            inOrder.verify(mockBridgeHandler).writeDecimalType(eq(testSensorId),
                    eq(new OwserverDeviceParameter(freqParam)), decimalTypeArgumentCaptor.capture());
            assertEquals(new DecimalType(10), decimalTypeArgumentCaptor.getValue());
            ((BAE0910) testDevice).writeChannel(mockBridgeHandler, dutyChannel, new QuantityType<>("25 %"));
            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId),
                    eq(new OwserverDeviceParameter(freqParam)));
            inOrder.verify(mockBridgeHandler).writeDecimalType(eq(testSensorId),
                    eq(new OwserverDeviceParameter(dutyParam)), decimalTypeArgumentCaptor.capture());
            assertEquals(new DecimalType(8192), decimalTypeArgumentCaptor.getValue());

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
