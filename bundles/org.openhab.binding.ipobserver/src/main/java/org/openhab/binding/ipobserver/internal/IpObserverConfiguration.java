/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ipobserver.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IpObserverConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpObserverConfiguration {
    // bindings config
    public String address = "";
    public int pollTime = 20;
    public int autoReboot = 2000;

    // Config settings parsed from weather station.
    public boolean imperialTemperature = false;
    public boolean imperialRain = false;
    // 0=lux, 1=w/m2, 2=fc
    public String solarUnit = "0";
    // 0=m/s, 1=km/h, 2=ft/s, 3=bft, 4=mph, 5=knot
    public String windUnit = "0";
}
