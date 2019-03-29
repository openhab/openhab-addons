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

import static org.openhab.binding.net.internal.NetBindingConstants.CHANNEL_PARAM_TRANSFORM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.net.internal.DataListener;
import org.openhab.binding.net.internal.automation.modules.NetThingActionsService;
import org.openhab.binding.net.internal.config.DataConfiguration;
import org.openhab.binding.net.internal.transformation.ChannelStateTransformation;
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
    private DataConfiguration config;
    private final Map<String, List<ChannelStateTransformation>> transformations = new HashMap<>();

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
        initializeBridge((getBridge() == null) ? null : getBridge().getHandler(),
                (getBridge() == null) ? null : getBridge().getStatus());

        for (final Channel channel : getThing().getChannels()) {
            logger.debug("Channel label '{}' : params: {}", channel.getLabel(), channel.getConfiguration());

            String transform = (String) channel.getConfiguration().get(CHANNEL_PARAM_TRANSFORM);

            final List<ChannelStateTransformation> transforms = new ArrayList<>();

            // Incoming value transformations
            String[] s = transform.split(TRANSFORMATION_SEPARATOR);
            Stream.of(s).filter(t -> StringUtils.isNotBlank(t))
                    .map(t -> new ChannelStateTransformation(t, transformationServiceProvider))
                    .forEach(t -> transforms.add(t));

            transformations.put(channel.getUID().toString(), transforms);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeBridge((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeBridge(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (bridgeStatus != null) {
            config = getConfigAs(DataConfiguration.class);
            if (thingHandler != null && bridgeStatus != null) {
                bridgeHandler = (AbstractServerBridge) thingHandler;
                bridgeHandler.registerDataListener(this);

                if (bridgeStatus == ThingStatus.ONLINE) {
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

    public void injectData(Object data) {
        for (final Channel channel : getThing().getChannels()) {
            logger.debug("Channel '{}' : params: {}", channel.getUID(), channel.getConfiguration());
            String sdata;
            if (data instanceof String) {
                sdata = (String) data;
            } else if (data instanceof byte[]) {
                sdata = HexUtils.bytesToHex((byte[]) data);
            } else {
                sdata = "";
            }

            final List<ChannelStateTransformation> list = transformations.get(channel.getUID().toString());
            for (ChannelStateTransformation t : list) {
                sdata = t.processValue(sdata);
            }

            updateState(channel.getUID(), new StringType(sdata));
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(NetThingActionsService.class);
    }
}
