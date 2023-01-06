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
package org.openhab.binding.konnected.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZoneConfiguration} class contains the configuration parameters for the zone.
 *
 * @author Haavar Valeur
 */
@NonNullByDefault
public class ZoneConfiguration {
    public String zone = "";
    public boolean dht22 = true;
    @Nullable
    public String ds18b20Address;
    public int pollInterval = 3;
    @Nullable
    public Integer times;
    @Nullable
    public Integer momentary;
    @Nullable
    public Integer pause;
    public int onValue = 1;
}
