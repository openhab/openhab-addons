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
package org.openhab.binding.tuya.internal.local.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TcpStatusPayload} encapsulates the payload of a TCP status message
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TcpStatusPayload {
    public int protocol = -1;
    public String devId = "";
    public String gwId = "";
    public String uid = "";
    public long t = 0;
    public Map<Integer, Object> dps = Map.of();
    public Data data = new Data();

    @Override
    public String toString() {
        return "TcpStatusPayload{protocol=" + protocol + ", devId='" + devId + "', gwId='" + gwId + "', uid='" + uid
                + "', t=" + t + ", dps=" + dps + ", data=" + data + "}";
    }

    public static class Data {
        public Map<Integer, Object> dps = Map.of();

        @Override
        public String toString() {
            return "Data{dps=" + dps + "}";
        }
    }
}
