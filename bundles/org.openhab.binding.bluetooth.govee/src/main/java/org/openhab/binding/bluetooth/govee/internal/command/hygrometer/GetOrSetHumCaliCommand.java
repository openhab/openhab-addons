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
package org.openhab.binding.bluetooth.govee.internal.command.hygrometer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public class GetOrSetHumCaliCommand extends GoveeCommand {

    private final CompletableFuture<@Nullable QuantityType<Dimensionless>> resultHandler;
    private final @Nullable QuantityType<Dimensionless> value;

    public GetOrSetHumCaliCommand(CompletableFuture<@Nullable QuantityType<Dimensionless>> resultHandler) {
        this.value = null;
        this.resultHandler = resultHandler;
    }

    public GetOrSetHumCaliCommand(QuantityType<Dimensionless> value,
            CompletableFuture<@Nullable QuantityType<Dimensionless>> resultHandler) {
        this.value = value;
        this.resultHandler = resultHandler;
    }

    @Override
    public byte getCommandType() {
        return value != null ? WRITE_TYPE : READ_TYPE;
    }

    @Override
    public byte getCommandCode() {
        return 6;
    }

    private static short convertQuantity(QuantityType<Dimensionless> quantity) {
        var percentQuantity = quantity.toUnit(Units.PERCENT);
        if (percentQuantity == null) {
            throw new IllegalArgumentException("Unable to convert quantity to percent");
        }
        return (short) (percentQuantity.doubleValue() * 100);
    }

    @Override
    protected byte @Nullable [] getData() {
        var v = value;
        if (v != null) {
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(convertQuantity(v)).array();
        }
        return null;
    }

    @Override
    public void handleResponse(byte @Nullable [] data, @Nullable Throwable th) {
        if (th != null) {
            resultHandler.completeExceptionally(th);
        }
        if (data != null) {
            short hum = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
            resultHandler.complete(new QuantityType<>(hum / 100.0, Units.PERCENT));
        } else {
            resultHandler.complete(null);
        }
    }
}
