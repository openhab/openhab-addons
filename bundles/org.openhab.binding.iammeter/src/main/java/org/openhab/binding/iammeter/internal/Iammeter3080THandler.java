/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link IammeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Yang Bo - Initial contribution
 */

@NonNullByDefault
public class Iammeter3080THandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Iammeter3080THandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private IammeterConfiguration config;
    private static final int TIMEOUT_MS = 5000;
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofSeconds(5), this::refresh);

    public Iammeter3080THandler(Thing thing) {
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

    @SuppressWarnings("null")
    private boolean refresh() {
        refreshCache.invalidateValue();
        IammeterConfiguration config = this.config;
        try {
            String httpMethod = "GET";
            String url = "http://" + config.username + ":" + config.password + "@" + config.host + ":" + config.port
                    + "/monitorjson";
            String content = "";
            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            String response = HttpUtil.executeUrl(httpMethod, url, stream, null, TIMEOUT_MS);
            JsonElement iammeterDataElement = new JsonParser().parse(response);
            JsonObject iammeterData = iammeterDataElement.getAsJsonObject();
            String keyWord = "Datas";
            if (iammeterData.has("Datas") && iammeterData.has("SN")) {
                String groups[] = { "powerPhaseA", "powerPhaseB", "powerPhaseC" };
                for (int row = 0; row < groups.length; row++) {
                    String gpName = groups[row];
                    List<Channel> chnList = getThing().getChannelsOfGroup(gpName);
                    for (IammeterWEM3080Channel channelConfig : IammeterWEM3080Channel.values()) {
                        Channel chnl = chnList.get(channelConfig.ordinal());
                        if (chnl != null) {
                            State state = getDecimal(iammeterData.get(keyWord).getAsJsonArray().get(row)
                                    .getAsJsonArray().get(channelConfig.ordinal()).toString(), channelConfig.getUnit());
                            updateState(chnl.getUID(), state);
                        }
                    }
                    updateStatus(ThingStatus.ONLINE);
                }
            }
            stream.close();
            updateStatus(ThingStatus.ONLINE);
            return true;
            // Very rudimentary Exception differentiation
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error with the device: " + e.getMessage());
        } catch (JsonSyntaxException je) {
            logger.warn("Invalid JSON when refreshing source {}: {}", getThing().getUID(), je.getMessage());
        }
        return false;
    }

    private State getDecimal(String value, Unit<?> unit) {
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
            refreshJob = null;
        }
        super.dispose();
    }

    public IammeterConfiguration getConfiguration() {
        return this.getConfigAs(IammeterConfiguration.class);
    }
}
