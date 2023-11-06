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
package org.openhab.binding.boschshc.internal.services.roomclimatecontrol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.dto.RoomClimateControlServiceState;

/**
 * Service of a virtual device which controls the radiator thermostats in a room.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class RoomClimateControlService extends BoschSHCService<RoomClimateControlServiceState> {
    public RoomClimateControlService() {
        super("RoomClimateControl", RoomClimateControlServiceState.class);
    }
}
