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
package org.openhab.binding.boschshc.internal.services.intrusion.dto;

/**
 * DTO for the arming state of the intrusion detection system.
 * <p>
 * Example data:
 * 
 * <pre>
 * "armingState": {
 *   "remainingTimeUntilArmed": 0,
 *   "state": "SYSTEM_ARMING",
 *   "deleted": true,
 *   "id": "string"
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class ArmingStateData {

    public long remainingTimeUntilArmed;

    public ArmingState state;
}
