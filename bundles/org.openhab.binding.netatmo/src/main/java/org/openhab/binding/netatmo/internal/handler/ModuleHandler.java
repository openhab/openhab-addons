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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.capability.CapabilityMap;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ModuleHandler} is the base class for all Netatmo things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ModuleHandler extends BaseThingHandler implements CommonInterface {
    private final Logger logger = LoggerFactory.getLogger(ModuleHandler.class);
    private final CapabilityMap capabilities = new CapabilityMap();
    private final TimeZoneProvider timeZoneProvider;

    public ModuleHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        commonInitialize();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged for thing {} to {}", getThing().getUID(), bridgeStatusInfo);
        commonInitialize();
    }

    @Override
    public void dispose() {
        commonDispose();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        commonHandleCommand(channelUID, command);
    }

    @Override
    public void setThingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail,
            @Nullable String thingStatusReason) {
        updateStatus(thingStatus, thingStatusDetail, thingStatusReason);
    }

    @Override
    public CapabilityMap getCapabilities() {
        return capabilities;
    }

    @Override
    public ThingBuilder editThing() {
        return super.editThing();
    }

    @Override
    public void updateThing(Thing thing) {
        super.updateThing(thing);
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public boolean isLinked(ChannelUID channelUID) {
        return super.isLinked(channelUID);
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void triggerChannel(String groupID, String channelID, String event) {
        super.triggerChannel(new ChannelUID(this.getThing().getUID(), groupID, channelID), event);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        List<Class<? extends ThingHandlerService>> result = new ArrayList<>();
        capabilities.values().forEach(cap -> result.addAll(cap.getServices()));
        return result;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public ZoneId getSystemTimeZone() {
        return timeZoneProvider.getTimeZone();
    }
}
