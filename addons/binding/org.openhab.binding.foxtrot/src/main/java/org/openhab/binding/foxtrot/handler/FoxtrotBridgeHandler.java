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
import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.Refreshable;
import org.openhab.binding.foxtrot.internal.ValuesReceiver;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSClient;
import org.openhab.binding.foxtrot.internal.config.FoxtrotConfiguration;
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

    private PlcComSClient client;
    private ValuesReceiver receiver;
    private CommandExecutor executor;

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
            client = new PlcComSClient(conf.hostname, conf.port);
            client.open();
            updateProperties(client.getInfos());
            logger.debug("Properties succesfully updated!");

            receiver = new ValuesReceiver(client);
            receiver.start();

            executor = new CommandExecutor(client);
            executor.start();

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Can't access PlcComS server");
        }
    }

    // todo pravidelny check connectu na PlcComS server, ak chyba tak recovery

    @Override
    public void dispose() {
        logger.debug("Disposing Foxtrot PLC bridge handler connections to PLCComS server ...");

        if (executor != null) {
            executor.stop();
        }

        if (receiver != null) {
            receiver.stop();
        }

        if (client != null) {
            client.close();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Foxtrot Bridge is read-only and does not handle commands");
    }

    void register(String var, Refreshable handler) throws IOException {
        register(var, handler, null);
    }

    void register(String var, Refreshable handler, BigDecimal delta) throws IOException {
        receiver.register(var, handler, delta);
    }

    void unregister(String var) {
        receiver.unregister(var);
    }

    CommandExecutor getCommandExecutor() {
        return executor;
    }
}
