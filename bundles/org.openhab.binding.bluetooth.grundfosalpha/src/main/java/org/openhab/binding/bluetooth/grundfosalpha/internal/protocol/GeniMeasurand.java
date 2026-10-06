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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Objects;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Describes class-10 member locations, encodings and physical units for {@link GeniResponseDecoder}.
 * Values are converted to their publication unit before applying measurement-specific decimal precision.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum GeniMeasurand {
    Flow(GeniReadRequest.FlowHead, 0, ValueFormat.Float32, GeniUnit.CubicMetrePerSecond, Units.CUBICMETRE_PER_HOUR, 3),
    Head(GeniReadRequest.FlowHead, 4, ValueFormat.Float32, GeniUnit.TenthMillimetre, SIUnits.METRE, 5),
    VoltageAC(GeniReadRequest.Power, 0, ValueFormat.Float32, GeniUnit.Volt, 1),
    PowerConsumption(GeniReadRequest.Power, 12, ValueFormat.Float32, GeniUnit.Watt, 1),
    MotorSpeed(GeniReadRequest.Power, 20, ValueFormat.Float32, GeniUnit.RevolutionsPerMinute, 0);

    private final GeniReadRequest readRequest;
    private final int offset;
    private final ValueFormat format;
    private final Unit<?> sourceUnit;
    private final Unit<?> unit;
    private final int decimals;

    GeniMeasurand(GeniReadRequest readRequest, int offset, ValueFormat format, GeniUnit sourceUnit, int decimals) {
        this(readRequest, offset, format, sourceUnit, sourceUnit.unit(), decimals);
    }

    GeniMeasurand(GeniReadRequest readRequest, int offset, ValueFormat format, GeniUnit sourceUnit, Unit<?> unit,
            int decimals) {
        this.readRequest = readRequest;
        this.offset = offset;
        this.format = format;
        this.sourceUnit = sourceUnit.unit();
        this.unit = unit;
        this.decimals = decimals;
    }

    public GeniReadRequest readRequest() {
        return readRequest;
    }

    public int offset() {
        return offset;
    }

    int valueLength() {
        return switch (format) {
            case Float32 -> Float.BYTES;
            case Float64 -> Double.BYTES;
            case SignedInt32 -> Integer.BYTES;
        };
    }

    double readValue(ByteBuffer data, int dataOffset) {
        return switch (format) {
            case Float32 -> data.getFloat(dataOffset + offset);
            case Float64 -> data.getDouble(dataOffset + offset);
            case SignedInt32 -> data.getInt(dataOffset + offset);
        };
    }

    boolean isValid(double value) {
        return Double.isFinite(value);
    }

    /**
     * Returns the publication unit of the decoded and rounded value.
     */
    public Unit<?> unit() {
        return unit;
    }

    BigDecimal scale(BigDecimal value) {
        var converted = Objects.requireNonNull(new QuantityType<>(value, sourceUnit).toUnit(unit));
        return converted.toBigDecimal().setScale(decimals, RoundingMode.HALF_UP);
    }

    private enum ValueFormat {
        Float32,
        Float64,
        SignedInt32
    }
}
