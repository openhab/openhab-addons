/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tesla.internal.protocol.Vehicle;
import org.openhab.binding.tesla.internal.protocol.VehicleConfig;

/**
 * The {@link VehicleListener} interface can be implemented by classes that want to be informed about
 * existing vehicles of a given account. They need to register on a {@link TeslaAccountHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public interface VehicleListener {

    /**
     * This method is called by the {@link TeslaAccountHandler}, if a vehicle is identified.
     *
     * @param vehicle a vehicle that was found within an account.
     * @param vehicleConfig vehicle configuration that was read from the vehicle or null, if not available.
     */
    void vehicleFound(Vehicle vehicle, @Nullable VehicleConfig vehicleConfig);
}
