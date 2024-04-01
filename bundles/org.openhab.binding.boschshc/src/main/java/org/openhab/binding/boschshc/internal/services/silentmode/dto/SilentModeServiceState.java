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
package org.openhab.binding.boschshc.internal.services.silentmode.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.silentmode.SilentModeState;
import org.openhab.core.library.types.OnOffType;

/**
 * Represents the state of the silent mode for thermostats.
 * <p>
 * Example JSON for normal mode:
 * 
 * <pre>
 * {
 *   "@type": "silentModeState",
 *   "mode": "MODE_NORMAL"
 * }
 * </pre>
 * 
 * Example JSON for silent mode:
 * 
 * <pre>
 * {
 *   "@type": "silentModeState",
 *   "mode": "MODE_SILENT"
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class SilentModeServiceState extends BoschSHCServiceState {

    public SilentModeServiceState() {
        super("silentModeState");
    }

    public SilentModeState mode;

    public OnOffType toOnOffType() {
        return OnOffType.from(mode == SilentModeState.MODE_SILENT);
    }
}
