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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * DTO for messages sent by the Smart Home Controller.
 * <p>
 * JSON Example:
 * 
 * <pre>
 * {
 *   "result": [{
 *     "sourceId": "hdm:ZigBee:5cc7c1f6fe11fc23",
 *     "sourceType": "DEVICE",
 *     "@type": "message",
 *     "flags": [],
 *     "messageCode": {
 *       "name": "TILT_DETECTED",
 *       "category": "WARNING"
 *     },
 *     "location": "Kitchen",
 *     "arguments": {
 *       "deviceModel": "WLS"
 *     },
 *     "id": "3499a60e-45b5-4c29-ae1a-202c2182970c",
 *     "sourceName": "Bosch_water_detector_1",
 *     "timestamp": 1714375556426
 *   }],
 *   "jsonrpc": "2.0"
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 */
public class Message extends BoschSHCServiceState {

    /**
     * Source type indicating that a message is device-specific
     */
    public static final String SOURCE_TYPE_DEVICE = "DEVICE";

    public Message() {
        super("message");
    }

    public String id;
    public String sourceId;
    public String sourceName;
    public String sourceType;
    public String location;
    public long timestamp;

    public MessageCode messageCode;
}
