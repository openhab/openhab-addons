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
package org.openhab.binding.carnet.internal.api.carnet;

import static org.openhab.binding.carnet.internal.BindingConstants.*;
import static org.openhab.binding.carnet.internal.CarUtils.getString;
import static org.openhab.binding.carnet.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_CAR_FINDER;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.OpenStreetMapApiDTO;
import org.openhab.binding.carnet.internal.api.ApiBaseService;
import org.openhab.binding.carnet.internal.api.ApiException;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CarPosition;
import org.openhab.binding.carnet.internal.handler.VehicleCarNetHandler;
import org.openhab.binding.carnet.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceCarFinder} implements the carFinder service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceCarFinder extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceCarFinder.class);
    private final OpenStreetMapApiDTO osmApi = new OpenStreetMapApiDTO();

    public CarNetServiceCarFinder(VehicleCarNetHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_CAR_FINDER, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> ch) throws ApiException {
        addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_GEO, ITEMT_LOCATION, null, false, true);
        addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, ITEMT_DATETIME, null, false, true);
        addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_ADDRESS, ITEMT_STRING, null, false, true);
        addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_PARK_LOCATION, ITEMT_LOCATION, null, false, true);
        addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_PARK_ADDRESS, ITEMT_STRING, null, false, true);
        addChannel(ch, CHANNEL_GROUP_LOCATION, CHANNEL_PARK_TIME, ITEMT_DATETIME, null, false, true);
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        boolean updated = false;
        try {
            logger.debug("{}: Get Vehicle Position", thingId);
            CarPosition position = api.getVehiclePosition();
            updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_GEO, position.getAsPointType());

            String time = position.getCarSentTime();
            updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, new DateTimeType(time));
            updated |= updateAddress(position, CHANNEL_LOCATTION_ADDRESS);

            position = api.getStoredPosition();
            updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_PARK_LOCATION, position.getAsPointType());
            updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, new DateTimeType(time));
            updated |= updateAddress(position, CHANNEL_PARK_ADDRESS);
            String parkingTime = getString(position.getParkingTime());
            updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_PARK_TIME,
                    !parkingTime.isEmpty() ? getDateTime(parkingTime) : UnDefType.UNDEF);
            updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_CAR_MOVING, OnOffType.OFF);
        } catch (ApiException e) {
            updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_GEO, UnDefType.UNDEF);
            updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_TIME, UnDefType.UNDEF);
            if (e.getApiResult().httpCode == HttpStatus.NO_CONTENT_204) {
                updated |= updateChannel(CHANNEL_GROUP_LOCATION, CHANNEL_CAR_MOVING, OnOffType.ON);
            } else {
                throw e;
            }
        }
        return updated;
    }

    private boolean updateAddress(CarPosition position, String channel) {
        if (getConfig().vehicle.enableAddressLookup) {
            try {
                String address = osmApi.getAddressFromPosition(api.getHttp(), position.getAsPointType());
                return updateChannel(CHANNEL_GROUP_LOCATION, channel, new StringType(address));
            } catch (ApiException e) {
                updateChannel(CHANNEL_GROUP_LOCATION, channel, UnDefType.UNDEF);
            }
        }
        return false;
    }
}
