/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.discovery;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.hdpowerview.internal.dto.Firmware;
import org.openhab.binding.hdpowerview.internal.dto.HubFirmware;
import org.openhab.binding.hdpowerview.internal.dto.UserData;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Info;
import org.openhab.binding.hdpowerview.internal.dto.responses.FirmwareVersion;
import org.openhab.binding.hdpowerview.internal.dto.responses.UserDataResponse;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This helper class fetches gateway / hub serial numbers and version numbers to be used in the discovery services
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = HDPowerviewPropertyGetter.class)
public class HDPowerviewPropertyGetter {

    private static final String API_V1_FMT_USERDATA = "http://%s/api/userdata";
    private static final String API_V1_FMT_FIRMWARE = "http://%s/api/fwversion";
    private static final String API_V3_FMT_INFO = "http://%s/gateway/info";

    private final Gson gsonParser = new Gson();
    private final HttpClientFactory httpClientFactory;

    @Activate
    public HDPowerviewPropertyGetter(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Get the main processor firmware revision (i.e. the generation number) of the API v1 hub on the given IPv4 address
     *
     * @param host a dotted ipv4 address
     * @return the generation number
     * @throws HubException if anything failed
     */
    public String getGenerationApiV1(String host) throws HubException {
        try {
            HttpClient httpClient = httpClientFactory.getCommonHttpClient();
            String uri = String.format(API_V1_FMT_FIRMWARE, host);
            ContentResponse content = httpClient.GET(uri);
            if (HttpStatus.OK_200 == content.getStatus()) {
                String json = content.getContentAsString();
                FirmwareVersion fwVersion = gsonParser.fromJson(json, FirmwareVersion.class);
                if (fwVersion != null && (fwVersion.firmware instanceof HubFirmware fwHub)
                        && (fwHub.mainProcessor instanceof Firmware fwMainProcessor)) {
                    return String.valueOf(fwMainProcessor.revision);
                }
                throw new HubInvalidResponseException("getGenerationApiV1(): no firmware revision");
            }
            throw new HubProcessingException("getGenerationApiV1(): HTTP error " + content.getReason());
        } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new HubProcessingException("getGenerationApiV1(): " + e.getMessage(), e);
        }
    }

    /**
     * Get the serial number of the API v1 hub on the given IPv4 address
     *
     * @param host a dotted ipv4 address
     * @return the serial number
     * @throws HubException if anything failed
     */
    public String getSerialNumberApiV1(String host) throws HubException {
        try {
            HttpClient httpClient = httpClientFactory.getCommonHttpClient();
            String uri = String.format(API_V1_FMT_USERDATA, host);
            ContentResponse content = httpClient.GET(uri);
            if (HttpStatus.OK_200 == content.getStatus()) {
                String json = content.getContentAsString();
                UserDataResponse userDataResponse = gsonParser.fromJson(json, UserDataResponse.class);
                if (userDataResponse != null && userDataResponse.userData instanceof UserData userData
                        && userData.serialNumber instanceof String serial) {
                    return serial;
                }
                throw new HubInvalidResponseException("getSerialNumberApiV1(): no serial number");
            }
            throw new HubProcessingException("getSerialNumberApiV1(): " + content.getReason());
        } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new HubProcessingException("getSerialNumberApiV1(): " + e.getMessage(), e);
        }
    }

    /**
     * Get the serial number of the API v3 gateway on the given IPv4 address
     *
     * @param host a dotted ipv4 address
     * @return the serial number
     * @throws HubException if anything failed
     */
    public String getSerialNumberApiV3(String host) throws HubException {
        try {
            HttpClient httpClient = httpClientFactory.getCommonHttpClient();
            String uri = String.format(API_V3_FMT_INFO, host);
            ContentResponse content = httpClient.GET(uri);
            if (HttpStatus.OK_200 == content.getStatus()) {
                String json = content.getContentAsString();
                Info info = gsonParser.fromJson(json, Info.class);
                if (info != null && info.getSerialNumber() instanceof String serial) {
                    return serial;
                }
                throw new HubInvalidResponseException("getSerialNumberApiV3(): no serial number");
            }
            throw new HubProcessingException("getSerialNumberApiV3(): " + content.getReason());
        } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new HubProcessingException("getSerialNumberApiV3(): " + e.getMessage(), e);
        }
    }
}
