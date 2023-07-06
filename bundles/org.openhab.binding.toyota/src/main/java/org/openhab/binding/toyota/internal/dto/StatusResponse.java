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
package org.openhab.binding.toyota.internal.dto;

import java.lang.reflect.Type;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * This class holds all objects describing the status of the car
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class StatusResponse {
    public static Type ANSWER_CLASS = new TypeToken<StatusResponse>() {
    }.getType();

    public enum TripStatus {
        @SerializedName("0")
        STOPPED,
        @SerializedName("1")
        STARTED,
        UNKNOWN;
    }

    public ProtectionState protectionState;
    public Event event;
    public TripStatus tripStatus;
    public Climate climate;
}
