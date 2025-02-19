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
package org.openhab.binding.amazonechocontrol.internal.dto;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link DeviceTO} encapsulate information about a single device
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DeviceTO {
    public String accountName;
    public String serialNumber;
    public String deviceOwnerCustomerId;
    public String deviceAccountId;
    public String deviceFamily;
    public String deviceType;
    public String softwareVersion;
    public boolean online;
    public Set<String> capabilities = Set.of();

    @Override
    public @NonNull String toString() {
        return "Device{accountName='" + accountName + "', serialNumber='" + serialNumber + "', deviceOwnerCustomerId='"
                + deviceOwnerCustomerId + "', deviceAccountId='" + deviceAccountId + "', deviceFamily='" + deviceFamily
                + "', deviceType='" + deviceType + "', softwareVersion='" + softwareVersion + "', online=" + online
                + ", capabilities=" + capabilities + "}";
    }
}
