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
package org.openhab.binding.boschshc.internal.services.presence;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.presence.dto.PresenceSimulationConfigurationServiceState;

/**
 * Service to control the state of the presence simulation.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceSimulationConfigurationService
        extends BoschSHCService<PresenceSimulationConfigurationServiceState> {

    public static final String PRESENCE_SIMULATION_CONFIGURATION_SERVICE_NAME = "PresenceSimulationConfiguration";

    public PresenceSimulationConfigurationService() {
        super(PRESENCE_SIMULATION_CONFIGURATION_SERVICE_NAME, PresenceSimulationConfigurationServiceState.class);
    }
}
