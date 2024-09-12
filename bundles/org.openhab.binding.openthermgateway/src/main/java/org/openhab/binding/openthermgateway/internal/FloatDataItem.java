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
 * The {@link FloatDataItem} represents a 2-byte float value in a 2's complement format.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class FloatDataItem extends DataItem {

    private @Nullable Unit<?> unit;

    public @Nullable Unit<?> getUnit() {
        return unit;
    }

    public FloatDataItem(Msg msg, String subject) {
        this(msg, subject, null, null);
    }

    public FloatDataItem(Msg msg, String subject, Unit<?> unit) {
        this(msg, subject, unit, null);
    }

    public FloatDataItem(Msg msg, String subject, CodeType codetype) {
        this(msg, subject, null, codetype);
    }

    public FloatDataItem(Msg msg, String subject, @Nullable Unit<?> unit, @Nullable CodeType codetype) {
        super(msg, ByteType.BOTH, subject, codetype);

        this.unit = unit;
    }

    @Override
    public State createState(Message message) {
        @Nullable
        Unit<?> unit = this.getUnit();
        float value = message.getFloat();
        return (unit == null) ? new DecimalType(value) : new QuantityType<>(value, unit);
    }
}
