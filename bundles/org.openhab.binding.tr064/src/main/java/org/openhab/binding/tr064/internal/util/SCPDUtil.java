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
package org.openhab.binding.tr064.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tr064.internal.SCPDException;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDRootType;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDScpdType;

/**
 * The {@link SCPDUtil} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SCPDUtil {
    private SCPDRootType scpdRoot;
    private final List<SCPDDeviceType> scpdDevicesList = new ArrayList<>();
    private final Map<String, SCPDScpdType> serviceMap = new HashMap<>();

    public SCPDUtil(HttpClient httpClient, String endpoint) throws SCPDException {
        SCPDRootType scpdRoot = Util.getAndUnmarshalXML(httpClient, endpoint + "/tr64desc.xml", SCPDRootType.class);
        if (scpdRoot == null) {
            throw new SCPDException("could not get SCPD root");
        }
        this.scpdRoot = scpdRoot;

        scpdDevicesList.addAll(flatDeviceList(scpdRoot.getDevice()).collect(Collectors.toList()));
        for (SCPDDeviceType device : scpdDevicesList) {
            for (SCPDServiceType service : device.getServiceList()) {
                SCPDScpdType scpd = serviceMap.computeIfAbsent(service.getServiceId(), serviceId -> Util
                        .getAndUnmarshalXML(httpClient, endpoint + service.getSCPDURL(), SCPDScpdType.class));
                if (scpd == null) {
                    throw new SCPDException("could not get SCPD service");
                }
            }
        }
    }

    /**
     * recursively flatten the device tree to a stream
     *
     * @param device a device
     * @return stream of sub-devices
     */
    private Stream<SCPDDeviceType> flatDeviceList(SCPDDeviceType device) {
        return Stream.concat(Stream.of(device), device.getDeviceList().stream().flatMap(this::flatDeviceList));
    }

    /**
     * get a list of all sub-devices (root device not included)
     *
     * @return the device list
     */
    public List<SCPDDeviceType> getAllSubDevices() {
        return scpdDevicesList.stream().filter(device -> !device.getUDN().equals(scpdRoot.getDevice().getUDN()))
                .collect(Collectors.toList());
    }

    /**
     * get a single device by it's UDN
     *
     * @param udn the device UDN
     * @return the device
     */
    public Optional<SCPDDeviceType> getDevice(String udn) {
        if (udn.isEmpty()) {
            return Optional.of(scpdRoot.getDevice());
        } else {
            return getAllSubDevices().stream().filter(device -> udn.equals(device.getUDN())).findFirst();
        }
    }

    /**
     * get a single service by it's serviceId
     *
     * @param serviceId the service id
     * @return the service
     */
    public Optional<SCPDScpdType> getService(String serviceId) {
        return Optional.ofNullable(serviceMap.get(serviceId));
    }
}
