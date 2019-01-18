/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.toon.internal.api;

/**
 * The {@link DeviceConfig} class defines the json object as received by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class DeviceConfig {
    // shared fields
    private String devUUID;
    private String name;

    // deviceConfigInfo fields
    private String devType;
    private Long usageCapable;

    // deviceStatusInfo fields
    private Long currentState;
    private Long isConnected;
    private Double currentUsage;

    public String getDevUUID() {
        return devUUID;
    }

    public void setDevUUID(String devUUID) {
        this.devUUID = devUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public Long getUsageCapable() {
        return usageCapable;
    }

    public void setUsageCapable(Long usageCapable) {
        this.usageCapable = usageCapable;
    }

    public Long getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Long currentState) {
        this.currentState = currentState;
    }

    public Long getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Long isConnected) {
        this.isConnected = isConnected;
    }

    public Double getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(Double currentUsage) {
        this.currentUsage = currentUsage;
    }
}
