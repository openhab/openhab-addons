/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.server;

import java.io.IOException;

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
    private HomematicConfig config;
    private RpcEventListener listener;

    public BinRpcServer(RpcEventListener listener, HomematicConfig config) {
        this.listener = listener;
        this.config = config;
    }

    @Override
    public void start() throws IOException {
        logger.debug("Initializing BIN-RPC server at port {}", config.getBinCallbackPort());

        networkService = new BinRpcNetworkService(listener, config);
        networkServiceThread = new Thread(networkService);
        networkServiceThread.setName("HomematicRpcServer");
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
