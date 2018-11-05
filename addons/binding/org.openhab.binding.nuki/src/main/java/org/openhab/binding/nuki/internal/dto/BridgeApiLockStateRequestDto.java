/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dto;

/**
 * The {@link BridgeApiLockStateRequestDto} class defines the Data Transfer Object (POJO) which is send from the Nuki
 * Bridge to the openHAB Server.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiLockStateRequestDto {

    private int nukiId;
    private int state;
    private String stateName;
    private boolean batteryCritical;

    public int getNukiId() {
        return nukiId;
    }

    public void setNukiId(int nukiId) {
        this.nukiId = nukiId;
    }

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

}
