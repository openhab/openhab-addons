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
package org.openhab.binding.shelly.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ShellyDiscoveryInterface} defines the API necessary for discovery.
 *
 * @author Ravi Nadahar - Initial contribution
 */
@NonNullByDefault
public interface ShellyDiscoveryInterface {
    void initialize() throws ShellyApiException;

    ShellySettingsDevice getDeviceInfo() throws ShellyApiException;

    ShellyDeviceProfile getDeviceProfile(ThingTypeUID thingTypeUID, @Nullable ShellySettingsDevice device)
            throws ShellyApiException;

    void close();
}
