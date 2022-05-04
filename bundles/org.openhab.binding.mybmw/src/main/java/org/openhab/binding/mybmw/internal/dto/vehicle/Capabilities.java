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
package org.openhab.binding.mybmw.internal.dto.vehicle;

/**
 * The {@link Capabilities} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */

public class Capabilities {
    public boolean isRemoteServicesBookingRequired;
    public boolean isRemoteServicesActivationRequired;
    public boolean isRemoteHistorySupported;
    public boolean canRemoteHistoryBeDeleted;
    public boolean isChargingHistorySupported;
    public boolean isScanAndChargeSupported;
    public boolean isDCSContractManagementSupported;
    public boolean isBmwChargingSupported;
    public boolean isMiniChargingSupported;
    public boolean isChargeNowForBusinessSupported;
    public boolean isDataPrivacyEnabled;
    public boolean isChargingPlanSupported;
    public boolean isChargingPowerLimitEnable;
    public boolean isChargingTargetSocEnable;
    public boolean isChargingLoudnessEnable;
    public boolean isChargingSettingsEnabled;
    public boolean isChargingHospitalityEnabled;
    public boolean isEvGoChargingSupported;
    public boolean isFindChargingEnabled;
    public boolean isCustomerEsimSupported;
    public boolean isCarSharingSupported;
    public boolean isEasyChargeSupported;

    public RemoteService lock;
    public RemoteService unlock;
    public RemoteService lights;
    public RemoteService horn;
    public RemoteService vehicleFinder;
    public RemoteService sendPoi;
    public RemoteService climateNow;
}
