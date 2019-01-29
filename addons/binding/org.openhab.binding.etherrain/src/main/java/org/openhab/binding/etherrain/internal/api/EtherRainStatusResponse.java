/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etherrain.internal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtherRainStatusResponse {

    private final Logger logger = LoggerFactory.getLogger(EtherRainStatusResponse.class);

    private String uniqueName;
    private String macAddress;
    private String serviceAccount;

    private OperatingStatus operatingStatus = null;
    private CommandStatus lastCommandStatus = null;
    private CommandResult lastCommandResult = null;

    private int lastActiveValue;
    private boolean rainSensor;

    public enum OperatingStatus {
        STATUS_READY("RD"),
        STATUS_WAITING("WT"),
        STATUS_BUSY("BZ");

        private String status;

        OperatingStatus(String status) {
            this.status = status;
        }

        public static OperatingStatus fromString(String text) {
            for (OperatingStatus b : OperatingStatus.values()) {
                if (b.status.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    public enum CommandStatus {
        STATUS_OK("OK"),
        STATUS_ERROR("ER"),
        STATUS_UNATHORIZED("NA");

        private String status;

        CommandStatus(String status) {
            this.status = status;
        }

        public static CommandStatus fromString(String text) {
            for (CommandStatus b : CommandStatus.values()) {
                if (b.status.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum CommandResult {
        RESULT_OK("OK"),
        RESULT_INTERRUPTED_RAIN("RN"),
        RESULT_INTERUPPTED_SHORT("SH"),
        RESULT_INCOMPLETE("NC");

        private String result;

        CommandResult(String result) {
            this.result = result;
        }

        public static CommandResult fromString(String text) {
            for (CommandResult b : CommandResult.values()) {
                if (b.result.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

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

    public OperatingStatus getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(OperatingStatus operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    public CommandStatus getLastCommandStatus() {
        return lastCommandStatus;
    }

    public void setLastCommandStatus(CommandStatus lastCommandStatus) {
        this.lastCommandStatus = lastCommandStatus;
    }

    public CommandResult getLastCommandResult() {
        return lastCommandResult;
    }

    public void setLastCommandResult(CommandResult lastCommandResult) {
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

    public void printDebug() {

        logger.debug("EtherRain Status Update");

        logger.debug("Unique Name: " + uniqueName);
        logger.debug("Mac Address: " + macAddress);
        logger.debug("Service Account: " + serviceAccount);

        logger.debug("Operating Status: " + operatingStatus.toString());
        logger.debug("Last Command Status: " + lastCommandStatus.toString());
        logger.debug("Last Command Result: " + lastCommandResult.toString());

        logger.debug("Last Active Valve: " + lastActiveValue);
        logger.debug("Rain Sensor: " + rainSensor);

    }

}
