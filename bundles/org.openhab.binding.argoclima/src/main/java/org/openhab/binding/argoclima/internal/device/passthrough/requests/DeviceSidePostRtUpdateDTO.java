/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.device.passthrough.requests;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

/**
 * Device's update - sent from AC to manufacturer's remote server (via POST ...CM=UI_RT command)
 *
 * @implNote These updates seem to only be sent if requested by the remote side (when the response to {@code GET UI_FLG}
 *           contains a
 *           {@link org.openhab.binding.argoclima.internal.device.passthrough.responses.RemoteGetUiFlgResponseDTO.UiFlgResponsePreamble#flag0requestPostUiRt
 *           flag0requestPostUiRt} bit set in the preamble}
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class DeviceSidePostRtUpdateDTO {
    /** The name of the POST command carried in body. Seems fixed to {@code UI_RT} for this format */
    public final String command;

    /** The username for the remote server (and hence the UI) */
    public final String username;

    /** A MD5 hash of password to the remote server (and hence the UI) */
    public final String passwordHash;

    /** The CPU_ID (unique and immutable HVAC identifier) send by the device */
    public final String cpuId;

    /** Unknown purpose, seems to be set to 1 in all requests observed. DEL is for delta? */
    public final String delParam;

    /**
     * Unknown format - has multiple comma-separated values, and looks like a massive superset of the HMI string
     * typically sent (156 values vs 39)
     *
     * @implNote The ordering of values is different from the HMI string, though there are similarities. Since it
     *           doesn't seem to carry anything immediately obvious or attractive, this is not being parsed at this
     *           point. Likely conveys all the "schedule" settings as well as configuration parameters though...
     */
    public final String dataParam;

    /**
     * Private c-tor (from response body, which seems to be URL-encoded query-like param set)
     *
     * @param bodyArgumentMap The payload, decomposed into K->V map
     */
    private DeviceSidePostRtUpdateDTO(Map<String, String> bodyArgumentMap) {
        this.command = Objects.requireNonNullElse(bodyArgumentMap.get("CM"), "");
        this.username = Objects.requireNonNullElse(bodyArgumentMap.get("USN"), "");
        this.passwordHash = Objects.requireNonNullElse(bodyArgumentMap.get("PSW"), "");
        this.cpuId = Objects.requireNonNullElse(bodyArgumentMap.get("CPU_ID"), "");

        this.delParam = Objects.requireNonNullElse(bodyArgumentMap.get("DEL"), "");
        this.dataParam = Objects.requireNonNullElse(bodyArgumentMap.get("DATA"), "");
    }

    /**
     * Named c-tor (constructs this DTO from device-side request body)
     *
     * @implNote Headers or URL do not seem to carry any meaningful (variable) information, hence not parsing them
     * @implNote This class does only shallow parsing for now (ex. does not decode the 'data' element to an array)
     * @param requestBody The body of the device-side request to parse
     * @return Pre-parsed DTO
     */
    public static DeviceSidePostRtUpdateDTO fromDeviceRequestBody(String requestBody) {
        var paramsParsed = new MultiMap<@Nullable String>(); // @Nullable here due to UrlEncoded API
        UrlEncoded.decodeTo(requestBody, paramsParsed, StandardCharsets.US_ASCII);

        Map<String, String> flattenedParams = paramsParsed.keySet().stream().collect(TreeMap::new,
                (m, v) -> m.put(Objects.requireNonNull(v), Objects.requireNonNull(paramsParsed.getString(v))),
                TreeMap::putAll);

        return new DeviceSidePostRtUpdateDTO(flattenedParams);
    }

    @Override
    public String toString() {
        return String.format(
                "Device-side POST update:\n\tCommand=%s,\n\tCredentials=[username=%s, password(MD5)=%s],\n\tCPU_ID=%s,\n\tDEL=%s,\n\tDATA=%s.",
                this.command, this.username, this.passwordHash, this.cpuId, this.delParam, this.dataParam);
    }
}
