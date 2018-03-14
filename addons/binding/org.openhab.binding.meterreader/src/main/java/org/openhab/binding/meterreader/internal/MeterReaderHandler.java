/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.List;
import java.util.Map;

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
import org.openhab.binding.meterreader.MeterReaderConfiguration;
import org.openhab.binding.meterreader.internal.helper.Baudrate;
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
    private Cancelable valueReader;

    public MeterReaderHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SmlReader handler.");

        MeterReaderConfiguration config = getConfigAs(MeterReaderConfiguration.class);
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
                this.smlDevice = MeterDeviceFactory.getDevice(config.mode, this.thing.getUID().getAsString(),
                        config.port, pullSequence, baudrate, config.baudrateChangeDelay);
                updateStatus(ThingStatus.ONLINE);
            } catch (DecoderException e) {
                logger.error("Failed to decode init message", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Parameter 'initMessage' can not be decoded: " + e.getLocalizedMessage());
            }
            updateOBISValue();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelRead();
    }

    private void cancelRead() {
        if (this.valueReader != null) {
            this.valueReader.cancel();
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
            cancelRead();

            this.smlDevice.addValueChangeListener(new MeterValueListener() {
                @Override
                public void valueChanged(MeterValue value) {
                    ThingBuilder thingBuilder = editThing();

                    String obis = value.getObisCode();

                    String obisChannelString = getObisChannelId(obis);
                    Channel channel = thing.getChannel(obisChannelString);
                    OBISTypeValue obisType = getObisType(obis, channel);
                    if (channel == null) {
                        logger.debug("Adding channel: {} with item type: {}", obisChannelString, obisType);

                        // channel has not been created yet
                        ChannelBuilder channelBuilder = ChannelBuilder
                                .create(new ChannelUID(thing.getUID(), obisChannelString), obisType.itemType)
                                .withType(obisType.channelType);

                        Configuration configuration = new Configuration();
                        configuration.put(MeterReaderBindingConstants.CONFIGURATION_CONVERSION, 1);
                        channelBuilder.withConfiguration(configuration);
                        channelBuilder.withLabel(obis);
                        Map<String, String> channelProps = new HashMap<>();
                        channelProps.put(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                        channelBuilder.withProperties(channelProps);
                        channelBuilder.withDescription(MessageFormat.format("Value for OBIS code: {0} with Unit: {1}",
                                obis, obisType.obisValue.getUnit()));
                        channel = channelBuilder.build();
                        ChannelUID channelId = channel.getUID();

                        // add all valid channels to the thing builder
                        List<Channel> channels = new ArrayList<Channel>(getThing().getChannels());
                        if (channels.stream().filter((element) -> element.getUID().equals(channelId)).count() == 0) {
                            channels.add(channel);
                            thingBuilder.withChannels(channels);
                            updateThing(thingBuilder.build());
                        }

                    }

                    if (!channel.getProperties().containsKey(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS)) {
                        addObisPropertyToChannel(obis, channel);
                    }
                    updateState(channel.getUID(), obisType.type);
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                }

                private void addObisPropertyToChannel(String obis, Channel channel) {
                    ChannelBuilder newChannel = ChannelBuilder.create(channel.getUID(), channel.getAcceptedItemType());
                    newChannel.withDefaultTags(channel.getDefaultTags());
                    newChannel.withConfiguration(channel.getConfiguration());
                    newChannel.withDescription(channel.getDescription());
                    newChannel.withKind(channel.getKind());
                    newChannel.withLabel(channel.getLabel());
                    newChannel.withType(channel.getChannelTypeUID());
                    HashMap<String, String> properties = new HashMap<>(channel.getProperties());
                    properties.put(MeterReaderBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                    newChannel.withProperties(properties);
                    updateThing(editThing().withoutChannel(channel.getUID()).withChannel(newChannel.build()).build());
                }

                @Override
                public void valueRemoved(MeterValue value) {

                    // channels that are not available are removed
                    String obisChannelId = getObisChannelId(value.getObisCode());
                    logger.debug("Removing channel: {}", obisChannelId);
                    ThingBuilder thingBuilder = editThing();
                    thingBuilder.withoutChannel(new ChannelUID(thing.getUID(), obisChannelId));
                    updateThing(thingBuilder.build());

                }

                @Override
                public void errorOccoured(Exception e) {
                    MeterValueListener.super.errorOccoured(e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                }
            });

            MeterReaderConfiguration config = getConfigAs(MeterReaderConfiguration.class);
            int delay = config.refresh != null ? config.refresh : DEFAULT_REFRESH_PERIOD;

            valueReader = this.smlDevice.readValues(this.scheduler, delay);

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
            if (obis != null) {
                OBISTypeValue obisType = getObisType(obis, channel);
                if (obisType != null) {

                    updateState(channel.getUID(), obisType.type);
                }
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
                            newValue = smlDevice.getEnergyDirection().convert(newValue);
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

    protected String getObisChannelId(String obis) {
        return obis.replaceAll("\\.", "-").replaceAll(":|\\*", "#");
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

        @Override
        public String toString() {
            return "OBISTypeValue [itemType=" + itemType + ", obisValue=" + obisValue + "]";
        }
    }
}
