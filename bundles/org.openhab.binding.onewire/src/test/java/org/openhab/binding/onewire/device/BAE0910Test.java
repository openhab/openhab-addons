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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.BAE0910;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * Tests cases for {@link BAE0910}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BAE0910Test extends DeviceTestParent<BAE0910> {
    @BeforeEach
    public void setupMocks() {
        setupMocks(THING_TYPE_BAE091X, BAE0910.class);
    }

    // pin 1: counter

    @Test
    public void counter() throws OwException {
        addChannel(CHANNEL_COUNTER, "Number");
        final BAE0910 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter("/counter"))))
                .thenReturn(new DecimalType(34567));

        testDevice.enableChannel(CHANNEL_COUNTER);
        testDevice.configureChannels(mockBridgeHandler);

        // refresh
        ArgumentCaptor<State> stateArgumentCaptor = ArgumentCaptor.forClass(State.class);
        testDevice.refresh(mockBridgeHandler, true);
        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId),
                eq(new OwserverDeviceParameter("/counter")));
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_COUNTER), stateArgumentCaptor.capture());
        assertEquals(new DecimalType(34567), stateArgumentCaptor.getValue());

        // write
        assertFalse(testDevice.writeChannel(mockBridgeHandler, CHANNEL_COUNTER, new DecimalType(12345)));

        inOrder.verifyNoMoreInteractions();
    }

    // pin 2: digital2 or pwm1

    @Test
    public void digitalOut2() throws OwException {
        addChannel(CHANNEL_DIGITAL2, "Switch");
        digitalBaseChannel(CHANNEL_DIGITAL2, bitSet(3, 4), 0, "/out", bitSet(0), true);
    }

    @Test
    public void pwm4() throws OwException {
        pwmBaseChannel(CHANNEL_PWM_FREQ2, CHANNEL_PWM_DUTY4, "/period2", "/duty4", 2);
    }

    // pin 6: pio or pwm 3

    @Test
    public void digital6PioIn() throws OwException {
        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put("pulldevice", "pulldown");
        channelConfig.put("mode", "input");
        addChannel(CHANNEL_DIGITAL6, "Switch", new Configuration(channelConfig));
        digitalBaseChannel(CHANNEL_DIGITAL6, bitSet(1, 2, 3, 4), 1, "/pio", bitSet(0), false);
    }

    @Test
    public void digital6PioOut() throws OwException {
        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put("mode", "output");
        addChannel(CHANNEL_DIGITAL6, "Switch", new Configuration(channelConfig));
        digitalBaseChannel(CHANNEL_DIGITAL6, bitSet(0, 3, 4), 1, "/pio", bitSet(0), true);
    }

    @Test
    public void pwm3() throws OwException {
        pwmBaseChannel(CHANNEL_PWM_FREQ1, CHANNEL_PWM_DUTY3, "/period1", "/duty3", 1);
    }

    // pin 7: analog, output, pwm2

    @Test
    public void analog() throws OwException {
        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put("hires", "true");
        addChannel(CHANNEL_VOLTAGE, "Number:ElectricPotential", new Configuration(channelConfig));

        final BAE0910 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter("/adc"))))
                .thenReturn(new DecimalType(5.2));

        testDevice.enableChannel(CHANNEL_VOLTAGE);
        testDevice.configureChannels(mockBridgeHandler);

        // test configuration
        assertEquals(bitSet(3, 4), checkConfiguration(2));

        // refresh
        ArgumentCaptor<State> stateArgumentCaptor = ArgumentCaptor.forClass(State.class);
        testDevice.refresh(mockBridgeHandler, true);
        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter("/adc")));
        inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_VOLTAGE), stateArgumentCaptor.capture());
        assertEquals(new QuantityType<>("5.2 V"), stateArgumentCaptor.getValue());

        // write (should fail)
        assertFalse(testDevice.writeChannel(mockBridgeHandler, CHANNEL_VOLTAGE, new QuantityType<>("3 V")));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void digitalOut7() throws OwException {
        addChannel(CHANNEL_DIGITAL7, "Switch");
        digitalBaseChannel(CHANNEL_DIGITAL7, bitSet(4), 4, "/tpm2c", bitSet(4, 7), true);
    }

    @Test
    public void pwm2() throws OwException {
        pwmBaseChannel(CHANNEL_PWM_FREQ2, CHANNEL_PWM_DUTY2, "/period2", "/duty2", 2);
    }

    // pin 8: digital in, digital out or pwm

    @Test
    public void digitalIn8() throws OwException {
        addChannel(CHANNEL_DIGITAL8, "Switch", new ChannelTypeUID(BINDING_ID, "bae-in"));
        digitalBaseChannel(CHANNEL_DIGITAL8, bitSet(4, 5), 3, "/tpm1c", bitSet(4, 5, 7), false);
    }

    @Test
    public void digitalOut8() throws OwException {
        addChannel(CHANNEL_DIGITAL8, "Switch");
        digitalBaseChannel(CHANNEL_DIGITAL8, bitSet(4), 3, "/tpm1c", bitSet(4, 7), true);
    }

    @Test
    public void pwm1() throws OwException {
        pwmBaseChannel(CHANNEL_PWM_FREQ1, CHANNEL_PWM_DUTY1, "/period1", "/duty1", 1);
    }

    /**
     * base test for digital channels
     *
     * @param channel channel name
     * @param configBitSet expected config register
     * @param configRegister config register number
     * @param channelParam channel parameter
     * @param returnBitSet which bitset should be returned on read
     * @param isOutput if this channel is an output
     */
    private void digitalBaseChannel(String channel, BitSet configBitSet, int configRegister, String channelParam,
            BitSet returnBitSet, boolean isOutput) throws OwException {
        final BAE0910 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readBitSet(eq(testSensorId), eq(new OwserverDeviceParameter(channelParam))))
                .thenReturn(returnBitSet);

        testDevice.enableChannel(channel);
        testDevice.configureChannels(mockBridgeHandler);

        // test configuration
        assertEquals(configBitSet, checkConfiguration(configRegister));

        // refresh
        ArgumentCaptor<State> stateArgumentCaptor = ArgumentCaptor.forClass(State.class);
        testDevice.refresh(mockBridgeHandler, true);
        inOrder.verify(mockBridgeHandler).readBitSet(eq(testSensorId), eq(new OwserverDeviceParameter(channelParam)));
        inOrder.verify(mockThingHandler).postUpdate(eq(channel), stateArgumentCaptor.capture());
        assertEquals(OnOffType.ON, stateArgumentCaptor.getValue());

        // write
        if (isOutput) {
            ArgumentCaptor<BitSet> bitSetArgumentCaptor = ArgumentCaptor.forClass(BitSet.class);
            assertTrue(testDevice.writeChannel(mockBridgeHandler, channel, OnOffType.ON));
            inOrder.verify(mockBridgeHandler).writeBitSet(eq(testSensorId),
                    eq(new OwserverDeviceParameter(channelParam)), bitSetArgumentCaptor.capture());
            assertEquals(returnBitSet, bitSetArgumentCaptor.getValue());
        } else {
            assertFalse(testDevice.writeChannel(mockBridgeHandler, channel, OnOffType.ON));
        }

        inOrder.verifyNoMoreInteractions();
    }

    /**
     * base test case for PWM channels
     *
     * @param freqChannel channel name for frequency
     * @param dutyChannel channel name for duty cycle
     * @param freqParam owfs parameter for frequency
     * @param dutyParam owfs parameter for duty cycle
     * @param registerIndex index for TPM configuration register
     */
    private void pwmBaseChannel(String freqChannel, String dutyChannel, String freqParam, String dutyParam,
            int registerIndex) throws OwException {
        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put("prescaler", 5);
        addChannel(freqChannel, "Number:Frequency", new Configuration(channelConfig));
        addChannel(dutyChannel, "Number:Dimensionless");

        final BAE0910 testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(freqParam))))
                .thenReturn(new DecimalType(32768));
        Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(dutyParam))))
                .thenReturn(new DecimalType(16384));

        testDevice.enableChannel(freqChannel);
        testDevice.enableChannel(dutyChannel);
        testDevice.configureChannels(mockBridgeHandler);

        // test configuration
        assertEquals(bitSet(0, 2), checkConfiguration(registerIndex + 2));

        // refresh
        ArgumentCaptor<State> stateArgumentCaptor = ArgumentCaptor.forClass(State.class);
        testDevice.refresh(mockBridgeHandler, true);
        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(freqParam)));
        inOrder.verify(mockThingHandler).postUpdate(eq(freqChannel), stateArgumentCaptor.capture());
        assertEquals(new QuantityType<>("15.2587890625 Hz"), stateArgumentCaptor.getValue());
        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(dutyParam)));
        inOrder.verify(mockThingHandler).postUpdate(eq(dutyChannel), stateArgumentCaptor.capture());
        assertEquals(new QuantityType<>("50 %"), stateArgumentCaptor.getValue());

        // write
        ArgumentCaptor<DecimalType> decimalTypeArgumentCaptor = ArgumentCaptor.forClass(DecimalType.class);
        assertTrue(testDevice.writeChannel(mockBridgeHandler, freqChannel, new QuantityType<>("50000 Hz")));
        inOrder.verify(mockBridgeHandler).writeDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(freqParam)),
                decimalTypeArgumentCaptor.capture());
        assertEquals(new DecimalType(10), decimalTypeArgumentCaptor.getValue());
        testDevice.writeChannel(mockBridgeHandler, dutyChannel, new QuantityType<>("25 %"));
        inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(freqParam)));
        inOrder.verify(mockBridgeHandler).writeDecimalType(eq(testSensorId), eq(new OwserverDeviceParameter(dutyParam)),
                decimalTypeArgumentCaptor.capture());
        assertEquals(new DecimalType(8192), decimalTypeArgumentCaptor.getValue());

        inOrder.verifyNoMoreInteractions();
    }

    /**
     * check if all registers are written and return one
     *
     * @param registerIndex number of register to return
     * @return this register's BitSet
     * @throws OwException
     */
    private BitSet checkConfiguration(int registerIndex) throws OwException {
        ArgumentCaptor<BitSet> configArgumentCaptor = ArgumentCaptor.forClass(BitSet.class);
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

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
        return configArgumentCaptor.getAllValues().get(registerIndex);
    }

    /**
     * BitSet with pre-set bits
     *
     * @param bits which bits to set
     * @return the BitSet
     */
    private BitSet bitSet(int... bits) {
        BitSet bitSet = new BitSet(8);
        Arrays.stream(bits).forEach(b -> bitSet.set(b));
        return bitSet;
    }
}
