/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal.handler;

import org.apache.commons.lang.StringEscapeUtils;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;

import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.binding.bsblan.internal.configuration.BsbLanParameterConfiguration;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterQueryResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetRequest.Type;
import org.openhab.binding.bsblan.internal.BsbLanBindingConstants;
import org.openhab.binding.bsblan.internal.BsbLanBindingConstants.Channels;
import org.openhab.binding.bsblan.internal.api.BsbLanApiCaller;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BsbLanParameterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanParameterHandler extends BsbLanBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BsbLanParameterHandler.class);
    private BsbLanParameterConfiguration parameterConfig;

    public BsbLanParameterHandler(Thing thing) {
        super(thing);
    }

    public Integer getParameterId() {
        return parameterConfig.id;
    }

    @Override
    protected String getDescription() {
        return "BSB-LAN Parameter";
    }

    @Override
    public void refresh(BsbLanBridgeConfiguration bridgeConfiguration) {
        updateChannels();
    }

    @Override
    public void initialize() {
        parameterConfig = getConfigAs(BsbLanParameterConfiguration.class);
        super.initialize();

        // validate 'setId' configuration -> fallback to value of 'id' if invalid or not specified
        if (parameterConfig.setId == null || parameterConfig.setId <= 0) {
            parameterConfig.setId = parameterConfig.id;
        }

        // validate 'setType' configuration -> fallback to 'SET' if invalid or not specified
        parameterConfig.setType = Type.getTypeWithFallback(parameterConfig.setType).toString();

        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    @Override
    protected void updateChannel(String channelId) {
        BsbLanApiParameterQueryResponse data = getBridgeHandler().getCachedParameterQueryResponse();
        updateChannel(channelId, data);
    }

    private void updateChannel(String channelId, BsbLanApiParameterQueryResponse data) {
        if (data == null) {
            logger.warn("no data available while updating channel '{}' of parameter {}", channelId, parameterConfig.id);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE,
                "No data received from BSB-LAN device");
            return;
        }

        BsbLanApiParameter parameter = data.getOrDefault(parameterConfig.id, null);
        if (parameter == null){
            logger.warn("parameter {} not part of response data", parameterConfig.id);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                String.format("No data received for parameter %s", parameterConfig.id));
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        if (!isLinked(channelId)) {
            return;
        }

        switch (channelId) {
            case BsbLanBindingConstants.Channels.Parameter.Name:
                updateNameChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.DESCRIPTION:
                updateDescriptionChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.DATATYPE:
                updateDatatypeChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.NUMBER_VALUE:
                updateNumberValueChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.STRING_VALUE:
                updateStringValueChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.SWITCH_VALUE:
                updateSwitchValueChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.UNIT:
                updateUnitChannel(parameter);
                break;

            default:
                logger.warn("unsupported channel '{}' while updating state", channelId);
        }
    }

    void updateNameChannel(BsbLanApiParameter parameter) {
        State state = new StringType(parameter.name);
        updateState(BsbLanBindingConstants.Channels.Parameter.Name, state);
    }

    void updateDescriptionChannel(BsbLanApiParameter parameter) {
        State state = new StringType(parameter.description);
        updateState(BsbLanBindingConstants.Channels.Parameter.DESCRIPTION, state);
    }

    void updateUnitChannel(BsbLanApiParameter parameter) {
        String value = StringEscapeUtils.unescapeHtml(parameter.unit);
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.UNIT, state);
    }

    void updateDatatypeChannel(BsbLanApiParameter parameter) {
        int value = parameter.dataType.getValue();
        State state = new DecimalType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.DATATYPE, state);
    }

    void updateNumberValueChannel(BsbLanApiParameter parameter) {
        try {
            State state = null;

            switch (parameter.dataType)
            {
                // parse enum data type as integer
                case DT_ENUM:
                {
                    int value = Integer.parseInt(parameter.value);
                    state = new DecimalType(value);
                }
                break;

                default:
                {
                    double value = Double.parseDouble(parameter.value);
                    state = new DecimalType(value);
                }
                break;
            }
            updateState(BsbLanBindingConstants.Channels.Parameter.NUMBER_VALUE, state);
        }
        catch (NumberFormatException e) {
            // silently ignore - there is not "tryParse"
        }
    }

    void updateStringValueChannel(BsbLanApiParameter parameter) {
        State state = new StringType(parameter.value);
        updateState(BsbLanBindingConstants.Channels.Parameter.STRING_VALUE, state);
    }

    void updateSwitchValueChannel(BsbLanApiParameter parameter) {
        // treat "0" as OFF and everything else as ON
        State state = parameter.value.equals("0") ?  OnOffType.OFF : OnOffType.ON;
        updateState(BsbLanBindingConstants.Channels.Parameter.SWITCH_VALUE, state);
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @param command the value to be set
     */
    @Override
    protected void setChannel(String channelId, Command command) {
        logger.debug("Received command '{}' for channel '{}'", command, channelId);

        if (!channelId.equals(Channels.Parameter.NUMBER_VALUE)
         && !channelId.equals(Channels.Parameter.STRING_VALUE)
         && !channelId.equals(Channels.Parameter.SWITCH_VALUE)) {
            logger.debug("Channel '{}' is read only. Ignoring command", command, channelId);
            return;
        }

        String value = null;
        switch (channelId) {
            case Channels.Parameter.NUMBER_VALUE:
                value = getValueForNumberValueChannel(command);
                break;

            case Channels.Parameter.STRING_VALUE:
                value = getValueForStringValueChannel(command);
                break;

            case Channels.Parameter.SWITCH_VALUE:
                value = getValueForSwitchValueChannel(command);
                break;

            default:
                logger.debug("Channel '{}' is read only. Ignoring command", channelId);
                return;
        }

        BsbLanApiCaller api = getApiCaller();

        boolean success = api.setParameter(parameterConfig.setId, value, Type.getTypeWithFallback(parameterConfig.setType));
        if (!success) {
            logger.warn("Failed to set parameter {} to '{}' for channel '{}'", parameterConfig.setId, value, channelId);
        }

        // refresh value
        BsbLanApiParameterQueryResponse queryResponse = api.queryParameter(parameterConfig.id);
        if (queryResponse == null) {
            logger.warn("Failed to refresh parameter {} after set request", parameterConfig.id);
            return;
        }

        updateChannel(channelId, queryResponse);
    }

    private String getValueForNumberValueChannel(Command command) {
        // more logic required?
        return command.toString();
    }

    private String getValueForStringValueChannel(Command command) {
        return command.toString();
    }

    private String getValueForSwitchValueChannel(Command command) {
        return command.equals(OnOffType.ON) ? "1" : "0";
    }
}
