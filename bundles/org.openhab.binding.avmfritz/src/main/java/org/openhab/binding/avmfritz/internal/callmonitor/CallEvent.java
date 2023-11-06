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
package org.openhab.binding.avmfritz.internal.callmonitor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Call Events received from a fritzbox.
 *
 * 12.07.20 09:11:30;RING;0;0171123456;888888;SIP2;
 * 12.07.20 09:13:40;DISCONNECT;0;0;
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class CallEvent {

    public static final String CALL_TYPE_CALL = "CALL";
    public static final String CALL_TYPE_CONNECT = "CONNECT";
    public static final String CALL_TYPE_RING = "RING";
    public static final String CALL_TYPE_DISCONNECT = "DISCONNECT";

    private final String rawEvent;
    private final String timestamp;
    private final String callType;
    private final String id;
    private @Nullable String externalNo;
    private @Nullable String internalNo;
    private @Nullable String connectionType;
    private @Nullable String line;

    public CallEvent(String rawEvent) {
        this.rawEvent = rawEvent;

        String[] fields = rawEvent.split(";");
        if (fields.length < 4) {
            throw new IllegalArgumentException("Cannot parse call event: " + rawEvent);
        }

        timestamp = fields[0];
        callType = fields[1];
        id = fields[2];

        switch (callType) {
            case CALL_TYPE_RING:
                externalNo = fields[3];
                internalNo = fields[4];
                connectionType = fields[5];
                break;
            case CALL_TYPE_CONNECT:
                line = fields[3];
                if (fields.length > 4) {
                    externalNo = fields[4];
                } else {
                    externalNo = "Unknown";
                }
                break;
            case CALL_TYPE_CALL:
                line = fields[3];
                internalNo = fields[4];
                externalNo = fields[5];
                connectionType = fields[6];
                break;
            case CALL_TYPE_DISCONNECT:
                // no fields to set
                break;
            default:
                throw new IllegalArgumentException("Invalid call type: " + callType);
        }
    }

    public @Nullable String getLine() {
        return line;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCallType() {
        return callType;
    }

    public String getId() {
        return id;
    }

    public @Nullable String getExternalNo() {
        return externalNo;
    }

    public @Nullable String getInternalNo() {
        return internalNo;
    }

    public @Nullable String getConnectionType() {
        return connectionType;
    }

    public String getRaw() {
        return rawEvent;
    }

    @Override
    public String toString() {
        return "CallEvent [timestamp=" + timestamp + ", callType=" + callType + ", id=" + id + ", externalNo="
                + externalNo + ", internalNo=" + internalNo + ", connectionType=" + connectionType + ", line=" + line
                + "]";
    }
}
