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
 * Sent when the check-in state of an intermittently connected (sleepy) device changes
 *
 * @author Dan Cunningham - Initial contribution
 */
public class IcdStateMessage extends IcdState {
    public BigInteger nodeId;
    public IcdEvent event;

    public enum IcdEvent {
        @SerializedName("registered")
        REGISTERED,
        @SerializedName("unregistered")
        UNREGISTERED,
        @SerializedName("checkedIn")
        CHECKED_IN,
        /** The device did not check in when expected */
        @SerializedName("checkInMissed")
        CHECK_IN_MISSED
    }
}
