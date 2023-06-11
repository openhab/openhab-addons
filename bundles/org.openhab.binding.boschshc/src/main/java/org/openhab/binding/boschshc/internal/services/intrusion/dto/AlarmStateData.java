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
 * DTO for the alarm state of the intrusion detection system.
 * <p>
 * Example data:
 * 
 * <pre>
 * "alarmState": {
 *   "value": "ALARM_OFF",
 *   "incidents": [
 *     {
 *       "id": "string",
 *       "triggerName": "string",
 *       "type": "SYSTEM_ARMED",
 *       "time": 0,
 *       "location": "string",
 *       "locationId": "string"
 *     }
 *   ],
 *   "deleted": true,
 *   "id": "string"
 * }
 * </pre>
 * 
 * <p>
 * <b>Note:</b> Incidents are not supported yet as they do not seem to be included in the responses from the bridge.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class AlarmStateData {

    public AlarmState value;
}
