/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.config;

/**
 * The {@link FreeboxAirPlayDeviceConfiguration} is responsible for holding
 * configuration informations associated to a Freebox AirPlay Device thing type
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxAirPlayDeviceConfiguration {

    public static final String NAME = "name";
    public static final String PASSWORD = "password";
    public static final String ACCEPT_ALL_MP3 = "acceptAllMp3";

    public String name;
    public String password;
    public Boolean acceptAllMp3;
}
