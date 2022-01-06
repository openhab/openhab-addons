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
package org.openhab.binding.generacmobilelink.internal.dto;

/**
 * {@link GeneratorStatusDTO} object from the MobileLink API
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GeneratorStatusDTO {
    public Integer gensetID;
    public String generatorDate;
    public String generatorName;
    public String generatorSerialNumber;
    public String generatorModel;
    public String generatorDescription;
    public String generatorMDN;
    public String generatorImei;
    public String generatorIccid;
    public String generatorTetherSerial;
    public Boolean connected;
    public Boolean greenLightLit;
    public Boolean yellowLightLit;
    public Boolean redLightLit;
    public Boolean blueLightLit;
    public String generatorStatus;
    public String generatorStatusDate;
    public String currentAlarmDescription;
    public Integer runHours;
    public Integer exerciseHours;
    public String batteryVoltage;
    public Integer fuelType;
    public Integer fuelLevel;
    public String generatorBrandImageURL;
    public Boolean generatorServiceStatus;
    public String signalStrength;
    public String deviceId;
    public Integer deviceTypeId;
    public String firmwareVersion;
    public String timezone;
    public String mACAddress;
    public String iPAddress;
    public String sSID;
}
