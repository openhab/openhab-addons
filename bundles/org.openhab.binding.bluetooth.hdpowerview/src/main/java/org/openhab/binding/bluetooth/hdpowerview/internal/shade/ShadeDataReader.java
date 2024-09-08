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
package org.openhab.binding.bluetooth.hdpowerview.internal.shade;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;

/**
 * Parser for data returned by an HD PowerView Generation 3 Shade.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDataReader {

    // internal values 0 to 4000 scale to real position values 0% to 100%
    private static final double SCALE = 40;

    // indexes to data field positions in the incoming bytes
    private static final int INDEX_MANUFACTURER_ID = 0;
    private static final int INDEX_HOME_ID = 2;
    private static final int INDEX_TYPE_ID = 4;
    private static final int INDEX_PRIMARY = 5;
    private static final int INDEX_SECONDARY = 7;
    private static final int INDEX_TILT = 9;
    private static final int INDEX_VELOCITY = 10;

    private int manufacturerId;
    private int homeId;
    private int typeId;
    private double primary;
    private double secondary;
    private double tilt;
    private double velocity; // not 100% sure about this

    public ShadeDataReader() {
    }

    public int getManufacturerId() {
        return manufacturerId;
    }

    public int getHomeId() {
        return homeId;
    }

    public PercentType getPrimary() {
        return new PercentType(BigDecimal.valueOf(primary));
    }

    public PercentType getSecondary() {
        return new PercentType(BigDecimal.valueOf(secondary));
    }

    public PercentType getTilt() {
        return new PercentType(BigDecimal.valueOf(tilt));
    }

    public int getTypeId() {
        return typeId;
    }

    public double getVelocity() {
        return velocity;
    }

    public ShadeDataReader setBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        manufacturerId = buffer.getShort(INDEX_MANUFACTURER_ID);
        homeId = buffer.getShort(INDEX_HOME_ID);
        typeId = buffer.get(INDEX_TYPE_ID);
        velocity = buffer.get(INDEX_VELOCITY);

        primary = Math.max(0, Math.min(100, buffer.getShort(INDEX_PRIMARY) / SCALE));
        secondary = Math.max(0, Math.min(100, buffer.getShort(INDEX_SECONDARY) / SCALE));
        tilt = Math.max(0, Math.min(100, buffer.get(INDEX_TILT)));

        return this;
    }
}
