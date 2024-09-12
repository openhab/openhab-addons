/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.hw;

/**
 * Configuration settings for a {@link org.openhab.binding.lutron.internal.hw.HwDimmerHandler}.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwDimmerConfig {
    private static final int DEFAULT_FADE = 1;
    private static final int DEFAULT_LEVEL = 100;

    private String address;
    private Integer fadeTime = DEFAULT_FADE;
    private Integer defaultLevel = DEFAULT_LEVEL;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getFadeTime() {
        return this.fadeTime;
    }

    public void setFadeTime(Integer fadeTime) {
        this.fadeTime = fadeTime;
    }

    public Integer getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(Integer defaultLevel) {
        this.defaultLevel = defaultLevel;
    }
}
