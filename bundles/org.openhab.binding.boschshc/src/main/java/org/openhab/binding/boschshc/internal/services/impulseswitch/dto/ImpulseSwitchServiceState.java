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
package org.openhab.binding.boschshc.internal.services.impulseswitch.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Data transfer object for impulses to be sent to relays.
 * <p>
 * Example JSON:
 * 
 * <pre>
 * {
 *   "@type": "ImpulseSwitchState",
 *   "impulseState": true,
 *   "impulseLength": 100,
 *   "instantOfLastImpulse": "2024-04-14T15:52:31.677366Z"
 * }
 * </pre>
 * 
 * The <code>impulseLength</code> specifies the time (in tenth seconds) until the relay switches off again.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class ImpulseSwitchServiceState extends BoschSHCServiceState {

    public ImpulseSwitchServiceState() {
        super("ImpulseSwitchState");
    }

    public boolean impulseState;

    public int impulseLength;

    public String instantOfLastImpulse;
}
