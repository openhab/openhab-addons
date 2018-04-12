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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    @Nullable
    private PlcComSClient client;
    @Nullable
    private ValuesReceiver receiver;
    @Nullable
    private CommandExecutor executor;

    private FoxtrotConfiguration conf;
    private ScheduledFuture watchDogJob;

    public FoxtrotBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        logger.debug("Initializing Foxtrot bridge handler ...");
        conf = getConfigAs(FoxtrotConfiguration.class);
        updateProperty(PROPERTY_PLCCOMS_HOST, conf.hostname);
        updateProperty(PROPERTY_PLCCOMS_PORT, String.valueOf(conf.port));

        logger.debug("Opening connections to PLCComS server at {}:{} ...", conf.hostname, conf.port);
        try {
            internalInit();

            watchDogJob = scheduler.scheduleWithFixedDelay(() -> {
                logger.trace("Foxtrot PlcComS client liveness check ...");
                if (client.isClosed() || !receiver.isRunning() || !executor.isRunning()) {
                    try {
                        internalDispose();
                        sleep(10000);
                        internalInit();
                    }  catch (IOException e) {
                        logger.error("Attempt to initialize PlcComS client failed!", e);
                    }
                }
            }, 1, 1, TimeUnit.MINUTES);

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Can't access PlcComS server");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Foxtrot PLC bridge handler connections to PLCComS server ...");
        if (watchDogJob != null && !watchDogJob.isCancelled()) {
            watchDogJob.cancel(true);
        }

        // sleep for a while to give time other things to unregister properly
        sleep(3000);

        internalDispose();
    }

    private void internalInit() throws IOException {
        client = new PlcComSClient(conf.hostname, conf.port);
        client.open();
        updateProperties(client.getInfos());

        receiver = new ValuesReceiver(client);
        receiver.start();

        executor = new CommandExecutor(client);
        executor.start();
    }

    private void internalDispose() {
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

    void register(String var, Refreshable handler, @Nullable BigDecimal delta) throws IOException {
        receiver.register(var, handler, delta);
    }

    void unregister(String var) {
        receiver.unregister(var);
    }

    CommandExecutor getCommandExecutor() {
        return executor;
    }

    private void sleep(long milis) {
        try { Thread.sleep(milis); } catch (InterruptedException ignored) { }
    }
}
