/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.solarlog.SolarLogBindingConstants;
import org.openhab.binding.solarlog.internal.HttpUtils;
import org.openhab.binding.solarlog.internal.SolarLogSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link SolarLogHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johann Richard - Initial contribution
 */
public class SolarLogHandler extends BaseThingHandler {

    private SolarLogSourceConfig config;
    private List<SolarLogChannelConfig> channelConfigs;
    private Logger logger = LoggerFactory.getLogger(SolarLogHandler.class);

    public SolarLogHandler(Thing thing) {
        super(thing);
        channelConfigs = new ArrayList<>();
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_LASTUPDATETIME,
                SolarLogBindingConstants.CHANNEL_LASTUPDATETIME));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_PAC,
                SolarLogBindingConstants.CHANNEL_PAC));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_PDC,
                SolarLogBindingConstants.CHANNEL_PDC));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_UAC,
                SolarLogBindingConstants.CHANNEL_UAC));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_UDC,
                SolarLogBindingConstants.CHANNEL_UDC));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_YIELDDAY,
                SolarLogBindingConstants.CHANNEL_YIELDDAY));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_YIELDYESTERDAY,
                SolarLogBindingConstants.CHANNEL_YIELDYESTERDAY));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_YIELDMONTH,
                SolarLogBindingConstants.CHANNEL_YIELDMONTH));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_YIELDYEAR,
                SolarLogBindingConstants.CHANNEL_YIELDYEAR));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_YIELDTOTAL,
                SolarLogBindingConstants.CHANNEL_YIELDTOTAL));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_CONSPAC,
                SolarLogBindingConstants.CHANNEL_CONSPAC));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_CONSYIELDDAY,
                SolarLogBindingConstants.CHANNEL_CONSYIELDDAY));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_CONSYIELDYESTERDAY,
                SolarLogBindingConstants.CHANNEL_CONSYIELDYESTERDAY));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_CONSYIELDMONTH,
                SolarLogBindingConstants.CHANNEL_CONSYIELDMONTH));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_CONSYIELDYEAR,
                SolarLogBindingConstants.CHANNEL_CONSYIELDYEAR));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_CONSYIELDTOTAL,
                SolarLogBindingConstants.CHANNEL_CONSYIELDTOTAL));
        channelConfigs.add(new SolarLogChannelConfig(SolarLogBindingConstants.CHANNEL_ID_TOTALPOWER,
                SolarLogBindingConstants.CHANNEL_TOTALPOWER));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        config = getConfigAs(SolarLogSourceConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getClass().getName() + ":" + e.getMessage());
                    logger.debug("Error refreshing source " + getThing().getUID(), e);
                }
            }

        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void refresh() throws Exception {
        // Get the JSON - somehow
        JsonElement solarLogDataElement = HttpUtils.getSolarLogData(config.url);
        JsonObject solarLogData = solarLogDataElement.getAsJsonObject();

        // Check whether the data is well-formed
        if (solarLogData.has(SolarLogBindingConstants.SOLARLOG_JSON_ROOT)) {
            solarLogData = solarLogData.getAsJsonObject(SolarLogBindingConstants.SOLARLOG_JSON_ROOT);
            if (solarLogData.has(SolarLogBindingConstants.SOLARLOG_JSON_PROPERTIES)) {
                solarLogData = solarLogData.getAsJsonObject(SolarLogBindingConstants.SOLARLOG_JSON_PROPERTIES);

                for (SolarLogChannelConfig channelConfig : channelConfigs) {
                    if (solarLogData.has(channelConfig.index)) {
                        String value = solarLogData.get(channelConfig.index).getAsString();
                        Channel channel = getThing().getChannel(channelConfig.id);
                        State state = getState(value);

                        updateState(channel.getUID(), state);
                    } else {
                        logger.debug("Error refreshing source " + getThing().getUID(), channelConfig.id);
                    }
                }
            }
        }

    }

    private State getState(String value) {
        try {
            return new DecimalType(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return new StringType(value);
        }
    }
}
