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
package org.openhab.binding.mielecloud.internal.webservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceCollection;

/**
 * A cache for {@link Device} objects associated with unique identifiers.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
class DeviceCache {
    private final Map<String, Device> entries = new HashMap<>();

    public void replaceAllDevices(DeviceCollection deviceCollection) {
        clear();
        deviceCollection.getDeviceIdentifiers().stream().forEach(i -> entries.put(i, deviceCollection.getDevice(i)));
    }

    public void clear() {
        entries.clear();
    }

    public Set<String> getDeviceIds() {
        return entries.keySet();
    }

    public Optional<Device> getDevice(String deviceIdentifier) {
        return Optional.ofNullable(entries.get(deviceIdentifier));
    }
}
