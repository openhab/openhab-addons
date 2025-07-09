/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.spi.SystemOfUnits;

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
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
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

    private static final int HISTORICAL_YEAR_START = 2018;
    private static final String QUERY_ID_HISTORICAL_BY_YEAR = "CUMULATIVE_START_OF_YEAR";
    private static final String QUERY_ID_USAGE_FROM_BASELINE = "USAGE_FROM_BASELINE";

    private ExpiringCache<FlumeApiDevice> apiDeviceCache = new ExpiringCache<FlumeApiDevice>(
            Duration.ofMinutes(5).toMillis(), this::getDeviceInfo);

    public record CumulativeStore(LocalDateTime lastUpdate, float value, FlumeApi.UnitType unit) {
    }

    private CumulativeStore cumulativeStore;

    private FlumeDeviceConfig config = new FlumeDeviceConfig();

    private long expiryCumulativeUsage = 0;
    private Duration refreshIntervalCumulative = Duration.ofMinutes(DEFAULT_POLLING_INTERVAL_CUMULATIVE_MIN);

    private float instantUsage = 0;
    private long expiryInstantUsage = 0;
    private Duration refreshIntervalInstant = Duration.ofMinutes(DEFAULT_POLLING_INTERVAL_INSTANTANEOUS_MIN);

    private Instant lastUsageAlert = Instant.MIN;

    private long expiryUsageAlertFetch = 0;
    private static final Duration USAGE_ALERT_FETCH_INTERVAL = Duration.ofMinutes(5);

    private final Storage<CumulativeStore> storage;
    private static final String STORAGE_KEY_CUMULATIVE_USAGE = "CumulativeUsage";
    private final boolean imperialUnits;

    public FlumeDeviceHandler(Thing thing, final SystemOfUnits systemOfUnits, final StorageService storageService) {
        super(thing);
        this.imperialUnits = systemOfUnits instanceof ImperialUnits;
        this.storage = storageService.getStorage(thing.getUID().toString(), CumulativeStore.class.getClassLoader());

        CumulativeStore lCumulativeStore = storage.get(STORAGE_KEY_CUMULATIVE_USAGE);

        if (lCumulativeStore == null || !lCumulativeStore.unit()
                .equals(imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS)) {
            lCumulativeStore = new CumulativeStore(LocalDateTime.MIN, 0,
                    imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS);
        }
        this.cumulativeStore = lCumulativeStore;
    }

    @Override
    public void initialize() {
        config = getConfigAs(FlumeDeviceConfig.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::goOnline);
    }

    public synchronized void goOnline() {
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

        FlumeApiDevice apiDevice = apiDeviceCache.getValue();
        if (apiDevice != null) {
            updateDeviceInfo(apiDevice);
        }

        lastUsageAlert = Instant.now(); // don't retrieve any usage alerts prior to going online

        try {
            tryFetchUsageAlerts(true);
            tryQueryUsage(true);
            tryGetCurrentFlowRate(true);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            handleApiException(e);
        }

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

    public boolean isImperial() {
        return imperialUnits;
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
                updateState(CHANNEL_DEVICE_LASTSEEN, new DateTimeType(apiDevice.lastSeen));
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
     * {@link tryUpdateCumulativeStore} will query annual usage from HISTORICAL_YEAR_START to the end of last year.
     * Flume
     * limits queries to a duration of 1 year, so these must be broken up to individual queries. The result is stored
     * so that subsequent startups don't have to requery.
     */
    protected void tryUpdateCumulativeStore()
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        @Nullable
        List<HashMap<String, List<FlumeApiQueryBucket>>> result;
        List<FlumeApiQueryWaterUsage> listQuery = new ArrayList<FlumeApiQueryWaterUsage>();
        LocalDateTime now = LocalDateTime.now();

        // Get sum of all historical full year readings only when binding starts or its the start of a new year
        // This is to reduce the time it takes on the periodic queries.
        boolean updateBaselineYears = cumulativeStore.lastUpdate() == LocalDateTime.MIN
                || Duration.between(now, cumulativeStore.lastUpdate()).toDays() > 365;
        if (updateBaselineYears) {
            for (int year = HISTORICAL_YEAR_START; year < now.getYear(); year++) {
                listQuery.add(new FlumeApiQueryWaterUsage( //
                        QUERY_ID_HISTORICAL_BY_YEAR + year, //
                        LocalDateTime.of(year, 1, 1, 0, 0, 0), //
                        LocalDateTime.of(year, 12, 31, 23, 59, 59), //
                        FlumeApi.BucketType.YR, //
                        1, //
                        FlumeApi.OperationType.SUM, //
                        imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS, //
                        FlumeApi.SortDirectionType.ASC //
                ));
            }
        }

        // Get the total usage for complete months since lastUpdate OR beginning of the year
        // (note, flume returns the full month usage regardless of the time, hence the need to query a full complete
        // month
        LocalDateTime fromDateTime = (updateBaselineYears) ? LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0)
                : cumulativeStore.lastUpdate();
        LocalDateTime toDateTime = now.withDayOfMonth(1).minusDays(1);

        // Add query for current year up to end of last month. Note, the max multiplier is 100, so we have to use Months
        // vs. Days as the
        // minimum granularity since if Days was used the query could be for a total of 365 days
        listQuery.add(new FlumeApiQueryWaterUsage( //
                QUERY_ID_HISTORICAL_BY_YEAR + "MONTH", //
                fromDateTime, //
                toDateTime, //
                FlumeApi.BucketType.MON, //
                99, // max of 100
                FlumeApi.OperationType.SUM, //
                imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS, //
                FlumeApi.SortDirectionType.ASC //
        ));

        result = getApi().queryUsage(config.id, listQuery);

        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.cloud-connection-issue");
            return;
        }

        Map<String, List<FlumeApiQueryBucket>> queryData = result.get(0);
        float cumulativeUsage = (float) queryData.values().stream().map((bucket) -> bucket.get(0).value)
                .mapToDouble(Float::doubleValue).sum();

        this.cumulativeStore = new CumulativeStore(LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0),
                cumulativeUsage, imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS);
        storage.put(STORAGE_KEY_CUMULATIVE_USAGE, cumulativeStore);
    }

    /**
     * Queries usage from baseline to now if expired and channel is linked. Will update the falues in ExpiringCache as
     * necessary
     *
     * @throws FlumeApiException
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    protected void tryQueryUsage(boolean forceUpdate)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        if (System.nanoTime() <= this.expiryCumulativeUsage && !forceUpdate) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // update cumulativeStore if no storage exists (first time), or we are at the start of a new month
        if (cumulativeStore.lastUpdate() == LocalDateTime.MIN
                || LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0).isAfter(cumulativeStore.lastUpdate())) {
            tryUpdateCumulativeStore();
        }

        if (!this.isLinked(CHANNEL_DEVICE_CUMULATIVEUSAGE)) {
            return;
        }

        Float result = getApi().queryUsage(config.id, //
                new FlumeApiQueryWaterUsage(QUERY_ID_USAGE_FROM_BASELINE, //
                        cumulativeStore.lastUpdate(), //
                        now, //
                        FlumeApi.BucketType.MON, //
                        1, //
                        FlumeApi.OperationType.SUM, //
                        imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS, //
                        FlumeApi.SortDirectionType.ASC //
                ));

        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.cloud-connection-issue");
            return;
        }

        updateState(CHANNEL_DEVICE_CUMULATIVEUSAGE, new QuantityType<>(cumulativeStore.value() + result,
                imperialUnits ? ImperialUnits.GALLON_LIQUID_US : Units.LITRE));
        this.expiryCumulativeUsage = System.nanoTime() + this.refreshIntervalCumulative.toNanos();
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
            tryFetchUsageAlerts(false);
            tryQueryUsage(false);
            tryGetCurrentFlowRate(false);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            this.handleApiException(e);
            return;
        }
    }

    public void tryFetchUsageAlerts(boolean forceUpdate)
            throws FlumeApiException, IOException, InterruptedException, TimeoutException, ExecutionException {
        List<FlumeApiUsageAlert> resultList;
        FlumeApiUsageAlert alert;
        FlumeApiQueryWaterUsage query;

        if (System.nanoTime() <= this.expiryUsageAlertFetch && !forceUpdate) {
            return;
        }

        resultList = getApi().fetchUsageAlerts(config.id, 1);
        this.expiryUsageAlertFetch = System.nanoTime() + USAGE_ALERT_FETCH_INTERVAL.toNanos();

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

        query = new FlumeApiQueryWaterUsage( //
                alert.query.requestId(), //
                alert.query.sinceDateTime(), //
                alert.query.untilDateTime(), //
                alert.query.bucket(), //
                alert.query.groupMultiplier(), //
                FlumeApi.OperationType.AVG, //
                imperialUnits ? FlumeApi.UnitType.GALLONS : FlumeApi.UnitType.LITERS, //
                alert.query.sortDirection() //
        );
        logger.debug("Alert query: {}", query);

        Float avgUsage;
        try {
            avgUsage = getApi().queryUsage(config.id, query);
        } catch (FlumeApiException | IOException | InterruptedException | TimeoutException | ExecutionException e) {
            this.handleApiException(e);
            return;
        }
        long minutes = Duration.between(query.sinceDateTime(), query.untilDateTime()).toMinutes();

        LocalDateTime localWhenTriggered = LocalDateTime.ofInstant(alert.triggeredDateTime, ZoneId.systemDefault());

        String stringAlert = String.format(stringAlertFormat, alert.eventRuleName, localWhenTriggered.toString(),
                minutes, avgUsage, imperialUnits ? "gallons" : "liters");

        logger.debug("Alert: {}", stringAlert);
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
