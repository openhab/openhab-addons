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
package org.openhab.binding.boschshc.internal.services.powermeter.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * State for {@link org.openhab.binding.boschshc.internal.services.powermeter.PowerMeterService}
 *
 * @author Stefan Kästle - Initial contribution
 */
public class PowerMeterServiceState extends BoschSHCServiceState {

    public PowerMeterServiceState() {
        super("powerMeterState");
    }

    public double energyConsumption;
    public double powerConsumption;
}
