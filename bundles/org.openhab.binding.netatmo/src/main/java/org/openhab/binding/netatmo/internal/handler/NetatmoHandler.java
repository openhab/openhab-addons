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
package org.openhab.binding.netatmo.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.action.DeviceActions;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.ModuleType.RefreshPolicy;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.config.NetatmoThingConfiguration;
import org.openhab.binding.netatmo.internal.deserialization.NAThingMap;
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.openhab.binding.netatmo.internal.handler.capability.MeasureCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.PropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoHandler} is the class used to handle the Health Home Coach device
 *
 * @author Michael Svinth - Initial contribution
 *
 */
@NonNullByDefault
public abstract class NetatmoHandler extends BaseBridgeHandler implements ConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(NetatmoHandler.class);
    private final List<AbstractChannelHelper> channelHelpers;

    private @Nullable ScheduledFuture<?> refreshJob;
    private Optional<RefreshStrategy> refreshStrategy = Optional.empty();
    private Map<Class<?>, Capability<?>> capabilities = new HashMap<>();
    protected final ApiBridge apiBridge;
    protected final PropertyHelper propertyHelper;
    protected NetatmoServlet webhookServlet;
    protected final NetatmoDescriptionProvider descriptionProvider;

    public NetatmoHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet, PropertyHelper propHelper) {
        super(bridge);
        this.apiBridge = apiBridge;
        this.channelHelpers = channelHelpers;
        this.propertyHelper = propHelper;
        this.webhookServlet = webhookServlet;
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        if (propertyHelper.getModuleType().refreshPolicy != RefreshPolicy.NONE) {
            NetatmoThingConfiguration config = getThing().getConfiguration().as(NetatmoThingConfiguration.class);
            refreshStrategy = Optional.of(new RefreshStrategy(config.refreshInterval));
            apiBridge.addConnectionListener(this);
        } else {
            NetatmoHandler bridgeHandler = getBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.expireData();
            }
        }

        channelHelpers.stream().filter(c -> c instanceof MeasuresChannelHelper).findFirst()
                .map(MeasuresChannelHelper.class::cast)
                .ifPresent(helper -> defineCapability(new MeasureCapability(getThing(), helper, apiBridge)));
    }

    protected void defineCapability(Capability<?> capability) {
        capabilities.put(capability.getClass(), capability);
    }

    protected <T extends Capability<?>> Optional<T> getCapability(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T cap = (T) capabilities.get(clazz);
        return Optional.ofNullable(cap);
    }

    protected List<NAObject> updateReadings() throws NetatmoException {
        getCapability(MeasureCapability.class).ifPresent(cap -> {
            String bridgeId = getBridgeId();
            String deviceId = bridgeId != null ? bridgeId : getId();
            String moduleId = bridgeId != null ? getId() : null;
            cap.updateMeasurements(deviceId, moduleId);
        });
        return List.of();
    }

    @Override
    public void dispose() {
        freeRefreshJob();
        apiBridge.removeConnectionListener(this);
        capabilities.values().forEach(cap -> cap.dispose());
        super.dispose();
    }

    @Override
    public void connectionEvent(boolean connected) {
        if (connected) {
            ThingStatus status = getThing().getStatus();
            if (status != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                // Wait a little bit before refreshing because a dispose may be running in parallel
                scheduler.schedule(() -> scheduleRefreshJob(), 2, TimeUnit.SECONDS);
            }
        } else {
            freeRefreshJob();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/status-bridge-offlilne");
        }
    }

    private void scheduleRefreshJob() {
        refreshStrategy.ifPresent(strategy -> {
            long delay = strategy.nextRunDelay().toSeconds();
            logger.debug("Scheduling update channel thread in {} s", delay);
            updateChannels(false);
            freeRefreshJob();
            refreshJob = scheduler.schedule(() -> scheduleRefreshJob(), delay, TimeUnit.SECONDS);
        });
    }

    private synchronized void updateChannels(boolean requireDefinedRefreshInterval) {
        refreshStrategy.ifPresent(strategy -> {
            boolean dataOutdated = (requireDefinedRefreshInterval && strategy.isSearchingRefreshInterval()) ? false
                    : strategy.isDataOutdated();
            if (dataOutdated) {
                logger.debug("Trying to update channels on device {}", getId());
                try {
                    List<NAObject> newData = updateReadings();
                    newData.forEach(dataSet -> {
                        logger.debug("Successfully updated device {} readings! Now updating channels", getId());
                        setNewData(dataSet);
                        updateChilds(dataSet);
                    });
                } catch (NetatmoException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            } else {
                logger.debug("Data still valid for device {}", getId());
            }
        });
    }

    protected void updateChilds(NAObject newData) {
        if (newData instanceof NADevice) {
            NAThingMap modules = ((NADevice) newData).getModules();
            getThing().getThings().stream().map(t -> t.getHandler()).map(NetatmoHandler.class::cast)
                    .forEach(handler -> {
                        try {
                            handler.updateReadings();
                            NAThing data = modules.get(handler.getId());
                            if (data != null) {
                                handler.setNewData(data);
                            }
                        } catch (NetatmoException e) {
                            logger.warn("Error updating child information : {}", e.getMessage());
                        }
                    });
        }
        capabilities.values().forEach(cap -> cap.setNewData(newData));
    }

    public String getId() {
        String id = (String) getThing().getConfiguration().get("id");
        return id;
    }

    public void setNewData(NAObject newData) {
        if (newData instanceof NAThing) {
            NAThing localNaThing = (NAThing) newData;
            localNaThing.getLastSeen()
                    .ifPresent(ts -> refreshStrategy.ifPresent(strategy -> strategy.setDataTimeStamp(ts)));

            if (localNaThing.isReachable()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/device-not-connected");
            }
        }
        propertyHelper.setNewData(newData);
        channelHelpers.forEach(helper -> helper.setNewData(newData));
        getThing().getChannels().stream()
                .filter(channel -> ChannelKind.STATE.equals(channel.getKind()) && isLinked(channel.getUID()))
                .forEach(channel -> {
                    ChannelUID channelUID = channel.getUID();
                    String channelID = channelUID.getIdWithoutGroup();
                    String groupId = channelUID.getGroupId();
                    Configuration channelConfig = channel.getConfiguration();
                    // State state = null;
                    for (AbstractChannelHelper helper : channelHelpers) {
                        State state = helper.getChannelState(channelID, groupId, channelConfig);
                        if (state != null) {
                            updateState(channelUID, state);
                            break;
                        }
                    }
                });
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    public void expireData() {
        scheduler.schedule(() -> {
            refreshStrategy.ifPresent(RefreshStrategy::expireData);
            scheduleRefreshJob();
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing thing");
            expireData();
        } else {
            String channelName = channelUID.getIdWithoutGroup();
            capabilities.values().forEach(cap -> cap.internalHandleCommand(channelName, command));
            internalHandleCommand(channelName, command);
        }
    }

    protected void internalHandleCommand(String channelName, Command command) {
    }

    protected @Nullable NetatmoHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof NetatmoHandler ? (NetatmoHandler) bridge.getHandler()
                : null;
    }

    protected Optional<HomeHandler> getHomeHandler() {
        NetatmoHandler bridgeHandler = getBridgeHandler();
        return Optional.ofNullable(
                bridgeHandler != null && bridgeHandler instanceof HomeHandler ? (HomeHandler) bridgeHandler : null);
    }

    protected <T extends Capability<?>> Optional<T> getHomeCapability(Class<T> clazz) {
        return getHomeHandler().map(handler -> handler.getCapability(clazz)).orElse(Optional.empty());
    }

    private @Nullable String getBridgeId() {
        NetatmoHandler bridge = getBridgeHandler();
        return bridge != null ? bridge.getId() : null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(DeviceActions.class);
    }

    public void reconnectApi() {
        apiBridge.openConnection(null);
    }

    public Stream<NetatmoHandler> getActiveChildren() {
        return getThing().getThings().stream()
                .filter(t -> t.getStatus().equals(ThingStatus.ONLINE) || t.getStatus().equals(ThingStatus.INITIALIZING))
                .map(t -> t.getHandler()).map(NetatmoHandler.class::cast);
    }
}
