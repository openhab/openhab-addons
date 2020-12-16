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
package org.openhab.binding.generacmobilelink.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link GeneratorStatus} object from the MobileLink API
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneratorStatus {
    public @Nullable Integer gensetID;
    public @Nullable String generatorDate;
    public @Nullable String generatorName;
    public @Nullable String generatorSerialNumber;
    public @Nullable String generatorModel;
    public @Nullable String generatorDescription;
    public @Nullable String generatorMDN;
    public @Nullable String generatorImei;
    public @Nullable String generatorIccid;
    public @Nullable String generatorTetherSerial;
    public @Nullable Boolean connected;
    public @Nullable Boolean greenLightLit;
    public @Nullable Boolean yellowLightLit;
    public @Nullable Boolean redLightLit;
    public @Nullable Boolean blueLightLit;
    public @Nullable String generatorStatus;
    public @Nullable String generatorStatusDate;
    public @Nullable String currentAlarmDescription;
    public @Nullable Integer runHours;
    public @Nullable Integer exerciseHours;
    public @Nullable String batteryVoltage;
    public @Nullable Integer fuelType;
    public @Nullable Integer fuelLevel;
    public @Nullable String generatorBrandImageURL;
    public @Nullable Boolean generatorServiceStatus;
    public @Nullable String signalStrength;
    public @Nullable String deviceId;
    public @Nullable Integer deviceTypeId;
    public @Nullable String firmwareVersion;
    public @Nullable String timezone;
    public @Nullable String mACAddress;
    public @Nullable String iPAddress;
    public @Nullable String sSID;
}
