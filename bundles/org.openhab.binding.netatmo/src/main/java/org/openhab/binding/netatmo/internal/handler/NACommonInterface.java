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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.config.NetatmoThingConfiguration;
import org.openhab.binding.netatmo.internal.handler.capability.CapabilityMap;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RefreshCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RestCapability;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link NACommonInterface} defines common methods of NABridgeHandlers and NAThingHandlers used by Capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public interface NACommonInterface {
    Thing getThing();

    CapabilityMap getCapabilities();

    boolean isLinked(ChannelUID channelUID);

    void updateState(ChannelUID channelUID, State state);

    void setThingStatus(ThingStatus thingStatus, @Nullable String thingStatusReason);

    void removeChannels(List<Channel> channels);

    void triggerChannel(String channelID, String event);

    @Nullable
    Bridge getBridge();

    default @Nullable NACommonInterface getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof NABridgeHandler ? (NABridgeHandler) bridge.getHandler()
                : null;
    }

    default @Nullable String getBridgeId() {
        NACommonInterface bridge = getBridgeHandler();
        return bridge != null ? bridge.getId() : null;
    }

    default void expireData() {
        getCapabilities().values().forEach(cap -> cap.expireData());
    }

    default String getId() {
        return (String) getThing().getConfiguration().get("id");
    }

    default Stream<Channel> getActiveChannels() {
        return getThing().getChannels().stream()
                .filter(channel -> ChannelKind.STATE.equals(channel.getKind()) && isLinked(channel.getUID()));
    }

    default Optional<NACommonInterface> getHomeHandler() {
        NACommonInterface bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            return bridgeHandler.getCapabilities().get(HomeCapability.class).isPresent() ? Optional.of(bridgeHandler)
                    : Optional.empty();
        }
        return Optional.empty();
    }

    default Stream<NACommonInterface> getActiveChildren() {
        Thing thing = getThing();
        if (thing instanceof Bridge) {
            return ((Bridge) thing).getThings().stream().filter(
                    t -> t.getStatus().equals(ThingStatus.ONLINE) || t.getStatus().equals(ThingStatus.INITIALIZING))
                    .map(t -> t.getHandler()).map(NACommonInterface.class::cast);
        }
        return Stream.of();
    }

    default <T extends RestCapability<?>> Optional<T> getHomeCapability(Class<T> clazz) {
        return getHomeHandler().map(handler -> handler.getCapabilities().get(clazz)).orElse(Optional.empty());
    }

    default void setNewData(@Nullable NAObject newData) {
        getCapabilities().values().forEach(cap -> cap.setNewData(newData));
    }

    default void commonHandleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            expireData();
            return;
        }
        String channelName = channelUID.getIdWithoutGroup();
        getCapabilities().values().forEach(cap -> cap.handleCommand(channelName, command));
    }

    default void proceedWithUpdate(boolean requireDefinedRefreshInterval) {
        updateReadings().forEach(dataSet -> setNewData(dataSet));
    }

    default List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        getCapabilities().values().forEach(cap -> result.addAll(cap.updateReadings()));
        getActiveChildren().forEach(child -> result.addAll(child.updateReadings()));
        return result;
    }

    default void commonInitialize(ApiBridge apiBridge, ScheduledExecutorService scheduler) {
        ModuleType moduleType = ModuleType.valueOf(getThing().getThingTypeUID().getId());
        if (ModuleType.UNKNOWN.equals(moduleType.getBridge())) {
            NetatmoThingConfiguration config = getThing().getConfiguration().as(NetatmoThingConfiguration.class);
            getCapabilities().put(new RefreshCapability(this, apiBridge, scheduler, config.refreshInterval));
        }
        getCapabilities().values().forEach(cap -> cap.initialize());
        NACommonInterface bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.expireData();
        }
    }
}
