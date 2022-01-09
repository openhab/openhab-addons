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
package org.openhab.binding.bsblan.internal.handler;

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bsblan.internal.api.BsbLanApiCaller;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterQueryResponseDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetRequestDTO.Type;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.binding.bsblan.internal.configuration.BsbLanParameterConfiguration;
import org.openhab.binding.bsblan.internal.helper.BsbLanParameterConverter;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BsbLanParameterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanParameterHandler extends BsbLanBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BsbLanParameterHandler.class);
    private BsbLanParameterConfiguration parameterConfig = new BsbLanParameterConfiguration();

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

        // it will take up to refreshInterval seconds until we receive a value and thing goes online
        // see notes in {@link BsbLanBridgeHandler#registerThing(BsbLanBaseThingHandler)}
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    @Override
    protected void updateChannel(String channelId) {
        BsbLanApiParameterQueryResponseDTO data = null;
        BsbLanBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            data = bridgeHandler.getCachedParameterQueryResponse();
        }
        updateChannel(channelId, data);
    }

    private void updateChannel(String channelId, @Nullable BsbLanApiParameterQueryResponseDTO data) {
        if (data == null) {
            logger.debug("no data available while updating channel '{}' of parameter {}", channelId,
                    parameterConfig.id);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE,
                    "No data received from BSB-LAN device");
            return;
        }

        BsbLanApiParameterDTO parameter = data.getOrDefault(parameterConfig.id, null);
        if (parameter == null) {
            logger.debug("parameter {} is not part of response data while updating channel '{}' ", parameterConfig.id,
                    channelId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("No data received for parameter %s", parameterConfig.id));
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        if (!isLinked(channelId)) {
            return;
        }

        State state = BsbLanParameterConverter.getState(channelId, parameter);
        if (state == null) {
            return;
        }

        updateState(channelId, state);
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @param command the value to be set
     */
    @Override
    protected void setChannel(String channelId, Command command) {
        logger.trace("Received command '{}' for channel '{}'", command, channelId);

        if (!WRITEABLE_CHANNELS.contains(channelId)) {
            logger.warn("Channel '{}' is read only. Ignoring command", channelId);
            return;
        }

        String value = BsbLanParameterConverter.getValue(channelId, command);
        if (value == null) {
            logger.warn("Channel '{}' is read only or conversion failed. Ignoring command", channelId);
            return;
        }

        BsbLanApiCaller api = getApiCaller();
        if (api == null) {
            logger.debug("Failed to set parameter {} (API unavailable)", parameterConfig.setId);
            return;
        }

        boolean success = api.setParameter(parameterConfig.setId, value,
                Type.getTypeWithFallback(parameterConfig.setType));
        if (!success) {
            logger.debug("Failed to set parameter {} to '{}' for channel '{}'", parameterConfig.setId, value,
                    channelId);
        }

        // refresh value
        BsbLanApiParameterQueryResponseDTO queryResponse = api.queryParameter(parameterConfig.id);
        if (queryResponse == null) {
            logger.debug("Failed to refresh parameter {} after set request", parameterConfig.id);
            return;
        }

        updateChannel(channelId, queryResponse);
    }
}
