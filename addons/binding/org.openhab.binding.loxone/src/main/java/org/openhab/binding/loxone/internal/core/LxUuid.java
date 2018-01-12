/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Unique identifier of an object on Loxone Miniserver.
 * <p>
 * It is defined by the Miniserver. UUID can represent a control, room, category, etc. and provides a unique ID space
 * across all objects residing on the Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxUuid {
    private String uuid;
    private String uuidOriginal;
    private boolean updated;

    /**
     * Create a new {@link LxUuid} object from an UUID on a Miniserver.
     *
     * @param uuid
     *            identifier retrieved from Loxone Miniserver
     */
    public LxUuid(String uuid) {
        init(uuid);
    }

    public LxUuid(byte data[], int offset) {
        String id = String.format("%08x-%04x-%04x-%02x%02x%02x%02x%02x%02x%02x%02x",
                ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt(),
                ByteBuffer.wrap(data, offset + 4, 2).order(ByteOrder.LITTLE_ENDIAN).getShort(),
                ByteBuffer.wrap(data, offset + 6, 2).order(ByteOrder.LITTLE_ENDIAN).getShort(), data[offset + 8],
                data[offset + 9], data[offset + 10], data[offset + 11], data[offset + 12], data[offset + 13],
                data[offset + 14], data[offset + 15]);
        init(id);
    }

    private void init(String uuid) {
        uuidOriginal = uuid;
        this.uuid = uuidOriginal.replaceAll("[^a-zA-Z0-9-]", "-").toUpperCase();
        updated = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        LxUuid id = (LxUuid) o;
        return uuid.equals(id.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return uuid;
    }

    /**
     * Returns an original string that was used to create UUID.
     *
     * @return
     *         original string for the UUID
     */

    public String getOriginalString() {
        return uuidOriginal;
    }

    /**
     * Indicate the object corresponding to UUID has recently been updated.
     *
     * @param updated
     *            true if object has been updated
     */
    void setUpdate(boolean updated) {
        this.updated = updated;
    }

    /**
     * See if the object corresponding to UUID has been recently updated.
     *
     * @return
     *         true if object was updated
     */
    boolean getUpdate() {
        return updated;
    }
}
