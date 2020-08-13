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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
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
public class IammeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IammeterHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private IammeterConfiguration config;
    private static final int TIMEOUT_MS = 5000;

    public IammeterHandler(Thing thing) {
        super(thing);
        config = getConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private boolean bExtraChannelRemoved = false;

    @Override
    public void initialize() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        config = getConfiguration();
        if (refreshJob == null) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refreshInterval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    private void refresh() {
        IammeterConfiguration config = this.config;
        try {
            String httpMethod = "GET";
            String url = "http://admin:admin@" + config.host + ":" + config.port + "/monitorjson";
            String content = "";
            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            String response = HttpUtil.executeUrl(httpMethod, url, stream, null, TIMEOUT_MS);
            JsonElement iammeterDataElement = new JsonParser().parse(response);
            JsonObject iammeterData = iammeterDataElement.getAsJsonObject();
            String keyWord = "Data";
            boolean bRemoveChannels = false;
            String channelPrefix = "";
            if (iammeterData.has("data") || (iammeterData.has("Data") && iammeterData.has("SN"))) {
                bRemoveChannels = true;
                if (iammeterData.has("data")) {
                    keyWord = "data";
                }
                for (IammeterWEM3080Channel channelConfig : IammeterWEM3080Channel.values()) {
                    Channel channel = getThing().getChannel(channelConfig.getId());
                    if (channel != null) {
                        channelPrefix = IammeterBindingConstants.THING_TYPE_POWERMETER + ":"
                                + channel.getUID().getThingUID().getId();
                        State state = getDecimal(
                                iammeterData.get(keyWord).getAsJsonArray().get(channelConfig.getIndex()).toString(),
                                channelConfig.getUnit());
                        updateState(channel.getUID(), state);
                    }
                }
            } else if (iammeterData.has("Datas") && iammeterData.has("SN")) {
                keyWord = "Datas";
                for (IammeterWEM3080TChannel channelConfig : IammeterWEM3080TChannel.values()) {
                    Channel channel = getThing().getChannel(channelConfig.getId());
                    if (channel != null) {
                        State state = getDecimal(iammeterData.get(keyWord).getAsJsonArray().get(channelConfig.getRow())
                                .getAsJsonArray().get(channelConfig.getCol()).toString(), channelConfig.getUnit());
                        updateState(channel.getUID(), state);
                    }
                }
            }
            if (bRemoveChannels) {
                if (!bExtraChannelRemoved) {
                    thingStructureChanged(channelPrefix);
                }
            }
            stream.close();
            updateStatus(ThingStatus.ONLINE);
            // Very rudimentary Exception differentiation
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication error with the device: " + e.getMessage());
        } catch (JsonSyntaxException je) {
            logger.warn("Invalid JSON when refreshing source {}: {}", getThing().getUID(), je.getMessage());
        }
    }

    protected void thingStructureChanged(String channelPrefix) {
        List<ChannelUID> noUsedItems = new ArrayList<>();
        noUsedItems.add(new ChannelUID(channelPrefix + ":frequency_a"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":pf_a"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":voltage_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":current_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":power_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":importenergy_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":exportgrid_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":frequency_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":pf_b"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":voltage_c"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":current_c"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":power_c"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":importenergy_c"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":exportgrid_c"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":frequency_c"));
        noUsedItems.add(new ChannelUID(channelPrefix + ":pf_c"));
        ThingBuilder thingBuilder = editThing();
        for (ChannelUID chl : noUsedItems) {
            thingBuilder.withoutChannel(chl);
        }
        updateThing(thingBuilder.build());
        bExtraChannelRemoved = true;
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
