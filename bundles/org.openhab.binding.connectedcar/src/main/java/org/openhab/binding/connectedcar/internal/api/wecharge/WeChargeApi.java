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
package org.openhab.binding.connectedcar.internal.api.wecharge;

import static org.openhab.binding.connectedcar.internal.CarUtils.fromJson;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;

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
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargingRecordResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCRfidCardsResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCStationDetails;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCStationList;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCSubscriptionsResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCSubscriptionsResponse.WeChargeSubscription;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCTariffResponse;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStationDetails;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WeChargeApi} implements the WeCharge API calls
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class WeChargeApi extends ApiWithOAuth implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(WeChargeApi.class);

    protected final WeChargeStatus baseStatus = new WeChargeStatus();

    public WeChargeApi(ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
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
        String json = callApi("", "https://wecharge.apps.emea.vwapps.io/home-charging/v1/stations?limit=25",
                crerateParameters().getHeaders(), "getStationList", String.class);
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
        updateVehicleStatus();
        return new VehicleStatus(baseStatus);
    }

    @Override
    public String refreshVehicleStatus() {
        try {
            baseStatus.clearCache(); // force reload of subscriptions, tariffs, rfidCards
            updateVehicleStatus();
            return API_REQUEST_SUCCESSFUL;
        } catch (ApiException e) {
            return API_REQUEST_ERROR;
        }
    }

    protected void updateVehicleStatus() throws ApiException {
        logger.debug("{}: Updating WeCharge status", thingId);
        baseStatus.station = getStationDetails(config.vehicle.vin);
        if (baseStatus.subscriptions.size() == 0) {
            loadSubscriptions(); // subscriptions + tariffs
            loadRfidCards();
        }

        // Load charing records
        loadChargingData();
    }

    private void loadSubscriptions() throws ApiException {
        WCSubscriptionsResponse subscriptions = wcRequest("/charge-and-pay/v1/user/subscriptions", "getSubscriptions",
                WCSubscriptionsResponse.class);
        for (int i = 0; i < subscriptions.result.size(); i++) {
            WeChargeSubscription sub = subscriptions.result.get(i);
            if (sub != null) {
                baseStatus.addSubscription(sub);
                if (baseStatus.getTariffs(sub.tariffId) == null) {
                    baseStatus.addTariff(wcRequest("/charge-and-pay/v1/user/tariffs/" + sub.tariffId,
                            "getTariffDetails", WCTariffResponse.class).result);
                }
            }
        }
    }

    private void loadRfidCards() throws ApiException {
        WCRfidCardsResponse rfidCards = wcRequest("/charge-and-pay/v1/user/rfidcards", "getRfidCards",
                WCRfidCardsResponse.class);
        for (int i = 0; i < rfidCards.result.size(); i++) {
            baseStatus.addRfidCard(rfidCards.result.get(i));
        }
    }

    private void loadChargingData() throws ApiException {
        WCChargingRecordResponse crecords = wcRequest("/charge-and-pay/v1/charging/records?limit=10&offset=0",
                "getChargingRecords", WCChargingRecordResponse.class);
        for (int i = 0; i < crecords.result.size(); i++) {
            baseStatus.addChargingRecord(crecords.result.get(i));
        }
    }

    private WeChargeStationDetails getStationDetails(String id) throws ApiException {
        baseStatus.station = wcRequest("/home-charging/v1/stations/" + id, "getStationDetail",
                WCStationDetails.class).result;
        baseStatus.stationId = baseStatus.station.id;
        return baseStatus.station;
    }

    protected ApiHttpMap crerateParameters() throws ApiException {
        return new ApiHttpMap().headers(config.api.stdHeaders)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + createAccessToken())
                .header("wc_access_token", getWcAccessToken());
    }

    private <T> T wcRequest(String uri, String function, Class<T> classOfT) throws ApiException {
        return callApi("", "https://wecharge.apps.emea.vwapps.io" + uri, crerateParameters().getHeaders(),
                "getLocationList", classOfT);
    }

}
