/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.withings.internal.api.device.DevicesResponseDTO;
import org.openhab.binding.withings.internal.service.person.Person;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class WithingsDataModel {

    private final List<DevicesResponseDTO.Device> devices;
    private final Optional<Person> person;

    public WithingsDataModel(List<DevicesResponseDTO.Device> devices, Optional<Person> person) {
        this.devices = devices;
        this.person = person;
    }

    public List<DevicesResponseDTO.Device> getDevices() {
        return devices;
    }

    public Optional<DevicesResponseDTO.Device> getDevice(String deviceId) {
        for (DevicesResponseDTO.Device device : getDevices()) {
            if (deviceId.equals(device.getDeviceId())) {
                return Optional.of(device);
            }
        }
        return Optional.empty();
    }

    public Optional<Person> getPerson() {
        return person;
    }
}
