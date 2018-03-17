/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.PlcComSClient;
import org.openhab.binding.foxtrot.internal.config.FoxtrotConfiguration;
import org.openhab.binding.foxtrot.internal.RefreshGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FoxtrotBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Radovan Sninsky - Initial contribution
 */
@NonNullByDefault
public class FoxtrotBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FoxtrotBridgeHandler.class);

    private final RefreshGroup lowRefreshGroup = new RefreshGroup(RefreshGroup.RG_LOW);
    private final RefreshGroup mediumRefreshGroup = new RefreshGroup(RefreshGroup.RG_MEDIUM);
    private final RefreshGroup highRefreshGroup = new RefreshGroup(RefreshGroup.RG_HIGH);
    private final RefreshGroup realtimeRefreshGroup = new RefreshGroup(RefreshGroup.RG_REALTIME);

    public FoxtrotBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        logger.debug("Initializing Foxtrot bridge handler ...");
        FoxtrotConfiguration conf = getConfigAs(FoxtrotConfiguration.class);
        updateProperty(PROPERTY_PLCCOMS_HOST, conf.hostname);
        updateProperty(PROPERTY_PLCCOMS_PORT, String.valueOf(conf.port));

        logger.debug("Opening connections to PLCComS server at {}:{} ...", conf.hostname, conf.port);
        try {
            updateProperties(new PlcComSClient(conf.hostname, conf.port));

            lowRefreshGroup.start(scheduler, conf.lowRefreshInterval, new PlcComSClient(conf.hostname, conf.port));
            mediumRefreshGroup.start(scheduler, conf.mediumRefreshInterval, new PlcComSClient(conf.hostname, conf.port));
            highRefreshGroup.start(scheduler, conf.highRefreshInterval, new PlcComSClient(conf.hostname, conf.port));
            realtimeRefreshGroup.start(scheduler, conf.realtimeRefreshInterval, new PlcComSClient(conf.hostname, conf.port));

            CommandExecutor.init(new PlcComSClient(conf.hostname, conf.port));

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Can't access PlcComS server");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Foxtrot PLC bridge handler connections to PLCComS server ...");
        lowRefreshGroup.dispose();
        mediumRefreshGroup.dispose();
        highRefreshGroup.dispose();
        realtimeRefreshGroup.dispose();

        if (CommandExecutor.get() != null) {
            CommandExecutor.get().dispose();
        }
    }

    RefreshGroup findByName(String name) {
        if (RefreshGroup.RG_MEDIUM.equalsIgnoreCase(name)) {
            return mediumRefreshGroup;
        } else if (RefreshGroup.RG_HIGH.equalsIgnoreCase(name)) {
            return highRefreshGroup;
        } else if (RefreshGroup.RG_REALTIME.equalsIgnoreCase(name)) {
            return realtimeRefreshGroup;
        }
        return lowRefreshGroup;
    }

    private void updateProperties(PlcComSClient client) throws IOException {
        try {
            client.open();
            if (client.isOpen()) {
                Map<String, String> properties = editProperties();
                properties.put(PROPERTY_PLCCOMS_VERSION, client.getInfo("version"));
                properties.put(PROPERTY_PLCCOM_EPSNET_VERSION, client.getInfo("version_epsnet"));
                properties.put(PROPERTY_PLCCOM_INI_VERSION, client.getInfo("version_ini"));
                properties.put(PROPERTY_PLC_VERSION, client.getInfo("version_plc"));
                properties.put(PROPERTY_PLC_IP, client.getInfo("ipaddr_plc"));

                updateProperties(properties);
                logger.debug("Properties succesfully updated!");
            }
        } finally {
            client.close();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Foxtrot Bridge is read-only and does not handle commands");
    }
}
