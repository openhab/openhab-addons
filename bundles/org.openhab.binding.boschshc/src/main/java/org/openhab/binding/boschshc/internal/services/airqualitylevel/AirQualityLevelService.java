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
package org.openhab.binding.boschshc.internal.services.airqualitylevel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.airqualitylevel.dto.AirQualityLevelServiceState;

/**
 * This service constantly measures key air quality values to help you create a healthy room climate.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class AirQualityLevelService extends BoschSHCService<AirQualityLevelServiceState> {

    public AirQualityLevelService() {
        super("AirQualityLevel", AirQualityLevelServiceState.class);
    }
}
