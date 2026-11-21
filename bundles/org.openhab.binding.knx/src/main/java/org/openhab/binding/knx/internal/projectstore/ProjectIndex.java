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
package org.openhab.binding.knx.internal.projectstore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.monitor.MonitorDecoder;
import org.openhab.binding.knx.internal.project.KnxChannelMapper;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;

/**
 * What the monitor asks of a project, by raw address, built once per loaded project. Immutable.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProjectIndex {

    private final Map<Integer, String> addressNames;
    private final Map<Integer, String> addressTypes;
    private final Map<Integer, String> deviceNames;

    private ProjectIndex(Map<Integer, String> addressNames, Map<Integer, String> addressTypes,
            Map<Integer, String> deviceNames) {
        this.addressNames = Map.copyOf(addressNames);
        this.addressTypes = Map.copyOf(addressTypes);
        this.deviceNames = Map.copyOf(deviceNames);
    }

    static ProjectIndex of(KnxProject project) {
        Map<Integer, String> names = new HashMap<>();
        Map<Integer, String> types = new HashMap<>();
        Map<Integer, String> devices = new HashMap<>();
        KnxChannelMapper mapper = new KnxChannelMapper(project);
        Set<Integer> seen = new HashSet<>();
        for (KnxGroupAddress address : project.groupAddresses()) {
            int raw = address.rawAddress();
            if (!seen.add(raw)) {
                continue;
            }
            String name = ProjectNames.clean(address.name());
            if (!name.isEmpty()) {
                names.put(raw, name);
            }
            String value = mapper.forGroupAddress(address).value();
            int colon = value.indexOf(':');
            if (colon > 0 && MonitorDecoder.isSupported(value.substring(0, colon))) {
                types.put(raw, value.substring(0, colon));
            }
        }
        for (KnxDevice device : project.topology().devices()) {
            KnxIndividualAddress individual = device.individualAddress();
            if (individual == null) {
                continue;
            }
            String name = ProjectNames.clean(project.displayNameOf(device));
            if (!name.isEmpty()) {
                devices.putIfAbsent(raw(individual), name);
            }
        }
        return new ProjectIndex(names, types, devices);
    }

    private static int raw(KnxIndividualAddress address) {
        return (address.area() << 12) | (address.line() << 8) | address.device();
    }

    @Nullable
    String addressName(int rawAddress) {
        return addressNames.get(rawAddress);
    }

    @Nullable
    String addressType(int rawAddress) {
        return addressTypes.get(rawAddress);
    }

    @Nullable
    String deviceName(int rawAddress) {
        return deviceNames.get(rawAddress);
    }
}
