/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.config;

import java.math.BigDecimal;

/**
 * Configuration class for Dimmer type
 * 
 * @author Jeff Lauterbach
 *
 */
public class DimmerConfig {
    private int zoneNumber;
    private BigDecimal fadeOutSec;
    private BigDecimal fadeInSec;

    public int getZoneNumber() {
        return zoneNumber;
    }

    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    public BigDecimal getFadeOutSec() {
        return fadeOutSec;
    }

    public void setFadeOutSec(BigDecimal fadeOutSec) {
        this.fadeOutSec = fadeOutSec;
    }

    public BigDecimal getFadeInSec() {
        return fadeInSec;
    }

    public void setFadeInSec(BigDecimal fadeInSec) {
        this.fadeInSec = fadeInSec;
    }

}
