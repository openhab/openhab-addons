/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.hw;

import java.math.BigDecimal;

/**
 * Configuration settings for a {@link org.openhab.binding.lutron.handler.HWDimmerHandler}.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwDimmerConfig {
    private static final int DEFAULT_FADE = 1;

    private String address;
    private BigDecimal fadeInTime = new BigDecimal(DEFAULT_FADE);
    private BigDecimal fadeOutTime = new BigDecimal(DEFAULT_FADE);

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getFadeInTime() {
        return this.fadeInTime;
    }

    public void setFadeInTime(BigDecimal fadeInTime) {
        this.fadeInTime = fadeInTime;
    }

    public BigDecimal getFadeOutTime() {
        return this.fadeOutTime;
    }

    public void setFadeOutTime(BigDecimal fadeOutTime) {
        this.fadeOutTime = fadeOutTime;
    }
}
