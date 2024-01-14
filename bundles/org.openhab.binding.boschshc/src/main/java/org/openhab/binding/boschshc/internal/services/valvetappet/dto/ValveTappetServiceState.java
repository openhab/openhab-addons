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
package org.openhab.binding.boschshc.internal.services.valvetappet.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * Valve Tappet service state.
 * 
 * @author Christian Oeing - Initial contribution
 */
public class ValveTappetServiceState extends BoschSHCServiceState {
    public ValveTappetServiceState() {
        super("valveTappetState");
    }

    /**
     * Current open percentage of valve tappet (0 [closed] - 100 [open]).
     */
    private int position;

    /**
     * Current position state of valve tappet.
     */
    public State getPositionState() {
        return new DecimalType(this.position);
    }
}
