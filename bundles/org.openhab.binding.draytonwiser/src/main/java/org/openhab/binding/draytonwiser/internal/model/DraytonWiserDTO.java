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
package org.openhab.binding.draytonwiser.internal.model;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper class to get specific data from the domain object.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Moved domain object helper code to it's own class
 */
@NonNullByDefault
public class DraytonWiserDTO {

    private final DomainDTO domain;

    public DraytonWiserDTO(final DomainDTO domain) {
        this.domain = domain;
    }

    public List<RoomStatDTO> getRoomStats() {
        return domain.getRoomStat() == null ? Collections.emptyList() : domain.getRoomStat();
    }

    public List<SmartValveDTO> getSmartValves() {
        return domain.getSmartValve() == null ? Collections.emptyList() : domain.getSmartValve();
    }

    public List<SmartPlugDTO> getSmartPlugs() {
        return domain.getSmartPlug() == null ? Collections.emptyList() : domain.getSmartPlug();
    }

    public List<RoomDTO> getRooms() {
        return domain.getRoom() == null ? Collections.emptyList() : domain.getRoom();
    }

    public @Nullable RoomDTO getRoomByName(final String name) {
        for (final RoomDTO room : domain.getRoom()) {
            if (room.getName().equalsIgnoreCase(name)) {
                return room;
            }
        }
        return null;
    }

    public @Nullable RoomStatDTO getRoomStat(final String serialNumber) {
        final Integer id = getIdFromSerialNumber(serialNumber);

        return id == null ? null : getRoomStat(id);
    }

    public @Nullable RoomStatDTO getRoomStat(final int id) {
        for (final RoomStatDTO roomStat : domain.getRoomStat()) {
            if (roomStat.getId().equals(id)) {
                return roomStat;
            }
        }
        return null;
    }

    public @Nullable SmartPlugDTO getSmartPlug(final String serialNumber) {
        final Integer id = getIdFromSerialNumber(serialNumber);

        if (id == null) {
            return null;
        }
        for (final SmartPlugDTO smartPlug : domain.getSmartPlug()) {
            if (smartPlug.getId().equals(id)) {
                return smartPlug;
            }
        }
        return null;
    }

    public @Nullable DeviceDTO getExtendedDeviceProperties(final int id) {
        for (final DeviceDTO device : domain.getDevice()) {
            if (device.getId().equals(id)) {
                return device;
            }
        }

        return null;
    }

    public @Nullable SystemDTO getSystem() {
        return domain.getSystem();
    }

    public List<HeatingChannelDTO> getHeatingChannels() {
        return domain.getHeatingChannel() == null ? Collections.emptyList() : domain.getHeatingChannel();
    }

    public List<HotWaterDTO> getHotWater() {
        return domain.getHotWater() == null ? Collections.emptyList() : domain.getHotWater();
    }

    @Nullable
    public RoomDTO getRoomForDeviceId(final Integer id) {
        for (final RoomDTO room : domain.getRoom()) {
            if (room != null) {
                if (room.getRoomStatId() != null && room.getRoomStatId().equals(id)) {
                    return room;
                }

                final List<Integer> trvs = room.getSmartValveIds();
                if (trvs != null) {
                    for (final Integer itrv : trvs) {
                        if (itrv.equals(id)) {
                            return room;
                        }
                    }
                }
            }
        }
        return null;
    }

    public @Nullable SmartValveDTO getSmartValve(final String serialNumber) {
        final Integer id = getIdFromSerialNumber(serialNumber);

        if (id == null) {
            return null;
        }

        for (final SmartValveDTO smartValve : domain.getSmartValve()) {
            if (smartValve.getId().equals(id)) {
                return smartValve;
            }
        }
        return null;
    }

    private @Nullable Integer getIdFromSerialNumber(final String serialNumber) {
        for (final DeviceDTO device : domain.getDevice()) {
            if (device.getSerialNumber() != null
                    && device.getSerialNumber().toLowerCase().equals(serialNumber.toLowerCase())) {
                return device.getId();
            }
        }
        return null;
    }
}
