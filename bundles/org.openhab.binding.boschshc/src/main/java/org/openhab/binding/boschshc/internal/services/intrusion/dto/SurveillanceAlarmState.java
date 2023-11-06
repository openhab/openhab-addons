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

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * A state containing information about intrusion detection events.
 * <p>
 * Example data:
 * 
 * <pre>
 * {
 *   "@type": "surveillanceAlarmState",
 *   "incidents": [
 *     {
 *       "triggerName": "Motion Detector",
 *       "locationId": "hz_5",
 *       "location": "Living Room",
 *       "id": "hdm:ZigBee:000d6f0012f02342",
 *       "time": 1652615755336,
 *       "type": "INTRUSION"
 *     }
 *   ],
 *   "value": "ALARM_ON"
 * }
 * </pre>
 * 
 * <p>
 * <b>Note:</b> This state is not documented in the official Bosch API docs.
 * The type of the incidents seems to be very similar to <code>IncidentType</code> documented for
 * the system state. However, the type enum seems to be slightly different (<code>INTRUSION</code> instead of
 * <code>INTRUSION_DETECTED</code>).
 * For this reason incidents are not modeled in this state object for now.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class SurveillanceAlarmState extends BoschSHCServiceState {

    public SurveillanceAlarmState() {
        super("surveillanceAlarmState");
    }

    public AlarmState value;
}
