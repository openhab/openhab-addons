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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoModuleHandler<MODULE> extends AbstractNetatmoThingHandler {
    private final Logger logger = LoggerFactory.getLogger(NetatmoModuleHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable MODULE module;
    private boolean refreshRequired;

    protected NetatmoModuleHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected void initializeThing() {
        refreshJob = scheduler.schedule(() -> {
            requestParentRefresh();
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    protected @Nullable String getParentId() {
        Configuration conf = config;
        Object parentId = conf != null ? conf.get(PARENT_ID) : null;
        if (parentId instanceof String) {
            return ((String) parentId).toLowerCase();
        }
        return null;
    }

    public boolean childOf(AbstractNetatmoThingHandler naThingHandler) {
        return naThingHandler.matchesId(getParentId());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        try {
            Optional<MODULE> mod = getModule();
            if (channelId.equalsIgnoreCase(CHANNEL_LAST_MESSAGE) && mod.isPresent()) {
                Method getLastMessage = mod.get().getClass().getMethod("getLastMessage");
                Integer lastMessage = (Integer) getLastMessage.invoke(mod.get());
                return ChannelTypeUtils.toDateTimeType(lastMessage, timeZoneProvider.getTimeZone());
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.debug("The module has no method to access {} property ", channelId);
            return UnDefType.NULL;
        }

        return super.getNAThingProperty(channelId);
    }

    protected void updateChannels(Object module) {
        MODULE theModule = (MODULE) module;
        setModule(theModule);
        updateStatus(isReachable() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        getRadioHelper().ifPresent(helper -> helper.setModule(module));
        getBatteryHelper().ifPresent(helper -> helper.setModule(module));
        updateProperties(theModule);
        super.updateChannels();
    }

    protected void invalidateParentCacheAndRefresh() {
        setRefreshRequired(true);
        // Leave a bit of time to Netatmo Server to get in sync with new values sent
        scheduler.schedule(() -> {
            invalidateParentCache();
            requestParentRefresh();
        }, 2, TimeUnit.SECONDS);
    }

    protected void requestParentRefresh() {
        setRefreshRequired(true);
        findNAThing(getParentId()).ifPresent(AbstractNetatmoThingHandler::updateChannels);
    }

    private void invalidateParentCache() {
        findNAThing(getParentId()).map(NetatmoDeviceHandler.class::cast).ifPresent(NetatmoDeviceHandler::expireData);
    }

    protected void updateProperties(MODULE moduleData) {
    }

    protected boolean isRefreshRequired() {
        return refreshRequired;
    }

    protected void setRefreshRequired(boolean refreshRequired) {
        this.refreshRequired = refreshRequired;
    }

    protected Optional<MODULE> getModule() {
        return Optional.ofNullable(module);
    }

    public void setModule(MODULE module) {
        this.module = module;
    }
}
