/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.ItemUtil;
import org.eclipse.smarthome.core.library.CoreItemFactory;
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
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.openhab.binding.gruenbecksoftener.data.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerInputData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoftenerThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault
public class SoftenerThingHandler extends BaseThingHandler {

    private static final String CHANNEL_PROPERTIES_CODE = "code";

    private Logger logger = LoggerFactory.getLogger(SoftenerThingHandler.class);

    private static final String BASE64_ENCODED_CHANNEL = "base64";

    private static final Function<String, SoftenerXmlResponse> RESPONSE_PARSER_FUNCTION = new XmlResponseParser();
    private static final ResponseFunction SOFTENER_RESPONSE_FUNCTION = new HttpResponseFunction();

    private @NonNullByDefault({}) SoftenerHandler softenerHandler;
    private @NonNullByDefault({}) SoftenerXmlResponse softenerResponse;
    private ChannelTypeRegistry channelTypeRegistry;

    public SoftenerThingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Grünbeck Softener handler.");

        softenerHandler = new SoftenerHandler(scheduler);
        SoftenerConfiguration config = getConfigAs(SoftenerConfiguration.class);
        logger.debug("config host = {}", config.host);
        logger.debug("config refresh = {}", config.refresh);

        String errorMsg = null;
        if (config.refresh != null && config.refresh < 5) {
            errorMsg = "Parameter 'refresh' must be at least 5 minutes";
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Waiting for reading from device");
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    /**
     * Start the job refreshing the Softener data
     */
    private void startAutomaticRefresh() {
        this.softenerHandler.startAutomaticRefresh(getConfigAs(SoftenerConfiguration.class), SOFTENER_RESPONSE_FUNCTION,
                RESPONSE_PARSER_FUNCTION, response -> {
                    // Update all channels from the updated Softener response data
                    softenerResponse = response;
                    for (Channel channel : getThing().getChannels()) {
                        if (response.getData().containsKey(channel.getUID().getIdWithoutGroup())) {
                            try {
                                updateChannel(channel.getUID(), response);
                                updateStatus(ThingStatus.ONLINE);
                            } catch (RuntimeException e) {
                                logger.warn("Update of channel {} failed with data {}.", channel, response, e);
                            }
                        }
                    }
                },
                exception -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        exception.getLocalizedMessage()),
                () -> getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID())).map(channel -> {
                    SoftenerInputData softenerInputData = new SoftenerInputData();
                    softenerInputData.setDatapointId(channel.getUID().getIdWithoutGroup());
                    softenerInputData.setCode(channel.getProperties().get(CHANNEL_PROPERTIES_CODE));
                    String groupId = channel.getUID().getGroupId();
                    if (groupId != null) {
                        softenerInputData.setGroup(groupId);
                    }
                    return softenerInputData;
                }));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Grünbeck Softener handler.");

        if (this.softenerHandler != null) {
            this.softenerHandler.stopRefresh();
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
            editData.setCode(channel.getProperties().get(CHANNEL_PROPERTIES_CODE));
            String pattern;
            ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
            if (channelType != null) {
                pattern = channelType.getState().getPattern();
            } else {
                pattern = null;
            }
            String value = pattern != null ? state.format(pattern) : state.toFullString();
            boolean isBase64 = channelType != null && channelType.getTags().contains(BASE64_ENCODED_CHANNEL);
            if (isBase64) {
                value = Base64.getEncoder().encodeToString(value.getBytes());
            }
            editData.setValue(value);
            SoftenerXmlResponse response;
            try {
                response = softenerHandler.editParameter(SOFTENER_RESPONSE_FUNCTION,
                        getConfigAs(SoftenerConfiguration.class), editData, RESPONSE_PARSER_FUNCTION);
                logger.debug("Data {} was successfully set to {}", channelUID, response);
                updateChannel(channelUID, response);
            } catch (IOException e) {
                logger.warn("Failed to set the value {}", editData, e);
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
                ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID != null) {
                    State state = getValue(channelId, channelTypeUID, softenerResponse);
                    logger.debug("Update channel {} with state {}", channelId, state.toString());
                    // Update the channel
                    updateState(channelId, state);
                }
            }
        }
    }

    private State getValue(ChannelUID channelId, ChannelTypeUID channelTypeId, SoftenerXmlResponse data) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeId);
        boolean isBase64 = channelType != null && channelType.getTags().contains(BASE64_ENCODED_CHANNEL);

        String value = data.getData().get(channelId.getIdWithoutGroup());
        value = value.trim();
        if (isBase64 && !value.isEmpty() && !value.equals("-")) {
            value = new String(Base64.getDecoder().decode(value));
        }
        List<Class<? extends State>> acceptedTypes;
        if (channelType != null && CoreItemFactory.NUMBER.equals(ItemUtil.getMainItemType(channelType.getItemType()))) {
            acceptedTypes = Arrays.asList(QuantityType.class, DecimalType.class);
            if (value.equals("-")) {
                value = "0";
            }
        } else {
            acceptedTypes = Arrays.asList(DateTimeType.class, StringType.class);
        }
        Unit<?> unit = channelType != null ? UnitUtils.parseUnit(channelType.getState().getPattern()) : null;
        State state = TypeParser.parseState(acceptedTypes, value + (unit != null ? " " + unit.toString() : ""));
        return state;
    }

}
