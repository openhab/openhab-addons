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
package org.openhab.binding.unifi.internal.api.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * Data object to keep track of all port data, including all port_override data (both for ports and additional data) on
 * a switch device.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class UniFiSwitchPorts {

    /**
     * Port data grouped by port id.
     */
    private final Map<Integer, UniFiPortTuple> ports = new HashMap<>();
    /**
     * Additional none port specific override data. Keep track to send to device when updating override data.
     */
    private final Set<JsonObject> otherOverrides = new HashSet<>();

    /**
     * Return port data for the given port
     *
     * @param portIdx port to get the data for
     * @return Return port data for the given port
     */
    public @Nullable UniFiPortTuple getPort(final int portIdx) {
        return ports.get(portIdx);
    }

    /**
     * Return port data for the given port or if none exists set a new data object and return it.
     *
     * @param portIdx port to get the data for
     * @return Return port data for the given port or if none exists set a new data object and return it.
     */
    public UniFiPortTuple computeIfAbsent(final int portIdx) {
        final UniFiPortTuple tuple = ports.computeIfAbsent(portIdx, t -> new UniFiPortTuple());
        if (tuple == null) {
            // This should never happen because ports can never contain a null value, and computeIfAbsent should never
            // return null. However to satisfy the compiler a check for null was added.
            throw new IllegalStateException("UniFiPortTuple is null for portIdx " + portIdx);
        }
        return tuple;
    }

    /**
     * @return Returns the list of PoE Ports.
     */
    public List<UniFiPortTuple> getPoePorts() {
        return ports.values().stream().filter(e -> e.getTable().isPortPoe()).collect(Collectors.toList());
    }

    /**
     * Returns the override data as list with json objects after calling the updateMethod on the data for the given
     * portIdx.
     * The update method changes the data in the internal structure.
     *
     * @param portIdx port to call updateMethod for
     * @param updateMethod method to call to update data for a specific port
     * @return Returns a list of json objects of all override data
     */
    public List<JsonObject> updatedList(final int portIdx, final Consumer<UnfiPortOverrideJsonObject> updateMethod) {
        @SuppressWarnings("null")
        final List<UnfiPortOverrideJsonObject> updatedList = ports.entrySet().stream()
                .map(e -> e.getValue().getJsonElement()).filter(Objects::nonNull).collect(Collectors.toList());

        updatedList.stream().filter(p -> p.getPortIdx() == portIdx).findAny().ifPresent(updateMethod::accept);

        return Stream
                .concat(otherOverrides.stream(), updatedList.stream().map(UnfiPortOverrideJsonObject::getJsonObject))
                .collect(Collectors.toList());
    }

    /**
     * Set the port override object. If it's for a specific port set bind it to the port data, otherwise store it as
     * generic data.
     *
     * @param jsonObject json object to set
     */
    public void setOverride(final JsonObject jsonObject) {
        if (UnfiPortOverrideJsonObject.hasPortIdx(jsonObject)) {
            final UnfiPortOverrideJsonObject po = new UnfiPortOverrideJsonObject(jsonObject);
            final UniFiPortTuple tuple = ports.get(po.getPortIdx());

            if (tuple == null) {
                otherOverrides.add(jsonObject);
            } else {
                tuple.setJsonElement(po);
            }
        } else {
            otherOverrides.add(jsonObject);
        }
    }
}
