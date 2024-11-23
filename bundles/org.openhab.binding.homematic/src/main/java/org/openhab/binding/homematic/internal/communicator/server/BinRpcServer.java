/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.server;

import java.io.IOException;

import org.openhab.binding.homematic.internal.HomematicBindingConstants;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server implementation for receiving messages via BIN-RPC from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BinRpcServer implements RpcServer {
    private final Logger logger = LoggerFactory.getLogger(BinRpcServer.class);

    private Thread networkServiceThread;
    private BinRpcNetworkService networkService;
    private final HomematicConfig config;
    private final RpcEventListener listener;
    private final String id;

    public BinRpcServer(RpcEventListener listener, HomematicConfig config, String id) {
        this.listener = listener;
        this.config = config;
        this.id = id;
    }

    @Override
    public void start() throws IOException {
        logger.debug("Initializing BIN-RPC server at port {}", config.getBinCallbackPort());

        networkService = new BinRpcNetworkService(listener, config);
        networkServiceThread = new Thread(networkService);
        networkServiceThread
                .setName("OH-binding-" + HomematicBindingConstants.THING_TYPE_BRIDGE + ":" + id + "-rpcServer");
        networkServiceThread.start();
    }

    @Override
    public void shutdown() {
        if (networkService != null) {
            logger.debug("Stopping BIN-RPC server");
            try {
                if (networkServiceThread != null) {
                    networkServiceThread.interrupt();
                }
            } catch (Exception e) {
                logger.error("{}", e.getMessage(), e);
            }
            networkService.shutdown();
            networkService = null;
        }
    }
}
