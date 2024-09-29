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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public class GetTemHumCommand extends GetCommand {

    private CompletableFuture<@Nullable TemHumDTO> resultHandler;

    public GetTemHumCommand(CompletableFuture<@Nullable TemHumDTO> resultHandler) {
        this.resultHandler = resultHandler;
    }

    @Override
    public byte getCommandCode() {
        return 10;
    }

    @Override
    public void handleResponse(byte @Nullable [] data, @Nullable Throwable th) {
        if (th != null) {
            resultHandler.completeExceptionally(th);
        }
        if (data != null) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int temp = buffer.getShort();
            int hum = Short.toUnsignedInt(buffer.getShort());

            TemHumDTO temhum = new TemHumDTO();
            temhum.temperature = new QuantityType<>(temp / 100.0, SIUnits.CELSIUS);
            temhum.humidity = new QuantityType<>(hum / 100.0, Units.PERCENT);
            resultHandler.complete(temhum);
        } else {
            resultHandler.complete(null);
        }
    }
}
