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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
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
 * @author yangbo - Initial contribution
 */
@NonNullByDefault
public class IammeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IammeterHandler.class);

    private @Nullable IammeterConfiguration config;

    private @Nullable ScheduledFuture pollingJob;

    public IammeterHandler(Thing thing) {
        super(thing);
    }

    private final int timeout = 5000;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // read only
    }

    private boolean bExtraChannelRemoved = false;

    @Override
    public void initialize() {
        config = getConfigAs(IammeterConfiguration.class);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // execute some binding specific polling code
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                    // Very rudimentary Exception differentiation
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Communication error with the device. Please retry later.");
                } catch (JsonSyntaxException je) {
                    logger.warn("Invalid JSON when refreshing source {}: {}", getThing().getUID(), je.getMessage());
                } catch (Exception e) {
                    logger.warn("Error refreshing source {}: {}", getThing().getUID(), e.getMessage(), e);
                }
            }
        };
        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, config.refreshInterval, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            boolean thingReachable = true;
            if (thingReachable) {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Communication error with the device. Please retry later.");
                } catch (JsonSyntaxException je) {
                    logger.warn("Invalid JSON when refreshing source {}: {}", getThing().getUID(), je.getMessage());
                } catch (Exception e) {
                    logger.warn("Error refreshing source {}: {}", getThing().getUID(), e.getMessage(), e);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    private void refresh() throws Exception {
        // Get the JSON - somehow
        logger.trace("Starting refresh handler");
        String httpMethod = "GET";
        String url = "http://admin:admin@" + config.host + ":" + config.port + "/monitorjson";
        String content = "";
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String response = HttpUtil.executeUrl(httpMethod, url, stream, null, timeout);
        JsonElement iammeterDataElement = new JsonParser().parse(response);
        JsonObject iammeterData = iammeterDataElement.getAsJsonObject();
        String keyWord = "Data";
        boolean bRemoveChannels = false;
        String channelProfix = "";
        if (iammeterData.has("data") || (iammeterData.has("Data") && iammeterData.has("SN"))) {
            bRemoveChannels = true;
            if (iammeterData.has("data")) {
                keyWord = "data";
            }
            for (IammeterWEM3080Channel channelConfig : IammeterWEM3080Channel.values()) {
                Channel channel = getThing().getChannel(channelConfig.getId());
                channelProfix = IammeterBindingConstants.THING_TYPE_POWERMETER + ":"
                        + channel.getUID().getThingUID().getId();
                State state = getDecimal(
                        iammeterData.get(keyWord).getAsJsonArray().get(channelConfig.getIndex()).toString());
                updateState(channel.getUID(), state);
            }
        } else if (iammeterData.has("Datas") && iammeterData.has("SN")) {
            keyWord = "Datas";
            for (IammeterWEM3080TChannel channelConfig : IammeterWEM3080TChannel.values()) {
                Channel channel = getThing().getChannel(channelConfig.getId());
                State state = getDecimal(iammeterData.get(keyWord).getAsJsonArray().get(channelConfig.getRow())
                        .getAsJsonArray().get(channelConfig.getCol()).toString());
                updateState(channel.getUID(), state);
            }
        }
        if (bRemoveChannels) {
            if (!bExtraChannelRemoved) {
                thingStructureChanged(channelProfix);
            }
        }
    }

    protected void thingStructureChanged(String channelProfix) {
        List<ChannelUID> noUsedItems = new ArrayList<>();
        noUsedItems.add(new ChannelUID(channelProfix + ":frequency_a"));
        noUsedItems.add(new ChannelUID(channelProfix + ":pf_a"));
        noUsedItems.add(new ChannelUID(channelProfix + ":voltage_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":current_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":power_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":importenergy_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":exportgrid_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":frequency_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":pf_b"));
        noUsedItems.add(new ChannelUID(channelProfix + ":voltage_c"));
        noUsedItems.add(new ChannelUID(channelProfix + ":current_c"));
        noUsedItems.add(new ChannelUID(channelProfix + ":power_c"));
        noUsedItems.add(new ChannelUID(channelProfix + ":importenergy_c"));
        noUsedItems.add(new ChannelUID(channelProfix + ":exportgrid_c"));
        noUsedItems.add(new ChannelUID(channelProfix + ":frequency_c"));
        noUsedItems.add(new ChannelUID(channelProfix + ":pf_c"));
        ThingBuilder thingBuilder = editThing();
        for (ChannelUID chl : noUsedItems) {
            thingBuilder.withoutChannel(chl);
        }
        updateThing(thingBuilder.build());
        bExtraChannelRemoved = true;
    }

    private State getDecimal(String value) {
        try {
            return new DecimalType(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return UnDefType.UNDEF;
        }
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }

}
