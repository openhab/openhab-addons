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
package org.openhab.binding.boschshc.internal.services.intrusion.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Represents the state of the intrusion detection system as reported by the Bosch Smart Home Controller.
 * <p>
 * Example data:
 * 
 * <pre>
 * {
 *     "@type": "systemState",
 *     "systemAvailability": {
 *         "@type": "systemAvailabilityState",
 *         "available": true,
 *         "deleted": false
 *     },
 *     "armingState": {
 *         "@type": "armingState",
 *         "state": "SYSTEM_DISARMED",
 *         "deleted": false
 *     },
 *     "alarmState": {
 *         "@type": "alarmState",
 *         "value": "ALARM_OFF",
 *         "incidents": [],
 *         "deleted": false
 *     },
 *     "activeConfigurationProfile": {
 *         "@type": "activeConfigurationProfile",
 *         "deleted": false
 *     },
 *     "securityGapState": {
 *         "@type": "securityGapState",
 *         "securityGaps": [],
 *         "deleted": false
 *     },
 *     "deleted": false
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class IntrusionDetectionSystemState extends BoschSHCServiceState {

    public IntrusionDetectionSystemState() {
        super("systemState");
    }

    public SystemAvailabilityStateData systemAvailability;

    public ArmingStateData armingState;

    public AlarmStateData alarmState;

    public ActiveConfigurationProfileData activeConfigurationProfile;
}
