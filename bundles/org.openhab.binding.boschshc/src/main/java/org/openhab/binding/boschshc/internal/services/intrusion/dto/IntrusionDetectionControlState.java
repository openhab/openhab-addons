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
package org.openhab.binding.boschshc.internal.services.intrusion.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * A state which is periodically sent by the intrusion detection control state while arming.
 * <p>
 * Example data:
 * 
 * <pre>
 * {
 *   "@type": "intrusionDetectionControlState",
 *   "activeProfile": "0",
 *   "alarmActivationDelayTime": 30,
 *   "actuators": [
 *     {
 *       "readonly": false,
 *       "active": true,
 *       "id": "intrusion:video"
 *     },
 *     {
 *       "readonly": false,
 *       "active": false,
 *       "id": "intrusion:siren"
 *     }
 *   ],
 *   "remainingTimeUntilArmed": 29559,
 *   "armActivationDelayTime": 30,
 *   "triggers": [
 *     {
 *       "readonly": false,
 *       "active": true,
 *       "id": "hdm:ZigBee:000d6f0012f02378"
 *     }
 *   ],
 *   "value": "SYSTEM_ARMING"
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class IntrusionDetectionControlState extends BoschSHCServiceState {

    public IntrusionDetectionControlState() {
        super("intrusionDetectionControlState");
    }

    public String activeProfile;

    public int alarmActivationDelayTime;

    public long remainingTimeUntilArmed;

    public int armActivationDelayTime;

    public ArmingState value;
}
