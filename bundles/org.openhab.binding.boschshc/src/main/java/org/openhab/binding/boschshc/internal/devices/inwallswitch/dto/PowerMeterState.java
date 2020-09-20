/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.inwallswitch.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * PowerMeterState
 *
 * @author Stefan Kästle - Initial contribution
 */
public class PowerMeterState extends BoschSHCServiceState {

    public PowerMeterState() {
        super("powerMeterState");
    }

    public double energyConsumption;
    public double powerConsumption;
}
