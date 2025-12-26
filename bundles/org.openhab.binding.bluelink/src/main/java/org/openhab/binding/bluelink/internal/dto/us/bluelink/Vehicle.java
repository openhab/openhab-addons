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
package org.openhab.binding.bluelink.internal.dto.us.bluelink;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluelink.internal.model.IVehicle;

/**
 * Model class for a vehicle.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public record Vehicle(@Override @Nullable String id, @Override String vin, @Override @Nullable String nickName,
        @Override @NonNull EngineType engineType, @Override @Nullable String model, @Override int modelYear,
        @Nullable String generation, double odometer) implements IVehicle {
}
