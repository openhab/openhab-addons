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
package org.openhab.binding.vwweconnect.internal.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Vehicle representation.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class Vehicle extends BaseVehicle {

    private @Nullable String errorCode;
    private CompleteVehicleJson completeVehicleJson = new CompleteVehicleJson();
    private Details vehicleDetails = new Details();
    private Status vehicleStatus = new Status();
    private Trips trips = new Trips();
    private Location vehicleLocation = new Location();
    private HeaterStatus heaterStatus = new HeaterStatus();
    private EManager eManager = new EManager();

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public CompleteVehicleJson getCompleteVehicleJson() {
        return completeVehicleJson;
    }

    public Details getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(Details vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public Status getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(Status vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public HeaterStatus getHeaterStatus() {
        return heaterStatus;
    }

    public void setHeaterStatus(HeaterStatus heaterStatus) {
        this.heaterStatus = heaterStatus;
    }

    public Trips getTrips() {
        return trips;
    }

    public void setTrips(Trips trips) {
        this.trips = trips;
    }

    public Location getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(Location vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public EManager getEManager() {
        return eManager;
    }

    public void setEManager(EManager eManager) {
        this.eManager = eManager;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode)
                .append("completeVehicleJson", completeVehicleJson).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorCode).append(completeVehicleJson).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Vehicle)) {
            return false;
        }
        Vehicle rhs = ((Vehicle) other);
        return new EqualsBuilder().append(errorCode, rhs.errorCode).append(completeVehicleJson, rhs.completeVehicleJson)
                .append(vehicleDetails, rhs.vehicleDetails).append(vehicleStatus, rhs.vehicleStatus)
                .append(vehicleLocation, rhs.vehicleLocation).isEquals();
    }

    public class CompleteVehicleJson {

        private @Nullable String vin;
        private @Nullable String name;
        private boolean expired;
        private @Nullable String model;
        private @Nullable String modelCode;
        private @Nullable String modelYear;
        private @Nullable String imageUrl;
        private Object vehicleSpecificFallbackImageUrl = new Object();
        private Object modelSpecificFallbackImageUrl = new Object();
        private @Nullable String defaultImageUrl;
        private @Nullable String vehicleBrand;
        private @Nullable String enrollmentDate;
        private boolean deviceOCU1;
        private boolean deviceOCU2;
        private boolean deviceMIB;
        private boolean engineTypeCombustian;
        private boolean engineTypeHybridOCU1;
        private boolean engineTypeHybridOCU2;
        private boolean engineTypeElectric;
        private boolean engineTypeCNG;
        private boolean engineTypeDefault;
        private @Nullable String stpStatus;
        private boolean windowstateSupported;
        private @Nullable String dashboardUrl;
        private boolean vhrRequested;
        private boolean vsrRequested;
        private boolean vhrConfigAvailable;
        private boolean verifiedByDealer;
        private boolean vhr2;
        private boolean roleEnabled;
        private boolean isEL2Vehicle;
        private boolean workshopMode;
        private boolean hiddenUserProfiles;
        private Object mobileKeyActivated = new Object();
        private @Nullable String enrollmentType;
        private boolean ocu3Low;
        private List<PackageService> packageServices = new ArrayList<>();
        private boolean fullyEnrolled;
        private boolean secondaryUser;
        private boolean fleet;
        private boolean touareg;
        private boolean iceSupported;
        private boolean flightMode;
        private boolean esimCompatible;
        private boolean dkyenabled;
        private Object smartCardKeyActivated = new Object();
        private boolean selected;
        private boolean defaultCar;
        private boolean vwConnectPowerLayerAvailable;

        public @Nullable String getVin() {
            return vin;
        }

        public @Nullable String getName() {
            return name;
        }

        public boolean getExpired() {
            return expired;
        }

        public @Nullable String getModel() {
            return model;
        }

        public @Nullable String getModelCode() {
            return modelCode;
        }

        public @Nullable String getModelYear() {
            return modelYear;
        }

        public @Nullable String getImageUrl() {
            return imageUrl;
        }

        public Object getVehicleSpecificFallbackImageUrl() {
            return vehicleSpecificFallbackImageUrl;
        }

        public Object getModelSpecificFallbackImageUrl() {
            return modelSpecificFallbackImageUrl;
        }

        public @Nullable String getDefaultImageUrl() {
            return defaultImageUrl;
        }

        public @Nullable String getVehicleBrand() {
            return vehicleBrand;
        }

        public @Nullable ZonedDateTime getEnrollmentStartDate() {
            String formattedTime = enrollmentDate;
            if (formattedTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault());
                LocalDate date = LocalDate.parse(formattedTime, formatter);
                ZonedDateTime zdt = date.atStartOfDay(ZoneId.systemDefault());

                return zdt;
            }
            return null;
        }

        public @Nullable String getEnrollmentDate() {
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

        public @Nullable String getStpStatus() {
            return stpStatus;
        }

        public boolean getWindowstateSupported() {
            return windowstateSupported;
        }

        public @Nullable String getDashboardUrl() {
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

        public Object getMobileKeyActivated() {
            return mobileKeyActivated;
        }

        public @Nullable String getEnrollmentType() {
            return enrollmentType;
        }

        public boolean getOcu3Low() {
            return ocu3Low;
        }

        public List<PackageService> getPackageServices() {
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

        public Object getSmartCardKeyActivated() {
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
            return new ToStringBuilder(this).append("vin", vin).append("name", name).append("expired", expired)
                    .append("model", model).append("modelCode", modelCode).append("modelYear", modelYear)
                    .append("imageUrl", imageUrl)
                    .append("vehicleSpecificFallbackImageUrl", vehicleSpecificFallbackImageUrl)
                    .append("modelSpecificFallbackImageUrl", modelSpecificFallbackImageUrl)
                    .append("defaultImageUrl", defaultImageUrl).append("vehicleBrand", vehicleBrand)
                    .append("enrollmentDate", enrollmentDate).append("deviceOCU1", deviceOCU1)
                    .append("deviceOCU2", deviceOCU2).append("deviceMIB", deviceMIB)
                    .append("engineTypeCombustian", engineTypeCombustian)
                    .append("engineTypeHybridOCU1", engineTypeHybridOCU1)
                    .append("engineTypeHybridOCU2", engineTypeHybridOCU2)
                    .append("engineTypeElectric", engineTypeElectric).append("engineTypeCNG", engineTypeCNG)
                    .append("engineTypeDefault", engineTypeDefault).append("stpStatus", stpStatus)
                    .append("windowstateSupported", windowstateSupported).append("dashboardUrl", dashboardUrl)
                    .append("vhrRequested", vhrRequested).append("vsrRequested", vsrRequested)
                    .append("vhrConfigAvailable", vhrConfigAvailable).append("verifiedByDealer", verifiedByDealer)
                    .append("vhr2", vhr2).append("roleEnabled", roleEnabled).append("isEL2Vehicle", isEL2Vehicle)
                    .append("workshopMode", workshopMode).append("hiddenUserProfiles", hiddenUserProfiles)
                    .append("mobileKeyActivated", mobileKeyActivated).append("enrollmentType", enrollmentType)
                    .append("ocu3Low", ocu3Low).append("packageServices", packageServices)
                    .append("fullyEnrolled", fullyEnrolled).append("secondaryUser", secondaryUser)
                    .append("fleet", fleet).append("touareg", touareg).append("iceSupported", iceSupported)
                    .append("flightMode", flightMode).append("esimCompatible", esimCompatible)
                    .append("dkyenabled", dkyenabled).append("smartCardKeyActivated", smartCardKeyActivated)
                    .append("selected", selected).append("defaultCar", defaultCar)
                    .append("vwConnectPowerLayerAvailable", vwConnectPowerLayerAvailable).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(fleet).append(vhrRequested).append(vhr2).append(fullyEnrolled)
                    .append(verifiedByDealer).append(enrollmentDate).append(mobileKeyActivated).append(ocu3Low)
                    .append(deviceMIB).append(engineTypeCombustian).append(modelCode).append(vin).append(model)
                    .append(selected).append(windowstateSupported).append(defaultCar).append(isEL2Vehicle)
                    .append(deviceOCU1).append(hiddenUserProfiles).append(deviceOCU2).append(modelYear)
                    .append(engineTypeDefault).append(flightMode).append(defaultImageUrl).append(stpStatus)
                    .append(touareg).append(smartCardKeyActivated).append(name).append(vsrRequested)
                    .append(enrollmentType).append(workshopMode).append(roleEnabled).append(engineTypeElectric)
                    .append(modelSpecificFallbackImageUrl).append(vehicleBrand).append(expired)
                    .append(engineTypeHybridOCU2).append(vwConnectPowerLayerAvailable).append(imageUrl)
                    .append(engineTypeHybridOCU1).append(dashboardUrl).append(engineTypeCNG).append(dkyenabled)
                    .append(iceSupported).append(vhrConfigAvailable).append(secondaryUser).append(packageServices)
                    .append(esimCompatible).append(vehicleSpecificFallbackImageUrl).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof CompleteVehicleJson)) {
                return false;
            }
            CompleteVehicleJson rhs = ((CompleteVehicleJson) other);
            return new EqualsBuilder().append(fleet, rhs.fleet).append(vhrRequested, rhs.vhrRequested)
                    .append(vhr2, rhs.vhr2).append(fullyEnrolled, rhs.fullyEnrolled)
                    .append(verifiedByDealer, rhs.verifiedByDealer).append(enrollmentDate, rhs.enrollmentDate)
                    .append(mobileKeyActivated, rhs.mobileKeyActivated).append(ocu3Low, rhs.ocu3Low)
                    .append(deviceMIB, rhs.deviceMIB).append(engineTypeCombustian, rhs.engineTypeCombustian)
                    .append(modelCode, rhs.modelCode).append(vin, rhs.vin).append(model, rhs.model)
                    .append(selected, rhs.selected).append(windowstateSupported, rhs.windowstateSupported)
                    .append(defaultCar, rhs.defaultCar).append(isEL2Vehicle, rhs.isEL2Vehicle)
                    .append(deviceOCU1, rhs.deviceOCU1).append(hiddenUserProfiles, rhs.hiddenUserProfiles)
                    .append(deviceOCU2, rhs.deviceOCU2).append(modelYear, rhs.modelYear)
                    .append(engineTypeDefault, rhs.engineTypeDefault).append(flightMode, rhs.flightMode)
                    .append(defaultImageUrl, rhs.defaultImageUrl).append(stpStatus, rhs.stpStatus)
                    .append(touareg, rhs.touareg).append(smartCardKeyActivated, rhs.smartCardKeyActivated)
                    .append(name, rhs.name).append(vsrRequested, rhs.vsrRequested)
                    .append(enrollmentType, rhs.enrollmentType).append(workshopMode, rhs.workshopMode)
                    .append(roleEnabled, rhs.roleEnabled).append(engineTypeElectric, rhs.engineTypeElectric)
                    .append(modelSpecificFallbackImageUrl, rhs.modelSpecificFallbackImageUrl)
                    .append(vehicleBrand, rhs.vehicleBrand).append(expired, rhs.expired)
                    .append(engineTypeHybridOCU2, rhs.engineTypeHybridOCU2)
                    .append(vwConnectPowerLayerAvailable, rhs.vwConnectPowerLayerAvailable)
                    .append(imageUrl, rhs.imageUrl).append(engineTypeHybridOCU1, rhs.engineTypeHybridOCU1)
                    .append(dashboardUrl, rhs.dashboardUrl).append(engineTypeCNG, rhs.engineTypeCNG)
                    .append(dkyenabled, rhs.dkyenabled).append(iceSupported, rhs.iceSupported)
                    .append(vhrConfigAvailable, rhs.vhrConfigAvailable).append(secondaryUser, rhs.secondaryUser)
                    .append(packageServices, rhs.packageServices).append(esimCompatible, rhs.esimCompatible)
                    .append(vehicleSpecificFallbackImageUrl, rhs.vehicleSpecificFallbackImageUrl).isEquals();
        }
    }

    public class PackageService {

        private @Nullable String packageServiceId;
        private @Nullable String propertyKeyReference;
        private @Nullable String packageServiceName;
        private @Nullable String trackingName;
        private @Nullable String activationDate;
        private @Nullable String expirationDate;
        private boolean expired;
        private boolean expireInAMonth;
        private @Nullable String packageType;
        private @Nullable String enrollmentPackageType;

        public @Nullable String getPackageServiceId() {
            return packageServiceId;
        }

        public @Nullable String getPropertyKeyReference() {
            return propertyKeyReference;
        }

        public @Nullable String getPackageServiceName() {
            return packageServiceName;
        }

        public @Nullable String getTrackingName() {
            return trackingName;
        }

        public @Nullable String getActivationDate() {
            return activationDate;
        }

        public @Nullable String getExpirationDate() {
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

        public @Nullable String getEnrollmentPackageType() {
            return enrollmentPackageType;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("packageServiceId", packageServiceId)
                    .append("propertyKeyReference", propertyKeyReference)
                    .append("packageServiceName", packageServiceName).append("trackingName", trackingName)
                    .append("activationDate", activationDate).append("expirationDate", expirationDate)
                    .append("expired", expired).append("expireInAMonth", expireInAMonth)
                    .append("packageType", packageType).append("enrollmentPackageType", enrollmentPackageType)
                    .toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(trackingName).append(packageServiceName).append(packageServiceId)
                    .append(expired).append(expireInAMonth).append(propertyKeyReference).append(enrollmentPackageType)
                    .append(activationDate).append(packageType).append(expirationDate).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof PackageService)) {
                return false;
            }
            PackageService rhs = ((PackageService) other);
            return new EqualsBuilder().append(trackingName, rhs.trackingName)
                    .append(packageServiceName, rhs.packageServiceName).append(packageServiceId, rhs.packageServiceId)
                    .append(expired, rhs.expired).append(expireInAMonth, rhs.expireInAMonth)
                    .append(propertyKeyReference, rhs.propertyKeyReference)
                    .append(enrollmentPackageType, rhs.enrollmentPackageType).append(activationDate, rhs.activationDate)
                    .append(packageType, rhs.packageType).append(expirationDate, rhs.expirationDate).isEquals();
        }
    }

    public class VehicleDetails {

        private List<String> lastConnectionTimeStamp = new ArrayList<>();
        private double distanceCovered = BaseVehicle.UNDEFINED;
        private double range = BaseVehicle.UNDEFINED;
        private @Nullable String serviceInspectionData;
        private @Nullable String oilInspectionData;
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

        public @Nullable String getServiceInspectionData() {
            return serviceInspectionData;
        }

        public @Nullable String getOilInspectionData() {
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
            return new ToStringBuilder(this).append("lastConnectionTimeStamp", lastConnectionTimeStamp)
                    .append("distanceCovered", distanceCovered).append("range", range)
                    .append("serviceInspectionData", serviceInspectionData)
                    .append("oilInspectionData", oilInspectionData).append("showOil", showOil)
                    .append("showService", showService).append("flightMode", flightMode).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(oilInspectionData).append(distanceCovered).append(range)
                    .append(serviceInspectionData).append(lastConnectionTimeStamp).append(showOil).append(flightMode)
                    .append(showService).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof VehicleDetails)) {
                return false;
            }
            VehicleDetails rhs = ((VehicleDetails) other);
            return new EqualsBuilder().append(oilInspectionData, rhs.oilInspectionData)
                    .append(distanceCovered, rhs.distanceCovered).append(range, rhs.range)
                    .append(serviceInspectionData, rhs.serviceInspectionData)
                    .append(lastConnectionTimeStamp, rhs.lastConnectionTimeStamp).append(showOil, rhs.showOil)
                    .append(flightMode, rhs.flightMode).append(showService, rhs.showService).isEquals();
        }
    }
}
