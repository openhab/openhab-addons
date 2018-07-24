/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.config;

import java.math.BigDecimal;

/**
 * Configuration settings for a {@link org.openhab.binding.lutron.handler.DimmerHandler}.
 *
 * @author Allan Tong - Initial contribution
 */
public class DimmerConfig {
    private static final int DEFAULT_FADE = 1;

    private int integrationId;
    private BigDecimal fadeInTime = new BigDecimal(DEFAULT_FADE);
    private BigDecimal fadeOutTime = new BigDecimal(DEFAULT_FADE);

    public int getIntegrationId() {
        return this.integrationId;
    }

    public void setIntegrationId(int integrationId) {
        this.integrationId = integrationId;
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
