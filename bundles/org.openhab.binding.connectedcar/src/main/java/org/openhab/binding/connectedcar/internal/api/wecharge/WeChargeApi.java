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

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.fromJson;

import java.time.Instant;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.ApiWithOAuth;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargeHomeRecordResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargeHomeRecordResponse.WeChargeHomeRecord;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargePayRecordResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargePayRecordResponse.WeChargePayRecord;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargingHistory;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCHomeSessionsResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCHomeSessionsResponse.WeChargeHomeSession;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCRfidCardsResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCStationDetails;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCStationList;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCSubscriptionsResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCSubscriptionsResponse.WeChargeSubscription;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCTariffResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStationDetails;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStatus;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WeChargeApi} implements the WeCharge API calls
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class WeChargeApi extends ApiWithOAuth implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(WeChargeApi.class);
    protected final WeChargeStatus chargerStatus = new WeChargeStatus();

    public WeChargeApi(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public String getApiUrl() {
        return "https://wecharge.apps.emea.vwapps.io";
    }

    @Override
    public String getHomeReguionUrl() {
        return getApiUrl();
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        String json = callApi("", "home-charging/v1/stations?limit=10", crerateParameters().getHeaders(),
                "getStationList", String.class);
        WCStationList stations = fromJson(gson, json, WCStationList.class);
        ArrayList<String> list = new ArrayList<String>();
        for (WeChargeStationDetails station : stations.result.stations) {
            list.add(station.id);
        }
        return list;
    }

    @Override
    public VehicleDetails getVehicleDetails(String id) throws ApiException {
        return new VehicleDetails(config, getStationDetails(id));
    }

    @Override
    public VehicleStatus getVehicleStatus() throws ApiException {
        return new VehicleStatus(updateVehicleStatus(chargerStatus));
    }

    @Override
    public String refreshVehicleStatus() {
        try {
            chargerStatus.clearCache(); // force reload of subscriptions, tariffs, rfidCards
            updateVehicleStatus(chargerStatus);
            return API_REQUEST_SUCCESSFUL;
        } catch (ApiException e) {
            return API_REQUEST_ERROR;
        }
    }

    @Override
    public String controlEngine(boolean restart) throws ApiException {
        if (restart) {
            restartStation(chargerStatus.stationId);
        }
        return API_REQUEST_SUCCESSFUL;
    }

    protected WeChargeStatus updateVehicleStatus(WeChargeStatus status) throws ApiException {
        logger.debug("{}: Updating WeCharge status", thingId);
        status.station = getStationDetails(config.vehicle.vin);
        status.stationId = status.station.id;
        if (status.subscriptions.size() == 0) {
            loadSubscriptions(status); // subscriptions + tariffs
            loadRfidCards(status);
        }

        // Load charing records
        loadHomeSession(status);
        loadHomeChargingData(status);
        loadPayChargingData(status);

        getChargingHistory(status);
        return status;
    }

    private void loadSubscriptions(WeChargeStatus status) throws ApiException {
        WCSubscriptionsResponse subscriptions = wcRequest("/charge-and-pay/v1/user/subscriptions", "getSubscriptions",
                WCSubscriptionsResponse.class);
        for (int i = 0; i < subscriptions.result.size(); i++) {
            WeChargeSubscription sub = subscriptions.result.get(i);
            if (sub != null) {
                status.addSubscription(sub);
                if (status.getTariffs(sub.tariffId) == null) {
                    status.addTariff(wcRequest("/charge-and-pay/v1/user/tariffs/" + sub.tariffId, "getTariffDetails",
                            WCTariffResponse.class).result);
                }
            }
        }
    }

    private void loadRfidCards(WeChargeStatus status) throws ApiException {
        WCRfidCardsResponse rfidCards = wcRequest("/charge-and-pay/v1/user/rfidcards", "getRfidCards",
                WCRfidCardsResponse.class);
        for (int i = 0; i < rfidCards.result.size(); i++) {
            status.addRfidCard(rfidCards.result.get(i));
        }
    }

    private WeChargeStationDetails getStationDetails(String id) throws ApiException {
        return wcRequest("/home-charging/v1/stations/" + id, "getStationDetail", WCStationDetails.class).result;
    }

    private WCChargingHistory getChargingHistory(WeChargeStatus status) throws ApiException {
        if (status.chargerPro) {
            status.chargingHistory = wcRequest(
                    "home-charging/v1/charging/records/total-charged?created_at_after=2020-01-01T00:00:00.000Z&created_at_before="
                            + Instant.now().toString(),
                    "getChargingHistory", WCChargingHistory.class);
            WCChargingHistory history = status.chargingHistory;
            if (history.totalCount > 0 || history.totalEngergyWh > 0) {
                return history;
            }
            status.chargerPro = false; // disable fruther calls
        }
        return new WCChargingHistory();
    }

    private void restartStation(String stationId) throws ApiException {
        String json = http.post("home-charging/v1/stations/" + stationId + "/reboot", crerateParameters().getHeaders(),
                "{\"reboot_mode\":\"immediate\"}").response;
    }

    private void loadHomeChargingData(WeChargeStatus status) throws ApiException {
        WCChargeHomeRecordResponse crecords = wcRequest(
                "home-charging/v1/charging/records?limit=" + config.vehicle.numChargingRecords + "&offset=0",
                "getHomeChargingRecords", WCChargeHomeRecordResponse.class);
        for (WeChargeHomeRecord record : crecords.chargingRecords) {
            status.addHomeChargingRecord(record);
        }
    }

    private void loadPayChargingData(WeChargeStatus status) throws ApiException {
        WCChargePayRecordResponse crecords = wcRequest(
                "charge-and-pay/v1/charging/records?limit=" + config.vehicle.numChargingRecords + "&offset=0",
                "getPayChargingRecords", WCChargePayRecordResponse.class);
        for (WeChargePayRecord record : crecords.result) {
            status.addPayChargingRecord(record);
        }
    }

    private void loadHomeSession(WeChargeStatus status) throws ApiException {
        WCHomeSessionsResponse sessions = wcRequest(
                "/home-charging/v1/charging/sessions?station_id=" + chargerStatus.stationId + "&limit=5&offset=0",
                "getHomeChargingSessions", WCHomeSessionsResponse.class);
        for (WeChargeHomeSession s : sessions.chargingSessions) {
            // we are still lacking the format
        }
    }

    protected ApiHttpMap crerateParameters() throws ApiException {
        return new ApiHttpMap().headers(config.api.stdHeaders)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + createAccessToken())
                .header("wc_access_token", getWcAccessToken());
    }

    private <T> T wcRequest(String uri, String function, Class<T> classOfT) throws ApiException {
        return callApi("", uri, crerateParameters().getHeaders(), "getLocationList", classOfT);
    }
}
