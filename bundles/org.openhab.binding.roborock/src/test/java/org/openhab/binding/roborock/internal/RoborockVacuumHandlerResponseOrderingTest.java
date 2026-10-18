/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.api.enums.RobotCapabilities;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.UnDefType;

/**
 * Tests the getMap / getRoomMapping response ordering: the two requests are independent, so a map
 * can be parsed and cached while the segment-id to room-name table is still empty. Installing the
 * table must re-evaluate the cached map, because while docked no further map response is due that
 * would self-correct the channel.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault({})
class RoborockVacuumHandlerResponseOrderingTest {

    /** Coordinate-to-pixel divisor of the RR map format, mirroring {@code RoomAtRobotResolver}. */
    private static final int MM = 50;

    private static final int WIDTH = 8;
    private static final int HEIGHT = 8;
    private static final int SEGMENT = 4;
    private static final int ROBOT_PIXEL_X = 3;
    private static final int ROBOT_PIXEL_Y = 3;

    @Test
    void roomMappingArrivingAfterTheMapReresolvesTheCachedMap() throws Exception {
        ThingUID thingUID = new ThingUID(RoborockBindingConstants.ROBOROCK_VACUUM, "test");
        ChannelUID currentRoomUID = new ChannelUID(thingUID, RobotCapabilities.CURRENT_ROOM.getChannel());
        Channel currentRoomChannel = ChannelBuilder.create(currentRoomUID, "String").build();

        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getChannel(RobotCapabilities.CURRENT_ROOM.getChannel())).thenReturn(currentRoomChannel);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        RoborockVacuumHandler handler = new RoborockVacuumHandler(thing, mock(ChannelTypeRegistry.class),
                mock(RoborockStateDescriptionOptionProvider.class));
        handler.setCallback(callback);

        handler.registerRequest("getMap", 1);
        handler.handleGetMap(1, mapPayloadWithRobotOnSegment());
        // the map resolves the robot's segment, but its name is not known yet
        verify(callback).stateUpdated(currentRoomUID, UnDefType.UNDEF);

        handler.installSegmentRoomNames(Map.of(SEGMENT, "Flur"));

        verify(callback).stateUpdated(currentRoomUID, new StringType("Flur"));
    }

    /** A parseable map payload whose robot position sits on a pixel of {@link #SEGMENT}. */
    private static byte[] mapPayloadWithRobotOnSegment() throws Exception {
        byte[] imageData = new byte[WIDTH * HEIGHT];
        for (int i = 0; i < imageData.length; i++) {
            // classifier 7 in the low 3 bits, segment id in the high 5 bits
            imageData[i] = (byte) ((SEGMENT << 3) | 0x07);
        }

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        byte[] mainHeader = new byte[20];
        mainHeader[2] = 20; // main header length
        payload.write(mainHeader);
        payload.write(imageBlock(imageData));
        payload.write(simpleDataBlock(8, new int[] { toMapCoordinate(ROBOT_PIXEL_X), toMapCoordinate(ROBOT_PIXEL_Y) }));
        return payload.toByteArray();
    }

    private static byte[] imageBlock(byte[] imageData) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[24];
        writeUInt16LE(header, 0, 2);
        writeUInt16LE(header, 2, 24);
        writeUInt32LE(header, 4, imageData.length);
        writeUInt32LE(header, 8, 0);
        writeUInt32LE(header, 12, 0);
        writeUInt32LE(header, 16, HEIGHT);
        writeUInt32LE(header, 20, WIDTH);
        output.write(header);
        output.write(imageData);
        return output.toByteArray();
    }

    private static byte[] simpleDataBlock(int type, int[] values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] header = new byte[8];
        writeUInt16LE(header, 0, type);
        writeUInt16LE(header, 2, 8);
        writeUInt32LE(header, 4, values.length * 4);
        output.write(header);
        for (int value : values) {
            byte[] data = new byte[4];
            writeUInt32LE(data, 0, value);
            output.write(data);
        }
        return output.toByteArray();
    }

    /** Inverse of the resolver's pixel transform for a map with {@code top} and {@code left} 0. */
    private static int toMapCoordinate(int pixel) {
        return (pixel + 1) * MM;
    }

    private static void writeUInt16LE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xFF);
        target[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private static void writeUInt32LE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xFF);
        target[offset + 1] = (byte) ((value >> 8) & 0xFF);
        target[offset + 2] = (byte) ((value >> 16) & 0xFF);
        target[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }
}
