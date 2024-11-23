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
package org.openhab.binding.openthermgateway.internal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * The {@link UIntDataItem} represents an 8 or 16 bit unsigned integer.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class UIntDataItem extends DataItem {

    private @Nullable Unit<?> unit;

    public @Nullable Unit<?> getUnit() {
        return unit;
    }

    public UIntDataItem(Msg msg, ByteType byteType, String subject) {
        this(msg, byteType, subject, null);
    }

    public UIntDataItem(Msg msg, ByteType byteType, String subject, @Nullable Unit<?> unit) {
        super(msg, byteType, subject, null);

        this.unit = unit;
    }

    @Override
    public State createState(Message message) {
        @Nullable
        Unit<?> unit = getUnit();
        int value = message.getUInt(super.getByteType());
        return (unit == null) ? new DecimalType(value) : new QuantityType<>(value, unit);
    }
}
