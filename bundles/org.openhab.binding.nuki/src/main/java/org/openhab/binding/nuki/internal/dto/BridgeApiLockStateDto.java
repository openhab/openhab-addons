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
package org.openhab.binding.nuki.internal.dto;

/**
 * The {@link BridgeApiLockStateDto} class defines the Data Transfer Object (POJO) for the Nuki Bridge API /lockState
 * endpoint.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiLockStateDto {

    private int state;
    private String stateName;
    private boolean batteryCritical;
    private boolean success;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public boolean isBatteryCritical() {
        return batteryCritical;
    }

    public void setBatteryCritical(boolean batteryCritical) {
        this.batteryCritical = batteryCritical;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
