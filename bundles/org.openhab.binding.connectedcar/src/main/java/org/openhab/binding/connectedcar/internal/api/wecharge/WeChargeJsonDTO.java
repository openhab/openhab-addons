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
package org.openhab.binding.connectedcar.internal.api.wecharge;

import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargeHomeRecordResponse.WeChargeHomeRecord;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargePayRecordResponse.WeChargePayRecord;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCRfidCardsResponse.WeChargeRfidCard;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCSubscriptionsResponse.WeChargeSubscription;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCTariffResponse.WeChargeTariff;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link WeChargeJsonDTO} defines data formats for the WeCharge API
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
public class WeChargeJsonDTO {

    public class WCLocationAddress {
        public String street, zip, city, country;

        @Override
        public String toString() {
            return street + ", " + zip + " " + city + ", " + country;
        }
    }

    public class WCLocationList {
        public class WeChargeLocationList {
            public class WCLocationsResult {
                public class WCLocationsEntry {
                    public class WCListStation {
                        public String id;
                    }

                    public String id;
                    public WCLocationAddress address;
                    public String description;
                    public ArrayList<WCListStation> stations;
                }

                public ArrayList<WCLocationsEntry> locations;
            }

            public String timestamp;
            public WCLocationsResult result;
        }

        public WeChargeLocationList data;
    }

    public class WCStationLocation {
        public class WCStatuionGeo {
            public String longitude;
            public String latitude;
        }

        public String id;
        public WCLocationAddress address;
        @SerializedName("geo_location")
        public WCStatuionGeo geoLocation;
        public String description;
    }

    public class WeChargeStationDetails {
        public class WCStationConnector {
            public String id, availability;
        }

        @SerializedName("connection_state")
        public String connectionState;
        public ArrayList<WCStationConnector> connectors;
        public String id;
        public String last_contact;
        public String lifecycle_state;
        public String name;
        public String authorization_mode;
        public String model;
        public String last_connect;
        public WCStationLocation location;
    }

    public static class WCChargingHistory {
        @SerializedName("total_count")
        public Integer totalCount = 0;
        @SerializedName("total_energy_wh")
        public Integer totalEngergyWh = 0;
    }

    public class WCStationDetails {
        public String timestamp;
        public WeChargeStationDetails result;
    }

    public class WCStationList {
        public class WeChargeStationList {
            @SerializedName("total_count")
            public Integer totalCount;
            public Integer offset;
            public Integer limit;
            public ArrayList<WeChargeStationDetails> stations;
        }

        public String timestamp;
        public WeChargeStationList result;
    }

    public class WCSubscriptionsResponse {
        public class WeChargeSubscription {
            public String id;
            @SerializedName("created_at")
            public String createdAt;
            @SerializedName("start_date")
            public String startDate;
            @SerializedName("end_date")
            public String endDate;
            @SerializedName("status")
            public String status;
            @SerializedName("subscriber_id")
            public String subscriberId;
            @SerializedName("tariff_id")
            public String tariffId;
            @SerializedName("tariff_name")
            public String tariffName;
            @SerializedName("currency")
            public String currency;
            @SerializedName("sign_up_fee")
            public String sign_upFee;
            @SerializedName("monthly_fee")
            public String monthlyFee;
            @SerializedName("updated_at")
            public String updatedAt;
            @SerializedName("automatic_renewal")
            public Boolean automaticRenewal;
        }

        public String timestamp;
        public ArrayList<WeChargeSubscription> result;
    }

    public class WCTariffResponse {
        public class WeChargeTariff {
            public class WCChargingCondition {
                @SerializedName("country_code")
                public String countryCode;
                public String currency;
                @SerializedName("cpo_code")
                public String cpoCode;
                @SerializedName("fixed_pricing_ac_price")
                public Double fixedPricingAcPrice;
                @SerializedName("fixed_pricing_dc_price")
                public Double fixedPricingDcPrice;
                @SerializedName("fixed_pricing_unit")
                public String fixedPricingUnit;
                @SerializedName("transaction_fee")
                public Double transactionFee;
            }

            String id, name, description, code;
            @SerializedName("country_code")
            public String country_Code;
            public String currency;
            @SerializedName("duration_months")
            public Integer durationMonths;
            @SerializedName("automatic_renewal")
            public Boolean automaticRenewal;
            public Integer revision;
            @SerializedName("tenant_id")
            public String tenantId;
            @SerializedName("subscription_monthly_fee")
            public Double subscriptionMonthlyFee;
            @SerializedName("sign_up_fee")
            public Double signUpFee;
            @SerializedName("charging_conditions")
            public ArrayList<WCChargingCondition> chargingConditions;
        }

        public String timestamp;
        public WeChargeTariff result;
    }

    public class WCHomeSessionsResponse {
        public class WeChargeHomeSession {

        }

        public String timestamp;
        @SerializedName("charging_sessions")
        public ArrayList<WeChargeHomeSession> chargingSessions;
    }

    public class WCRfidCardsResponse {
        public class WeChargeRfidCard {
            public String id, number;
            @SerializedName("brand_id")
            public Integer brandId;
            @SerializedName("created_at")
            public String createdAt;
            @SerializedName("pairing_date")
            public String pairingDate;
            @SerializedName("public_charging")
            public Boolean publicCharging;
            public String status;
            @SerializedName("subscriber_id")
            public String subscriberId;
            @SerializedName("subscription_id")
            public String subscriptionId;
            @SerializedName("tenant_id")
            public String tenantId;
            @SerializedName("tenant_name")
            public String tenantName;
            @SerializedName("updated_at")
            public String updatedAt;
            @SerializedName("design_template")
            public Integer designTemplate;
        }

        public String timestamp;
        public ArrayList<WeChargeRfidCard> result;
    }

    public static class WCChargePayRecordResponse {
        public static class WeChargePayRecord {
            public String id;
            @SerializedName("subscription_id")
            public String subscriptionId;
            @SerializedName("location_evse_id")
            public String locationEvseId;
            @SerializedName("location_name")
            public String locationName;
            @SerializedName("location_address")
            public String locationAddress;
            @SerializedName("location_connector_power_type")
            public String locationConnectorPowerType;
            @SerializedName("location_coordinates_latitude")
            public String locationCoordinatesLatitude;
            @SerializedName("location_coordinates_longitude")
            public String locationCoordinatesLongitude;
            @SerializedName("currency")
            public String currency;
            @SerializedName("total_energy")
            public Double totalEnergy;
            @SerializedName("total_price")
            public Double totalPrice;
            @SerializedName("total_time")
            public Double totalTime;
            @SerializedName("created_at")
            public String createdAt;
            @SerializedName("updated_at")
            public String updatedAt;
            @SerializedName("start_date_time")
            public String startDateTime;
            @SerializedName("end_date_time")
            public String endDateTime;
            @SerializedName("timezone")
            public String timezone;
        }

        public String timestamp;
        public ArrayList<WeChargePayRecord> result;
    }

    public class WCChargeHomeRecordResponse {
        public class WeChargeHomeRecord {
            @SerializedName("authentication_method")
            public String authenticationMethod;
            @SerializedName("authorization_mode")
            public String authorizationMode;
            @SerializedName("charging_session_id")
            public String chargingSessionId;
            @SerializedName("connector_id")
            public Double connectorId;
            @SerializedName("rfid_card_id")
            public String rfidCardId;
            @SerializedName("rfid_card_label")
            public String rfidCardLabel;
            @SerializedName("rfid_card_serial_number")
            public String rfidCardSerialNumber;
            @SerializedName("session_faulted")
            public Boolean sessionFaulted;
            @SerializedName("start_date_time")
            public String startDateTime;
            @SerializedName("station_id")
            public String stationId;
            @SerializedName("station_name")
            public String stationName;
            @SerializedName("station_serial_number")
            public String stationSerialNumber;
            @SerializedName("stop_date_time")
            public String stopDateTime;
            @SerializedName("total_energy_wh")
            public Double totalEngergyWh;
            @SerializedName("transaction_id")
            public String transactionId;
            @SerializedName("station_model")
            public String stationModel;
            public WCStationLocation location;
            @SerializedName("current_station_name")
            public String currentStationName;
            @SerializedName("created_at")
            public String createdAt;
        }

        public String timestamp;

        @SerializedName("total_count")
        public Integer totalCount;
        public Integer offset;
        public Integer limit;

        @SerializedName("charging_records")
        public ArrayList<WeChargeHomeRecord> chargingRecords;
    }

    public static class WeChargeRecord extends WeChargePayRecord {
        public Boolean publicCharging;
        public String rfidCard, tariff;
        public Boolean sessionFaulted;

        public WeChargeRecord(WeChargeHomeRecord r, WeChargeStatus status) {
            this.id = getString(r.transactionId);
            this.publicCharging = false;
            createdAt = getString(r.createdAt);
            updatedAt = getString(r.createdAt);
            rfidCard = getString(r.rfidCardLabel) + " (" + getString(r.rfidCardSerialNumber) + ")";
            sessionFaulted = r.sessionFaulted;
            totalEnergy = r.totalEngergyWh;
            startDateTime = getString(r.startDateTime);
            endDateTime = getString(r.stopDateTime);
            Date start = convertDateTime(startDateTime);
            Date end = convertDateTime(endDateTime);
            long diff = end.getTime() - start.getTime();
            if (diff > 0) {
                totalTime = diff / 1000 / 60 + 1.0;
            }
            locationEvseId = getString(this.endDateTime);
            locationName = getString(r.currentStationName);
            locationAddress = r.location.address.toString();
            if (r.location.geoLocation != null) {
                locationCoordinatesLatitude = r.location.geoLocation.latitude;
                locationCoordinatesLongitude = r.location.geoLocation.longitude;
            } else {
                locationCoordinatesLatitude = "";
                locationCoordinatesLongitude = "";
            }

            WeChargeRfidCard rfid = status.getRfidCard(getString(r.rfidCardLabel));
            this.subscriptionId = rfid != null ? rfid.subscriptionId : "";
            WeChargeSubscription sub = status.getSubscription(this.subscriptionId);
            this.currency = sub != null ? sub.currency : "";
            this.tariff = sub != null ? sub.tariffName : "";
        }

        public WeChargeRecord(WeChargePayRecord r, WeChargeStatus status) {
            id = r.id;
            publicCharging = true;
            createdAt = getString(r.createdAt);
            updatedAt = getString(r.updatedAt);
            locationName = getString(r.locationName);
            locationEvseId = getString(r.locationEvseId);
            locationAddress = getString(r.locationAddress);
            locationCoordinatesLatitude = getString(r.locationCoordinatesLatitude);
            locationCoordinatesLongitude = getString(r.locationCoordinatesLongitude);
            locationConnectorPowerType = getString(r.locationConnectorPowerType);
            startDateTime = getString(r.startDateTime);
            endDateTime = getString(r.endDateTime);
            totalTime = r.totalTime;
            totalEnergy = r.totalEnergy;
            totalPrice = r.totalPrice;
            rfidCard = ""; // no info included!
            sessionFaulted = false;
        }
    }

    public static class WeChargeStatus {
        public String stationId = "";
        public boolean chargerPro = true;

        public WeChargeStationDetails station;
        public WCChargingHistory chargingHistory;
        public Map<String, WeChargeSubscription> subscriptions = new HashMap<>();
        public Map<String, WeChargeTariff> tariffs = new HashMap<>();;
        public Map<String, WeChargeRfidCard> rfidCards = new HashMap<>();;
        public Map<String, WeChargeRecord> chargingRecords = new HashMap<>();;

        public WeChargeStatus() {
        }

        public void addStation(WeChargeStationDetails station) {
            this.stationId = station.id;
            this.station = station;
        }

        public void addSubscription(WeChargeSubscription r) {
            subscriptions.put(r.id, r);
        }

        public void addTariff(@Nullable WeChargeTariff tariff) {
            if (tariff != null) {
                tariffs.put(tariff.id, tariff);
            }
        }

        public void addRfidCard(WeChargeRfidCard r) {
            rfidCards.put(r.id, r);
        }

        public void addHomeChargingRecord(WeChargeHomeRecord r) {
            WeChargeRecord record = new WeChargeRecord(r, this);
            chargingRecords.put(record.id, record);
        }

        public void addPayChargingRecord(WeChargePayRecord r) {
            WeChargeRecord record = new WeChargeRecord(r, this);
            chargingRecords.put(record.id, record);
        }

        public @Nullable WeChargeTariff getTariffs(String id) {
            return tariffs.get(id);
        }

        public @Nullable WeChargeRfidCard getRfidCard(String id) {
            return rfidCards.get(id);
        }

        public @Nullable WeChargeSubscription getSubscription(String id) {
            return subscriptions.get(id);
        }

        public void clearCache() {
            subscriptions.clear();
            tariffs.clear();
            rfidCards.clear();
        }
    }
}
