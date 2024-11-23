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
package org.openhab.binding.boschshc.internal.services.shuttercontrol.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.OperationState;

/**
 * State for a shutter control device
 * 
 * @author Christian Oeing - Initial contribution
 */
public class ShutterControlServiceState extends BoschSHCServiceState {
    /**
     * Current open ratio of shutter (0.0 [closed] to 1.0 [open])
     */
    public Double level;

    /**
     * Current operation state of shutter
     */
    public OperationState operationState;

    public ShutterControlServiceState() {
        super("shutterControlState");
        this.operationState = OperationState.STOPPED;
    }

    public ShutterControlServiceState(double level) {
        this();
        this.level = level;
    }
}
