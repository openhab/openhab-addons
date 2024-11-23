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
package org.openhab.binding.nuki.internal.dataexchange;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuki.internal.dto.BridgeApiListDeviceDto;

/**
 * The {@link BridgeListResponse} class wraps {@link BridgeApiListDeviceDto} class.
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public class BridgeListResponse extends NukiBaseResponse {

    private final List<BridgeApiListDeviceDto> devices;

    public BridgeListResponse(int status, @Nullable String message, @Nullable List<BridgeApiListDeviceDto> devices) {
        super(status, message);
        setSuccess(devices != null);
        this.devices = devices == null ? Collections.emptyList() : Collections.unmodifiableList(devices);
    }

    public BridgeListResponse(NukiBaseResponse nukiBaseResponse) {
        this(nukiBaseResponse.getStatus(), nukiBaseResponse.getMessage(), null);
    }

    public List<BridgeApiListDeviceDto> getDevices() {
        return devices;
    }

    public @Nullable BridgeApiListDeviceDto getDevice(String nukiId) {
        for (BridgeApiListDeviceDto device : this.devices) {
            if (device.getNukiId().equals(nukiId)) {
                return device;
            }
        }
        return null;
    }
}
