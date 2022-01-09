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
package org.openhab.binding.revogi.internal.api;

import java.util.Objects;

/**
 * @author Andi Bräu - Initial contribution
 */
public class DiscoveryRawResponseDTO {

    private final int response;
    private final DiscoveryResponseDTO data;
    private String ipAddress;

    public DiscoveryRawResponseDTO(int response, DiscoveryResponseDTO data) {
        this.response = response;
        this.data = data;
    }

    public int getResponse() {
        return response;
    }

    public DiscoveryResponseDTO getData() {
        return data;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoveryRawResponseDTO that = (DiscoveryRawResponseDTO) o;
        return response == that.response && data.equals(that.data) && Objects.equals(ipAddress, that.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response, data, ipAddress);
    }
}
