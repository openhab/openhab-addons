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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public class GetOrSetTemCaliCommand extends GoveeCommand {
    private final CompletableFuture<@Nullable QuantityType<Temperature>> resultHandler;
    private final @Nullable QuantityType<Temperature> value;

    public GetOrSetTemCaliCommand(CompletableFuture<@Nullable QuantityType<Temperature>> resultHandler) {
        this.value = null;
        this.resultHandler = resultHandler;
    }

    public GetOrSetTemCaliCommand(QuantityType<Temperature> value,
            CompletableFuture<@Nullable QuantityType<Temperature>> resultHandler) {
        this.value = value;
        this.resultHandler = resultHandler;
    }

    @Override
    public byte getCommandType() {
        return value != null ? WRITE_TYPE : READ_TYPE;
    }

    @Override
    public byte getCommandCode() {
        return 7;
    }

    private static short convertQuantity(QuantityType<Temperature> quantity) {
        var celciusQuantity = quantity.toUnit(SIUnits.CELSIUS);
        if (celciusQuantity == null) {
            throw new IllegalArgumentException("Unable to convert quantity to celcius");
        }
        return (short) (celciusQuantity.doubleValue() * 100);
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
            short tem = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
            resultHandler.complete(new QuantityType<>(tem / 100.0, SIUnits.CELSIUS));
        } else {
            resultHandler.complete(null);
        }
    }
}
