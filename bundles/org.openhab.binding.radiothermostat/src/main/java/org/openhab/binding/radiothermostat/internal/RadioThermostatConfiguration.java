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
package org.openhab.binding.radiothermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RadioThermostatConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatConfiguration {
    public @Nullable String hostName;
    public @Nullable Integer refresh;
    public @Nullable Integer logRefresh;
    public boolean isCT80 = false;
    public boolean disableLogs = false;
    public boolean clockSync = false;
    public String setpointMode = "temporary";
}
