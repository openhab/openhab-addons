/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openhab.binding.mybmw.internal.dto.charge.RemoteChargingCommands;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleCapabilities} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored to Java Bean
 */

public class VehicleCapabilities {
    private final Logger logger = LoggerFactory.getLogger(VehicleCapabilities.class);

    private static final String PREFIX_IS = "is";
    public static final String SUPPORTED_SUFFIX = "Supported";
    public static final String ENABLED_SUFFIX = "Enabled";

    // private boolean remoteChargingCommands = false; // {}, don't know what comes
    // private boolean specialThemeSupport = false; // [] don't know what comes here
    private boolean checkSustainabilityDPP = false; // false,
    private boolean climateNow = false; // true,
    private boolean horn = false; // true,
    private boolean isBmwChargingSupported = false; // false,
    private boolean isCarSharingSupported = false; // false,
    private boolean isChargeNowForBusinessSupported = false; // false,
    private boolean isChargingHistorySupported = false; // false,
    private boolean isChargingHospitalityEnabled = false; // false,
    private boolean isChargingLoudnessEnabled = false; // false,
    private boolean isChargingPlanSupported = false; // false,
    private boolean isChargingPowerLimitEnabled = false; // false,
    private boolean isChargingSettingsEnabled = false; // false,
    private boolean isChargingTargetSocEnabled = false; // false,
    private boolean isClimateTimerSupported = false; // true,
    private boolean isClimateTimerWeeklyActive = false; // true,
    private boolean isCustomerEsimSupported = false; // false,
    private boolean isDataPrivacyEnabled = false; // false,
    private boolean isDCSContractManagementSupported = false; // false,
    private boolean isEasyChargeEnabled = false; // false,
    private boolean isEvGoChargingSupported = false; // false,
    private boolean isMiniChargingSupported = false; // false,
    private boolean isNonLscFeatureEnabled = false; // false,
    private boolean isRemoteEngineStartSupported = false; // false,
    private boolean isRemoteHistoryDeletionSupported = false; // false,
    private boolean isRemoteHistorySupported = false; // true,
    private boolean isRemoteParkingSupported = false; // false,
    private boolean isRemoteServicesActivationRequired = false; // false,
    private boolean isRemoteServicesBookingRequired = false; // false,
    private boolean isScanAndChargeSupported = false; // false,
    private boolean isSustainabilityAccumulatedViewEnabled = false; // false,
    private boolean isSustainabilitySupported = false; // false,
    private boolean isWifiHotspotServiceSupported = false; // true,
    private boolean lights = false; // true,
    private boolean lock = false; // true,
    private boolean remote360 = false;
    private RemoteChargingCommands remoteChargingCommands = new RemoteChargingCommands();
    private boolean remoteSoftwareUpgrade = false; // true,
    private boolean sendPoi = false; // true,
    private boolean speechThirdPartyAlexa = false; // true,
    private boolean speechThirdPartyAlexaSDK = false; // false,
    private boolean unlock = false; // true,

    /**
     * @return the climateNow
     */
    public boolean isClimateNow() {
        return climateNow;
    }

    /**
     * @return the horn
     */
    public boolean isHorn() {
        return horn;
    }

    /**
     * @return the lights
     */
    public boolean isLights() {
        return lights;
    }

    /**
     * @return the lock
     */
    public boolean isLock() {
        return lock;
    }

    /**
     * @return the remote360
     */
    public boolean isRemote360() {
        return remote360;
    }

    /**
     * @return the sendPoi
     */
    public boolean isSendPoi() {
        return sendPoi;
    }

    /**
     * @return the unlock
     */
    public boolean isUnlock() {
        return unlock;
    }

    /**
     * @return the vehicleFinder
     */
    public boolean isVehicleFinder() {
        return vehicleFinder;
    }

    /**
     * @return the digitalKey
     */
    public DigitalKey getDigitalKey() {
        return digitalKey;
    }

    private boolean vehicleFinder = false; // true,
    private DigitalKey digitalKey = new DigitalKey();
    private String a4aType = ""; // NOT_SUPPORTED,
    private String climateFunction = ""; // VENTILATION,
    private String climateTimerTrigger = ""; // DEPARTURE_TIMER,
    private String lastStateCallState = ""; // ACTIVATED,
    private String vehicleStateSource = ""; // LAST_STATE_CALL,

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "VehicleCapabilities [checkSustainabilityDPP=" + checkSustainabilityDPP + ", climateNow=" + climateNow
                + ", horn=" + horn + ", isBmwChargingSupported=" + isBmwChargingSupported + ", isCarSharingSupported="
                + isCarSharingSupported + ", isChargeNowForBusinessSupported=" + isChargeNowForBusinessSupported
                + ", isChargingHistorySupported=" + isChargingHistorySupported + ", isChargingHospitalityEnabled="
                + isChargingHospitalityEnabled + ", isChargingLoudnessEnabled=" + isChargingLoudnessEnabled
                + ", isChargingPlanSupported=" + isChargingPlanSupported + ", isChargingPowerLimitEnabled="
                + isChargingPowerLimitEnabled + ", isChargingSettingsEnabled=" + isChargingSettingsEnabled
                + ", isChargingTargetSocEnabled=" + isChargingTargetSocEnabled + ", isClimateTimerSupported="
                + isClimateTimerSupported + ", isClimateTimerWeeklyActive=" + isClimateTimerWeeklyActive
                + ", isCustomerEsimSupported=" + isCustomerEsimSupported + ", isDataPrivacyEnabled="
                + isDataPrivacyEnabled + ", isDCSContractManagementSupported=" + isDCSContractManagementSupported
                + ", isEasyChargeEnabled=" + isEasyChargeEnabled + ", isEvGoChargingSupported="
                + isEvGoChargingSupported + ", isMiniChargingSupported=" + isMiniChargingSupported
                + ", isNonLscFeatureEnabled=" + isNonLscFeatureEnabled + ", isRemoteEngineStartSupported="
                + isRemoteEngineStartSupported + ", isRemoteHistoryDeletionSupported="
                + isRemoteHistoryDeletionSupported + ", isRemoteHistorySupported=" + isRemoteHistorySupported
                + ", isRemoteParkingSupported=" + isRemoteParkingSupported + ", isRemoteServicesActivationRequired="
                + isRemoteServicesActivationRequired + ", isRemoteServicesBookingRequired="
                + isRemoteServicesBookingRequired + ", isScanAndChargeSupported=" + isScanAndChargeSupported
                + ", isSustainabilityAccumulatedViewEnabled=" + isSustainabilityAccumulatedViewEnabled
                + ", isSustainabilitySupported=" + isSustainabilitySupported + ", isWifiHotspotServiceSupported="
                + isWifiHotspotServiceSupported + ", lights=" + lights + ", lock=" + lock + ", remote360=" + remote360
                + ", remoteChargingCommands=" + remoteChargingCommands + ", remoteSoftwareUpgrade="
                + remoteSoftwareUpgrade + ", sendPoi=" + sendPoi + ", speechThirdPartyAlexa=" + speechThirdPartyAlexa
                + ", speechThirdPartyAlexaSDK=" + speechThirdPartyAlexaSDK + ", unlock=" + unlock + ", vehicleFinder="
                + vehicleFinder + ", digitalKey=" + digitalKey + ", a4aType=" + a4aType + ", climateFunction="
                + climateFunction + ", climateTimerTrigger=" + climateTimerTrigger + ", lastStateCallState="
                + lastStateCallState + ", vehicleStateSource=" + vehicleStateSource + "]";
    }

    /**
     * returns a list of capabilities filtered by the provided suffix and the enabled requirement
     * 
     * @param suffix
     * @param enabled
     * @return
     */
    public String getCapabilitiesAsString(String suffix, boolean enabled) {
        StringBuffer capabilitiesAsString = new StringBuffer();
        List<String> capabilitiesAsStringList = getCapabilitiesAsStringList(suffix, enabled);

        for (String capEntry : capabilitiesAsStringList) {
            // remove "is" prefix and provided suffix
            String cut = capEntry.substring(2);
            if (cut.endsWith(suffix)) {
                if (capabilitiesAsString.length() > 0) {
                    capabilitiesAsString.append(Constants.SEMICOLON);
                }
                capabilitiesAsString.append(cut.substring(0, cut.length() - suffix.length()));
            }
        }
        return capabilitiesAsString.toString();
    }

    private List<String> getCapabilitiesAsStringList(String suffix, boolean compare) {
        List<String> l = new ArrayList<>();

        Arrays.asList(VehicleCapabilities.class.getDeclaredFields()).stream()
                .filter(field -> field.getName().startsWith(PREFIX_IS) && field.getName().endsWith(suffix))
                .forEach(field -> {
                    try {
                        boolean value = field.getBoolean(this);
                        if (compare == value) {
                            l.add(field.getName());
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        logger.trace("field {} not usable: ", field.getName());
                    }
                });

        return l;
    }
}
