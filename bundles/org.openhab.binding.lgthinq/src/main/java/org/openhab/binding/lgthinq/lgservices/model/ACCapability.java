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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ACCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ACCapability {

    private Map<String, String> opMod = Collections.emptyMap();
    private Map<String, String> fanSpeed = Collections.emptyMap();

    private List<String> supportedOpMode = Collections.emptyList();
    private List<String> supportedFanSpeed = Collections.emptyList();

    public Map<String, String> getOpMod() {
        return opMod;
    }

    public void setOpMod(Map<String, String> opMod) {
        this.opMod = opMod;
    }

    public Map<String, String> getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(Map<String, String> fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public List<String> getSupportedOpMode() {
        return supportedOpMode;
    }

    public void setSupportedOpMode(List<String> supportedOpMode) {
        this.supportedOpMode = supportedOpMode;
    }

    public List<String> getSupportedFanSpeed() {
        return supportedFanSpeed;
    }

    public void setSupportedFanSpeed(List<String> supportedFanSpeed) {
        this.supportedFanSpeed = supportedFanSpeed;
    }
}
