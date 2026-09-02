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
package org.openhab.binding.dahuadoor.internal.media;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parsed SIP audio target from INVITE SDP.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public final class SipAudioOffer {

    private final String remoteHost;
    private final int remotePort;
    private final int payloadType;
    private final String codecName;
    private final int clockRate;
    private final int ptimeMs;

    public SipAudioOffer(String remoteHost, int remotePort, int payloadType, String codecName, int clockRate,
            int ptimeMs) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.payloadType = payloadType;
        this.codecName = codecName;
        this.clockRate = clockRate;
        this.ptimeMs = ptimeMs;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public int getPayloadType() {
        return payloadType;
    }

    public String getCodecName() {
        return codecName;
    }

    public int getClockRate() {
        return clockRate;
    }

    public int getPtimeMs() {
        return ptimeMs;
    }
}
