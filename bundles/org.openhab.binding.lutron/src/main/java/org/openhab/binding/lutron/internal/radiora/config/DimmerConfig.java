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
package org.openhab.binding.lutron.internal.radiora.config;

import java.math.BigDecimal;

/**
 * Configuration class for Dimmer type
 *
 * @author Jeff Lauterbach - Initial contribution
 *
 */
public class DimmerConfig {
    public int zoneNumber;
    public int system = 0;
    public BigDecimal fadeOutSec;
    public BigDecimal fadeInSec;

    public int getZoneNumber() {
        return zoneNumber;
    }

    public BigDecimal getFadeOutSec() {
        return fadeOutSec;
    }

    public BigDecimal getFadeInSec() {
        return fadeInSec;
    }
}
