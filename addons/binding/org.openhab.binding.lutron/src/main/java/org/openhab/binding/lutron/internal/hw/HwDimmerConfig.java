/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.hw;

/**
 * Configuration settings for a {@link org.openhab.binding.lutron.handler.HWDimmerHandler}.
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
