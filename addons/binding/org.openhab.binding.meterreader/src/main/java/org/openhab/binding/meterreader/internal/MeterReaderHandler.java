/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.meterreader.MeterReaderBindingConstants;
import org.openhab.binding.meterreader.internal.helper.Baudrate;
import org.openhab.binding.meterreader.internal.sml.SmlReaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeterReaderHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
public class MeterReaderHandler extends BaseThingHandler {

    private static final int DEFAULT_REFRESH_PERIOD = 30;
    private Logger logger = LoggerFactory.getLogger(MeterReaderHandler.class);
    private MeterDevice<?> smlDevice;
    private ScheduledFuture<?> refreshJob;

    public MeterReaderHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SmlReader handler.");
        super.initialize();

        SmlReaderConfiguration config = getConfigAs(SmlReaderConfiguration.class);
        logger.debug("config port = {}", config.port);

        boolean validConfig = true;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.port) == null) {
            errorMsg = "Parameter 'port' is mandatory and must be configured";
            validConfig = false;
        }

        if (validConfig) {
            try {
                int baudrate = config.baudrate == null ? Baudrate.AUTO.getBaudrate()
                        : Baudrate.fromString(config.baudrate).getBaudrate();
                byte[] pullSequence = config.initMessage == null ? null
                        : Hex.decodeHex(StringUtils.deleteWhitespace(config.initMessage).toCharArray());
                this.smlDevice = MeterDeviceFactory.getDevice(config.mode, this.thing.getUID().getAsString(), config.port,
                        pullSequence, baudrate, config.baudrateChangeDelay);
            } catch (DecoderException e) {
                logger.error("Failed to decode init message", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Parameter 'initMessage' can not be decoded: " + e.getLocalizedMessage());
            }
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.refreshJob != null) {
            this.refreshJob.cancel(true);
        }
    }

    /**
     * Start the job refreshing the SML data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        updateOBISValue();
                    } catch (Exception e) {
                        logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            SmlReaderConfiguration config = getConfigAs(SmlReaderConfiguration.class);
            int delay = config.refresh != null ? config.refresh : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateOBISChannel(channelUID);
        } else {
            logger.debug("The SML reader binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Get new data the device
     *
     */
    private void updateOBISValue() {
        try {
            this.smlDevice.readValues();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            Map<String, Channel> channelMap = new HashMap<>();
            for (Channel channel : this.thing.getChannels()) {
                channelMap.put(channel.getUID().getId(), channel);
            }

            ThingBuilder thingBuilder = editThing();
            Map<String, Channel> channelsToAdd = new HashMap<>(channelMap);
            boolean channelsChanged = false;
            for (String obis : this.smlDevice.getObisCodes()) {

                String obisChannelString = obis.replaceAll("\\.", "-").replaceAll(":|\\*", "#");
                Channel channel = channelMap.remove(obisChannelString);
                OBISTypeValue obisType = getObisType(obis, channel);
                if (channel == null) {
                    logger.debug("Adding channel: {} with item type: {}", obisChannelString, obisType.itemType);
                    // channel has not been created yet
                    ChannelBuilder channelBuilder = ChannelBuilder
                            .create(new ChannelUID(this.thing.getUID(), obisChannelString), obisType.itemType)
                            .withType(obisType.channelType);

                    Configuration configuration = new Configuration();
                    configuration.put(MeterReaderBindingConstants.CONFIGURATION_CONVERSION, 1);
                    channelBuilder.withConfiguration(configuration);
                    String obisName = obisType.obisValue.getObisName();
                    channelBuilder.withLabel(obis + " (" + obisName + ")");
                    Map<String, String> channelProps = new HashMap<>();
                    channelProps.put(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                    channelBuilder.withProperties(channelProps);
                    channelBuilder.withDescription(MessageFormat.format("Value for OBIS code: {0} ({1}) with Unit: {2}",
                            obis, obisName, obisType.obisValue.getUnit()));
                    channel = channelBuilder.build();
                    channelsToAdd.put(obisChannelString, channel);
                    channelsChanged = true;
                }
                updateState(channel.getUID(), obisType.type);
            }

            // channels that are not available are removed
            for (Map.Entry<String, Channel> entry : channelMap.entrySet()) {
                logger.debug("Removing channel: {}", entry.getKey());
                channelsToAdd.remove(entry.getKey());
                channelsChanged = true;
            }
            // add all valid channels to the thing builder
            thingBuilder.withChannels(new ArrayList<>(channelsToAdd.values()));
            if (channelsChanged) {
                updateThing(thingBuilder.build());
            }

        } catch (Exception e) {
            // Update the thing status
            logger.error("Failed to read SML", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }

    }

    private void updateOBISChannel(ChannelUID channelId) {
        if (isLinked(channelId.getId())) {
            Channel channel = this.thing.getChannel(channelId.getId());

            String obis = channel.getProperties().get(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS);
            OBISTypeValue obisType = getObisType(obis, channel);
            if (obisType != null) {

                updateState(channel.getUID(), obisType.type);
            }
        }
    }

    private OBISTypeValue getObisType(String obis, Channel channel) {
        State type;
        String itemType;
        ChannelTypeUID channelType;
        if (this.smlDevice != null) {

            MeterValue obisValue = this.smlDevice.getSmlValue(obis);
            if (obisValue != null) {
                try {
                    type = DecimalType.valueOf(obisValue.getValue());
                    itemType = "Number";
                    channelType = new ChannelTypeUID(MeterReaderBindingConstants.BINDING_ID,
                            MeterReaderBindingConstants.CHANNEL_TYPE_NUMBER);
                    if (channel != null) {
                        Number conversionRatio = (Number) channel.getConfiguration()
                                .get(MeterReaderBindingConstants.CONFIGURATION_CONVERSION);
                        if (conversionRatio != null) {
                            double newValue = ((DecimalType) type).doubleValue() / conversionRatio.doubleValue();
                            type = new DecimalType(newValue);
                        }
                    }

                } catch (Exception e) {
                    type = StringType.valueOf(new String(obisValue.getValue().getBytes(), Charset.forName("UTF-8")));
                    itemType = "String";
                    channelType = new ChannelTypeUID(MeterReaderBindingConstants.BINDING_ID,
                            MeterReaderBindingConstants.CHANNEL_TYPE_STRING);
                }
                return new OBISTypeValue(itemType, type, obisValue, channelType);
            } else {

                logger.warn("OBIS {} is not available in {}!", obis, this.thing.getLabel());
            }
        }
        return null;
    }

    class OBISTypeValue {
        String itemType;
        State type;
        MeterValue obisValue;
        ChannelTypeUID channelType;

        public OBISTypeValue(String itemType, State type, MeterValue obisValue, ChannelTypeUID channelType) {
            super();
            this.itemType = itemType;
            this.type = type;
            this.obisValue = obisValue;
            this.channelType = channelType;
        }

    }
}
