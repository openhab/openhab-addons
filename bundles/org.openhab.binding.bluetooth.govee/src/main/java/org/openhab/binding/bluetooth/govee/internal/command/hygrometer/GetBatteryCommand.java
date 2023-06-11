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
public class GetBatteryCommand extends GetCommand {

    private CompletableFuture<@Nullable QuantityType<Dimensionless>> resultHandler;

    public GetBatteryCommand(CompletableFuture<@Nullable QuantityType<Dimensionless>> resultHandler) {
        this.resultHandler = resultHandler;
    }

    @Override
    public byte getCommandCode() {
        return 8;
    }

    @Override
    public void handleResponse(byte @Nullable [] data, @Nullable Throwable th) {
        if (th != null) {
            resultHandler.completeExceptionally(th);
        }
        if (data != null) {
            int value = data[0] & 0xFF;
            resultHandler.complete(new QuantityType<Dimensionless>(value, Units.PERCENT));
        } else {
            resultHandler.complete(null);
        }
    }
}
