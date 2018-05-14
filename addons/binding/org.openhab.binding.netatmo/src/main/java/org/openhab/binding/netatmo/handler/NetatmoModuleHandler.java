/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NetatmoModuleHandler<MODULE> extends AbstractNetatmoThingHandler {
    private Logger logger = LoggerFactory.getLogger(NetatmoModuleHandler.class);
    private ScheduledFuture<?> refreshJob;
    @Nullable
    protected MODULE module;
    private boolean refreshRequired;

    protected NetatmoModuleHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        refreshJob = scheduler.schedule(() -> {
            requestParentRefresh();
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    protected String getParentId() {
        String parentId = (String) config.get(PARENT_ID);
        return parentId.toLowerCase();
    }

    public boolean childOf(AbstractNetatmoThingHandler naThingHandler) {
        return naThingHandler.matchesId(getParentId());
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        try {
            if (channelId.equalsIgnoreCase(CHANNEL_LAST_MESSAGE) && module != null) {
                Method getLastMessage = module.getClass().getMethod("getLastMessage");
                Integer lastMessage = (Integer) getLastMessage.invoke(module);
                return ChannelTypeUtils.toDateTimeType(lastMessage);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.debug("The module has no method to access {} property ", channelId);
            return UnDefType.NULL;
        }

        return super.getNAThingProperty(channelId);
    }

    @SuppressWarnings("unchecked")
    protected void updateChannels(Object module) {
        if (module != null) {
            this.module = (MODULE) module;
            updateStatus(ThingStatus.ONLINE);
            radioHelper.ifPresent(helper -> helper.setModule(module));
            batteryHelper.ifPresent(helper -> helper.setModule(module));
            updateProperties(this.module);
            super.updateChannels();
        }
    }

    protected void requestParentRefresh() {
        setRefreshRequired(true);
        Optional<AbstractNetatmoThingHandler> parent = getBridgeHandler().findNAThing(getParentId());
        parent.ifPresent(AbstractNetatmoThingHandler::updateChannels);
    }

    protected void updateProperties(MODULE moduleData) {
    }

    protected boolean isRefreshRequired() {
        return refreshRequired;
    }

    protected void setRefreshRequired(boolean refreshRequired) {
        this.refreshRequired = refreshRequired;
    }

}
