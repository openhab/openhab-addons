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
package org.openhab.binding.volvocars.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of a Volvo vehicle Thing.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VehicleConfiguration {
    public String vin = "";
    public int refreshInterval = 5;
    public int slowRefreshInterval = 30;
    public int engineRunTime = 15;
}
