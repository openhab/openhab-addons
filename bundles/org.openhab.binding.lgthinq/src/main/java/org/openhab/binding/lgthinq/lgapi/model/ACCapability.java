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
package org.openhab.binding.lgthinq.lgapi.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link ACCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
public class ACCapability {

    private Map<String, String> opMod;
    private Map<String, String> fanSpeed;

    private List<String> supportedOpMode;
    private List<String> supportedFanSpeed;

    @NonNull
    public Map<String, String> getOpMod() {
        return opMod == null ? Collections.emptyMap() : opMod;
    }

    public void setOpMod(Map<String, String> opMod) {
        this.opMod = opMod;
    }

    @NonNull
    public Map<String, String> getFanSpeed() {
        return fanSpeed == null ? Collections.emptyMap() : fanSpeed;
    }

    public void setFanSpeed(Map<String, String> fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public List<String> getSupportedOpMode() {
        return supportedOpMode == null ? Collections.emptyList() : supportedOpMode;
    }

    public void setSupportedOpMode(List<String> supportedOpMode) {
        this.supportedOpMode = supportedOpMode;
    }

    public List<String> getSupportedFanSpeed() {
        return supportedFanSpeed == null ? Collections.emptyList() : supportedFanSpeed;
    }

    public void setSupportedFanSpeed(List<String> supportedFanSpeed) {
        this.supportedFanSpeed = supportedFanSpeed;
    }
}
