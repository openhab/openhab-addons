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

import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.binding.bsblan.internal.configuration.BsbLanParameterConfiguration;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterQueryResponse;
import org.openhab.binding.bsblan.internal.BsbLanBindingConstants;
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
        return parameterConfig.parameterId;
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
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @return the last retrieved data
     */
    @Override
    protected void updateChannel(String channelId) {
        BsbLanApiParameterQueryResponse data = getBridgeHandler().getCachedParameterQueryResponse();
        if (data == null) {
            // todo: add log entry - no data received
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        BsbLanApiParameter parameter = data.getParameters().getOrDefault(parameterConfig.parameterId.toString(), null);
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
            case BsbLanBindingConstants.ParameterName:
                updateNameChannel(parameter);
                break;

            case BsbLanBindingConstants.ParameterDescription:
                updateDescriptionChannel(parameter);
                break;

            case BsbLanBindingConstants.ParameterDataType:
                updateDatatypeChannel(parameter);
                break;

            case BsbLanBindingConstants.ParameterNumberValue:
                updateNumberValueChannel(parameter);
                break;

            case BsbLanBindingConstants.ParameterStringValue:
                updateStringValueChannel(parameter);
                break;

            case BsbLanBindingConstants.ParameterSwitchValue:
                updateSwitchValueChannel(parameter);
                break;

            case BsbLanBindingConstants.ParameterUnit:
                updateUnitChannel(parameter);
                break;

            default:
                logger.warn("unsupported channel '{}' while updating state", channelId);
        }
    }

    void updateNameChannel(BsbLanApiParameter parameter) {
        String value = parameter.getName();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.ParameterName, state);
    }

    void updateDescriptionChannel(BsbLanApiParameter parameter) {
        String value = parameter.getDescription();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.ParameterDescription, state);
    }

    void updateUnitChannel(BsbLanApiParameter parameter) {
        // String value = StringEscapeUtils.unescapeHtml4(parameter.getUnit());
        String value = parameter.getUnit();
        State state = new StringType(value);
        updateState(BsbLanBindingConstants.ParameterUnit, state);
    }

    void updateDatatypeChannel(BsbLanApiParameter parameter) {
        int value = parameter.getDataType().getValue();
        State state = new DecimalType(value);
        updateState(BsbLanBindingConstants.ParameterDataType, state);
    }

    void updateNumberValueChannel(BsbLanApiParameter parameter) {
        try {
            double value = Double.parseDouble(parameter.getValue());
            State state = new DecimalType(value);
            updateState(BsbLanBindingConstants.ParameterNumberValue, state);
        }
        catch (NumberFormatException e) {
            // silently ignore - there is not "tryParse"
        }
    }

    void updateStringValueChannel(BsbLanApiParameter parameter) {
        State state = new DecimalType(parameter.getValue());
        updateState(BsbLanBindingConstants.ParameterStringValue, state);
    }

    void updateSwitchValueChannel(BsbLanApiParameter parameter) {
        // treat "0" as OFF and everything else as ON
        State state = parameter.getValue() == "0" ?  OnOffType.OFF : OnOffType.ON;
        updateState(BsbLanBindingConstants.ParameterSwitchValue, state);
    }
}
