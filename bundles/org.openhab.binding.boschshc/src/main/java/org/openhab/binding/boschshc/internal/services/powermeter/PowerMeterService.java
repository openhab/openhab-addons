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
package org.openhab.binding.boschshc.internal.services.powermeter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;

/**
 * With this service you always have an eye on energy consumption.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class PowerMeterService extends BoschSHCService<PowerMeterServiceState> {

    public PowerMeterService() {
        super("PowerMeter", PowerMeterServiceState.class);
    }
}
