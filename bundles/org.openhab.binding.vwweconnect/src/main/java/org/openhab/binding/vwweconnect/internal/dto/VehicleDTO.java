/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.dto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Vehicle representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VehicleDTO extends BaseVehicleDTO {

    private String errorCode = "";
    private CompleteVehicleJsonDTO completeVehicleJson = new CompleteVehicleJsonDTO();
    private DetailsDTO vehicleDetails = new DetailsDTO();
    private StatusDTO vehicleStatus = new StatusDTO();
    private TripsDTO trips = new TripsDTO();
    private LocationDTO vehicleLocation = new LocationDTO();
    private HeaterStatusDTO heaterStatus = new HeaterStatusDTO();
    private EManagerDTO eManager = new EManagerDTO();

    public String getErrorCode() {
        return errorCode;
    }

    public CompleteVehicleJsonDTO getCompleteVehicleJson() {
        return completeVehicleJson;
    }

    public DetailsDTO getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(DetailsDTO vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public StatusDTO getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(StatusDTO vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public HeaterStatusDTO getHeaterStatus() {
        return heaterStatus;
    }

    public void setHeaterStatus(HeaterStatusDTO heaterStatus) {
        this.heaterStatus = heaterStatus;
    }

    public TripsDTO getTrips() {
        return trips;
    }

    public void setTrips(TripsDTO trips) {
        this.trips = trips;
    }

    public LocationDTO getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(LocationDTO vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public EManagerDTO getEManager() {
        return eManager;
    }

    public void setEManager(EManagerDTO eManager) {
        this.eManager = eManager;
    }

    @Override
    public String toString() {
        return "VehicleDTO [errorCode=" + errorCode + ", completeVehicleJson=" + completeVehicleJson
                + ", vehicleDetails=" + vehicleDetails + ", vehicleStatus=" + vehicleStatus + ", trips=" + trips
                + ", vehicleLocation=" + vehicleLocation + ", heaterStatus=" + heaterStatus + ", eManager=" + eManager
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + completeVehicleJson.hashCode();
        result = prime * result + eManager.hashCode();
        result = prime * result + errorCode.hashCode();
        result = prime * result + heaterStatus.hashCode();
        result = prime * result + trips.hashCode();
        result = prime * result + vehicleDetails.hashCode();
        result = prime * result + vehicleLocation.hashCode();
        result = prime * result + vehicleStatus.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VehicleDTO other = (VehicleDTO) obj;
        if (!completeVehicleJson.equals(other.completeVehicleJson)) {
            return false;
        }
        if (!eManager.equals(other.eManager)) {
            return false;
        }
        if (!errorCode.equals(other.errorCode)) {
            return false;
        }
        if (!heaterStatus.equals(other.heaterStatus)) {
            return false;
        }
        if (!trips.equals(other.trips)) {
            return false;
        }
        if (!vehicleDetails.equals(other.vehicleDetails)) {
            return false;
        }
        if (!vehicleLocation.equals(other.vehicleLocation)) {
            return false;
        }
        if (!vehicleStatus.equals(other.vehicleStatus)) {
            return false;
        }
        return true;
    }

    public class CompleteVehicleJsonDTO {
        private String vin = "";
        private String name = "";
        private boolean expired;
        private String model = "";
        private String modelCode = "";
        private String modelYear = "";
        private String imageUrl = "";
        private @Nullable Object vehicleSpecificFallbackImageUrl = new Object();
        private @Nullable Object modelSpecificFallbackImageUrl = new Object();
        private String defaultImageUrl = "";
        private String vehicleBrand = "";
        private String enrollmentDate = "";
        private boolean deviceOCU1;
        private boolean deviceOCU2;
        private boolean deviceMIB;
        private boolean engineTypeCombustian;
        private boolean engineTypeHybridOCU1;
        private boolean engineTypeHybridOCU2;
        private boolean engineTypeElectric;
        private boolean engineTypeCNG;
        private boolean engineTypeDefault;
        private String stpStatus = "";
        private boolean windowstateSupported;
        private String dashboardUrl = "";
        private boolean vhrRequested;
        private boolean vsrRequested;
        private boolean vhrConfigAvailable;
        private boolean verifiedByDealer;
        private boolean vhr2;
        private boolean roleEnabled;
        private boolean isEL2Vehicle;
        private boolean workshopMode;
        private boolean hiddenUserProfiles;
        private @Nullable Object mobileKeyActivated = new Object();
        private String enrollmentType = "";
        private boolean ocu3Low;
        private @Nullable List<PackageServiceDTO> packageServices = new ArrayList<>();
        private boolean fullyEnrolled;
        private boolean secondaryUser;
        private boolean fleet;
        private boolean touareg;
        private boolean iceSupported;
        private boolean flightMode;
        private boolean esimCompatible;
        private boolean dkyenabled;
        private @Nullable Object smartCardKeyActivated = new Object();
        private boolean selected;
        private boolean defaultCar;
        private boolean vwConnectPowerLayerAvailable;

        public String getVin() {
            return vin;
        }

        public String getName() {
            return name;
        }

        public boolean getExpired() {
            return expired;
        }

        public String getModel() {
            return model;
        }

        public String getModelCode() {
            return modelCode;
        }

        public String getModelYear() {
            return modelYear;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public @Nullable Object getVehicleSpecificFallbackImageUrl() {
            return vehicleSpecificFallbackImageUrl;
        }

        public @Nullable Object getModelSpecificFallbackImageUrl() {
            return modelSpecificFallbackImageUrl;
        }

        public String getDefaultImageUrl() {
            return defaultImageUrl;
        }

        public String getVehicleBrand() {
            return vehicleBrand;
        }

        public @Nullable ZonedDateTime getEnrollmentStartDate() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault());
            LocalDate date = LocalDate.parse(enrollmentDate, formatter);
            ZonedDateTime zdt = date.atStartOfDay(ZoneId.systemDefault());

            return zdt;
        }

        public String getEnrollmentDate() {
            return enrollmentDate;
        }

        public boolean getDeviceOCU1() {
            return deviceOCU1;
        }

        public boolean getDeviceOCU2() {
            return deviceOCU2;
        }

        public boolean getDeviceMIB() {
            return deviceMIB;
        }

        public boolean getEngineTypeCombustian() {
            return engineTypeCombustian;
        }

        public boolean getEngineTypeHybridOCU1() {
            return engineTypeHybridOCU1;
        }

        public boolean getEngineTypeHybridOCU2() {
            return engineTypeHybridOCU2;
        }

        public boolean getEngineTypeElectric() {
            return engineTypeElectric;
        }

        public boolean getEngineTypeCNG() {
            return engineTypeCNG;
        }

        public boolean getEngineTypeDefault() {
            return engineTypeDefault;
        }

        public String getStpStatus() {
            return stpStatus;
        }

        public boolean getWindowstateSupported() {
            return windowstateSupported;
        }

        public String getDashboardUrl() {
            return dashboardUrl;
        }

        public boolean getVhrRequested() {
            return vhrRequested;
        }

        public boolean getVsrRequested() {
            return vsrRequested;
        }

        public boolean getVhrConfigAvailable() {
            return vhrConfigAvailable;
        }

        public boolean getVerifiedByDealer() {
            return verifiedByDealer;
        }

        public boolean getVhr2() {
            return vhr2;
        }

        public boolean getRoleEnabled() {
            return roleEnabled;
        }

        public boolean getIsEL2Vehicle() {
            return isEL2Vehicle;
        }

        public boolean getWorkshopMode() {
            return workshopMode;
        }

        public boolean getHiddenUserProfiles() {
            return hiddenUserProfiles;
        }

        public @Nullable Object getMobileKeyActivated() {
            return mobileKeyActivated;
        }

        public String getEnrollmentType() {
            return enrollmentType;
        }

        public boolean getOcu3Low() {
            return ocu3Low;
        }

        public @Nullable List<PackageServiceDTO> getPackageServices() {
            return packageServices;
        }

        public boolean getFullyEnrolled() {
            return fullyEnrolled;
        }

        public boolean getSecondaryUser() {
            return secondaryUser;
        }

        public boolean getFleet() {
            return fleet;
        }

        public boolean getTouareg() {
            return touareg;
        }

        public boolean getIceSupported() {
            return iceSupported;
        }

        public boolean getFlightMode() {
            return flightMode;
        }

        public boolean getEsimCompatible() {
            return esimCompatible;
        }

        public boolean getDkyenabled() {
            return dkyenabled;
        }

        public @Nullable Object getSmartCardKeyActivated() {
            return smartCardKeyActivated;
        }

        public boolean getSelected() {
            return selected;
        }

        public boolean getDefaultCar() {
            return defaultCar;
        }

        public boolean getVwConnectPowerLayerAvailable() {
            return vwConnectPowerLayerAvailable;
        }

        @Override
        public String toString() {
            return "CompleteVehicleJsonDTO [vin=" + vin + ", name=" + name + ", expired=" + expired + ", model=" + model
                    + ", modelCode=" + modelCode + ", modelYear=" + modelYear + ", imageUrl=" + imageUrl
                    + ", vehicleSpecificFallbackImageUrl=" + vehicleSpecificFallbackImageUrl
                    + ", modelSpecificFallbackImageUrl=" + modelSpecificFallbackImageUrl + ", defaultImageUrl="
                    + defaultImageUrl + ", vehicleBrand=" + vehicleBrand + ", enrollmentDate=" + enrollmentDate
                    + ", deviceOCU1=" + deviceOCU1 + ", deviceOCU2=" + deviceOCU2 + ", deviceMIB=" + deviceMIB
                    + ", engineTypeCombustian=" + engineTypeCombustian + ", engineTypeHybridOCU1="
                    + engineTypeHybridOCU1 + ", engineTypeHybridOCU2=" + engineTypeHybridOCU2 + ", engineTypeElectric="
                    + engineTypeElectric + ", engineTypeCNG=" + engineTypeCNG + ", engineTypeDefault="
                    + engineTypeDefault + ", stpStatus=" + stpStatus + ", windowstateSupported=" + windowstateSupported
                    + ", dashboardUrl=" + dashboardUrl + ", vhrRequested=" + vhrRequested + ", vsrRequested="
                    + vsrRequested + ", vhrConfigAvailable=" + vhrConfigAvailable + ", verifiedByDealer="
                    + verifiedByDealer + ", vhr2=" + vhr2 + ", roleEnabled=" + roleEnabled + ", isEL2Vehicle="
                    + isEL2Vehicle + ", workshopMode=" + workshopMode + ", hiddenUserProfiles=" + hiddenUserProfiles
                    + ", mobileKeyActivated=" + mobileKeyActivated + ", enrollmentType=" + enrollmentType + ", ocu3Low="
                    + ocu3Low + ", packageServices=" + packageServices + ", fullyEnrolled=" + fullyEnrolled
                    + ", secondaryUser=" + secondaryUser + ", fleet=" + fleet + ", touareg=" + touareg
                    + ", iceSupported=" + iceSupported + ", flightMode=" + flightMode + ", esimCompatible="
                    + esimCompatible + ", dkyenabled=" + dkyenabled + ", smartCardKeyActivated=" + smartCardKeyActivated
                    + ", selected=" + selected + ", defaultCar=" + defaultCar + ", vwConnectPowerLayerAvailable="
                    + vwConnectPowerLayerAvailable + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + dashboardUrl.hashCode();
            result = prime * result + (defaultCar ? 1231 : 1237);
            result = prime * result + defaultImageUrl.hashCode();
            result = prime * result + (deviceMIB ? 1231 : 1237);
            result = prime * result + (deviceOCU1 ? 1231 : 1237);
            result = prime * result + (deviceOCU2 ? 1231 : 1237);
            result = prime * result + (dkyenabled ? 1231 : 1237);
            result = prime * result + (engineTypeCNG ? 1231 : 1237);
            result = prime * result + (engineTypeCombustian ? 1231 : 1237);
            result = prime * result + (engineTypeDefault ? 1231 : 1237);
            result = prime * result + (engineTypeElectric ? 1231 : 1237);
            result = prime * result + (engineTypeHybridOCU1 ? 1231 : 1237);
            result = prime * result + (engineTypeHybridOCU2 ? 1231 : 1237);
            result = prime * result + enrollmentDate.hashCode();
            result = prime * result + enrollmentType.hashCode();
            result = prime * result + (esimCompatible ? 1231 : 1237);
            result = prime * result + (expired ? 1231 : 1237);
            result = prime * result + (fleet ? 1231 : 1237);
            result = prime * result + (flightMode ? 1231 : 1237);
            result = prime * result + (fullyEnrolled ? 1231 : 1237);
            result = prime * result + (hiddenUserProfiles ? 1231 : 1237);
            result = prime * result + (iceSupported ? 1231 : 1237);
            result = prime * result + imageUrl.hashCode();
            result = prime * result + (isEL2Vehicle ? 1231 : 1237);
            Object mobileKeyActivated2 = mobileKeyActivated;
            result = prime * result + ((mobileKeyActivated2 == null) ? 0 : mobileKeyActivated2.hashCode());
            result = prime * result + model.hashCode();
            result = prime * result + modelCode.hashCode();
            Object modelSpecificFallbackImageUrl2 = modelSpecificFallbackImageUrl;
            result = prime * result
                    + ((modelSpecificFallbackImageUrl2 == null) ? 0 : modelSpecificFallbackImageUrl2.hashCode());
            result = prime * result + modelYear.hashCode();
            result = prime * result + name.hashCode();
            result = prime * result + (ocu3Low ? 1231 : 1237);
            List<PackageServiceDTO> packageServices2 = packageServices;
            result = prime * result + ((packageServices2 == null) ? 0 : packageServices2.hashCode());
            result = prime * result + (roleEnabled ? 1231 : 1237);
            result = prime * result + (secondaryUser ? 1231 : 1237);
            result = prime * result + (selected ? 1231 : 1237);
            Object smartCardKeyActivated2 = smartCardKeyActivated;
            result = prime * result + ((smartCardKeyActivated2 == null) ? 0 : smartCardKeyActivated2.hashCode());
            result = prime * result + stpStatus.hashCode();
            result = prime * result + (touareg ? 1231 : 1237);
            result = prime * result + vehicleBrand.hashCode();
            Object vehicleSpecificFallbackImageUrl2 = vehicleSpecificFallbackImageUrl;
            result = prime * result
                    + ((vehicleSpecificFallbackImageUrl2 == null) ? 0 : vehicleSpecificFallbackImageUrl2.hashCode());
            result = prime * result + (verifiedByDealer ? 1231 : 1237);
            result = prime * result + (vhr2 ? 1231 : 1237);
            result = prime * result + (vhrConfigAvailable ? 1231 : 1237);
            result = prime * result + (vhrRequested ? 1231 : 1237);
            result = prime * result + vin.hashCode();
            result = prime * result + (vsrRequested ? 1231 : 1237);
            result = prime * result + (vwConnectPowerLayerAvailable ? 1231 : 1237);
            result = prime * result + (windowstateSupported ? 1231 : 1237);
            result = prime * result + (workshopMode ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CompleteVehicleJsonDTO other = (CompleteVehicleJsonDTO) obj;
            if (!dashboardUrl.equals(other.dashboardUrl)) {
                return false;
            }
            if (defaultCar != other.defaultCar) {
                return false;
            }
            if (!defaultImageUrl.equals(other.defaultImageUrl)) {
                return false;
            }
            if (deviceMIB != other.deviceMIB) {
                return false;
            }
            if (deviceOCU1 != other.deviceOCU1) {
                return false;
            }
            if (deviceOCU2 != other.deviceOCU2) {
                return false;
            }
            if (dkyenabled != other.dkyenabled) {
                return false;
            }
            if (engineTypeCNG != other.engineTypeCNG) {
                return false;
            }
            if (engineTypeCombustian != other.engineTypeCombustian) {
                return false;
            }
            if (engineTypeDefault != other.engineTypeDefault) {
                return false;
            }
            if (engineTypeElectric != other.engineTypeElectric) {
                return false;
            }
            if (engineTypeHybridOCU1 != other.engineTypeHybridOCU1) {
                return false;
            }
            if (engineTypeHybridOCU2 != other.engineTypeHybridOCU2) {
                return false;
            }
            if (!enrollmentDate.equals(other.enrollmentDate)) {
                return false;
            }
            if (!enrollmentType.equals(other.enrollmentType)) {
                return false;
            }
            if (esimCompatible != other.esimCompatible) {
                return false;
            }
            if (expired != other.expired) {
                return false;
            }
            if (fleet != other.fleet) {
                return false;
            }
            if (flightMode != other.flightMode) {
                return false;
            }
            if (fullyEnrolled != other.fullyEnrolled) {
                return false;
            }
            if (hiddenUserProfiles != other.hiddenUserProfiles) {
                return false;
            }
            if (iceSupported != other.iceSupported) {
                return false;
            }
            if (!imageUrl.equals(other.imageUrl)) {
                return false;
            }
            if (isEL2Vehicle != other.isEL2Vehicle) {
                return false;
            }
            Object mobileKeyActivated2 = mobileKeyActivated;
            if (mobileKeyActivated2 == null) {
                if (other.mobileKeyActivated != null) {
                    return false;
                }
            } else if (!mobileKeyActivated2.equals(other.mobileKeyActivated)) {
                return false;
            }
            if (!model.equals(other.model)) {
                return false;
            }
            if (!modelCode.equals(other.modelCode)) {
                return false;
            }
            Object modelSpecificFallbackImageUrl2 = modelSpecificFallbackImageUrl;
            if (modelSpecificFallbackImageUrl2 == null) {
                if (other.modelSpecificFallbackImageUrl != null) {
                    return false;
                }
            } else if (!modelSpecificFallbackImageUrl2.equals(other.modelSpecificFallbackImageUrl)) {
                return false;
            }
            if (!modelYear.equals(other.modelYear)) {
                return false;
            }
            if (!name.equals(other.name)) {
                return false;
            }
            if (ocu3Low != other.ocu3Low) {
                return false;
            }
            List<PackageServiceDTO> packageServices2 = packageServices;
            if (packageServices2 == null) {
                if (other.packageServices != null) {
                    return false;
                }
            } else if (!packageServices2.equals(other.packageServices)) {
                return false;
            }
            if (roleEnabled != other.roleEnabled) {
                return false;
            }
            if (secondaryUser != other.secondaryUser) {
                return false;
            }
            if (selected != other.selected) {
                return false;
            }
            Object smartCardKeyActivated2 = smartCardKeyActivated;
            if (smartCardKeyActivated2 == null) {
                if (other.smartCardKeyActivated != null) {
                    return false;
                }
            } else if (!smartCardKeyActivated2.equals(other.smartCardKeyActivated)) {
                return false;
            }
            if (!stpStatus.equals(other.stpStatus)) {
                return false;
            }
            if (touareg != other.touareg) {
                return false;
            }
            if (!vehicleBrand.equals(other.vehicleBrand)) {
                return false;
            }
            Object vehicleSpecificFallbackImageUrl2 = vehicleSpecificFallbackImageUrl;
            if (vehicleSpecificFallbackImageUrl2 == null) {
                if (other.vehicleSpecificFallbackImageUrl != null) {
                    return false;
                }
            } else if (!vehicleSpecificFallbackImageUrl2.equals(other.vehicleSpecificFallbackImageUrl)) {
                return false;
            }
            if (verifiedByDealer != other.verifiedByDealer) {
                return false;
            }
            if (vhr2 != other.vhr2) {
                return false;
            }
            if (vhrConfigAvailable != other.vhrConfigAvailable) {
                return false;
            }
            if (vhrRequested != other.vhrRequested) {
                return false;
            }
            if (!vin.equals(other.vin)) {
                return false;
            }
            if (vsrRequested != other.vsrRequested) {
                return false;
            }
            if (vwConnectPowerLayerAvailable != other.vwConnectPowerLayerAvailable) {
                return false;
            }
            if (windowstateSupported != other.windowstateSupported) {
                return false;
            }
            if (workshopMode != other.workshopMode) {
                return false;
            }
            return true;
        }

        private VehicleDTO getEnclosingInstance() {
            return VehicleDTO.this;
        }
    }

    public class PackageServiceDTO {

        private String packageServiceId = "";
        private String propertyKeyReference = "";
        private String packageServiceName = "";
        private String trackingName = "";
        private String activationDate = "";
        private String expirationDate = "";
        private boolean expired;
        private boolean expireInAMonth;
        private @Nullable String packageType = "";
        private String enrollmentPackageType = "";

        public String getPackageServiceId() {
            return packageServiceId;
        }

        public String getPropertyKeyReference() {
            return propertyKeyReference;
        }

        public String getPackageServiceName() {
            return packageServiceName;
        }

        public String getTrackingName() {
            return trackingName;
        }

        public String getActivationDate() {
            return activationDate;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public boolean getExpired() {
            return expired;
        }

        public boolean getExpireInAMonth() {
            return expireInAMonth;
        }

        public @Nullable String getPackageType() {
            return packageType;
        }

        public String getEnrollmentPackageType() {
            return enrollmentPackageType;
        }

        @Override
        public String toString() {
            return "PackageServiceDTO [packageServiceId=" + packageServiceId + ", propertyKeyReference="
                    + propertyKeyReference + ", packageServiceName=" + packageServiceName + ", trackingName="
                    + trackingName + ", activationDate=" + activationDate + ", expirationDate=" + expirationDate
                    + ", expired=" + expired + ", expireInAMonth=" + expireInAMonth + ", packageType=" + packageType
                    + ", enrollmentPackageType=" + enrollmentPackageType + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + activationDate.hashCode();
            result = prime * result + enrollmentPackageType.hashCode();
            result = prime * result + expirationDate.hashCode();
            result = prime * result + (expireInAMonth ? 1231 : 1237);
            result = prime * result + (expired ? 1231 : 1237);
            result = prime * result + packageServiceId.hashCode();
            result = prime * result + packageServiceName.hashCode();
            String packageType2 = packageType;
            result = prime * result + ((packageType2 == null) ? 0 : packageType2.hashCode());
            result = prime * result + propertyKeyReference.hashCode();
            result = prime * result + trackingName.hashCode();
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PackageServiceDTO other = (PackageServiceDTO) obj;
            if (!activationDate.equals(other.activationDate)) {
                return false;
            }
            if (!enrollmentPackageType.equals(other.enrollmentPackageType)) {
                return false;
            }
            if (!expirationDate.equals(other.expirationDate)) {
                return false;
            }
            if (expireInAMonth != other.expireInAMonth) {
                return false;
            }
            if (expired != other.expired) {
                return false;
            }
            if (!packageServiceId.equals(other.packageServiceId)) {
                return false;
            }
            if (!packageServiceName.equals(other.packageServiceName)) {
                return false;
            }
            if (packageType != null && !packageType.equals(other.packageType)) {
                return false;
            }
            if (!propertyKeyReference.equals(other.propertyKeyReference)) {
                return false;
            }
            if (!trackingName.equals(other.trackingName)) {
                return false;
            }
            return true;
        }

        private VehicleDTO getEnclosingInstance() {
            return VehicleDTO.this;
        }
    }

    public class VehicleDetailsDTO {

        private List<String> lastConnectionTimeStamp = new ArrayList<>();
        private double distanceCovered = BaseVehicleDTO.UNDEFINED;
        private double range = BaseVehicleDTO.UNDEFINED;
        private String serviceInspectionData = "";
        private String oilInspectionData = "";
        private boolean showOil;
        private boolean showService;
        private boolean flightMode;

        public List<String> getLastConnectionTimeStamp() {
            return lastConnectionTimeStamp;
        }

        public double getDistanceCovered() {
            return distanceCovered;
        }

        public double getRange() {
            return range;
        }

        public String getServiceInspectionData() {
            return serviceInspectionData;
        }

        public String getOilInspectionData() {
            return oilInspectionData;
        }

        public boolean getShowOil() {
            return showOil;
        }

        public boolean getShowService() {
            return showService;
        }

        public boolean getFlightMode() {
            return flightMode;
        }

        @Override
        public String toString() {
            return "VehicleDetailsDTO [lastConnectionTimeStamp=" + lastConnectionTimeStamp + ", distanceCovered="
                    + distanceCovered + ", range=" + range + ", serviceInspectionData=" + serviceInspectionData
                    + ", oilInspectionData=" + oilInspectionData + ", showOil=" + showOil + ", showService="
                    + showService + ", flightMode=" + flightMode + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            long temp;
            temp = Double.doubleToLongBits(distanceCovered);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + (flightMode ? 1231 : 1237);
            result = prime * result + lastConnectionTimeStamp.hashCode();
            result = prime * result + oilInspectionData.hashCode();
            temp = Double.doubleToLongBits(range);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + serviceInspectionData.hashCode();
            result = prime * result + (showOil ? 1231 : 1237);
            result = prime * result + (showService ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            VehicleDetailsDTO other = (VehicleDetailsDTO) obj;
            if (Double.doubleToLongBits(distanceCovered) != Double.doubleToLongBits(other.distanceCovered)) {
                return false;
            }
            if (flightMode != other.flightMode) {
                return false;
            }
            if (!lastConnectionTimeStamp.equals(other.lastConnectionTimeStamp)) {
                return false;
            }
            if (!oilInspectionData.equals(other.oilInspectionData)) {
                return false;
            }
            if (Double.doubleToLongBits(range) != Double.doubleToLongBits(other.range)) {
                return false;
            }
            if (!serviceInspectionData.equals(other.serviceInspectionData)) {
                return false;
            }
            if (showOil != other.showOil) {
                return false;
            }
            if (showService != other.showService) {
                return false;
            }
            return true;
        }

        private VehicleDTO getEnclosingInstance() {
            return VehicleDTO.this;
        }
    }
}
