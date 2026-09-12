/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.eyeonwater.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EyeOnWaterBridgeConfiguration} class represents the configuration for the account bridge.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterBridgeConfiguration {
    public String username = "";
    public String password = "";
    public String hostname = "eyeonwater.com";
    public int refreshInterval = 15;
    public boolean preferNewSearch = true;
}
