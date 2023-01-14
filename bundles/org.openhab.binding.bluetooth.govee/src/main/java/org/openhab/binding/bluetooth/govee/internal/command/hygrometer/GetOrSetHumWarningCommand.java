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

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public class GetOrSetHumWarningCommand extends GoveeCommand {

    private final @Nullable WarningSettingsDTO<Dimensionless> settings;
    private final CompletableFuture<@Nullable WarningSettingsDTO<Dimensionless>> resultHandler;

    public GetOrSetHumWarningCommand(CompletableFuture<@Nullable WarningSettingsDTO<Dimensionless>> resultHandler) {
        this.settings = null;
        this.resultHandler = resultHandler;
    }

    public GetOrSetHumWarningCommand(WarningSettingsDTO<Dimensionless> settings,
            CompletableFuture<@Nullable WarningSettingsDTO<Dimensionless>> resultHandler) {
        this.settings = settings;
        this.resultHandler = resultHandler;
    }

    @Override
    public byte getCommandType() {
        return settings == null ? READ_TYPE : WRITE_TYPE;
    }

    @Override
    public byte getCommandCode() {
        return 3;
    }

    private static short convertQuantity(@Nullable QuantityType<Dimensionless> quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Unable to convert quantity to percent");
        }
        QuantityType<Dimensionless> percentQuantity = quantity.toUnit(Units.PERCENT);
        if (percentQuantity == null) {
            throw new IllegalArgumentException("Unable to convert quantity to percent");
        }
        return (short) (percentQuantity.doubleValue() * 100);
    }

    @Override
    protected byte @Nullable [] getData() {
        WarningSettingsDTO<Dimensionless> localSettins = settings;
        if (localSettins == null || localSettins.min == null || localSettins.max == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(localSettins.enableAlarm == OnOffType.ON ? (byte) 1 : 0);
        buffer.putShort(convertQuantity(localSettins.min));
        buffer.putShort(convertQuantity(localSettins.max));
        return buffer.array();
    }

    @Override
    public void handleResponse(byte @Nullable [] data, @Nullable Throwable th) {
        if (th != null) {
            resultHandler.completeExceptionally(th);
        }
        if (data != null) {
            WarningSettingsDTO<Dimensionless> result = new WarningSettingsDTO<Dimensionless>();

            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            result.enableAlarm = OnOffType.from(buffer.get() == 1);
            result.min = new QuantityType<Dimensionless>(buffer.getShort() / 100.0, Units.PERCENT);
            result.max = new QuantityType<Dimensionless>(buffer.getShort() / 100.0, Units.PERCENT);

            resultHandler.complete(result);
        } else {
            resultHandler.complete(null);
        }
    }
}
