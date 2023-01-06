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
package org.openhab.binding.wled.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

/**
 * The {@link WledState} class holds the state and replies for a WLED device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WledState {
    protected final Gson gson = new Gson();
    public JsonResponse jsonResponse = new JsonResponse();
    public StateResponse stateResponse = new StateResponse();
    public InfoResponse infoResponse = new InfoResponse();
    public LedInfo ledInfo = new LedInfo();
    public NightLightState nightLightState = new NightLightState();
    public UdpnState udpnState = new UdpnState();
    public SegmentState segmentState = new SegmentState();
    public PresetState[] presetState = new PresetState[1];

    public class JsonResponse {
        public List<String> effects = new ArrayList<>();
        public List<String> palettes = new ArrayList<>();
    }

    public void unpackJsonObjects() {
        @Nullable
        NightLightState localNightLightState = gson.fromJson(stateResponse.nl.toString(), NightLightState.class);
        if (localNightLightState != null) {
            nightLightState = localNightLightState;
        }

        @Nullable
        UdpnState localUdpnState = gson.fromJson(stateResponse.udpn.toString(), UdpnState.class);
        if (localUdpnState != null) {
            udpnState = localUdpnState;
        }
    }

    public class StateResponse {
        public boolean on = true;
        public Object nl = "{}";
        public Object udpn = "{}";
        public SegmentState[] seg = new SegmentState[1];
        public int bri = 0;
        public int transition = 7;
        public int ps = -1;
        public int pss = 0;
        public int pl = -1;
        public int lor = 0;
    }

    public class UdpnState {
        public boolean send = false;
        public boolean recv = false;
    }

    public class SegmentState {
        public int id = 0;
        public int start = 0;
        public int stop = 0;
        public int len = 0;
        public int grp = 0;
        public int spc = 0;
        public boolean on = true;
        public int bri = 0;
        public Object[] col = new Object[1];
        public int fx = 0;
        public int sx = 0;
        public int ix = 0;
        public int pal = 0;
        public boolean sel = true;
        public boolean rev = false;
        public boolean mi = false;
        public String n = "Segment X";
    }

    public class NightLightState {
        public boolean on = true;
        public int dur = 0;
        public int mode = 0;
        public int tbri = 0;
        public int rem = 0;
    }

    public class InfoResponse {
        public String ver = "00000";
        public String mac = "";
        public Object leds = "{}";
    }

    public class LedInfo {
        public boolean rgbw = false;
    }

    public class PresetState {
        public String n = "";// Name of preset
        public int bri = 0;// brightness in 255, 0 means it is a playlist as bri was not defined
    }
}
