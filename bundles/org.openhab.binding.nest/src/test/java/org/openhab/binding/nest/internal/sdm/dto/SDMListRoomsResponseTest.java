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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.fromJson;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMRoomInfoTrait;

/**
 * Tests deserialization of {@link org.openhab.binding.nest.internal.sdm.dto.SDMListRoomsResponse}s
 * from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMListRoomsResponseTest {

    @Test
    public void deserializeListDevicesResponse() throws IOException {
        SDMListRoomsResponse response = fromJson("list-rooms-response.json", SDMListRoomsResponse.class);
        assertThat(response, is(notNullValue()));

        List<SDMRoom> rooms = response.rooms;
        assertThat(rooms, is(notNullValue()));
        assertThat(rooms, hasSize(2));

        SDMRoom room = rooms.get(0);
        assertThat(room, is(notNullValue()));
        assertThat(room.name.name, is("enterprises/project-id/structures/structure-id/rooms/kitchen-room-id"));
        SDMTraits traits = room.traits;
        assertThat(traits.traitList(), hasSize(1));
        SDMRoomInfoTrait roomInfo = room.traits.roomInfo;
        assertThat(roomInfo, is(notNullValue()));
        assertThat(roomInfo.customName, is("Kitchen"));

        room = rooms.get(1);
        assertThat(room, is(notNullValue()));
        assertThat(room.name.name, is("enterprises/project-id/structures/structure-id/rooms/living-room-id"));
        traits = room.traits;
        assertThat(traits.traitList(), hasSize(1));
        roomInfo = room.traits.roomInfo;
        assertThat(roomInfo, is(notNullValue()));
        assertThat(roomInfo.customName, is("Living"));
    }
}
