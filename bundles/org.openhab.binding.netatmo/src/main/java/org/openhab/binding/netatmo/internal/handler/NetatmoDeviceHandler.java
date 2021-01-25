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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.VENDOR;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.ConnectionStatus;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.ModuleType.RefreshPolicy;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureLimit;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.config.MeasureChannelConfig;
import org.openhab.binding.netatmo.internal.config.NetatmoThingConfiguration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoDeviceHandler} is the abstract class that handles
 * common behaviors of all netatmo bridges (devices)
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
@NonNullByDefault
public class NetatmoDeviceHandler extends BaseBridgeHandler implements ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(NetatmoDeviceHandler.class);

    public final Map<String, NetatmoDeviceHandler> dataListeners = new ConcurrentHashMap<>();

    protected final List<AbstractChannelHelper> channelHelpers;
    protected final ZoneId zoneId;
    protected final NetatmoDescriptionProvider descriptionProvider;
    protected final Optional<MeasuresChannelHelper> measureChannelHelper;
    protected final ApiBridge apiBridge;

    protected @Nullable NAThing naThing;
    protected @NonNullByDefault({}) NetatmoThingConfiguration config;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable RefreshStrategy refreshStrategy;

    public NetatmoDeviceHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NetatmoDescriptionProvider descriptionProvider) {
        super(bridge);
        this.apiBridge = apiBridge;
        this.descriptionProvider = descriptionProvider;
        this.channelHelpers = channelHelpers;
        this.zoneId = timeZoneProvider.getTimeZone();

        measureChannelHelper = channelHelpers.stream().filter(c -> c instanceof MeasuresChannelHelper).findFirst()
                .map(MeasuresChannelHelper.class::cast);
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        config = getThing().getConfiguration().as(NetatmoThingConfiguration.class);
        ModuleType supportedThingType = ModuleType.valueOf(getThing().getThingTypeUID().getId());
        NetatmoDeviceHandler bridgeHandler = getBridgeHandler(getBridge());
        if (bridgeHandler != null) {
            bridgeHandler.registerDataListener(config.id, this);
        }

        refreshStrategy = supportedThingType.refreshPeriod == RefreshPolicy.AUTO ? new RefreshStrategy(-1)
                : supportedThingType.refreshPeriod == RefreshPolicy.CONFIG ? new RefreshStrategy(config.refreshInterval)
                        : null;

        measureChannelHelper.ifPresent(channelHelper -> channelHelper.collectMeasuredChannels());
        apiBridge.addConnectionListener(this);
    }

    @Override
    public void notifyStatusChange(ConnectionStatus connectionStatus) {
        if (connectionStatus.isConnected()) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, connectionStatus.getMessage());
            scheduleRefreshJob();
        } else {
            freeRefreshJob();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, connectionStatus.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        freeRefreshJob();
        apiBridge.removeConnectionListener(this);
        NetatmoDeviceHandler bridgeHandler = getBridgeHandler(getBridge());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDataListener(this);
        }
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    protected void scheduleRefreshJob() {
        RefreshStrategy strategy = refreshStrategy;
        if (strategy != null) {
            long delay = strategy.nextRunDelayInS();
            logger.debug("Scheduling update channel thread in {} s", delay);

            updateChannels(false);
            ScheduledFuture<?> job = refreshJob;
            if (job != null) {
                job.cancel(false);
            }
            refreshJob = scheduler.schedule(() -> scheduleRefreshJob(), delay, TimeUnit.SECONDS);
        }
    }

    protected NADevice<?> updateReadings() throws NetatmoException {
        throw new NetatmoException("Should not be called");
    }

    private synchronized void updateChannels(boolean requireDefinedRefreshInterval) {
        RefreshStrategy strategy = refreshStrategy;
        if (strategy != null) {
            logger.debug("Data aged of {} s", strategy.dataAge() / 1000);
            boolean dataOutdated = (requireDefinedRefreshInterval && strategy.isSearchingRefreshInterval()) ? false
                    : strategy.isDataOutdated();
            if (dataOutdated) {
                logger.debug("Trying to update channels on device {}", config.id);
                try {
                    NADevice<?> newDeviceReading = updateReadings();
                    logger.debug("Successfully updated device {} readings! Now updating channels", config.id);
                    setNAThing(newDeviceReading);
                    updateStatus(newDeviceReading.isReachable() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
                    updateProperties(newDeviceReading);
                    strategy.setDataTimeStamp(newDeviceReading.getLastSeen(), zoneId);
                } catch (NetatmoException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to connect Netatmo API : " + e.getLocalizedMessage());
                }
            } else {
                logger.debug("Data still valid for device {}", config.id);
            }
            updateChildModules();
        }
    }

    protected void updateChildModules() {
        NAThing localNaThing = this.naThing;
        if (localNaThing != null) {
            if (localNaThing instanceof NADevice) {
                NADevice<?> localNaDevice = (NADevice<?>) localNaThing;
                localNaDevice.getChilds().entrySet().forEach(entry -> notifyListener(entry.getKey(), entry.getValue()));
            }
        }
    }

    protected void notifyListener(String id, NAObject newData) {
        NetatmoDeviceHandler listener = dataListeners.get(id);
        if (listener != null) {
            if (newData instanceof NAEvent) {
                listener.setEvent((NAEvent) newData);
            } else {
                listener.setNAThing((NAThing) newData);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing '{}'", channelUID);
            updateState(channelUID, getNAThingProperty(channelUID));
        }
    }

    public void setNAThing(NAThing naThing) {
        updateStatus(ThingStatus.ONLINE);
        this.naThing = naThing;
        channelHelpers.forEach(helper -> helper.setNewData(naThing));
        measureChannelHelper.ifPresent(channelHelper -> {
            channelHelper.collectMeasuredChannels();
            if (refreshStrategy == null) {
                NetatmoDeviceHandler bridgeHandler = getBridgeHandler(getBridge());
                if (bridgeHandler != null) {
                    bridgeHandler.callGetMeasurements(config.id, channelHelper.getMeasures());
                }
            } else {
                callGetMeasurements(null, channelHelper.getMeasures());
            }
        });
        getThing().getChannels().stream()
                .filter(channel -> !ChannelKind.TRIGGER.equals(channel.getKind()) && isLinked(channel.getUID()))
                .map(channel -> channel.getUID()).forEach(channelUID -> {
                    updateState(channelUID, getNAThingProperty(channelUID));
                });
    }

    public void callGetMeasurements(@Nullable String moduleId, Map<MeasureChannelConfig, Double> measures) {
        measures.keySet().forEach(measureDef -> {
            double result = Double.NaN;
            try {
                WeatherApi api = apiBridge.getRestManager(WeatherApi.class);
                if (api != null) {
                    if (measureDef.limit == MeasureLimit.NONE) {
                        result = api.getMeasurements(config.id, moduleId, measureDef.period, measureDef.type);
                    } else {
                        result = api.getMeasurements(config.id, moduleId, measureDef.period, measureDef.type,
                                measureDef.limit);
                    }
                }
            } catch (NetatmoException e) {
                logger.warn("Error getting measurement {} on period {} for module {} : {}", measureDef.type,
                        measureDef.period, moduleId, e.getMessage());
            }
            measures.put(measureDef, result);
        });
    }

    protected void updateProperties(NAThing naThing) {
        int firmware = naThing.getFirmware();
        if (firmware != -1) {
            Map<String, String> properties = editProperties();
            ModuleType modelId = naThing.getType();
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, Integer.toString(firmware));
            properties.put(Thing.PROPERTY_MODEL_ID, modelId.name());
            updateProperties(properties);
        }
    }

    public void expireData() {
        scheduler.schedule(() -> {
            RefreshStrategy strategy = refreshStrategy;
            if (strategy != null) {
                strategy.expireData();
            }
            scheduleRefreshJob();

        }, 3, TimeUnit.SECONDS);
    }

    protected void tryApiCall(Callable<Boolean> function) {
        try {
            function.call();
            expireData();
        } catch (Exception e) {
            logger.warn("Error calling api : {}", e.getMessage());
        }
    }

    public void registerDataListener(String id, NetatmoDeviceHandler dataListener) {
        dataListeners.put(id, dataListener);
        updateChildModules();
    }

    public void unregisterDataListener(NetatmoDeviceHandler dataListener) {
        dataListeners.entrySet().forEach(entry -> {
            if (entry.getValue().equals(dataListener)) {
                dataListeners.remove(entry.getKey());
            }
        });
    }

    public void setEvent(NAEvent event) {
    }

    protected void updateIfLinked(String group, String channelName, State state) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), group, channelName);
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
    }

    protected State getNAThingProperty(ChannelUID channelUID) {
        for (AbstractChannelHelper helper : channelHelpers) {
            State state = helper.getNAThingProperty(channelUID);
            if (state != null) {
                return state;
            }
        }
        return UnDefType.UNDEF;
    }

    protected @Nullable NetatmoDeviceHandler getBridgeHandler(@Nullable Bridge bridge) {
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            return (NetatmoDeviceHandler) bridge.getHandler();
        }
        return null;
    }
}
