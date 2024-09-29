/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.dto;

import java.util.List;

/**
 * The {@link Attributes} is responsible for storing
 * informations returned by vehicule attributes rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Attributes extends VocAnswer {
    public String vehicleType;
    public String registrationNumber;
    public Boolean carLocatorSupported;
    public Boolean honkAndBlinkSupported;
    public List<String> honkAndBlinkVersionsSupported;
    public Boolean remoteHeaterSupported;
    public Boolean unlockSupported;
    public Boolean lockSupported;
    public Boolean journalLogSupported;
    public Integer unlockTimeFrame;
    public Boolean journalLogEnabled;
    public Boolean preclimatizationSupported;
    public String vin;
    public Boolean engineStartSupported;

    /*
     * Currently unused in the binding, maybe interesting in the future
     * public class Country {
     * public @NonNullByDefault({}) String iso2;
     * }
     * private String engineCode;
     * private String exteriorCode;
     * private String interiorCode;
     * private String tyreDimensionCode;
     * private Object tyreInflationPressureLightCode;
     * private Object tyreInflationPressureHeavyCode;
     * private String gearboxCode;
     * private String fuelType;
     * private Integer fuelTankVolume;
     * private Integer grossWeight;
     * private Integer modelYear;
     * private String vehicleTypeCode;
     * private Integer numberOfDoors;
     * private Country country;
     * private Integer carLocatorDistance;
     * private Integer honkAndBlinkDistance;
     * private String bCallAssistanceNumber;
     * private Boolean assistanceCallSupported;
     * private Integer verificationTimeFrame;
     * private Integer timeFullyAccessible;
     * private Integer timePartiallyAccessible;
     * private String subscriptionType;
     * private String subscriptionStartDate;
     * private String subscriptionEndDate;
     * private String serverVersion;
     * private Boolean highVoltageBatterySupported;
     * private Object maxActiveDelayChargingLocations;
     * private Integer climatizationCalendarMaxTimers;
     * private String vehiclePlatform;
     * private Boolean statusParkedIndoorSupported;
     * private Boolean overrideDelayChargingSupported;
     * private @Nullable List<String> sendPOIToVehicleVersionsSupported = null;
     * private @Nullable List<String> climatizationCalendarVersionsSupported = null;
     */
}
