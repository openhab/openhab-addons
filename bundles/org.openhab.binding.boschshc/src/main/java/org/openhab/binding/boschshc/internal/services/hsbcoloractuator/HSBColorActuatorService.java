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
package org.openhab.binding.boschshc.internal.services.hsbcoloractuator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.hsbcoloractuator.dto.HSBColorActuatorServiceState;

/**
 * Service for devices that can emit colored light.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class HSBColorActuatorService extends BoschSHCService<HSBColorActuatorServiceState> {

    public HSBColorActuatorService() {
        super("HSBColorActuator", HSBColorActuatorServiceState.class);
    }
}
