/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OpenSprinklerState} class holds the state and replies for an OpenSprinkler device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerState {
    public JcResponse jcReply = new JcResponse();
    public JoResponse joReply = new JoResponse();
    public JsResponse jsReply = new JsResponse();
    public JpResponse jpReply = new JpResponse();
    public JnResponse jnReply = new JnResponse();
    public List<StateOption> programs = new ArrayList<>();
    public List<StateOption> stations = new ArrayList<>();

    public static class JsResponse {
        public int sn[] = new int[8];
        public int nstations = 8;
    }

    public static class JpResponse {
        public int nprogs = 0;
        public Object[] pd = {};
    }

    public static class JoResponse {
        public int wl;
        public int fwv = -1;
    }

    public static class JcResponse {
        public @Nullable List<List<Integer>> ps;
        @SerializedName(value = "sn1", alternate = "rs")
        public int rs;
        public long devt = 0;
        public long rdst = 0;
        public int en = 1;
        public int sn2 = -1;
        @SerializedName(value = "RSSI", alternate = "rssi") // json reply uses all uppercase
        public int rssi = 1;
        public int flcrt = -1;
        public int curr = -1;
    }

    public static class JnResponse {
        public List<String> snames = new ArrayList<>();
        @SerializedName(value = "ignore_rain", alternate = "ignoreRain")
        public byte[] ignoreRain = { 0 };
    }
}
