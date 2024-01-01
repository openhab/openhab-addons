/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.openhab.binding.onewire.internal.OwBindingConstants.CHANNEL_PRESENT;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.AbstractOwDevice;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Abtract test class for onewire devices.
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public abstract class DeviceTestParent<T extends AbstractOwDevice> {
    private @Nullable Class<T> deviceTestClazz;

    protected @Mock @NonNullByDefault({}) OwBaseThingHandler mockThingHandler;
    protected @Mock @NonNullByDefault({}) OwserverBridgeHandler mockBridgeHandler;
    protected @Mock @NonNullByDefault({}) Thing mockThing;

    protected SensorId testSensorId = new SensorId("00.000000000000");

    public void setupMocks(ThingTypeUID thingTypeUID, Class<T> deviceTestClazz) {
        this.deviceTestClazz = deviceTestClazz;

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

    public T instantiateDevice() {
        final Class<T> deviceTestClazz = this.deviceTestClazz;
        if (deviceTestClazz == null) {
            throw new IllegalStateException("deviceTestClazz is null");
        }
        try {
            Constructor<T> constructor = deviceTestClazz.getConstructor(SensorId.class, OwBaseThingHandler.class);
            T testDevice = constructor.newInstance(testSensorId, mockThingHandler);
            assertNotNull(testDevice);
            return testDevice;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public T instantiateDevice(OwSensorType sensorType) {
        final Class<T> deviceTestClazz = this.deviceTestClazz;
        if (deviceTestClazz == null) {
            throw new IllegalStateException("deviceTestClazz is null");
        }
        try {
            Constructor<T> constructor = deviceTestClazz.getConstructor(SensorId.class, OwSensorType.class,
                    OwBaseThingHandler.class);
            T testDevice = constructor.newInstance(testSensorId, sensorType, mockThingHandler);
            assertNotNull(testDevice);
            return testDevice;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public void presenceTest(OnOffType state) throws OwException {
        final T testDevice = instantiateDevice();
        final InOrder inOrder = Mockito.inOrder(mockThingHandler, mockBridgeHandler);

        Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(state);
        testDevice.checkPresence(mockBridgeHandler);

        inOrder.verify(mockThingHandler).updatePresenceStatus(eq(state));
    }
}
