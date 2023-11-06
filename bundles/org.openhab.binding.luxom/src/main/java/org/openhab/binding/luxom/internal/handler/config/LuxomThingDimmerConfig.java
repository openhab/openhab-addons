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
package org.openhab.binding.luxom.internal.handler.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link LuxomThingDimmerConfig} is the config class for Niko Home Control Dimmer Actions.
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class LuxomThingDimmerConfig extends LuxomThingConfig {
    private static final int DEFAULT_ONLEVEL = 100;

    public BigDecimal onLevel = new BigDecimal(DEFAULT_ONLEVEL);
    public Boolean onToLast = Boolean.FALSE;
    public Integer stepPercentage = 5;
}
