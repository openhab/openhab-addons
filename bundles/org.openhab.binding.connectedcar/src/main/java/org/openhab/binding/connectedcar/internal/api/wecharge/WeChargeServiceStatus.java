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

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_WECHARGE;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_VEHICLE_STATUS_REPORT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.GeoPosition;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCChargingRecordResponse.WeChargeRecord;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCRfidCardsResponse.WeChargeRfidCard;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WCSubscriptionsResponse.WeChargeSubscription;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStationDetails;
import org.openhab.binding.connectedcar.internal.api.wecharge.WeChargeJsonDTO.WeChargeStatus;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectServiceStatus;
import org.openhab.binding.connectedcar.internal.handler.ThingBaseHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeConnectServiceStatus} implements the Status Service for WeConnect.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class WeChargeServiceStatus extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(WeChargeServiceStatus.class);
    String thingId = API_BRAND_WECHARGE;

    public WeChargeServiceStatus(ThingBaseHandler thingHandler, ApiBase api) {
        super(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
        thingId = getConfig().vehicle.vin;
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        // Try to query status information from vehicle
        WeChargeStatus status = api.getVehicleStatus().weChargeStatus;
        if (status == null) {
            logger.warn("{}: Unable to read charger status, can't create channels!", thingId);
            return false;
        }

        String group = CHANNEL_GROUP_CHARGER;
        addChannels(channels, group, true, CHANNEL_CHARGER_NAME, CHANNEL_CHARGER_ADDRESS, CHANNEL_CHARGER_LAST_CONNECT,
                CHANNEL_CHARGER_PLUG_STATE);

        // Create subscriptions
        for (int s = 0; s < status.subscriptions.size(); s++) {
            group = CHANNEL_GROUP_SUBSCRIPTION + (s + 1);
            addChannels(channels, group, true, CHANNEL_SUB_ENDDATE, CHANNEL_SUB_STATUS, CHANNEL_SUB_TARIFF,
                    CHANNEL_SUB_MFEE);
        }

        // Create RFID cars
        for (int c = 0; c < status.rfidCards.size(); c++) {
            group = CHANNEL_CHANNEL_GROUP_RFID + (c + 1);
            addChannels(channels, group, true, CHANNEL_RFID_ID, CHANNEL_RFID_PUBLIC, CHANNEL_RFID_STATUS,
                    CHANNEL_RFID_UPDATE);
        }

        // Create charging records
        int num = getConfig().vehicle.numChargingRecords;
        for (int r = 1; r <= num; r++) {
            group = CHANNEL_CHANNEL_GROUP_TRANSACTIONS + r;
            addChannels(channels, group, true, CHANNEL_TRANS_ID, CHANNEL_TRANS_LOCATION, CHANNEL_TRANS_SUBID,
                    CHANNEL_TRANS_EVSE, CHANNEL_TRANS_PTYPE, CHANNEL_TRANS_START, CHANNEL_TRANS_PTYPE,
                    CHANNEL_TRANS_START, CHANNEL_TRANS_END, CHANNEL_TRANS_ENERGY, CHANNEL_TRANS_PRICE,
                    CHANNEL_TRANS_DURATION);
        }
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Charger Status", thingId);
        WeChargeStatus status = api.getVehicleStatus().weChargeStatus;
        if (status == null) {
            logger.warn("{}: Unable to read charger status, can't create channels!", thingId);
            return false;
        }

        boolean updated = false;
        WeChargeStationDetails station = status.station;
        if (!status.stationId.isEmpty()) {
            String value = getString(station.name) + " (" + station.model + ")";
            updated |= updateChannel(CHANNEL_CHARGER_NAME, getStringType(value));
            value = station.location.address.street + "," + station.location.address.zip + ","
                    + station.location.address.city + "," + station.location.address.country + " ("
                    + station.location.description + ")";
            updated |= updateChannel(CHANNEL_CHARGER_ADDRESS, getStringType(value));
            updated |= updateChannel(CHANNEL_CHARGER_PLUG_STATE,
                    getOnOff("connected".equals(getString(station.connectionState))));
            updated |= updateChannel(CHANNEL_CHARGER_LAST_CONNECT, getDateTime(getString(station.last_connect)));
        }

        int i = 1;
        for (Map.Entry<String, WeChargeSubscription> s : status.subscriptions.entrySet()) {
            WeChargeSubscription sub = s.getValue();
            String group = CHANNEL_GROUP_SUBSCRIPTION + i++;
            updated |= updateChannel(group, CHANNEL_SUB_STATUS, getStringType(sub.status));
            updated |= updateChannel(group, CHANNEL_SUB_TARIFF, getStringType(sub.tariffName));
            updated |= updateChannel(group, CHANNEL_SUB_ENDDATE, getDateTime(sub.endDate));
            Double value = Double.parseDouble(sub.monthlyFee);
            updated |= updateChannel(group, CHANNEL_SUB_MFEE, getDecimal(value));
        }

        i = 1;
        for (Map.Entry<String, WeChargeRfidCard> c : status.rfidCards.entrySet()) {
            WeChargeRfidCard card = c.getValue();
            String group = CHANNEL_CHANNEL_GROUP_RFID + i++;
            updated |= updateChannel(group, CHANNEL_RFID_ID, getStringType(card.number));
            updated |= updateChannel(group, CHANNEL_RFID_PUBLIC, getOnOff(card.publicCharging));
            updated |= updateChannel(group, CHANNEL_RFID_STATUS, getStringType(card.status));
            updated |= updateChannel(group, CHANNEL_RFID_UPDATE, getDateTime(card.updatedAt));
        }

        i = 1;
        for (Map.Entry<String, WeChargeRecord> c : status.chargingRecords.entrySet()) {
            WeChargeRecord record = c.getValue();
            String group = CHANNEL_CHANNEL_GROUP_TRANSACTIONS + i++;
            updated |= updateChannel(group, CHANNEL_TRANS_ID, getStringType(record.id));
            updated |= updateChannel(group, CHANNEL_TRANS_LOCATION,
                    new GeoPosition(record.locationCoordinatesLatitude, record.locationCoordinatesLongitude)
                            .getAsPointType());
            updated |= updateChannel(group, CHANNEL_TRANS_ADDRESS, getStringType(record.locationAddress));
            updated |= updateChannel(group, CHANNEL_TRANS_EVSE, getStringType(record.locationEvseId));
            updated |= updateChannel(group, CHANNEL_TRANS_SUBID, getStringType(record.subscriptionId));
            updated |= updateChannel(group, CHANNEL_TRANS_PTYPE, getStringType(record.locationConnectorPowerType));
            updated |= updateChannel(group, CHANNEL_TRANS_START, getDateTime(record.startDateTime));
            updated |= updateChannel(group, CHANNEL_TRANS_END, getDateTime(record.endDateTime));
            updated |= updateChannel(group, CHANNEL_TRANS_ENERGY, getDecimal(record.totalEnergy));
            updated |= updateChannel(group, CHANNEL_TRANS_DURATION, getDecimal(record.totalTime));
            updated |= updateChannel(group, CHANNEL_TRANS_PRICE, getDecimal(record.totalPrice));
            if (i > getConfig().vehicle.numChargingRecords) {
                break;
            }
        }
        return updated;
    }
}
