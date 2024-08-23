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
package org.openhab.binding.netatmo.internal.handler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.config.NAThingConfiguration;
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.openhab.binding.netatmo.internal.handler.capability.CapabilityMap;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RestCapability;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;

/**
 * {@link CommonInterface} defines common methods of AccountHandler and NAThingHandlers used by Capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public interface CommonInterface {
    Thing getThing();

    ThingBuilder editThing();

    CapabilityMap getCapabilities();

    Logger getLogger();

    ScheduledExecutorService getScheduler();

    boolean isLinked(ChannelUID channelUID);

    void updateState(ChannelUID channelUID, State state);

    default void updateState(String groupId, String id, State state) {
        updateState(new ChannelUID(getThing().getUID(), groupId, id), state);
    }

    void setThingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail,
            @Nullable String thingStatusReason);

    void triggerChannel(String channelID, String event);

    void updateThing(Thing thing);

    @Nullable
    Bridge getBridge();

    default @Nullable CommonInterface getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof DeviceHandler ? (DeviceHandler) bridge.getHandler()
                : null;
    }

    default Optional<ScheduledFuture<?>> schedule(Runnable arg0, Duration delay) {
        return Optional.of(getScheduler().schedule(arg0, delay.getSeconds(), TimeUnit.SECONDS));
    }

    default @Nullable ApiBridgeHandler getAccountHandler() {
        Bridge bridge = getBridge();
        BridgeHandler bridgeHandler = null;
        if (bridge != null) {
            bridgeHandler = bridge.getHandler();
            while (bridgeHandler != null && !(bridgeHandler instanceof ApiBridgeHandler)) {
                bridge = ((CommonInterface) bridgeHandler).getBridge();
                bridgeHandler = bridge != null ? bridge.getHandler() : null;
            }
        }
        return (ApiBridgeHandler) bridgeHandler;
    }

    default @Nullable String getBridgeId() {
        CommonInterface bridge = getBridgeHandler();
        return bridge != null ? bridge.getId() : null;
    }

    default void expireData() {
        getCapabilities().values().forEach(Capability::expireData);
    }

    default String getId() {
        return getThingConfigAs(NAThingConfiguration.class).getId();
    }

    default <T> T getThingConfigAs(Class<T> configurationClass) {
        return getThing().getConfiguration().as(configurationClass);
    }

    default Stream<Channel> getActiveChannels() {
        return getThing().getChannels().stream()
                .filter(channel -> ChannelKind.STATE.equals(channel.getKind()) && isLinked(channel.getUID()));
    }

    default Optional<CommonInterface> recurseUpToHomeHandler(@Nullable CommonInterface handler) {
        if (handler == null) {
            return Optional.empty();
        }
        return handler.getCapabilities().get(HomeCapability.class).isPresent() ? Optional.of(handler)
                : recurseUpToHomeHandler(handler.getBridgeHandler());
    }

    /**
     * Recurses down in the home/module/device tree
     *
     * @param bridge
     * @return the list of childs of the bridge
     */
    default List<CommonInterface> getAllActiveChildren(Bridge bridge) {
        List<CommonInterface> result = new ArrayList<>();
        bridge.getThings().stream().filter(Thing::isEnabled).map(Thing::getHandler).forEach(childHandler -> {
            if (childHandler != null) {
                Thing childThing = childHandler.getThing();
                if (childThing instanceof Bridge bridgeChild) {
                    result.addAll(getAllActiveChildren(bridgeChild));
                }
                result.add((CommonInterface) childHandler);
            }
        });
        return result;
    }

    default List<CommonInterface> getActiveChildren() {
        return getThing() instanceof Bridge bridge
                ? bridge.getThings().stream().filter(Thing::isEnabled)
                        .filter(th -> th.getStatusInfo().getStatusDetail() != ThingStatusDetail.BRIDGE_OFFLINE)
                        .map(Thing::getHandler).filter(CommonInterface.class::isInstance)
                        .map(CommonInterface.class::cast).toList()
                : List.of();
    }

    default Stream<CommonInterface> getActiveChildren(FeatureArea area) {
        return getActiveChildren().stream().filter(child -> child.getModuleType().feature == area);
    }

    default <T extends RestCapability<?>> Optional<T> getHomeCapability(Class<T> clazz) {
        return Objects.requireNonNull(recurseUpToHomeHandler(this).map(handler -> handler.getCapabilities().get(clazz))
                .orElse(Optional.empty()));
    }

    default void setNewData(NAObject newData) {
        if (newData instanceof NAThing thingData) {
            if (getId().equals(thingData.getBridge())) {
                getActiveChildren().stream().filter(child -> child.getId().equals(thingData.getId())).findFirst()
                        .ifPresent(child -> child.setNewData(thingData));
                return;
            }
        }
        String finalReason = null;
        for (Capability cap : getCapabilities().values()) {
            String thingStatusReason = cap.setNewData(newData);
            if (thingStatusReason != null) {
                finalReason = thingStatusReason;
            }
        }
        // Prevent turning ONLINE myself if in the meantime something turned account OFFLINE
        ApiBridgeHandler accountHandler = getAccountHandler();
        if (accountHandler != null && accountHandler.isConnected() && !newData.isIgnoredForThingUpdate()) {
            setThingStatus(finalReason == null ? ThingStatus.ONLINE : ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    finalReason);
        }
    }

    default void commonHandleCommand(ChannelUID channelUID, Command command) {
        if (ThingStatus.ONLINE.equals(getThing().getStatus())) {
            if (command == RefreshType.REFRESH) {
                expireData();
                return;
            }
            String channelName = channelUID.getIdWithoutGroup();
            getCapabilities().values().forEach(cap -> cap.handleCommand(channelName, command));
        } else {
            getLogger().debug("Command {} on channel {} dropped - thing is not ONLINE", command, channelUID);
        }
    }

    default void proceedWithUpdate() {
        updateReadings().forEach(this::setNewData);
    }

    default List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        getCapabilities().values().forEach(cap -> result.addAll(cap.updateReadings()));
        getActiveChildren().forEach(child -> result.addAll(child.updateReadings()));
        return result;
    }

    default void commonInitialize() {
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, null);
        } else if (!ThingStatus.ONLINE.equals(bridge.getStatus())) {
            setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
            getCapabilities().getParentUpdate().ifPresent(Capability::dispose);
            getCapabilities().getRefresh().ifPresent(Capability::dispose);
        } else {
            setThingStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
            getCapabilities().getParentUpdate().ifPresentOrElse(Capability::initialize, () -> {
                int interval = getThingConfigAs(NAThingConfiguration.class).getRefreshInterval();
                getCapabilities().getRefresh().ifPresent(cap -> cap.setInterval(Duration.ofSeconds(interval)));
            });
        }
    }

    default ModuleType getModuleType() {
        return ModuleType.from(getThing().getThingTypeUID());
    }

    default void commonDispose() {
        getCapabilities().values().forEach(Capability::dispose);
    }

    default void removeChannels(List<Channel> channels) {
        updateThing(editThing().withoutChannels(channels).build());
    }
}
