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
package org.openhab.binding.tr064.internal.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tr064.internal.SCPDException;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDRootType;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDScpdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SCPDUtil} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SCPDUtil {
    private final Logger logger = LoggerFactory.getLogger(SCPDUtil.class);

    private final HttpClient httpClient;

    private SCPDRootType scpdRoot;
    private final List<SCPDDeviceType> scpdDevicesList = new ArrayList<>();
    private final Map<String, @Nullable SCPDScpdType> serviceMap = new HashMap<>();

    public SCPDUtil(HttpClient httpClient, String endpoint) throws SCPDException {
        this.httpClient = httpClient;

        SCPDRootType scpdRoot = getAndUnmarshalSCPD(endpoint + "/tr64desc.xml", SCPDRootType.class);
        if (scpdRoot == null) {
            throw new SCPDException("could not get SCPD root");
        }
        this.scpdRoot = scpdRoot;

        scpdDevicesList.addAll(flatDeviceList(scpdRoot.getDevice()).collect(Collectors.toList()));
        for (SCPDDeviceType device : scpdDevicesList) {
            for (SCPDServiceType service : device.getServiceList()) {
                SCPDScpdType scpd = serviceMap.computeIfAbsent(service.getServiceId(),
                        serviceId -> getAndUnmarshalSCPD(endpoint + service.getSCPDURL(), SCPDScpdType.class));
                if (scpd == null) {
                    throw new SCPDException("could not get SCPD service");
                }
            }
        }
    }

    /**
     * generic unmarshaller
     *
     * @param uri the uri of the XML file
     * @param clazz the class describing the XML file
     * @return unmarshalling result
     */
    private <T> @Nullable T getAndUnmarshalSCPD(String uri, Class<T> clazz) {
        try {
            ContentResponse contentResponse = httpClient.newRequest(uri).timeout(2, TimeUnit.SECONDS)
                    .method(HttpMethod.GET).send();
            InputStream xml = new ByteArrayInputStream(contentResponse.getContent());

            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller um = context.createUnmarshaller();
            return um.unmarshal(new StreamSource(xml), clazz).getValue();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.debug("HTTP Failed to GET uri '{}': {}", uri, e.getMessage());
        } catch (JAXBException e) {
            logger.debug("Unmarshalling failed: {}", e.getMessage());
        }
        return null;
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
    public Optional<@Nullable SCPDScpdType> getService(String serviceId) {
        return Optional.ofNullable(serviceMap.get(serviceId));
    }
}
