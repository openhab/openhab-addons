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
package org.openhab.binding.lutron.internal.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration settings for a {@link org.openhab.binding.lutron.internal.handler.DimmerHandler}.
 *
 * @author Allan Tong - Initial contribution
 */
@NonNullByDefault
public class DimmerConfig {
    private static final int DEFAULT_FADE = 1;
    private static final int DEFAULT_ONLEVEL = 100;
    private static final boolean DEFAULT_ONTOLAST = false;

    public int integrationId;
    public BigDecimal fadeInTime = new BigDecimal(DEFAULT_FADE);
    public BigDecimal fadeOutTime = new BigDecimal(DEFAULT_FADE);
    public BigDecimal onLevel = new BigDecimal(DEFAULT_ONLEVEL);
    public Boolean onToLast = DEFAULT_ONTOLAST;
}
