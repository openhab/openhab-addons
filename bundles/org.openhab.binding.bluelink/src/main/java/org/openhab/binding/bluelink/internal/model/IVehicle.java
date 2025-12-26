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
package org.openhab.binding.bluelink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Model class for a vehicle.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public interface IVehicle {
    @Nullable
    String id();

    String vin();

    @Nullable
    String nickName();

    EngineType engineType();

    @Nullable
    String model();

    int modelYear();

    enum EngineType {
        UNKNOWN,
        EV,
        // internal combustion
        ICE,
        // hybrid
        PHEV
    }

    default boolean isElectric() {
        return engineType() == EngineType.EV || engineType() == EngineType.PHEV;
    }

    default String getDisplayName() {
        final String nickName = nickName();
        if (nickName != null && !nickName.isBlank()) {
            return nickName;
        }
        final String model = model();
        if (model != null && !model.isBlank()) {
            return model;
        }
        return vin();
    }
}
