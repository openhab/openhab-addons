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
        return switch (messageType) {
            case ACKNOWLEDGEMENT_V1, ACKNOWLEDGEMENT_V2 ->
                new AcknowledgementMessage(messageType, sequenceNumber, payload);
            case ANNOUNCE_AWAKE_REQUEST -> new AnnounceAwakeRequestMessage(sequenceNumber, payload);
            case BROADCAST_GROUP_SWITCH_RESPONSE -> new BroadcastGroupSwitchResponseMessage(sequenceNumber, payload);
            case CLOCK_GET_RESPONSE -> new ClockGetResponseMessage(sequenceNumber, payload);
            case DEVICE_INFORMATION_RESPONSE -> new InformationResponseMessage(sequenceNumber, payload);
            case DEVICE_ROLE_CALL_RESPONSE -> new RoleCallResponseMessage(sequenceNumber, payload);
            case MODULE_JOINED_NETWORK_REQUEST -> new ModuleJoinedNetworkRequestMessage(sequenceNumber, payload);
            case NETWORK_STATUS_RESPONSE -> new NetworkStatusResponseMessage(sequenceNumber, payload);
            case NODE_AVAILABLE -> new NodeAvailableMessage(sequenceNumber, payload);
            case PING_RESPONSE -> new PingResponseMessage(sequenceNumber, payload);
            case POWER_BUFFER_RESPONSE -> new PowerBufferResponseMessage(sequenceNumber, payload);
            case POWER_CALIBRATION_RESPONSE -> new PowerCalibrationResponseMessage(sequenceNumber, payload);
            case POWER_INFORMATION_RESPONSE -> new PowerInformationResponseMessage(sequenceNumber, payload);
            case REAL_TIME_CLOCK_GET_RESPONSE -> new RealTimeClockGetResponseMessage(sequenceNumber, payload);
            case SENSE_REPORT_REQUEST -> new SenseReportRequestMessage(sequenceNumber, payload);
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}
