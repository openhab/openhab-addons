/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Classes that implement this interface are interested in vehicle information updates.
 *
 * @author Markus Michels - Initial Contribution
 */
@NonNullByDefault
public interface CarNetDeviceListener {
    void informationUpdate(@Nullable List<CarNetVehicleInformation> vehicleList);

    void stateChanged(ThingStatus status, ThingStatusDetail detail, String message);
}
