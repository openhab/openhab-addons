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
package org.openhab.binding.mqtt.espmilighthub.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ConfigOptions} Holds the config for the settings.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class ConfigOptions {
    public BigDecimal duvThreshold = new BigDecimal("0.003");
    public int whiteThreshold = -1;
    public int whiteSat = -1;
    public int whiteHue = -1;
    public int favouriteWhite = 200;
    public boolean oneTriggersNightMode = false;
    public boolean powerFailsToMinimum = false;
    public int dimmedCT = -1;
}
