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
package org.openhab.binding.philipsair.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;

import com.google.gson.JsonSyntaxException;

/**
 * Abstract class for handling communication with Philips Air purifiers
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */

@NonNullByDefault
public abstract class PhilipsAirAPIConnection {

    protected PhilipsAirConfiguration config;

    public PhilipsAirAPIConnection(PhilipsAirConfiguration config) {
        this.config = config;
    }

    public @Nullable abstract PhilipsAirPurifierDataDTO getAirPurifierStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException;

    public @Nullable abstract PhilipsAirPurifierDeviceDTO getAirPurifierDevice(String host)
            throws JsonSyntaxException, PhilipsAirAPIException;

    public @Nullable abstract PhilipsAirPurifierFiltersDTO getAirPurifierFiltersStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException;

    public @Nullable abstract PhilipsAirPurifierDataDTO sendCommand(String parameter,
            PhilipsAirPurifierWritableDataDTO value) throws PhilipsAirAPIException;

    public PhilipsAirConfiguration getConfig() {
        return this.config;
    }
}
