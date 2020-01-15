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
package org.openhab.binding.onewire.device;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.onewire.internal.OwBindingConstants.CHANNEL_PRESENT;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.junit.Assert;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.AbstractOwDevice;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;

/**
 * Abtract test class for onewire devices.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class DeviceTestParent {
    protected @Nullable Class<? extends AbstractOwDevice> deviceTestClazz;

    @Mock
    @NonNullByDefault({})
    protected OwBaseThingHandler mockThingHandler;

    @Mock
    @NonNullByDefault({})
    protected OwserverBridgeHandler mockBridgeHandler;

    @Mock
    @NonNullByDefault({})
    protected Thing mockThing;

    protected SensorId testSensorId = new SensorId("00.000000000000");

    public void setupMocks(ThingTypeUID thingTypeUID) {
        initMocks(this);

        Mockito.when(mockThingHandler.getThing()).thenReturn(mockThing);
        Mockito.when(mockThing.getUID()).thenReturn(new ThingUID(thingTypeUID, "testsensor"));

        addChannel(CHANNEL_PRESENT, "Switch");
    }

    public void addChannel(String channelId, String itemType) {
        Channel channel = ChannelBuilder.create(new ChannelUID(mockThing.getUID(), channelId), itemType).build();
        Mockito.when(mockThing.getChannel(channelId)).thenReturn(channel);
    }

    public void addChannel(String channelId, String itemType, Configuration channelConfiguration) {
        Channel channel = ChannelBuilder.create(new ChannelUID(mockThing.getUID(), channelId), itemType)
                .withConfiguration(channelConfiguration).build();
        Mockito.when(mockThing.getChannel(channelId)).thenReturn(channel);
    }

    public void addChannel(String channelId, String itemType, ChannelTypeUID channelTypeUID) {
        Channel channel = ChannelBuilder.create(new ChannelUID(mockThing.getUID(), channelId), itemType)
                .withType(channelTypeUID).build();
        Mockito.when(mockThing.getChannel(channelId)).thenReturn(channel);
    }

    public AbstractOwDevice instantiateDevice() throws IllegalStateException {
        final Class<? extends AbstractOwDevice> deviceTestClazz = this.deviceTestClazz;
        if (deviceTestClazz == null) {
            throw new IllegalStateException("deviceTestClazz is null");
        }
        try {
            Constructor<?> constructor = deviceTestClazz.getConstructor(SensorId.class, OwBaseThingHandler.class);
            AbstractOwDevice testDevice = (AbstractOwDevice) constructor
                    .newInstance(new Object[] { testSensorId, mockThingHandler });
            Assert.assertNotNull(testDevice);
            return testDevice;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public AbstractOwDevice instantiateDevice(OwSensorType sensorType) {
        final Class<? extends AbstractOwDevice> deviceTestClazz = this.deviceTestClazz;
        if (deviceTestClazz == null) {
            throw new IllegalStateException("deviceTestClazz is null");
        }
        try {
            Constructor<?> constructor = deviceTestClazz.getConstructor(SensorId.class, OwSensorType.class,
                    OwBaseThingHandler.class);
            AbstractOwDevice testDevice = (AbstractOwDevice) constructor
                    .newInstance(new Object[] { testSensorId, sensorType, mockThingHandler });
            Assert.assertNotNull(testDevice);
            return testDevice;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public void presenceTest(OnOffType state) {
        final AbstractOwDevice testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(state);
            testDevice.checkPresence(mockBridgeHandler);

            inOrder.verify(mockThingHandler).updatePresenceStatus(eq(state));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
