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
package org.openhab.binding.net.internal.handler;

import static org.openhab.binding.net.internal.NetBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.net.internal.DataListener;
import org.openhab.binding.net.internal.automation.modules.NetThingActionsService;
import org.openhab.binding.net.internal.config.DataHandlerConfiguration;
import org.openhab.binding.net.internal.transformation.DataTransformation;
import org.openhab.binding.net.internal.transformation.TransformationServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DataHandler extends BaseThingHandler implements DataListener {
    private static final String TRANSFORMATION_SEPARATOR = "∩";

    private final Logger logger = LoggerFactory.getLogger(DataHandler.class);

    private final TransformationServiceProvider transformationServiceProvider;
    private AbstractServerBridge bridgeHandler;
    private DataHandlerConfiguration configuration;
    private final List<DataTransformation> thingTransformations = new ArrayList<>();
    private final Map<String, List<DataTransformation>> channelTransformations = new HashMap<>();

    public DataHandler(@NonNull Thing thing, TransformationServiceProvider transformationServiceProvider) {
        super(thing);
        this.transformationServiceProvider = transformationServiceProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Unsupported command '{}' received for channel '{}'", command, channelUID);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());

        configuration = getConfigAs(DataHandlerConfiguration.class);
        logger.debug("Using configuration: {}", configuration);

        initializeTransformationRules();
        initializeBridge(getBridge());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeBridge(getBridge());
    }

    private void initializeBridge(Bridge bridge) {
        if (bridge != null) {
            logger.debug("initializeBridge {} for thing {}", bridge.getStatus(), getThing().getUID());

            if (bridge.getHandler() != null) {
                bridgeHandler = (AbstractServerBridge) bridge.getHandler();
                bridgeHandler.registerDataListener(this);

                if (bridge.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            // bridge is not defined, assume stand-alone data parser
            logger.debug("Stand-alone data handler for thing {}", getThing().getUID());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDataListener(this);
        }
        bridgeHandler = null;
        super.dispose();
    }

    @Override
    public void dataReceived(ThingUID bridge, Object data) {
        logger.debug("Received data from bridge '{}'", bridge);
        injectData(data);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(NetThingActionsService.class);
    }

    public void injectData(Object data) {
        final String strData;

        if (data instanceof String) {
            strData = doThingTransformation((String) data);
        } else if (data instanceof byte[]) {
            strData = doThingTransformation(HexUtils.bytesToHex((byte[]) data));
        } else {
            logger.debug("Unsupported data type '{}' received", data.getClass().getName());
            return;
        }

        for (Channel channel : getThing().getChannels()) {
            updateState(channel.getUID(), convertStateFromString(channel, doChannelTransformation(channel, strData)));
        }
    }

    private State convertStateFromString(Channel channel, String str) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID != null) {
            return TypeParser.parseState(CHANNEL_STATE_TYPES.get(channelTypeUID.getId()), str);
        }
        return null;
    }

    private String doThingTransformation(String data) {
        String strData = data;
        for (DataTransformation t : thingTransformations) {
            strData = t.processValue(strData);
        }
        return strData;
    }

    private String doChannelTransformation(Channel channel, String data) {
        logger.debug("Channel '{}' : params: {}", channel.getUID(), channel.getConfiguration());
        String strData = data;
        final List<DataTransformation> list = channelTransformations.get(channel.getUID().toString());
        for (DataTransformation t : list) {
            strData = t.processValue(strData);
        }
        return strData;
    }

    private void initializeTransformationRules() {
        thingTransformations.clear();
        if (configuration.transform != null) {
            String[] s = configuration.transform.split(TRANSFORMATION_SEPARATOR);
            Stream.of(s).filter(t -> StringUtils.isNotBlank(t))
                    .map(t -> new DataTransformation(t, transformationServiceProvider))
                    .forEach(t -> thingTransformations.add(t));
        }

        for (final Channel channel : getThing().getChannels()) {
            logger.debug("Channel label '{}' : params: {}", channel.getLabel(), channel.getConfiguration());
            String transform = (String) channel.getConfiguration().get(CHANNEL_PARAM_TRANSFORM);
            if (transform != null) {
                final List<DataTransformation> transforms = new ArrayList<>();
                String[] s = transform.split(TRANSFORMATION_SEPARATOR);
                Stream.of(s).filter(t -> StringUtils.isNotBlank(t))
                        .map(t -> new DataTransformation(t, transformationServiceProvider))
                        .forEach(t -> transforms.add(t));
                channelTransformations.put(channel.getUID().toString(), transforms);
            }
        }
    }
}
