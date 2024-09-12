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
package org.openhab.binding.mynice.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link It4WifiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class It4WifiConfiguration {
    public static final String PASSWORD = "password";
    public static final String HOSTNAME = "hostname";

    public String username = "";
    public String hostname = "";
    public String macAddress = "";
    public String password = "";
}
