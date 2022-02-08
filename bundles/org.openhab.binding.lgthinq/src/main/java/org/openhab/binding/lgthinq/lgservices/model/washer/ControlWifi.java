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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import static org.openhab.binding.lgthinq.lgservices.model.washer.WMDownload.EMPTY_WM_DOWNLOAD;
import static org.openhab.binding.lgthinq.lgservices.model.washer.WMStart.EMPTY_WM_START;
import static org.openhab.binding.lgthinq.lgservices.model.washer.WMStop.EMPTY_WM_STOP;
import static org.openhab.binding.lgthinq.lgservices.model.washer.WMWakeup.EMPTY_WM_WAKEUP;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link ControlWifi}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ControlWifi {
    static final ControlWifi EMPTY_CONTROL_WIFI = new ControlWifi();
    @JsonProperty("WMStart")
    private WMStart wmStart = EMPTY_WM_START;
    @JsonProperty("WMDownload")
    private WMDownload wmDownload = EMPTY_WM_DOWNLOAD;
    @JsonProperty("WMOff")
    private WMOff wmOff = WMOff.EMPTY_WM_OFF;
    @JsonProperty("WMStop")
    private WMStop wmStop = EMPTY_WM_STOP;
    @JsonProperty("WMWakeup")
    private WMWakeup wmWakeup = EMPTY_WM_WAKEUP;

    public void setWmStart(WMStart wmStart) {
        this.wmStart = wmStart;
    }

    public WMStart getWmStart() {
        return wmStart;
    }

    public void setWmDownload(WMDownload wmDownload) {
        this.wmDownload = wmDownload;
    }

    public WMDownload getWmDownload() {
        return wmDownload;
    }

    public void setWmOff(WMOff wmOff) {
        this.wmOff = wmOff;
    }

    public WMOff getWmOff() {
        return wmOff;
    }

    public void setWmStop(WMStop wmStop) {
        this.wmStop = wmStop;
    }

    public WMStop getWmStop() {
        return wmStop;
    }

    public void setWmWakeup(WMWakeup wmWakeup) {
        this.wmWakeup = wmWakeup;
    }

    public WMWakeup getWmWakeup() {
        return wmWakeup;
    }
}
