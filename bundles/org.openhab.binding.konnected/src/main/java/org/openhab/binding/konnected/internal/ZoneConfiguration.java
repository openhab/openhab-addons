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

/**
 * The {@link ZoneConfiguration} class contains the configuration parameters for the zone.
 *
 * @author Haavar Valeur
 */
public class ZoneConfiguration {
    public String zone;
    public boolean dht22;
    public String ds18b20Address;
    public int pollInterval = 3;
    public Integer times;
    public Integer momentary;
    public Integer pause;
    public int onValue = 1;
}
