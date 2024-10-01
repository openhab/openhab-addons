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
package org.openhab.binding.flume.internal.handler;

import static org.openhab.binding.flume.internal.FlumeBindingConstants.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flume.internal.FlumeBridgeConfig;
import org.openhab.binding.flume.internal.FlumeDeviceConfig;
import org.openhab.binding.flume.internal.actions.FlumeDeviceActions;
import org.openhab.binding.flume.internal.api.FlumeApi;
import org.openhab.binding.flume.internal.api.FlumeApiException;
import org.openhab.binding.flume.internal.api.dto.FlumeApiCurrentFlowRate;
import org.openhab.binding.flume.internal.api.dto.FlumeApiDevice;
import org.openhab.binding.flume.internal.api.dto.FlumeApiQueryBucket;
import org.openhab.binding.flume.internal.api.dto.FlumeApiQueryWaterUsage;
import org.openhab.binding.flume.internal.api.dto.FlumeApiUsageAlert;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FlumeDeviceHandler} is the implementation the flume meter device.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(FlumeDeviceHandler.class);

    // private final static String beginDateUsage = "2016-01-01 00:00:00";
    private static final LocalDateTime BEGIN_DATE_USAGE = LocalDateTime.of(2016, 1, 1, 0, 0, 0);
    private static final String QUERY_ID_CUMULATIVE_START_OF_YEAR = "cumulativeStartOfYear";
    private static final String QUERY_ID_YEAR_TO_DATE = "usageYTD";

    private ExpiringCache<FlumeApiDevice> apiDeviceCache = new ExpiringCache<FlumeApiDevice>(
            Duration.ofMinutes(5).toMillis(), this::getDeviceInfo);

    private FlumeDeviceConfig config = new FlumeDeviceConfig();

    private float cumulativeStartOfYear = 0;

    private float cumulativeUsage = 0;
    private long expiryCumulativeUsage = 0;
    private Duration refreshIntervalCumulative = Duration.ofMinutes(DEFAULT_POLLING_INTERVAL_CUMULATIVE);

    private float instantUsage = 0;
    private long expiryInstantUsage = 0;
    private Duration refreshIntervalInstant = Duration.ofMinutes(DEFAULT_POLLING_INTERVAL_INSTANTANEOUS);

    private LocalDateTime startOfYear = LocalDateTime.MIN;

    private Instant lastUsageAlert = Instant.MIN;

    private static final Duration USAGE_QUERY_FETCH_INTERVAL = Duration.ofMinutes(5);
    private long expiryUsageAlertFetch = 0;

    public FlumeDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(FlumeDeviceConfig.class);

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::goOnline);
    }

    public void goOnline() {
        if (this.getThing().getStatus() == ThingStatus.ONLINE) {
            return;
        }

        FlumeBridgeHandler bh = getBridgeHandler();

        if (bh == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return;
        }

        if (bh.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        FlumeBridgeConfig bridgeConfig = bh.getFlumeBridgeConfig();

        refreshIntervalCumulative = Duration.ofMinutes(bridgeConfig.refreshIntervalCumulative);
        refreshIntervalInstant = Duration.ofMinutes(bridgeConfig.refreshIntervalInstantaneous);

        // always update the startOfYear number;
        startOfYear = LocalDateTime.MIN;

        FlumeApiDevice apiDevice = apiDeviceCache.getValue();
        if (apiDevice != null) {
            updateDeviceInfo(apiDevice);
        }

        try {
            tryQueryUsage(true);
            tryGetCurrentFlowRate(true);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            handleApiException(e);
        }

        lastUsageAlert = Instant.now(); // don't retrieve any usage alerts prior to going online
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            goOnline();
        }
    }

    /**
     * Get the services registered for this bridge. Provides the discovery service.
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(FlumeDeviceActions.class);
    }

    public String getId() {
        return config.id;
    }

    public void updateDeviceChannel(@Nullable FlumeApiDevice apiDevice, String channelUID) {
        final Map<String, Integer> mapBatteryLevel = Map.of("low", 25, "medium", 50, "high", 100);
        if (apiDevice == null) {
            return;
        }

        Integer percent = mapBatteryLevel.get(apiDevice.batteryLevel);
        if (percent == null) {
            return;
        }

        switch (channelUID) {
            case CHANNEL_DEVICE_BATTERYLEVEL:
                updateState(CHANNEL_DEVICE_BATTERYLEVEL, new QuantityType<>(percent, Units.PERCENT));
                break;
            case CHANNEL_DEVICE_LOWBATTERY:
                updateState(CHANNEL_DEVICE_LOWBATTERY, (percent <= 25) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_DEVICE_LASTSEEN:
                updateState(CHANNEL_DEVICE_LASTSEEN,
                        new DateTimeType(ZonedDateTime.ofInstant(apiDevice.lastSeen, ZoneId.systemDefault())));
                break;
        }
    }

    public void handleApiException(Exception e) {
        if (e instanceof FlumeApiException flumeApiException) {
            if (flumeApiException.isConfigurationIssue()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        flumeApiException.getLocalizedMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        flumeApiException.getLocalizedMessage());
            }
        } else if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof InterruptedIOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof InterruptedException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof TimeoutException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else if (e instanceof ExecutionException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } else {
            // capture in log since this is an unexpected exception
            logger.warn("Unhandled Exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.getLocalizedMessage());
        }
    }

    /**
     * Pools together several usage queries based on whether the value is expired and whether a channel is linked. Also,
     * if necessary will update the usage from beginning to start of year so subsequent cumulative queries only need to
     * ytd. Will update the values in the ExpiringCache as necessary.
     *
     * @throws FlumeApiException
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    protected void tryQueryUsage(boolean forceUpdate)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        @Nullable
        List<HashMap<String, List<FlumeApiQueryBucket>>> result;

        boolean imperialUnits = isImperial();

        LocalDateTime now = LocalDateTime.now();

        List<FlumeApiQueryWaterUsage> listQuery = new ArrayList<FlumeApiQueryWaterUsage>();

        // Get sum of all historical readings only when binding starts or its the start of a new year
        // This is to reduce the time it takes on the periodic queries
        if (startOfYear.equals(LocalDateTime.MIN) || (now.getYear() != startOfYear.getYear())) {
            FlumeApiQueryWaterUsage query = new FlumeApiQueryWaterUsage();

            query.bucket = FlumeApi.BucketType.YR;
            query.sinceDateTime = BEGIN_DATE_USAGE;
            query.untilDateTime = now.minusYears(1);
            query.groupMultiplier = 100;
            query.operation = FlumeApi.OperationType.SUM;
            query.requestId = QUERY_ID_CUMULATIVE_START_OF_YEAR;
            query.units = imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS;

            listQuery.add(query);
        }

        if (System.nanoTime() > this.expiryUsageAlertFetch) {
            fetchUsageAlerts();
            this.expiryUsageAlertFetch = System.nanoTime() + USAGE_QUERY_FETCH_INTERVAL.toNanos();
        }

        if (this.isLinked(CHANNEL_DEVICE_CUMULATIVEUSAGE)
                && ((System.nanoTime() > this.expiryCumulativeUsage) || forceUpdate)) {
            FlumeApiQueryWaterUsage query = new FlumeApiQueryWaterUsage();

            query.bucket = FlumeApi.BucketType.DAY;
            query.untilDateTime = now;
            query.sinceDateTime = now.withDayOfYear(1);
            query.groupMultiplier = 400;
            query.operation = FlumeApi.OperationType.SUM;
            query.requestId = QUERY_ID_YEAR_TO_DATE;
            query.units = imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS;

            listQuery.add(query);
        }

        if (listQuery.isEmpty()) {
            return;
        }

        result = getApi().queryUsage(config.id, listQuery);

        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.cloud-connection-issue");
            return;
        }

        Map<String, List<FlumeApiQueryBucket>> queryData = result.get(0);
        List<FlumeApiQueryBucket> queryBuckets;

        queryBuckets = queryData.get(QUERY_ID_CUMULATIVE_START_OF_YEAR);
        if (queryBuckets != null) {
            cumulativeStartOfYear = queryBuckets.get(0).value;
            startOfYear = now.withDayOfYear(1);
        }

        queryBuckets = queryData.get(QUERY_ID_YEAR_TO_DATE);
        if (queryBuckets != null) {
            cumulativeUsage = queryBuckets.get(0).value + cumulativeStartOfYear;
            updateState(CHANNEL_DEVICE_CUMULATIVEUSAGE,
                    new QuantityType<>(cumulativeUsage, imperialUnits ? ImperialUnits.GALLON_LIQUID_US : Units.LITRE));
            this.expiryCumulativeUsage = System.nanoTime() + this.refreshIntervalCumulative.toNanos();
        }
    }

    protected void tryGetCurrentFlowRate(boolean forceUpdate)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        if (this.isLinked(CHANNEL_DEVICE_INSTANTUSAGE)
                && ((System.nanoTime() > this.expiryInstantUsage) || forceUpdate)) {
            FlumeApiCurrentFlowRate currentFlowRate = getApi().getCurrentFlowRate(config.id);
            if (currentFlowRate == null) {
                return;
            }

            instantUsage = currentFlowRate.gpm;
            updateState(CHANNEL_DEVICE_INSTANTUSAGE, new QuantityType<>(instantUsage, ImperialUnits.GALLON_PER_MINUTE));
            this.expiryInstantUsage = System.nanoTime() + this.refreshIntervalInstant.toNanos();
        }
    }

    protected @Nullable FlumeApiDevice tryGetDeviceInfo()
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        FlumeApiDevice deviceInfo = getApi().getDeviceInfo(config.id);
        if (deviceInfo == null) {
            return null;
        }

        return deviceInfo;
    }

    protected @Nullable FlumeApiDevice getDeviceInfo() {
        try {
            return this.tryGetDeviceInfo();
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            handleApiException(e);
            return null;
        }
    }

    protected void queryUsage() {
        // Try to go online if the device was previously taken offline due to connection issues w/ cloud
        if (getThing().getStatus() == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            goOnline();
            return;
        }

        try {
            tryQueryUsage(false);
            tryGetCurrentFlowRate(false);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            this.handleApiException(e);
            return;
        }

        if (System.nanoTime() > this.expiryUsageAlertFetch) {
            fetchUsageAlerts();
            this.expiryUsageAlertFetch = System.nanoTime() + USAGE_QUERY_FETCH_INTERVAL.toNanos();
        }
    }

    public void fetchUsageAlerts() {
        List<FlumeApiUsageAlert> resultList;
        FlumeApiUsageAlert alert;
        FlumeApiQueryWaterUsage query;

        boolean imperialUnits = isImperial();

        try {
            resultList = getApi().fetchUsageAlerts(config.id, 1);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            this.handleApiException(e);
            return;
        }

        if (resultList.isEmpty()) {
            return;
        }

        alert = resultList.get(0);
        // alert has already been notified or occurred before the device went online
        if (!alert.triggeredDateTime.isAfter(this.lastUsageAlert)) {
            logger.trace("alert: {}, lastUsageAlert: {}", alert.triggeredDateTime, this.lastUsageAlert);
            return;
        }

        lastUsageAlert = alert.triggeredDateTime;

        String stringAlertFormat = Objects.requireNonNull(getBridgeHandler())
                .getLocaleString("trigger.high-flow-alert");
        if (stringAlertFormat == null) {
            return;
        }

        query = alert.query;
        query.bucket = FlumeApi.BucketType.MIN;
        query.operation = FlumeApi.OperationType.AVG;
        query.units = imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS;

        Float avgUsage;
        try {
            avgUsage = getApi().queryUsage(config.id, query);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            this.handleApiException(e);
            return;
        }
        long minutes = Duration.between(query.sinceDateTime, query.untilDateTime).toMinutes();

        LocalDateTime localWhenTriggered = LocalDateTime.ofInstant(alert.triggeredDateTime, ZoneId.systemDefault());

        String stringAlert = String.format(stringAlertFormat, alert.eventRuleName, localWhenTriggered.toString(),
                minutes, avgUsage, imperialUnits ? "gallons" : "liters");

        triggerChannel(CHANNEL_DEVICE_USAGEALERT, stringAlert);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_DEVICE_CUMULATIVEUSAGE:
                    try {
                        tryQueryUsage(true);
                    } catch (FlumeApiException | IOException | InterruptedException | TimeoutException
                            | ExecutionException e) {
                        handleApiException(e);
                        return;
                    }

                    break;
                case CHANNEL_DEVICE_INSTANTUSAGE:
                    try {
                        tryGetCurrentFlowRate(true);
                    } catch (FlumeApiException | IOException | InterruptedException | TimeoutException
                            | ExecutionException e) {
                        handleApiException(e);
                        return;
                    }
                    break;
                case CHANNEL_DEVICE_BATTERYLEVEL:
                    updateDeviceChannel(apiDeviceCache.getValue(), CHANNEL_DEVICE_BATTERYLEVEL);
                    break;
                case CHANNEL_DEVICE_LOWBATTERY:
                    updateDeviceChannel(apiDeviceCache.getValue(), CHANNEL_DEVICE_LOWBATTERY);
                    break;
                case CHANNEL_DEVICE_LASTSEEN:
                    updateDeviceChannel(apiDeviceCache.getValue(), CHANNEL_DEVICE_LASTSEEN);
                    break;
            }
        }
    }

    public void updateDeviceInfo(FlumeApiDevice apiDevice) {
        apiDeviceCache.putValue(apiDevice);

        updateDeviceChannel(apiDevice, CHANNEL_DEVICE_BATTERYLEVEL);
        updateDeviceChannel(apiDevice, CHANNEL_DEVICE_LOWBATTERY);
        updateDeviceChannel(apiDevice, CHANNEL_DEVICE_LASTSEEN);
    }

    public boolean isImperial() {
        return Objects.requireNonNull(getBridgeHandler()).systemOfUnits instanceof ImperialUnits;
    }

    public @Nullable FlumeBridgeHandler getBridgeHandler() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            return null;
        }

        if (bridge.getHandler() instanceof FlumeBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }

        return null;
    }

    public FlumeApi getApi() {
        Bridge bridge = Objects.requireNonNull(getBridge());
        BridgeHandler handler = Objects.requireNonNull(bridge.getHandler());

        return ((FlumeBridgeHandler) handler).getApi();
    }
}
