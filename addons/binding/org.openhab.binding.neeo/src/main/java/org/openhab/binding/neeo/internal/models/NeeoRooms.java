/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * The model representing Neeo Rooms (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoRooms {

    /** The rooms. */
    private final NeeoRoom[] rooms;

    /**
     * Instantiates a new neeo rooms.
     *
     * @param rooms the rooms
     */
    public NeeoRooms(NeeoRoom[] rooms) {
        this.rooms = rooms;
    }

    /**
     * Gets the rooms.
     *
     * @return the rooms
     */
    public NeeoRoom[] getRooms() {
        return rooms;
    }

    /**
     * Gets the room.
     *
     * @param key the key
     * @return the room
     */
    public NeeoRoom getRoom(String key) {
        for (NeeoRoom room : rooms) {
            if (StringUtils.equalsIgnoreCase(key, room.getKey())) {
                return room;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NeeoRooms [rooms=" + Arrays.toString(rooms) + "]";
    }
}
