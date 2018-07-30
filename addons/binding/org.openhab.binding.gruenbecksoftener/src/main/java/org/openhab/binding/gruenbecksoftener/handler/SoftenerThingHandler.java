/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.handler;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.binding.gruenbecksoftener.SoftenerBindingConstants;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.openhab.binding.gruenbecksoftener.json.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.json.SoftenerXmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoftenerThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
public class SoftenerThingHandler extends BaseThingHandler {

    private static final Function<String, SoftenerXmlResponse> RESPONSE_PARSER_FUNCTION = new XmlResponseParser();

    private static final ResponseFunction SOFTENER_RESPONSE_FUNCTION = new HttpResponseFunction();

    private static final String SOFTENER_UNIT_ID = "D_C_2_1";

    private String hardnessUnit;

    private Logger logger = LoggerFactory.getLogger(SoftenerThingHandler.class);

    private SoftenerHandler softenerHandler;

    private Supplier<Boolean> cancelHandler;

    private SoftenerXmlResponse softenerResponse;

    private ItemFactory itemFactory;

    private ChannelTypeRegistry channelTypeRegistry;

    public SoftenerThingHandler(Thing thing, ItemFactory itemFactory, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.itemFactory = itemFactory;
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Grünbeck Softener handler.");

        softenerHandler = new SoftenerHandler();

        SoftenerConfiguration config = getConfigAs(SoftenerConfiguration.class);
        logger.debug("config host = {}", config.host);
        logger.debug("config refresh = {}", config.refresh);

        String errorMsg = null;

        if (config.refresh != null && config.refresh < 5) {
            errorMsg = "Parameter 'refresh' must be at least 5 minutes";
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.ONLINE);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job refreshing the Softener data
     */
    private void startAutomaticRefresh() {

        cancelHandler = this.softenerHandler.startAutomaticRefresh(getConfigAs(SoftenerConfiguration.class),
                SOFTENER_RESPONSE_FUNCTION, RESPONSE_PARSER_FUNCTION, scheduler, response -> {
                    // Update all channels from the updated Softener response data
                    softenerResponse = response;
                    for (Channel channel : getThing().getChannels()) {
                        if (response.getData().containsKey(channel.getUID().getIdWithoutGroup())) {
                            updateChannel(channel.getUID(), response);
                        }
                    }
                },
                exception -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        exception.getLocalizedMessage()),
                () -> getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID())).map(channel -> {
                    SoftenerInputData softenerInputData = new SoftenerInputData();
                    softenerInputData.setDatapointId(channel.getUID().getIdWithoutGroup());
                    softenerInputData
                            .setDatatype(SoftenerDataType.fromItemType(channel.getAcceptedItemType(), itemFactory));
                    softenerInputData.setCode(channel.getProperties().get("code"));
                    softenerInputData.setGroup(channel.getUID().getGroupId());
                    return softenerInputData;
                }));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Grünbeck Softener handler.");

        if (this.cancelHandler != null) {
            this.cancelHandler.get();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID, softenerResponse);
        } else {
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel == null) {
                throw new IllegalArgumentException("Channel with ID " + channelUID + " not known");
            }
            State state = (State) command;
            SoftenerEditData editData = new SoftenerEditData();
            editData.setDatapointId(channelUID.getIdWithoutGroup());
            String pattern;
            ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
            if (channelType != null) {
                pattern = channelType.getState().getPattern();
            } else {
                pattern = null;
            }
            String value = pattern != null ? state.format(pattern) : state.toFullString();
            editData.setValue(value);
            SoftenerXmlResponse response;
            try {
                response = SOFTENER_RESPONSE_FUNCTION.editParameter(getConfigAs(SoftenerConfiguration.class), editData,
                        RESPONSE_PARSER_FUNCTION);
                logger.debug("Data {} was successfully set to {}", channelUID, response.getData());
                updateChannel(channelUID, response);
            } catch (Exception e) {
                throw new RuntimeException("Data could not be set " + channelUID, e);
            }
        }
    }

    /**
     * Update the channel from the last Softener data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(ChannelUID channelId, SoftenerXmlResponse softenerResponse) {
        if (isLinked(channelId)) {
            Channel channel = getThing().getChannel(channelId.getId());
            if (channel != null) {

                extractHardnessUnit(softenerResponse);
                State state = getValue(channelId, channel.getChannelTypeUID(), softenerResponse);
                logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());

                // Update the channel
                if (state != null) {
                    updateState(channelId, state);
                }
            }

        }
    }

    private State getValue(ChannelUID channelId, ChannelTypeUID channelTypeId, SoftenerXmlResponse data) {
        if (data != null) {
            ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeId);
            boolean isHardnessUnitChannel = channelType != null
                    && channelType.getTags().contains(SoftenerBindingConstants.HARDNESS_UNIT_CONFIGURATION);
            String value = data.getData().get(channelId.getIdWithoutGroup());
            if (value != null) {
                value = value.trim();
                String unitToAssign;
                if (isHardnessUnitChannel) {
                    unitToAssign = getHardnessUnit();
                } else {
                    unitToAssign = null;
                }
                if (unitToAssign != null) {
                    value += " " + unitToAssign;
                }
                State state = TypeParser.parseState(
                        Arrays.asList(DateTimeType.class, QuantityType.class, DecimalType.class, StringType.class),
                        value);
                return state;
            }
        }
        return null;
    }

    private String getHardnessUnit() {
        if (hardnessUnit != null) {
            return hardnessUnit;
        }
        SoftenerInputData softenerInputData = new SoftenerInputData();
        softenerInputData.setDatapointId(SOFTENER_UNIT_ID);
        softenerInputData.setDatatype(SoftenerDataType.NUMBER);

        BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> responseFunction = SOFTENER_RESPONSE_FUNCTION
                .getResponseFunction(RESPONSE_PARSER_FUNCTION, this::extractHardnessUnit);
        responseFunction.accept(getConfigAs(SoftenerConfiguration.class), Stream.of(softenerInputData));
        return getHardnessUnit();
    }

    private void extractHardnessUnit(SoftenerXmlResponse data) {
        String defaultUnit = data.getData().get(SOFTENER_UNIT_ID);
        if (defaultUnit != null) {
            getThing().getChannels().stream()
                    .filter(channel -> channel.getUID().getIdWithoutGroup().equals(SOFTENER_UNIT_ID)).findFirst()
                    .ifPresent(channel -> {
                        ChannelType hardnessUnitchannelType = channelTypeRegistry
                                .getChannelType(channel.getChannelTypeUID());
                        if (hardnessUnitchannelType != null) {
                            hardnessUnitchannelType.getState().getOptions().stream()
                                    .filter(option -> option.getValue().equals(defaultUnit)).findFirst()
                                    .ifPresent(option -> hardnessUnit = option.getLabel());
                        }
                    });
        }
    }

}
