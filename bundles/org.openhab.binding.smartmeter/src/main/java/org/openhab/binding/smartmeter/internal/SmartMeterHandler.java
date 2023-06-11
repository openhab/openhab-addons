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
package org.openhab.binding.smartmeter.internal;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.binding.smartmeter.SmartMeterConfiguration;
import org.openhab.binding.smartmeter.internal.conformity.Conformity;
import org.openhab.binding.smartmeter.internal.helper.Baudrate;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.disposables.Disposable;

/**
 * The {@link SmartMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.ARRAY_CONTENTS, DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE,
        DefaultLocation.TYPE_ARGUMENT })
public class SmartMeterHandler extends BaseThingHandler {

    private static final long DEFAULT_TIMEOUT = 30000;
    private static final int DEFAULT_REFRESH_PERIOD = 30;
    private Logger logger = LoggerFactory.getLogger(SmartMeterHandler.class);
    private MeterDevice<?> smlDevice;
    private Disposable valueReader;
    private Conformity conformity;
    private MeterValueListener valueChangeListener;
    private SmartMeterChannelTypeProvider channelTypeProvider;
    private @NonNull Supplier<SerialPortManager> serialPortManagerSupplier;

    public SmartMeterHandler(Thing thing, SmartMeterChannelTypeProvider channelProvider,
            Supplier<SerialPortManager> serialPortManagerSupplier) {
        super(thing);
        Objects.requireNonNull(channelProvider, "SmartMeterChannelTypeProvider must not be null");
        this.channelTypeProvider = channelProvider;
        this.serialPortManagerSupplier = serialPortManagerSupplier;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Smartmeter handler.");
        cancelRead();

        SmartMeterConfiguration config = getConfigAs(SmartMeterConfiguration.class);

        String port = config.port;
        logger.debug("config port = {}", port);

        if (port == null || port.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'port' is mandatory and must be configured");
        } else {
            byte[] pullSequence = config.initMessage == null ? null
                    : HexUtils.hexToBytes(StringUtils.deleteWhitespace(config.initMessage));
            int baudrate = config.baudrate == null ? Baudrate.AUTO.getBaudrate()
                    : Baudrate.fromString(config.baudrate).getBaudrate();
            this.conformity = config.conformity == null ? Conformity.NONE : Conformity.valueOf(config.conformity);
            this.smlDevice = MeterDeviceFactory.getDevice(serialPortManagerSupplier, config.mode,
                    this.thing.getUID().getAsString(), port, pullSequence, baudrate, config.baudrateChangeDelay);
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING,
                    "Waiting for messages from device");

            smlDevice.addValueChangeListener(channelTypeProvider);

            updateOBISValue();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelRead();
        if (this.valueChangeListener != null) {
            this.smlDevice.removeValueChangeListener(valueChangeListener);
        }
        if (this.channelTypeProvider != null) {
            this.smlDevice.removeValueChangeListener(channelTypeProvider);
        }
    }

    private void cancelRead() {
        if (this.valueReader != null) {
            this.valueReader.dispose();
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
        cancelRead();

        valueChangeListener = new MeterValueListener() {
            @Override
            public <Q extends @NonNull Quantity<Q>> void valueChanged(MeterValue<Q> value) {
                ThingBuilder thingBuilder = editThing();

                String obis = value.getObisCode();

                String obisChannelString = SmartMeterBindingConstants.getObisChannelId(obis);
                Channel channel = thing.getChannel(obisChannelString);
                ChannelTypeUID channelTypeId = channelTypeProvider.getChannelTypeIdForObis(obis);

                ChannelType channelType = channelTypeProvider.getChannelType(channelTypeId, null);
                if (channelType != null) {
                    String itemType = channelType.getItemType();

                    State state = getStateForObisValue(value, channel);
                    if (channel == null) {
                        logger.debug("Adding channel: {} with item type: {}", obisChannelString, itemType);

                        // channel has not been created yet
                        ChannelBuilder channelBuilder = ChannelBuilder
                                .create(new ChannelUID(thing.getUID(), obisChannelString), itemType)
                                .withType(channelTypeId);

                        Configuration configuration = new Configuration();
                        configuration.put(SmartMeterBindingConstants.CONFIGURATION_CONVERSION, 1);
                        channelBuilder.withConfiguration(configuration);
                        channelBuilder.withLabel(obis);
                        Map<String, String> channelProps = new HashMap<>();
                        channelProps.put(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                        channelBuilder.withProperties(channelProps);
                        channelBuilder.withDescription(
                                MessageFormat.format("Value for OBIS code: {0} with Unit: {1}", obis, value.getUnit()));
                        channel = channelBuilder.build();
                        ChannelUID channelId = channel.getUID();

                        // add all valid channels to the thing builder
                        List<Channel> channels = new ArrayList<>(getThing().getChannels());
                        if (channels.stream().filter((element) -> element.getUID().equals(channelId)).count() == 0) {
                            channels.add(channel);
                            thingBuilder.withChannels(channels);
                            updateThing(thingBuilder.build());
                        }
                    }

                    if (!channel.getProperties().containsKey(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS)) {
                        addObisPropertyToChannel(obis, channel);
                    }
                    if (state != null) {
                        updateState(channel.getUID(), state);
                    }

                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                } else {
                    logger.warn("No ChannelType found for OBIS {}", obis);
                }
            }

            private void addObisPropertyToChannel(String obis, Channel channel) {
                String description = channel.getDescription();
                String label = channel.getLabel();
                ChannelBuilder newChannel = ChannelBuilder.create(channel.getUID(), channel.getAcceptedItemType())
                        .withDefaultTags(channel.getDefaultTags()).withConfiguration(channel.getConfiguration())
                        .withDescription(description == null ? "" : description).withKind(channel.getKind())
                        .withLabel(label == null ? "" : label).withType(channel.getChannelTypeUID());
                Map<String, String> properties = new HashMap<>(channel.getProperties());
                properties.put(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                newChannel.withProperties(properties);
                updateThing(editThing().withoutChannel(channel.getUID()).withChannel(newChannel.build()).build());
            }

            @Override
            public <Q extends @NonNull Quantity<Q>> void valueRemoved(MeterValue<Q> value) {
                // channels that are not available are removed
                String obisChannelId = SmartMeterBindingConstants.getObisChannelId(value.getObisCode());
                logger.debug("Removing channel: {}", obisChannelId);
                ThingBuilder thingBuilder = editThing();
                thingBuilder.withoutChannel(new ChannelUID(thing.getUID(), obisChannelId));
                updateThing(thingBuilder.build());
            }

            @Override
            public void errorOccurred(Throwable e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        };
        this.smlDevice.addValueChangeListener(valueChangeListener);

        SmartMeterConfiguration config = getConfigAs(SmartMeterConfiguration.class);
        int delay = config.refresh != null ? config.refresh : DEFAULT_REFRESH_PERIOD;
        valueReader = this.smlDevice.readValues(DEFAULT_TIMEOUT, this.scheduler, Duration.ofSeconds(delay));
    }

    private void updateOBISChannel(ChannelUID channelId) {
        if (isLinked(channelId.getId())) {
            Channel channel = this.thing.getChannel(channelId.getId());
            if (channel != null) {
                String obis = channel.getProperties().get(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS);
                if (obis != null) {
                    MeterValue<?> value = this.smlDevice.getMeterValue(obis);
                    if (value != null) {
                        State state = getStateForObisValue(value, channel);
                        if (state != null) {
                            updateState(channel.getUID(), state);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private @Nullable <Q extends Quantity<Q>> State getStateForObisValue(MeterValue<?> value,
            @Nullable Channel channel) {
        Unit<?> unit = value.getUnit();
        String valueString = value.getValue();
        if (unit != null) {
            valueString += " " + value.getUnit();
        }
        State state = TypeParser.parseState(List.of(QuantityType.class, StringType.class), valueString);
        if (channel != null && state instanceof QuantityType) {
            state = applyConformity(channel, (QuantityType<Q>) state);
            Number conversionRatio = (Number) channel.getConfiguration()
                    .get(SmartMeterBindingConstants.CONFIGURATION_CONVERSION);
            if (conversionRatio != null) {
                state = ((QuantityType<?>) state).divide(BigDecimal.valueOf(conversionRatio.doubleValue()));
            }
        }
        return state;
    }

    private <Q extends Quantity<Q>> State applyConformity(Channel channel, QuantityType<Q> currentState) {
        try {
            return this.conformity.apply(channel, currentState, getThing(), this.smlDevice);
        } catch (Exception e) {
            logger.warn("Failed to apply negation for channel: {}", channel.getUID(), e);
        }
        return currentState;
    }
}
