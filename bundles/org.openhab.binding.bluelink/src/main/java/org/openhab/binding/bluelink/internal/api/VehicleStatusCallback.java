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
package org.openhab.binding.bluelink.internal.api;

import java.time.Instant;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluelink.internal.dto.CommonVehicleStatus;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public interface VehicleStatusCallback {
    void acceptStatus(CommonVehicleStatus data);

    void acceptLastUpdateTimestamp(Instant lastUpdated);

    void acceptSmartKeyBatteryWarning(boolean smartKeyBattery);

    void acceptLocation(PointType location);

    void acceptOdometer(QuantityType<Length> odometer);
}
