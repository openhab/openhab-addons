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
package org.openhab.binding.ism8.internal;

import static org.mockito.ArgumentMatchers.eq;
import static org.openhab.binding.ism8.internal.Ism8BindingConstants.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.ism8.server.DataPointBool;
import org.openhab.binding.ism8.server.DataPointByteValue;
import org.openhab.binding.ism8.server.DataPointChangedEvent;
import org.openhab.binding.ism8.server.DataPointValue;
import org.openhab.binding.ism8.server.IDataPoint;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Leo Siepel - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class Ism8HandlerTest {

    private @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;
    private @NonNullByDefault({}) Thing ism8Thing;
    private @NonNullByDefault({}) Ism8Handler thingHandler;
    private ThingUID thingUID = new ThingUID(BINDING_ID, "ism8server");
    private ChannelUID channel1001 = new ChannelUID(thingUID, "switch1");
    private ChannelUID channel9001 = new ChannelUID(thingUID, "tempC");
    private ChannelUID channel9002 = new ChannelUID(thingUID, "tempD");
    private ChannelUID channel20001 = new ChannelUID(thingUID, "mode1");

    @BeforeEach
    public void initialize() {
        Configuration config = new Configuration();

        ism8Thing = ThingBuilder.create(THING_TYPE_DEVICE, thingUID).withConfiguration(config)
                .withChannel(ChannelBuilder.create(channel9002, "Number:Temperature")
                        .withConfiguration(createChannelConfig("5", "9.002")).build())
                .withChannel(ChannelBuilder.create(channel9001, "Number:Temperature")
                        .withConfiguration(createChannelConfig("4", "9.001")).build())
                .withChannel(ChannelBuilder.create(channel1001, "Switch")
                        .withConfiguration(createChannelConfig("9", "1.001")).build())
                .withChannel(ChannelBuilder.create(channel20001, "Switch")
                        .withConfiguration(createChannelConfig("2", "20.001")).build())
                .build();

        thingHandler = new Ism8Handler(ism8Thing);
        thingHandler.initialize();
    }

    private Configuration createChannelConfig(String id, String type) {
        Configuration config = new Configuration();
        config.put(CHANNEL_CONFIG_ID, id);
        config.put(CHANNEL_CONFIG_TYPE, type);
        return config;
    }

    @Test
    public void process1001MessageAndUpdateChannel() {
        // arrange
        IDataPoint dataPoint = new DataPointBool(9, "1.001", "Datapoint_1.001");
        dataPoint.processData(HexUtils.hexToBytes("0009030100"));
        DataPointChangedEvent event = new DataPointChangedEvent(new Object(), dataPoint);
        thingHandler.setCallback(thingHandlerCallback);

        // act
        thingHandler.dataPointChanged(event);

        // assert
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channel1001), eq(OnOffType.from(false)));
    }

    @Test
    public void process9001MessageAndUpdateChannel() {
        // arrange
        IDataPoint dataPoint = new DataPointValue(4, "9.001", "Datapoint_9.001");
        dataPoint.processData(HexUtils.hexToBytes("000403020FE9"));
        DataPointChangedEvent event = new DataPointChangedEvent(new Object(), dataPoint);
        thingHandler.setCallback(thingHandlerCallback);

        // act
        thingHandler.dataPointChanged(event);

        // assert
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channel9001),
                eq(new QuantityType<Temperature>(40.49999909475446, SIUnits.CELSIUS)));
    }

    @Test
    public void process20001MessageAndUpdateChannel() {
        // arrange
        IDataPoint dataPoint = new DataPointByteValue(2, "20.102", "Datapoint_20.102");
        dataPoint.processData(HexUtils.hexToBytes("0002030101"));
        DataPointChangedEvent event = new DataPointChangedEvent(new Object(), dataPoint);
        thingHandler.setCallback(thingHandlerCallback);

        // act
        thingHandler.dataPointChanged(event);

        // assert
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channel20001),
                eq(new QuantityType<Dimensionless>(1, Units.ONE)));
    }
}
