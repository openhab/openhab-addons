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

//import org.apache.commons.text.StringEscapeUtils;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;

import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.binding.bsblan.internal.configuration.BsbLanParameterConfiguration;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterQueryResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetResult;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetRequest;
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
public class BsbLanParameterThingHandler extends BsbLanBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BsbLanParameterThingHandler.class);
    private BsbLanParameterConfiguration parameterConfig;

    public BsbLanParameterThingHandler(Thing thing) {
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
        if (parameterConfig.setType == null) {
            parameterConfig.setType = BsbLanApiParameterSetRequest.Type.SET;
        }
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
            // todo: add log entry - no data received
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        BsbLanApiParameter parameter = data.getOrDefault(parameterConfig.id, null);
        if (parameter == null){
            // todo: add log entry - parameter not contained in response
            updateStatus(ThingStatus.OFFLINE);
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

            case BsbLanBindingConstants.Channels.Parameter.Description:
                updateDescriptionChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.DataType:
                updateDatatypeChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.NumberValue:
                updateNumberValueChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.StringValue:
                updateStringValueChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.SwitchValue:
                updateSwitchValueChannel(parameter);
                break;

            case BsbLanBindingConstants.Channels.Parameter.Unit:
                updateUnitChannel(parameter);
                break;

            default:
                logger.warn("unsupported channel '{}' while updating state", channelId);
        }
    }

    void updateNameChannel(BsbLanApiParameter parameter) {
        String value = parameter.getName();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.Name, state);
    }

    void updateDescriptionChannel(BsbLanApiParameter parameter) {
        String value = parameter.getDescription();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.Description, state);
    }

    void updateUnitChannel(BsbLanApiParameter parameter) {
        // String value = StringEscapeUtils.unescapeHtml4(parameter.getUnit());
        String value = parameter.getUnit();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.Unit, state);
    }

    void updateDatatypeChannel(BsbLanApiParameter parameter) {
        int value = parameter.getDataType().getValue();
        State state = new DecimalType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.DataType, state);
    }

    void updateNumberValueChannel(BsbLanApiParameter parameter) {
        try {
            double value = Double.parseDouble(parameter.getValue());
            State state = new DecimalType(value);
            updateState(BsbLanBindingConstants.Channels.Parameter.NumberValue, state);
        }
        catch (NumberFormatException e) {
            // silently ignore - there is not "tryParse"
        }
    }

    void updateStringValueChannel(BsbLanApiParameter parameter) {
        String value = parameter.getValue();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.Channels.Parameter.StringValue, state);
    }

    void updateSwitchValueChannel(BsbLanApiParameter parameter) {
        // treat "0" as OFF and everything else as ON
        State state = parameter.getValue().equals("0") ?  OnOffType.OFF : OnOffType.ON;
        updateState(BsbLanBindingConstants.Channels.Parameter.SwitchValue, state);
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

        if (!channelId.equals(Channels.Parameter.NumberValue)
         && !channelId.equals(Channels.Parameter.StringValue)
         && !channelId.equals(Channels.Parameter.SwitchValue)) {
            logger.debug("Channel '{}' is read only. Ignoring command", command, channelId);
            return;
        }

        String value = null;
        switch (channelId) {
            case Channels.Parameter.NumberValue:
                value = getValueForNumberValueChannel(command);
                break;

            case Channels.Parameter.StringValue:
                value = getValueForStringValueChannel(command);
                break;

            case Channels.Parameter.SwitchValue:
                value = getValueForSwitchValueChannel(command);
                break;

            default:
                logger.debug("Channel '{}' is read only. Ignoring command", channelId);
                return;
        }

        BsbLanApiCaller api = getApiCaller();

        BsbLanApiParameterSetResponse setResponse = api.setParameter(parameterConfig.setId, value, parameterConfig.setType);
        if (setResponse == null) {
            logger.warn("Failed to set parameter {} to '{}' for channel '{}': no response received", parameterConfig.setId, value, channelId);
            return;
        }

        BsbLanApiParameterSetResult result = setResponse.getOrDefault(parameterConfig.setId, null);
        if (result == null){
            logger.warn("Failed to set parameter {} to '{}' for channel '{}': result is null", parameterConfig.setId, value, channelId);
            return;
        }
        if (result.status == null) {
            logger.warn("Failed to set parameter {} to '{}' for channel '{}': status is null", parameterConfig.setId, value, channelId);
            return;
        }
        if (result.status != BsbLanApiParameterSetResult.Status.SUCCESS) {
            logger.warn("Failed to set parameter {} to '{}' for channel '{}': Status = {}", parameterConfig.setId, value, channelId, result.status);
            return;
        }

        // refresh value
        BsbLanApiParameterQueryResponse queryResponse = api.queryParameter(parameterConfig.id);
        if (queryResponse == null) {
            logger.warn("Failed to update parameter {} after set request", parameterConfig.id);
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
