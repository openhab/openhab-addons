/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RingThingConfig} class encapsulates the thing configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RingThingConfig {
    public String id = "";
    public BigDecimal offOffset = BigDecimal.ZERO;
    public int refreshInterval = 5;
}
