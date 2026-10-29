/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client.dto.ws;

import java.math.BigInteger;

import com.google.gson.annotations.SerializedName;

/**
 * Sent when the controller's firmware update of a device reaches the install step or fails
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OtaUpdateStatusMessage {
    public BigInteger nodeId;
    public Status status;

    public enum Status {
        /** The device downloaded the update and is installing it, it restarts when done */
        @SerializedName("applying")
        APPLYING,
        /** The update failed or was cancelled */
        @SerializedName("failed")
        FAILED
    }
}
