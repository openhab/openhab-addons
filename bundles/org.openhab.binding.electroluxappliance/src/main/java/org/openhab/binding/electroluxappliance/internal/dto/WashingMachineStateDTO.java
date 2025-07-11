/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.electroluxappliance.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirPurifierStateDTO} class defines the DTO for the Electrolux Washing Machines.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class WashingMachineStateDTO extends ApplianceStateDTO {

    private Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    public static class Properties {
        private Reported reported = new Reported();

        public Reported getReported() {
            return reported;
        }
    }

    public static class Reported {
        private String displayLight = "";
        private String doorState = "";
        private int timeToEnd;
        private Miscellaneous miscellaneous = new Miscellaneous();
        private String applianceUiSwVersion = "";
        private int applianceTotalWorkingTime;
        private String remoteControl = "";
        private String language = "";
        private FCMiscellaneousState fCMiscellaneousState = new FCMiscellaneousState();
        private String cyclePhase = "";
        private String endOfCycleSound = "";
        private int startTime;
        private UserSelections userSelections = new UserSelections();
        private String waterHardness = "";
        private String defaultExtraRinse = "";
        private int totalWashingTime;
        private ApplianceInfo applianceInfo = new ApplianceInfo();
        private String doorLock = "";
        private boolean uiLockMode;
        private int washingNominalLoadWeight;
        private int totalWashCyclesCount;
        private int fcOptisenseLoadWeight;
        private String waterSoftenerMode = "";
        private String applianceState = "";
        private String applianceMode = "";
        private String applianceMainBoardSwVersion = "";
        private int totalCycleCounter;
        private int measuredLoadWeight;
        private Object[] alerts = new Object[0];
        private Maintenance applianceCareAndMaintenance0 = new Maintenance();
        private NetworkInterface networkInterface = new NetworkInterface();
        private Maintenance applianceCareAndMaintenance1 = new Maintenance();
        private Maintenance applianceCareAndMaintenance2 = new Maintenance();
        private Maintenance applianceCareAndMaintenance3 = new Maintenance();
        private AutoDosing autoDosing = new AutoDosing();
        private String cycleSubPhase = "";
        private String connectivityState = "";

        // Getters for all fields
        public String getDisplayLight() {
            return displayLight;
        }

        public String getDoorState() {
            return doorState;
        }

        public int getTimeToEnd() {
            return timeToEnd;
        }

        public Miscellaneous getMiscellaneous() {
            return miscellaneous;
        }

        public String getApplianceUiSwVersion() {
            return applianceUiSwVersion;
        }

        public int getApplianceTotalWorkingTime() {
            return applianceTotalWorkingTime;
        }

        public String getRemoteControl() {
            return remoteControl;
        }

        public String getLanguage() {
            return language;
        }

        public FCMiscellaneousState getFCMiscellaneousState() {
            return fCMiscellaneousState;
        }

        public String getCyclePhase() {
            return cyclePhase;
        }

        public String getEndOfCycleSound() {
            return endOfCycleSound;
        }

        public int getStartTime() {
            return startTime;
        }

        public UserSelections getUserSelections() {
            return userSelections;
        }

        public String getWaterHardness() {
            return waterHardness;
        }

        public String getDefaultExtraRinse() {
            return defaultExtraRinse;
        }

        public int getTotalWashingTime() {
            return totalWashingTime;
        }

        public ApplianceInfo getApplianceInfo() {
            return applianceInfo;
        }

        public String getDoorLock() {
            return doorLock;
        }

        public boolean isUiLockMode() {
            return uiLockMode;
        }

        public int getWashingNominalLoadWeight() {
            return washingNominalLoadWeight;
        }

        public int getTotalWashCyclesCount() {
            return totalWashCyclesCount;
        }

        public int getFcOptisenseLoadWeight() {
            return fcOptisenseLoadWeight;
        }

        public String getWaterSoftenerMode() {
            return waterSoftenerMode;
        }

        public String getApplianceState() {
            return applianceState;
        }

        public String getApplianceMode() {
            return applianceMode;
        }

        public String getApplianceMainBoardSwVersion() {
            return applianceMainBoardSwVersion;
        }

        public int getTotalCycleCounter() {
            return totalCycleCounter;
        }

        public int getMeasuredLoadWeight() {
            return measuredLoadWeight;
        }

        public Object[] getAlerts() {
            return alerts;
        }

        public Maintenance getApplianceCareAndMaintenance0() {
            return applianceCareAndMaintenance0;
        }

        public NetworkInterface getNetworkInterface() {
            return networkInterface;
        }

        public Maintenance getApplianceCareAndMaintenance1() {
            return applianceCareAndMaintenance1;
        }

        public Maintenance getApplianceCareAndMaintenance2() {
            return applianceCareAndMaintenance2;
        }

        public Maintenance getApplianceCareAndMaintenance3() {
            return applianceCareAndMaintenance3;
        }

        public AutoDosing getAutoDosing() {
            return autoDosing;
        }

        public String getCycleSubPhase() {
            return cycleSubPhase;
        }

        public String getConnectivityState() {
            return connectivityState;
        }
    }

    public static class Miscellaneous {
        private boolean defaultSoftPlus;

        public boolean isDefaultSoftPlus() {
            return defaultSoftPlus;
        }
    }

    public static class FCMiscellaneousState {
        private int optisenseResult;
        private int detergentExtradosage;
        private boolean tankAReserve;
        private boolean tankBReserve;
        private int softenerExtradosage;
        private int waterUsage;
        private int tankADetLoadForNominalWeight;
        private int tankBDetLoadForNominalWeight;

        public int getOptisenseResult() {
            return optisenseResult;
        }

        public int getDetergentExtradosage() {
            return detergentExtradosage;
        }

        public boolean isTankAReserve() {
            return tankAReserve;
        }

        public boolean isTankBReserve() {
            return tankBReserve;
        }

        public int getSoftenerExtradosage() {
            return softenerExtradosage;
        }

        public int getWaterUsage() {
            return waterUsage;
        }

        public int getTankADetLoadForNominalWeight() {
            return tankADetLoadForNominalWeight;
        }

        public int getTankBDetLoadForNominalWeight() {
            return tankBDetLoadForNominalWeight;
        }
    }

    public static class UserSelections {
        private boolean ewx1493aUltraMix;
        private boolean ewx1493aStain;
        private String adTankBSel = "";
        private String adFineTuneSoftLevel = "";
        private boolean ewx1493aWetMode;
        private String analogSpinSpeed = "";
        private boolean ewx1493aEasyIron;
        private boolean ewx1493aRinseHold;
        private boolean ewx1493aWmEconomy;
        private boolean ewx1493aTcSensor;
        private String programUID = "";
        private boolean ewx1493aAnticreaseNoSteam;
        private boolean ewx1493aAnticreaseWSteam;
        private String adTankASel = "";
        private String timeManagerLevel = "";
        private boolean ewx1493aDryMode;
        private boolean ewx1493aPreWashPhase;
        private String extraRinseNumber = "";
        private String adFineTuneDetLevel = "";
        private boolean ewx1493aSteamMode;
        private String analogTemperature = "";
        private boolean ewx1493aNightCycle;
        private String steamValue = "";
        private boolean ewx1493aIntensive;
        private boolean ewx1493aPod;

        public boolean isEwx1493aUltraMix() {
            return ewx1493aUltraMix;
        }

        public boolean isEwx1493aStain() {
            return ewx1493aStain;
        }

        public String getAdTankBSel() {
            return adTankBSel;
        }

        public String getAdFineTuneSoftLevel() {
            return adFineTuneSoftLevel;
        }

        public boolean isEwx1493aWetMode() {
            return ewx1493aWetMode;
        }

        public String getAnalogSpinSpeed() {
            return analogSpinSpeed;
        }

        public boolean isEwx1493aEasyIron() {
            return ewx1493aEasyIron;
        }

        public boolean isEwx1493aRinseHold() {
            return ewx1493aRinseHold;
        }

        public boolean isEwx1493aWmEconomy() {
            return ewx1493aWmEconomy;
        }

        public boolean isEwx1493aTcSensor() {
            return ewx1493aTcSensor;
        }

        public String getProgramUID() {
            return programUID;
        }

        public boolean isEwx1493aAnticreaseNoSteam() {
            return ewx1493aAnticreaseNoSteam;
        }

        public boolean isEwx1493aAnticreaseWSteam() {
            return ewx1493aAnticreaseWSteam;
        }

        public String getAdTankASel() {
            return adTankASel;
        }

        public String getTimeManagerLevel() {
            return timeManagerLevel;
        }

        public boolean isEwx1493aDryMode() {
            return ewx1493aDryMode;
        }

        public boolean isEwx1493aPreWashPhase() {
            return ewx1493aPreWashPhase;
        }

        public String getExtraRinseNumber() {
            return extraRinseNumber;
        }

        public String getAdFineTuneDetLevel() {
            return adFineTuneDetLevel;
        }

        public boolean isEwx1493aSteamMode() {
            return ewx1493aSteamMode;
        }

        public String getAnalogTemperature() {
            return analogTemperature;
        }

        public boolean isEwx1493aNightCycle() {
            return ewx1493aNightCycle;
        }

        public String getSteamValue() {
            return steamValue;
        }

        public boolean isEwx1493aIntensive() {
            return ewx1493aIntensive;
        }

        public boolean isEwx1493aPod() {
            return ewx1493aPod;
        }
    }

    public static class ApplianceInfo {
        private String applianceType = "";

        public String getApplianceType() {
            return applianceType;
        }
    }

    public static class Maintenance {
        private CareThreshold careThreshold = new CareThreshold();

        public CareThreshold getCareThreshold() {
            return careThreshold;
        }

        public static class CareThreshold {
            private boolean occurred;
            private int threshold;

            public boolean isOccurred() {
                return occurred;
            }

            public int getThreshold() {
                return threshold;
            }
        }
    }

    public static class NetworkInterface {
        private String swVersion = "";
        private String linkQualityIndicator = "";
        private String otaState = "";
        private String niuSwUpdateCurrentDescription = "";
        private String swAncAndRevision = "";

        public String getSwVersion() {
            return swVersion;
        }

        public String getLinkQualityIndicator() {
            return linkQualityIndicator;
        }

        public String getOtaState() {
            return otaState;
        }

        public String getNiuSwUpdateCurrentDescription() {
            return niuSwUpdateCurrentDescription;
        }

        public String getSwAncAndRevision() {
            return swAncAndRevision;
        }
    }

    public static class AutoDosing {
        private int adTankBDetStandardDose;
        private boolean adLocalFineTuning;
        private String adTankAConfiguration = "";
        private int adTankBSoftStandardDose;
        private String adTankBConfiguration = "";
        private int adTankADetStandardDose;

        public int getAdTankBDetStandardDose() {
            return adTankBDetStandardDose;
        }

        public boolean isAdLocalFineTuning() {
            return adLocalFineTuning;
        }

        public String getAdTankAConfiguration() {
            return adTankAConfiguration;
        }

        public int getAdTankBSoftStandardDose() {
            return adTankBSoftStandardDose;
        }

        public String getAdTankBConfiguration() {
            return adTankBConfiguration;
        }

        public int getAdTankADetStandardDose() {
            return adTankADetStandardDose;
        }
    }
}
