/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.iammeter.internal;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link IammeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Yang Bo - Initial contribution
 */

@NonNullByDefault
public abstract class IammeterBaseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IammeterBaseHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private IammeterConfiguration config;
    private static final int TIMEOUT_MS = 5000;
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofSeconds(5), this::refresh);

    public IammeterBaseHandler(Thing thing) {
        super(thing);
        config = getConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshCache.getValue();
        }
    }

    @Override
    public void initialize() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        config = getConfiguration();
        if (refreshJob == null) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refreshInterval, TimeUnit.SECONDS);
            this.refreshJob = refreshJob;
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    protected abstract void resolveData(String response);

    @SuppressWarnings("null")
    private boolean refresh() {
        refreshCache.invalidateValue();
        IammeterConfiguration config = this.config;
        try {
            String httpMethod = "GET";
            String url = "http://" + config.username + ":" + config.password + "@" + config.host + ":" + config.port
                    + "/monitorjson";
            String response = HttpUtil.executeUrl(httpMethod, url, TIMEOUT_MS);
            resolveData(response);
            updateStatus(ThingStatus.ONLINE);
            return true;
            // Very rudimentary Exception differentiation
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error with the device: " + e.getMessage());
        } catch (JsonSyntaxException je) {
            logger.warn("Invalid JSON when refreshing source {}: {}", getThing().getUID(), je.getMessage());
            updateStatus(ThingStatus.OFFLINE);
        }
        return false;
    }

    protected State getQuantityState(String value, Unit<?> unit) {
        try {
            return QuantityType.valueOf(Float.parseFloat(value), unit);
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        super.dispose();
    }

    public IammeterConfiguration getConfiguration() {
        return this.getConfigAs(IammeterConfiguration.class);
    }
}
