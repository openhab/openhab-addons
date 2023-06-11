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
package org.openhab.binding.plugwise.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.protocol.field.MessageType;

/**
 * Creates instances of messages received from the Plugwise network.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class MessageFactory {

    public Message createMessage(MessageType messageType, int sequenceNumber, String payload)
            throws IllegalArgumentException {
        switch (messageType) {
            case ACKNOWLEDGEMENT_V1:
            case ACKNOWLEDGEMENT_V2:
                return new AcknowledgementMessage(messageType, sequenceNumber, payload);
            case ANNOUNCE_AWAKE_REQUEST:
                return new AnnounceAwakeRequestMessage(sequenceNumber, payload);
            case BROADCAST_GROUP_SWITCH_RESPONSE:
                return new BroadcastGroupSwitchResponseMessage(sequenceNumber, payload);
            case CLOCK_GET_RESPONSE:
                return new ClockGetResponseMessage(sequenceNumber, payload);
            case DEVICE_INFORMATION_RESPONSE:
                return new InformationResponseMessage(sequenceNumber, payload);
            case DEVICE_ROLE_CALL_RESPONSE:
                return new RoleCallResponseMessage(sequenceNumber, payload);
            case MODULE_JOINED_NETWORK_REQUEST:
                return new ModuleJoinedNetworkRequestMessage(sequenceNumber, payload);
            case NETWORK_STATUS_RESPONSE:
                return new NetworkStatusResponseMessage(sequenceNumber, payload);
            case NODE_AVAILABLE:
                return new NodeAvailableMessage(sequenceNumber, payload);
            case PING_RESPONSE:
                return new PingResponseMessage(sequenceNumber, payload);
            case POWER_BUFFER_RESPONSE:
                return new PowerBufferResponseMessage(sequenceNumber, payload);
            case POWER_CALIBRATION_RESPONSE:
                return new PowerCalibrationResponseMessage(sequenceNumber, payload);
            case POWER_INFORMATION_RESPONSE:
                return new PowerInformationResponseMessage(sequenceNumber, payload);
            case REAL_TIME_CLOCK_GET_RESPONSE:
                return new RealTimeClockGetResponseMessage(sequenceNumber, payload);
            case SENSE_REPORT_REQUEST:
                return new SenseReportRequestMessage(sequenceNumber, payload);
            default:
                throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }
    }
}
