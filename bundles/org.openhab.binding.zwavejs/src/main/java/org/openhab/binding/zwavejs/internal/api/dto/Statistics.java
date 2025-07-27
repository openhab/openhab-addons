/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.api.dto;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * @author Leo Siepel - Initial contribution
 */
public class Statistics {
    public int messagesTX;
    public int messagesRX;
    public int messagesDroppedRX;
    @SerializedName("NAK")
    public int nAK;
    @SerializedName("CAN")
    public int cAN;
    public int timeoutACK;
    public int timeoutResponse;
    public int timeoutCallback;
    public int messagesDroppedTX;
    public BackgroundRSSI backgroundRSSI;
    public int commandsTX;
    public int commandsRX;
    public int commandsDroppedRX;
    public int commandsDroppedTX;
    public double rtt;
    public Instant lastSeen;
    public int rssi;
    public Lwr lwr;
}
