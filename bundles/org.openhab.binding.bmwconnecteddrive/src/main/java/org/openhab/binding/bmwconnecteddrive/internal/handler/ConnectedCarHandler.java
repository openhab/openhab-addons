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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;
import static org.openhab.binding.bmwconnecteddrive.internal.handler.HTTPConstants.CONTENT_TYPE_JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedCarConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.CarType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.BevRexAttributes;
import org.openhab.binding.bmwconnecteddrive.internal.dto.BevRexAttributesMap;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.Efficiency;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.Score;
import org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ConnectedCarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedCarHandler extends ConnectedCarChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarHandler.class);
    private static final Gson GSON = new Gson();

    private String driveTrain;
    private HttpClient httpClient;
    private Token token = new Token();

    private @Nullable ConnectedDriveBridgeHandler bridgeHandler;
    protected @Nullable ScheduledFuture<?> refreshJob;

    private @Nullable String vehicleFingerprint;
    private @Nullable String efficiencyFingerprint;

    // Connected Drive APIs
    private @Nullable String vehicleAPI;
    private @Nullable String navigationAPI;
    private @Nullable String efficiencyAPI;
    private @Nullable String remoteControlAPI;
    private @Nullable String remoteExecutionAPI;
    private @Nullable String sendMessageAPI;
    private @Nullable String imageAPI;

    private @Nullable String lastTripAPI;// = baseUrl + "/statistics/lastTrip";
    private @Nullable String allTripsAPI;// = baseUrl + "/statistics/allTrips";
    private @Nullable String chargeAPI;// = baseUrl + "/chargingprofile";
    private @Nullable String destinationAPI;// = baseUrl + "/destinations";
    private List<String> allAPIs = new ArrayList<String>();

    private @Nullable String vehicleCache;
    private @Nullable String efficiencyCache;

    public ConnectedCarHandler(Thing thing, HttpClient hc, String type) {
        super(thing);
        httpClient = hc;
        driveTrain = type;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String group = channelUID.getGroupId();
            logger.info("REFRESH {}", group);
            if (CHANNEL_GROUP_LAST_TRIP.equals(group) || CHANNEL_GROUP_LAST_TRIP.equals(group)) {
                updateEfficiencyStates(efficiencyCache);
            } else if (CHANNEL_GROUP_CAR_STATUS.equals(group) || CHANNEL_GROUP_LOCATION.equals(group)) {
                updateRangeStates(vehicleCache);
            }
        } else {
            logger.info("No refresh");
        }
        if (channelUID.getIdWithoutGroup().equals(CARDATA_FINGERPRINT)) {
            logger.info("Trigger CarData Fingerprint");
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    if (vehicleFingerprint != null) {
                        logger.warn("BMW ConnectedDrive Binding - Car Data Troubleshoot fingerprint - BEGIN");
                        logger.warn("{}", vehicleFingerprint);
                        logger.warn("{}", efficiencyFingerprint);
                        logger.warn("BMW ConnectedDrive Binding - Car Data Troubleshoot fingerprint - END");
                    } else {
                        logger.warn(
                                "BMW ConnectedDrive Binding - No Car Data Troubleshoot fingerprint available. Please check for valid username and password Settings for proper connection towards ConnectDrive");
                    }
                }
                // Switch back to off immediately
                updateState(channelUID, OnOffType.OFF);
            }
        }
    }

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     *
     * """URLs for different services and error code mapping."""
     *
     * AUTH_URL = 'https://customer.bmwgroup.com/{gcdm_oauth_endpoint}/authenticate'
     * AUTH_URL_LEGACY = 'https://{server}/gcdm/oauth/token'
     * BASE_URL = 'https://{server}/webapi/v1'
     *
     * VEHICLES_URL = BASE_URL + '/user/vehicles'
     * VEHICLE_VIN_URL = VEHICLES_URL + '/{vin}'
     * VEHICLE_STATUS_URL = VEHICLE_VIN_URL + '/status'
     * REMOTE_SERVICE_STATUS_URL = VEHICLE_VIN_URL + '/serviceExecutionStatus?serviceType={service_type}'
     * REMOTE_SERVICE_URL = VEHICLE_VIN_URL + "/executeService"
     * VEHICLE_IMAGE_URL = VEHICLE_VIN_URL + "/image?width={width}&height={height}&view={view}"
     * VEHICLE_POI_URL = VEHICLE_VIN_URL + '/sendpoi'
     *
     * }
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        ConnectedCarConfiguration config = getConfigAs(ConnectedCarConfiguration.class);
        if (config != null) {
            scheduler.execute(() -> {
                Bridge bridge = getBridge();
                if (bridge != null) {
                    BridgeHandler handler = bridge.getHandler();
                    if (handler != null) {
                        bridgeHandler = ((ConnectedDriveBridgeHandler) handler);
                        String baseUrl = "https://" + bridgeHandler.getRegionServer() + "/webapi/v1/user/vehicles/"
                                + config.vin;
                        vehicleAPI = baseUrl + "/status";
                        allAPIs.add(vehicleAPI);
                        lastTripAPI = baseUrl + "/statistics/lastTrip";
                        allAPIs.add(lastTripAPI);
                        allTripsAPI = baseUrl + "/statistics/allTrips";
                        allAPIs.add(allTripsAPI);
                        chargeAPI = baseUrl + "/chargingprofile";
                        allAPIs.add(chargeAPI);
                        destinationAPI = baseUrl + "/destinations";
                        allAPIs.add(destinationAPI);
                        imageAPI = baseUrl + "/image?width=200&height=200&view=FRONT";
                        allAPIs.add(imageAPI);
                        tokenUpdate();
                        allAPIs.forEach(entry -> {
                            Request req = httpClient.newRequest(entry);
                            req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
                            req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
                            try {
                                ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
                                logger.info("Status {}", contentResponse.getStatus());
                                logger.info("Reason {}", contentResponse.getReason());
                                logger.info("{}", entry);
                                logger.info("{}", contentResponse.getContentAsString());
                            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                                logger.warn("Get Data Exception {}", e.getMessage());
                            }
                        });

                        /**
                         * REMOTE_SERVICE_STATUS_URL = VEHICLE_VIN_URL +
                         * '/serviceExecutionStatus?serviceType={service_type}'
                         * REMOTE_SERVICE_URL = VEHICLE_VIN_URL + "/executeService"
                         * VEHICLE_IMAGE_URL = VEHICLE_VIN_URL + "/image?width={width}&height={height}&view={view}"
                         * VEHICLE_POI_URL = VEHICLE_VIN_URL + '/sendpoi'
                         */

                        // String baseUrl = "https://" + bridgeHandler.getRegionServer();
                        // statusAPI = baseUrl + "/webapi/v1/user/vehicles/" + config.vin + "/status";
                        // lastTripAPI = baseUrl + "/webapi/v1/user/vehicles/" + config.vin + "/statistics/lastTrip";
                        // /webapi/v1/user/vehicles/:VIN/chargingprofile
                        // /webapi/v1/user/vehicles/:VIN/destinations
                        // /webapi/v1/user/vehicles/:VIN/statistics/allTrips
                        // /webapi/v1/user/vehicles/:VIN/rangemap
                    } else {
                        logger.warn("Brdige Handler null");
                    }
                } else {
                    logger.warn("Bridge null");
                }

                getImage();
                startSchedule(config.refreshInterval);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    /**
     * old APIS
     *
     * String baseUrl = "https://" + bridgeHandler.getRegionServer() + "/api/vehicle";
     * // https://b2vapi.bmwgroup.com/api/vehicle/dynamic/v1/
     * // from bimmer_connect project
     * // r = requests.get(self.vehicleApi+'/dynamic/v1/'+self.bmwVin+'?offset=-60',
     * vehicleAPI = baseUrl + "/dynamic/v1/" + config.vin + "?offset=-60";
     * // r = requests.post(self.vehicleApi+'/myinfo/v1', data=json.dumps(values),
     * sendMessageAPI = baseUrl + "/myinfo/v1";
     * // r = requests.get(self.vehicleApi+'/navigation/v1/'+self.bmwVin,
     * // headers=headers,allow_redirects=True)
     * navigationAPI = baseUrl + "/navigation/v1/" + config.vin;
     * // r = requests.get(self.vehicleApi+'/efficiency/v1/'+self.bmwVin,
     * // headers=headers,allow_redirects=True)
     * efficiencyAPI = baseUrl + "/efficiency/v1/" + config.vin;
     * // r = requests.post(self.vehicleApi+'/remoteservices/v1/'+self.bmwVin+'/'+command,
     * remoteControlAPI = baseUrl + "/remoteservices/v1/" + config.vin;
     * // r = requests.get(self.vehicleApi+'/remoteservices/v1/'+self.bmwVin+'/state/execution',
     * remoteExecutionAPI = baseUrl + "/remoteservices/v1/" + config.vin + "/state/execution";
     * imageAPI = "https://" + bridgeHandler.getRegionServer() + "/webapi/v1/user/vehicles/"
     * + config.vin + "/image?width=400&height=400&view=REARBIRDSEYE";
     *
     */

    private void startSchedule(int interval) {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            if (localRefreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES);
            } // else - scheduler is already running!
        } else {
            refreshJob = scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
    }

    public void getData() {
        if (!tokenUpdate()) {
            return;
        }

        Request req = httpClient.newRequest(vehicleAPI);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {}", contentResponse.getStatus());
            logger.info("Reason {}", contentResponse.getReason());
            logger.info("Vehicle {}", contentResponse.getContentAsString());
            updateStatus(ThingStatus.ONLINE);
            updateRangeStates(contentResponse.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }

        req = httpClient.newRequest(efficiencyAPI);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {}", contentResponse.getStatus());
            logger.info("Reason {}", contentResponse.getReason());
            logger.info("Efficiency {}", contentResponse.getContentAsString());
            updateEfficiencyStates(contentResponse.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }

        req = httpClient.newRequest(navigationAPI);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {}", contentResponse.getStatus());
            logger.info("Reason {}", contentResponse.getReason());
            logger.info("Navigation {}", contentResponse.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }
    }

    private void updateEfficiencyStates(@Nullable String content) {
        if (content == null) {
            logger.warn("No Efficiency Values available");
        }
        efficiencyCache = content;
        efficiencyFingerprint = content;
        if (driveTrain.equals(CarType.ELECTRIC_REX.toString()) || driveTrain.equals(CarType.ELECTRIC.toString())) {
            Efficiency eff = GSON.fromJson(content, Efficiency.class);

            if (eff.lastTripList != null) {
                eff.lastTripList.forEach(entry -> {
                    if (entry.name.equals(TripEntry.LASTTRIP_DELTA_KM)) {
                        updateState(tripDistance,
                                QuantityType.valueOf(entry.lastTrip, MetricPrefix.KILO(SIUnits.METRE)));
                    } else if (entry.name.equals(TripEntry.ACTUAL_DISTANCE_WITHOUT_CHARGING)) {
                        updateState(tripDistanceSinceCharging,
                                QuantityType.valueOf(entry.lastTrip, MetricPrefix.KILO(SIUnits.METRE)));
                    } else if (entry.name.equals(TripEntry.AVERAGE_ELECTRIC_CONSUMPTION)) {
                        updateState(tripAvgConsumption,
                                QuantityType.valueOf(entry.lastTrip, SmartHomeUnits.KILOWATT_HOUR));
                    } else if (entry.name.equals(TripEntry.AVERAGE_RECUPERATED_ENERGY_PER_100_KM)) {
                        ThingHandlerCallback thcb = super.getCallback();
                        logger.info("Callback {}", thcb);
                        updateState(tripAvgRecuperation,
                                QuantityType.valueOf(entry.lastTrip, SmartHomeUnits.KILOWATT_HOUR));
                    }
                });
            }
            if (eff.scoreList != null) {
                eff.scoreList.forEach(entry -> {
                    if (entry.attrName.equals(Score.CUMULATED_ELECTRIC_DRIVEN_DISTANCE)) {
                        updateState(lifeTimeCumulatedDrivenDistance,
                                QuantityType.valueOf(entry.lifeTime, MetricPrefix.KILO(SIUnits.METRE)));
                    } else if (entry.attrName.equals(Score.LONGEST_DISTANCE_WITHOUT_CHARGING)) {
                        updateState(lifeTimeSingleLongestDistance,
                                QuantityType.valueOf(entry.lifeTime, MetricPrefix.KILO(SIUnits.METRE)));
                    } else if (entry.attrName.equals(Score.AVERAGE_ELECTRIC_CONSUMPTION)) {
                        updateState(lifeTimeAverageConsumption,
                                QuantityType.valueOf(entry.lifeTime, SmartHomeUnits.KILOWATT_HOUR));
                    } else if (entry.attrName.equals(Score.AVERAGE_RECUPERATED_ENERGY_PER_100_KM)) {
                        // create channel on demand
                        // ChannelUID cuid = new ChannelUID(thing.getUID(), ConnectedDriveConstants.AVG_RECUPERATION);
                        // logger.info("update {} with {}", cuid, entry.lifeTime);
                        // updateState(cuid, QuantityType.valueOf(entry.lifeTime, SmartHomeUnits.KILOWATT_HOUR));
                        updateState(lifeTimeAverageRecuperation,
                                QuantityType.valueOf(entry.lifeTime, SmartHomeUnits.KILOWATT_HOUR));
                    }
                });
            }
        } else {
            logger.warn("No Efficiency values for {} supported yet", driveTrain);
        }
    }

    private void updateRangeStates(@Nullable String content) {
        if (content == null) {
            logger.warn("No Vehicle Values available");
        }
        vehicleCache = content;
        if (driveTrain.equals(CarType.ELECTRIC_REX.toString())) {
            BevRexAttributesMap data = GSON.fromJson(content, BevRexAttributesMap.class);
            BevRexAttributes bevRexAttributes = data.attributesMap;
            logger.info("Update Milage {} Channel {}", bevRexAttributes.mileage, mileage.toString());
            // based on unit of length decide if range shall be reported in km or miles
            if (bevRexAttributes.unitOfLength.equals("km")) {
                updateState(mileage, QuantityType.valueOf(bevRexAttributes.mileage, MetricPrefix.KILO(SIUnits.METRE)));
                updateState(remainingRangeElectric, QuantityType.valueOf(bevRexAttributes.beRemainingRangeElectricKm,
                        MetricPrefix.KILO(SIUnits.METRE)));
                updateState(remainingRangeFuel, QuantityType.valueOf(bevRexAttributes.beRemainingRangeFuelKm,
                        MetricPrefix.KILO(SIUnits.METRE)));
                updateState(remainingRange,
                        QuantityType.valueOf(
                                bevRexAttributes.beRemainingRangeElectricKm + bevRexAttributes.beRemainingRangeFuelKm,
                                MetricPrefix.KILO(SIUnits.METRE)));
                updateState(rangeRadius,
                        new DecimalType(
                                (bevRexAttributes.beRemainingRangeElectricKm + bevRexAttributes.beRemainingRangeFuelKm)
                                        * 1000));
            } else {
                updateState(mileage, QuantityType.valueOf(bevRexAttributes.mileage, ImperialUnits.MILE));
                updateState(remainingRangeElectric,
                        QuantityType.valueOf(bevRexAttributes.beRemainingRangeElectricMile, ImperialUnits.MILE));
                updateState(remainingRangeFuel,
                        QuantityType.valueOf(bevRexAttributes.beRemainingRangeFuelMile, ImperialUnits.MILE));
                updateState(remainingRange, QuantityType.valueOf(
                        bevRexAttributes.beRemainingRangeElectricMile + bevRexAttributes.beRemainingRangeFuelMile,
                        ImperialUnits.MILE));
                updateState(rangeRadius, new DecimalType(
                        (bevRexAttributes.beRemainingRangeElectricMile + bevRexAttributes.beRemainingRangeFuelMile)
                                * 1000));
            }
            updateState(remainingSoc, QuantityType.valueOf(bevRexAttributes.chargingLevelHv, SmartHomeUnits.PERCENT));
            updateState(remainingFuel, QuantityType.valueOf(bevRexAttributes.fuelPercent, SmartHomeUnits.PERCENT));
            updateState(lastUpdate, new StringType(bevRexAttributes.Segment_LastTrip_time_segment_end_formatted));

            updateState(longitude, new DecimalType(bevRexAttributes.gps_lng));
            updateState(latitude, new DecimalType(bevRexAttributes.gps_lat));
            updateState(latlong, new StringType(bevRexAttributes.gps_lat + "," + bevRexAttributes.gps_lng));
            logger.info("Update {} with {}", heading.toString(), bevRexAttributes.heading);
            updateState(heading, QuantityType.valueOf(bevRexAttributes.heading, SmartHomeUnits.DEGREE_ANGLE));
            // updateState(headingChannleUID, new DecimalType(bevRexAttributes.heading));

            bevRexAttributes.gps_lat = (float) 0.0;
            bevRexAttributes.gps_lng = (float) 0.0;
            vehicleFingerprint = GSON.toJson(bevRexAttributes);
        } else {
            vehicleFingerprint = content;
            logger.warn("No update of for {} which {}", driveTrain, CarType.ELECTRIC_REX.toString());
        }
    }

    public void getImage() {
        if (!tokenUpdate()) {
            return;
        }

        logger.info("Get image {}", imageAPI);
        Request req = httpClient.newRequest(imageAPI);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("getStatus {}", contentResponse.getStatus());
            logger.info("getMediaType {}", contentResponse.getMediaType());
            byte[] image = contentResponse.getContent();
            updateState(imageChannel, new RawType(image, RawType.DEFAULT_MIME_TYPE));
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }
    }

    public synchronized boolean tokenUpdate() {
        if (token.isExpired()) {
            token = bridgeHandler.getToken();
            if (token.isExpired() || !token.isValid()) {
                logger.info("Token update failed!");
                return false;
            }
        }
        return true;
    }
}
