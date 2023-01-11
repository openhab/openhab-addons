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
    private boolean vehicleFinder = false; // true,
    private DigitalKey digitalKey = new DigitalKey();
    private String a4aType = ""; // NOT_SUPPORTED,
    private String climateFunction = ""; // VENTILATION,
    private String climateTimerTrigger = ""; // DEPARTURE_TIMER,
    private String lastStateCallState = ""; // ACTIVATED,
    private String vehicleStateSource = ""; // LAST_STATE_CALL,

    public String getA4aType() {
        return a4aType;
    }

    public void setA4aType(String a4aType) {
        this.a4aType = a4aType;
    }

    public boolean isClimateNow() {
        return climateNow;
    }

    public void setClimateNow(boolean climateNow) {
        this.climateNow = climateNow;
    }

    public boolean isClimateTimerSupported() {
        return isClimateTimerSupported;
    }

    public void setClimateTimerSupported(boolean isClimateTimerSupported) {
        this.isClimateTimerSupported = isClimateTimerSupported;
    }

    public String getClimateTimerTrigger() {
        return climateTimerTrigger;
    }

    public void setClimateTimerTrigger(String climateTimerTrigger) {
        this.climateTimerTrigger = climateTimerTrigger;
    }

    public String getClimateFunction() {
        return climateFunction;
    }

    public void setClimateFunction(String climateFunction) {
        this.climateFunction = climateFunction;
    }

    public boolean isHorn() {
        return horn;
    }

    public void setHorn(boolean horn) {
        this.horn = horn;
    }

    public boolean isBmwChargingSupported() {
        return isBmwChargingSupported;
    }

    public void setBmwChargingSupported(boolean isBmwChargingSupported) {
        this.isBmwChargingSupported = isBmwChargingSupported;
    }

    public boolean isCarSharingSupported() {
        return isCarSharingSupported;
    }

    public void setCarSharingSupported(boolean isCarSharingSupported) {
        this.isCarSharingSupported = isCarSharingSupported;
    }

    public boolean isChargeNowForBusinessSupported() {
        return isChargeNowForBusinessSupported;
    }

    public void setChargeNowForBusinessSupported(boolean isChargeNowForBusinessSupported) {
        this.isChargeNowForBusinessSupported = isChargeNowForBusinessSupported;
    }

    public boolean isChargingHistorySupported() {
        return isChargingHistorySupported;
    }

    public void setChargingHistorySupported(boolean isChargingHistorySupported) {
        this.isChargingHistorySupported = isChargingHistorySupported;
    }

    public boolean isChargingHospitalityEnabled() {
        return isChargingHospitalityEnabled;
    }

    public void setChargingHospitalityEnabled(boolean isChargingHospitalityEnabled) {
        this.isChargingHospitalityEnabled = isChargingHospitalityEnabled;
    }

    public boolean isChargingLoudnessEnabled() {
        return isChargingLoudnessEnabled;
    }

    public void setChargingLoudnessEnabled(boolean isChargingLoudnessEnabled) {
        this.isChargingLoudnessEnabled = isChargingLoudnessEnabled;
    }

    public boolean isChargingPlanSupported() {
        return isChargingPlanSupported;
    }

    public void setChargingPlanSupported(boolean isChargingPlanSupported) {
        this.isChargingPlanSupported = isChargingPlanSupported;
    }

    public boolean isChargingPowerLimitEnabled() {
        return isChargingPowerLimitEnabled;
    }

    public void setChargingPowerLimitEnabled(boolean isChargingPowerLimitEnabled) {
        this.isChargingPowerLimitEnabled = isChargingPowerLimitEnabled;
    }

    public boolean isChargingSettingsEnabled() {
        return isChargingSettingsEnabled;
    }

    public void setChargingSettingsEnabled(boolean isChargingSettingsEnabled) {
        this.isChargingSettingsEnabled = isChargingSettingsEnabled;
    }

    public boolean isChargingTargetSocEnabled() {
        return isChargingTargetSocEnabled;
    }

    public void setChargingTargetSocEnabled(boolean isChargingTargetSocEnabled) {
        this.isChargingTargetSocEnabled = isChargingTargetSocEnabled;
    }

    public boolean isCustomerEsimSupported() {
        return isCustomerEsimSupported;
    }

    public void setCustomerEsimSupported(boolean isCustomerEsimSupported) {
        this.isCustomerEsimSupported = isCustomerEsimSupported;
    }

    public boolean isDataPrivacyEnabled() {
        return isDataPrivacyEnabled;
    }

    public void setDataPrivacyEnabled(boolean isDataPrivacyEnabled) {
        this.isDataPrivacyEnabled = isDataPrivacyEnabled;
    }

    public boolean isDCSContractManagementSupported() {
        return isDCSContractManagementSupported;
    }

    public void setDCSContractManagementSupported(boolean isDCSContractManagementSupported) {
        this.isDCSContractManagementSupported = isDCSContractManagementSupported;
    }

    public boolean isEasyChargeEnabled() {
        return isEasyChargeEnabled;
    }

    public void setEasyChargeEnabled(boolean isEasyChargeEnabled) {
        this.isEasyChargeEnabled = isEasyChargeEnabled;
    }

    public boolean isMiniChargingSupported() {
        return isMiniChargingSupported;
    }

    public void setMiniChargingSupported(boolean isMiniChargingSupported) {
        this.isMiniChargingSupported = isMiniChargingSupported;
    }

    public boolean isEvGoChargingSupported() {
        return isEvGoChargingSupported;
    }

    public void setEvGoChargingSupported(boolean isEvGoChargingSupported) {
        this.isEvGoChargingSupported = isEvGoChargingSupported;
    }

    public boolean isRemoteHistoryDeletionSupported() {
        return isRemoteHistoryDeletionSupported;
    }

    public void setRemoteHistoryDeletionSupported(boolean isRemoteHistoryDeletionSupported) {
        this.isRemoteHistoryDeletionSupported = isRemoteHistoryDeletionSupported;
    }

    public boolean isRemoteEngineStartSupported() {
        return isRemoteEngineStartSupported;
    }

    public void setRemoteEngineStartSupported(boolean isRemoteEngineStartSupported) {
        this.isRemoteEngineStartSupported = isRemoteEngineStartSupported;
    }

    public boolean isRemoteServicesActivationRequired() {
        return isRemoteServicesActivationRequired;
    }

    public void setRemoteServicesActivationRequired(boolean isRemoteServicesActivationRequired) {
        this.isRemoteServicesActivationRequired = isRemoteServicesActivationRequired;
    }

    public boolean isRemoteServicesBookingRequired() {
        return isRemoteServicesBookingRequired;
    }

    public void setRemoteServicesBookingRequired(boolean isRemoteServicesBookingRequired) {
        this.isRemoteServicesBookingRequired = isRemoteServicesBookingRequired;
    }

    public boolean isScanAndChargeSupported() {
        return isScanAndChargeSupported;
    }

    public void setScanAndChargeSupported(boolean isScanAndChargeSupported) {
        this.isScanAndChargeSupported = isScanAndChargeSupported;
    }

    public String getLastStateCallState() {
        return lastStateCallState;
    }

    public void setLastStateCallState(String lastStateCallState) {
        this.lastStateCallState = lastStateCallState;
    }

    public boolean isLights() {
        return lights;
    }

    public void setLights(boolean lights) {
        this.lights = lights;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isRemoteSoftwareUpgrade() {
        return remoteSoftwareUpgrade;
    }

    public void setRemoteSoftwareUpgrade(boolean remoteSoftwareUpgrade) {
        this.remoteSoftwareUpgrade = remoteSoftwareUpgrade;
    }

    public boolean isSendPoi() {
        return sendPoi;
    }

    public void setSendPoi(boolean sendPoi) {
        this.sendPoi = sendPoi;
    }

    public boolean isSpeechThirdPartyAlexa() {
        return speechThirdPartyAlexa;
    }

    public void setSpeechThirdPartyAlexa(boolean speechThirdPartyAlexa) {
        this.speechThirdPartyAlexa = speechThirdPartyAlexa;
    }

    public boolean isSpeechThirdPartyAlexaSDK() {
        return speechThirdPartyAlexaSDK;
    }

    public void setSpeechThirdPartyAlexaSDK(boolean speechThirdPartyAlexaSDK) {
        this.speechThirdPartyAlexaSDK = speechThirdPartyAlexaSDK;
    }

    public boolean isUnlock() {
        return unlock;
    }

    public void setUnlock(boolean unlock) {
        this.unlock = unlock;
    }

    public boolean isVehicleFinder() {
        return vehicleFinder;
    }

    public void setVehicleFinder(boolean vehicleFinder) {
        this.vehicleFinder = vehicleFinder;
    }

    public String getVehicleStateSource() {
        return vehicleStateSource;
    }

    public void setVehicleStateSource(String vehicleStateSource) {
        this.vehicleStateSource = vehicleStateSource;
    }

    public boolean isRemoteHistorySupported() {
        return isRemoteHistorySupported;
    }

    public void setRemoteHistorySupported(boolean isRemoteHistorySupported) {
        this.isRemoteHistorySupported = isRemoteHistorySupported;
    }

    public boolean isWifiHotspotServiceSupported() {
        return isWifiHotspotServiceSupported;
    }

    public void setWifiHotspotServiceSupported(boolean isWifiHotspotServiceSupported) {
        this.isWifiHotspotServiceSupported = isWifiHotspotServiceSupported;
    }

    public boolean isNonLscFeatureEnabled() {
        return isNonLscFeatureEnabled;
    }

    public void setNonLscFeatureEnabled(boolean isNonLscFeatureEnabled) {
        this.isNonLscFeatureEnabled = isNonLscFeatureEnabled;
    }

    public boolean isSustainabilitySupported() {
        return isSustainabilitySupported;
    }

    public void setSustainabilitySupported(boolean isSustainabilitySupported) {
        this.isSustainabilitySupported = isSustainabilitySupported;
    }

    public boolean isSustainabilityAccumulatedViewEnabled() {
        return isSustainabilityAccumulatedViewEnabled;
    }

    public void setSustainabilityAccumulatedViewEnabled(boolean isSustainabilityAccumulatedViewEnabled) {
        this.isSustainabilityAccumulatedViewEnabled = isSustainabilityAccumulatedViewEnabled;
    }

    public boolean isCheckSustainabilityDPP() {
        return checkSustainabilityDPP;
    }

    public void setCheckSustainabilityDPP(boolean checkSustainabilityDPP) {
        this.checkSustainabilityDPP = checkSustainabilityDPP;
    }

    public boolean isRemoteParkingSupported() {
        return isRemoteParkingSupported;
    }

    public void setRemoteParkingSupported(boolean isRemoteParkingSupported) {
        this.isRemoteParkingSupported = isRemoteParkingSupported;
    }

    public boolean isClimateTimerWeeklyActive() {
        return isClimateTimerWeeklyActive;
    }

    public void setClimateTimerWeeklyActive(boolean isClimateTimerWeeklyActive) {
        this.isClimateTimerWeeklyActive = isClimateTimerWeeklyActive;
    }

    public DigitalKey getDigitalKey() {
        return digitalKey;
    }

    public void setDigitalKey(DigitalKey digitalKey) {
        this.digitalKey = digitalKey;
    }

    /**
     * @return the remote360
     */
    public boolean isRemote360() {
        return remote360;
    }

    /**
     * @param remote360 the remote360 to set
     */
    public void setRemote360(boolean remote360) {
        this.remote360 = remote360;
    }

    /**
     * @return the remoteChargingCommands
     */
    public RemoteChargingCommands getRemoteChargingCommands() {
        return remoteChargingCommands;
    }

    /**
     * @param remoteChargingCommands the remoteChargingCommands to set
     */
    public void setRemoteChargingCommands(RemoteChargingCommands remoteChargingCommands) {
        this.remoteChargingCommands = remoteChargingCommands;
    }

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
