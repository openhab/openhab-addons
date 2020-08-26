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

import java.lang.reflect.Field;
import java.time.LocalDate;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedCarConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.CarType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.Community;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.Trip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CBSMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CCMMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Doors;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Status;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Windows;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
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
    private final static Gson GSON = new Gson();

    private String driveTrain;
    private HttpClient httpClient;
    private Token token = new Token();
    private boolean imperial = false;
    private boolean hasFuel = false;
    private boolean isElectric = false;
    private boolean isHybrid = false;

    private @Nullable ConnectedCarConfiguration configuration;
    private @Nullable ConnectedDriveBridgeHandler bridgeHandler;
    protected @Nullable ScheduledFuture<?> refreshJob;

    private @Nullable String vehicleFingerprint;
    private @Nullable String efficiencyFingerprint;

    // Connected Drive APIs
    private @Nullable String vehicleAPI;
    private @Nullable String imageAPI;
    private @Nullable String lastTripAPI;
    private @Nullable String allTripsAPI;
    private @Nullable String chargeAPI;
    private @Nullable String destinationAPI;

    private @Nullable String vehicleStatusCache;
    private @Nullable String lastTripCache;
    private @Nullable String allTripsCache;

    public ConnectedCarHandler(Thing thing, HttpClient hc, String type, boolean imperial) {
        super(thing);
        httpClient = hc;
        driveTrain = type;
        this.imperial = imperial;
        hasFuel = type.equals(CarType.CONVENTIONAL.toString()) || type.equals(CarType.PLUGIN_HYBRID.toString())
                || type.equals(CarType.ELECTRIC_REX.toString());
        isElectric = type.equals(CarType.PLUGIN_HYBRID.toString()) || type.equals(CarType.ELECTRIC_REX.toString())
                || type.equals(CarType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        logger.info("DriveTrain {} isElectric {} hasFuel {} Imperial {}", type, isElectric, hasFuel, imperial);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String group = channelUID.getGroupId();
            if (CHANNEL_GROUP_LAST_TRIP.equals(group)) {
                if (lastTripCache != null) {
                    updateLastTrip(lastTripCache);
                }
            } else if (CHANNEL_GROUP_LIFETIME.equals(group)) {
                if (allTripsCache != null) {
                    updateTripStatistics(allTripsCache);
                }
            } else {
                if (vehicleStatusCache != null) {
                    updateRangeValues(vehicleStatusCache);
                }
            }
            logger.info("REFRESH {}", group);
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
        configuration = config;
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
                        lastTripAPI = baseUrl + "/statistics/lastTrip";
                        allTripsAPI = baseUrl + "/statistics/allTrips";
                        chargeAPI = baseUrl + "/chargingprofile";
                        destinationAPI = baseUrl + "/destinations";
                        imageAPI = baseUrl + "/image";
                        // allAPIs.forEach(entry -> {
                        // Request req = httpClient.newRequest(entry);
                        // req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
                        // req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
                        // try {
                        // ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
                        // logger.info("Status {}", contentResponse.getStatus());
                        // logger.info("Reason {}", contentResponse.getReason());
                        // logger.info("{}", entry);
                        // logger.info("{}", contentResponse.getContentAsString());
                        // } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        // logger.warn("Get Data Exception {}", e.getMessage());
                        // }
                        // });

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
        String vehicleStatusData = getJSON(vehicleAPI);
        updateVehicleStatus(vehicleStatusData);
        updateRangeValues(vehicleStatusData);
        String lastTripData = getJSON(lastTripAPI);
        updateLastTrip(lastTripData);
        String allTripData = getJSON(allTripsAPI);
        updateTripStatistics(allTripData);
        String chargeData = getJSON(chargeAPI);
        String destinationData = getJSON(destinationAPI);
    }

    public @Nullable String getJSON(@Nullable String url) {
        if (url == null) {
            return null;
        }
        Request req = httpClient.newRequest(url);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            if (contentResponse.getStatus() != 200) {
                logger.info("URL {}", url);
                logger.info("Status {}", contentResponse.getStatus());
                logger.info("Reason {}", contentResponse.getReason());
            } else {
                updateStatus(ThingStatus.ONLINE);
                return contentResponse.getContentAsString();
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }
        return null;
    }

    private void updateTripStatistics(@Nullable String content) {
        if (content == null) {
            logger.warn("No Vehicle Values available");
            return;
        }
        allTripsCache = content;
        AllTrips at = GSON.fromJson(content, AllTrips.class);
        Community c = at.allTrips;
        updateState(lifeTimeCumulatedDrivenDistance, QuantityType
                .valueOf(Converter.round(c.totalElectricDistance.userTotal), MetricPrefix.KILO(SIUnits.METRE)));
        updateState(lifeTimeSingleLongestDistance,
                QuantityType.valueOf(Converter.round(c.chargecycleRange.userHigh), MetricPrefix.KILO(SIUnits.METRE)));
        updateState(lifeTimeAverageConsumption, QuantityType
                .valueOf(Converter.round(c.avgElectricConsumption.userAverage), SmartHomeUnits.KILOWATT_HOUR));
        updateState(lifeTimeAverageRecuperation,
                QuantityType.valueOf(Converter.round(c.avgRecuperation.userAverage), SmartHomeUnits.KILOWATT_HOUR));
        updateState(tripDistanceSinceCharging, QuantityType
                .valueOf(Converter.round(c.chargecycleRange.userCurrentChargeCycle), MetricPrefix.KILO(SIUnits.METRE)));
    }

    private void updateLastTrip(@Nullable String content) {
        if (content == null) {
            logger.warn("No Vehicle Values available");
            return;
        }
        lastTripCache = content;
        LastTrip lt = GSON.fromJson(content, LastTrip.class);
        Trip trip = lt.lastTrip;
        updateState(tripDistance,
                QuantityType.valueOf(Converter.round(trip.totalDistance), MetricPrefix.KILO(SIUnits.METRE)));
        // updateState(tripDistanceSinceCharging,
        // QuantityType.valueOf(entry.lastTrip, MetricPrefix.KILO(SIUnits.METRE)));
        updateState(tripAvgConsumption,
                QuantityType.valueOf(Converter.round(trip.avgElectricConsumption), SmartHomeUnits.KILOWATT_HOUR));
        updateState(tripAvgRecuperation,
                QuantityType.valueOf(Converter.round(trip.avgRecuperation), SmartHomeUnits.KILOWATT_HOUR));
    }

    void updateVehicleStatus(@Nullable String content) {
        if (content == null) {
            logger.warn("No Vehicle Values available");
            return;
        }
        logger.info("Vehicle Status {}", content);
        vehicleStatusCache = content;
        Status status = GSON.fromJson(content, Status.class);
        VehicleStatus vStatus = status.vehicleStatus;

        updateState(lock, StringType.valueOf(Converter.toTitleCase(vStatus.doorLockState)));
        Doors doorState = GSON.fromJson(GSON.toJson(vStatus), Doors.class);
        updateState(doors, StringType.valueOf(checkClosed(doorState)));
        Windows windowState = GSON.fromJson(GSON.toJson(vStatus), Windows.class);
        updateState(windows, StringType.valueOf(checkClosed(windowState)));
        updateState(checkControl, StringType.valueOf(getCheckControl(vStatus.checkControlMessages)));
        updateState(service, StringType.valueOf(getNextService(vStatus.cbsData)));
        if (isElectric) {
            updateState(chargingStatus, StringType.valueOf(Converter.toTitleCase(vStatus.chargingStatus)));
        }
    }

    private @Nullable String getNextService(List<CBSMessage> cbsData) {
        if (cbsData.isEmpty()) {
            return "No Service Requests";
        } else {
            LocalDate serviceDate = null;
            String service = null;
            for (int i = 0; i < cbsData.size(); i++) {
                CBSMessage entry = cbsData.get(i);
                LocalDate d = LocalDate.parse(entry.cbsDueDate + "-01", Converter.serviceDateInputPattern);
                if (serviceDate == null) {
                    serviceDate = d;
                    service = entry.cbsType;
                } else {
                    if (d.isBefore(serviceDate)) {
                        serviceDate = d;
                    }
                }
            }
            if (serviceDate != null) {
                return serviceDate.format(Converter.serviceDateOutputPattern) + " " + Converter.toTitleCase(service);
            } else {
                return "Unknown";
            }
        }
    }

    private @Nullable String getCheckControl(List<CCMMessage> checkControlMessages) {
        if (checkControlMessages.isEmpty()) {
            return "Ok";
        } else {
            return Converter.toTitleCase(checkControlMessages.get(0).ccmDescriptionShort);
        }
    }

    String checkClosed(Object dto) {
        for (Field field : dto.getClass().getDeclaredFields()) {
            try {
                if (field.get(dto).equals("OPEN")) {
                    // report the first door which is still open
                    return Converter.toTitleCase(field.getName() + " Open");
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.warn("Fields for {} Object not accesible", dto);
                return "Unknown";
            }
        }
        return "Closed";
    }

    void updateRangeValues(@Nullable String content) {
        if (content == null) {
            logger.warn("No Vehicle Values available");
            return;
        }
        Status status = GSON.fromJson(content, Status.class);
        VehicleStatus vStatus = status.vehicleStatus;
        // based on unit of length decide if range shall be reported in km or miles
        if (!imperial) {
            updateState(mileage, QuantityType.valueOf(vStatus.mileage, MetricPrefix.KILO(SIUnits.METRE)));
            float totalRange = 0;
            if (isElectric) {
                totalRange += vStatus.remainingRangeElectric;
                updateState(remainingRangeElectric,
                        QuantityType.valueOf(vStatus.remainingRangeElectric, MetricPrefix.KILO(SIUnits.METRE)));
                logger.info("updated {} {}", remainingRangeElectric, vStatus.remainingRangeElectric);
            } else {
                logger.info("{} not updated", remainingRangeElectric);
            }
            if (hasFuel) {
                totalRange += vStatus.remainingRangeFuel;
                updateState(remainingRangeFuel,
                        QuantityType.valueOf(vStatus.remainingRangeFuel, MetricPrefix.KILO(SIUnits.METRE)));
                logger.info("updated {} {}", remainingRangeFuel, vStatus.remainingRangeFuel);
            } else {
                logger.info("{} not updated", remainingRangeFuel);
            }
            if (isHybrid) {
                updateState(remainingRangeHybrid,
                        QuantityType.valueOf(Converter.round(totalRange), MetricPrefix.KILO(SIUnits.METRE)));
            }
            updateState(rangeRadius, new DecimalType((totalRange) * 1000));
        } else {
            updateState(mileage, QuantityType.valueOf(vStatus.mileage, ImperialUnits.MILE));
            float totalRange = 0;
            if (isElectric) {
                totalRange += vStatus.remainingRangeElectricMls;
                updateState(remainingRangeElectric,
                        QuantityType.valueOf(vStatus.remainingRangeElectricMls, ImperialUnits.MILE));
            }
            if (hasFuel) {
                totalRange = vStatus.remainingRangeFuelMls;
                updateState(remainingRangeFuel,
                        QuantityType.valueOf(vStatus.remainingRangeFuelMls, ImperialUnits.MILE));
            }
            if (isHybrid) {
                updateState(remainingRangeHybrid,
                        QuantityType.valueOf(Converter.round(totalRange), ImperialUnits.MILE));
            }
            updateState(rangeRadius, new DecimalType((totalRange) * 5280)); // Miles to feet
        }
        if (isElectric) {
            updateState(remainingSoc, QuantityType.valueOf(vStatus.chargingLevelHv, SmartHomeUnits.PERCENT));
        }
        if (hasFuel) {
            updateState(remainingFuel,
                    QuantityType.valueOf(vStatus.remainingFuel * 100 / vStatus.maxFuel, SmartHomeUnits.PERCENT));
            logger.info("updated {} {}", remainingFuel, vStatus.remainingFuel * 100 / vStatus.maxFuel);
        } else {
            logger.info("{} not updated", remainingRangeFuel);
        }

        updateState(lastUpdate, new StringType(Converter.getLocalDateTime(vStatus.internalDataTimeUTC)));

        Position p = vStatus.position;
        updateState(latitude, new DecimalType(p.lat));
        updateState(longitude, new DecimalType(p.lon));
        updateState(latlong, new StringType(p.lat + "," + p.lon));
        updateState(heading, QuantityType.valueOf(p.heading, SmartHomeUnits.DEGREE_ANGLE));

        vehicleFingerprint = GSON.toJson(vStatus);
    }

    public void getImage() {
        if (!tokenUpdate()) {
            logger.warn("Car image Authorization failed");
            return;
        }
        ConnectedCarConfiguration localConfig = configuration;
        if (localConfig == null) {
            logger.warn("Car image cannot be retrieved without config data");
            return;
        }

        String localImageUrl = imageAPI + "?width=" + localConfig.imageSize + "&height=" + localConfig.imageSize
                + "&view=" + localConfig.imageViewport;
        Request req = httpClient.newRequest(localImageUrl);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            if (contentResponse.getStatus() != 200) {
                logger.info("URL {}", localImageUrl);
                logger.info("Status {}", contentResponse.getStatus());
                logger.info("Reason {}", contentResponse.getReason());
            } else {
                byte[] image = contentResponse.getContent();
                String contentType = HttpUtil.guessContentTypeFromData(image);
                logger.info("Image Content Type {} Size {}", contentType, image.length);
                updateState(imageChannel, new RawType(image, contentType));
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
        }
    }

    public synchronized boolean tokenUpdate() {
        if (token.isExpired() || !token.isValid()) {
            token = bridgeHandler.getToken();
            if (token.isExpired() || !token.isValid()) {
                logger.info("Token update failed!");
                return false;
            }
        }
        return true;
    }
}
