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
package org.openhab.binding.etherrain.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EtherRainStatusResponse} is a encapsulation of responses from the EtherRain
 *
 * @author Joe Inkenbrandt - Initial contribution
 */

@NonNullByDefault
public class EtherRainStatusResponse {

    private String uniqueName = "";
    private String macAddress = "";
    private String serviceAccount = "";

    private EtherRainOperatingStatus operatingStatus = EtherRainOperatingStatus.WT;
    private EtherRainCommandStatus lastCommandStatus = EtherRainCommandStatus.ER;
    private EtherRainCommandResult lastCommandResult = EtherRainCommandResult.NC;

    private int lastActiveValue;
    private boolean rainSensor;

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public EtherRainOperatingStatus getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(EtherRainOperatingStatus operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    public EtherRainCommandStatus getLastCommandStatus() {
        return lastCommandStatus;
    }

    public void setLastCommandStatus(EtherRainCommandStatus lastCommandStatus) {
        this.lastCommandStatus = lastCommandStatus;
    }

    public EtherRainCommandResult getLastCommandResult() {
        return lastCommandResult;
    }

    public void setLastCommandResult(EtherRainCommandResult lastCommandResult) {
        this.lastCommandResult = lastCommandResult;
    }

    public int getLastActiveValue() {
        return lastActiveValue;
    }

    public void setLastActiveValue(int lastActiveValue) {
        this.lastActiveValue = lastActiveValue;
    }

    public boolean isRainSensor() {
        return rainSensor;
    }

    public void setRainSensor(boolean rainSensor) {
        this.rainSensor = rainSensor;
    }
}
