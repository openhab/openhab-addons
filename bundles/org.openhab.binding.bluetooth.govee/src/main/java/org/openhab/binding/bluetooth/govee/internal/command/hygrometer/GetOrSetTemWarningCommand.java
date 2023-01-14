/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public class GetOrSetTemWarningCommand extends GoveeCommand {
    private final @Nullable WarningSettingsDTO<Temperature> settings;
    private final CompletableFuture<@Nullable WarningSettingsDTO<Temperature>> resultHandler;

    public GetOrSetTemWarningCommand(CompletableFuture<@Nullable WarningSettingsDTO<Temperature>> resultHandler) {
        this.settings = null;
        this.resultHandler = resultHandler;
    }

    public GetOrSetTemWarningCommand(WarningSettingsDTO<Temperature> settings,
            CompletableFuture<@Nullable WarningSettingsDTO<Temperature>> resultHandler) {
        this.settings = settings;
        this.resultHandler = resultHandler;
    }

    @Override
    public byte getCommandType() {
        return settings == null ? READ_TYPE : WRITE_TYPE;
    }

    @Override
    public byte getCommandCode() {
        return 4;
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
        var settings = this.settings;
        if (settings == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(settings.enableAlarm == OnOffType.ON ? (byte) 1 : 0);
        buffer.putShort(convertQuantity(settings.min));
        buffer.putShort(convertQuantity(settings.max));
        return buffer.array();
    }

    @Override
    public void handleResponse(byte @Nullable [] data, @Nullable Throwable th) {
        if (th != null) {
            resultHandler.completeExceptionally(th);
        }
        if (data != null) {
            WarningSettingsDTO<Temperature> result = new WarningSettingsDTO<Temperature>();

            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            result.enableAlarm = OnOffType.from(buffer.get() == 1);
            result.min = new QuantityType<Temperature>(buffer.getShort() / 100.0, SIUnits.CELSIUS);
            result.max = new QuantityType<Temperature>(buffer.getShort() / 100.0, SIUnits.CELSIUS);

            resultHandler.complete(result);
        } else {
            resultHandler.complete(null);
        }
    }
}
