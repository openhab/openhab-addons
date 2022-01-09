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
package org.openhab.binding.venstarthermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VenstarThermostatConfiguration} is responsible for holding configuration information.
 *
 * @author William Welliver - Initial contribution
 */
@NonNullByDefault
public class VenstarThermostatConfiguration {
    public String username = "";
    public String password = "";
    public String url = "";
    public Integer refresh = 30;
}
