/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
