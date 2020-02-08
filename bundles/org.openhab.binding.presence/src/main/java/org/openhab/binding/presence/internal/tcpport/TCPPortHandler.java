/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.presence.internal.tcpport;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.presence.internal.BaseHandler;
import org.openhab.binding.presence.internal.binding.PresenceBindingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TCPPortHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public class TCPPortHandler extends BaseHandler {

    private final Logger logger = LoggerFactory.getLogger(TCPPortHandler.class);

    private @NonNullByDefault({}) TCPPortConfiguration config;

    public TCPPortHandler(Thing thing, PresenceBindingConfiguration bindingConfiguration) {
        super(thing, 2, bindingConfiguration);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing");
        config = getConfigAs(TCPPortConfiguration.class);

        super.initialize();

        updateStatus(ThingStatus.ONLINE);

        logger.debug("Finished initializing");
    }

    /*
     * This is the main process for detecting the presence of a service on a port.
     * It uses an asynchronous socket to attempt to connect to the specified port. If a connection is made
     * then the service is considered ON.
     *
     * If the underlying Future that this method is running under is
     * cancelled, then we will get an InterruptedException.
     * This only happens when the Thing is being disposed of.
     */
    @Override
    protected void getStatus() {
        logger.debug("starting TCP port detection process for {}", config.hostname);

        try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {

            // Try to connect
            Future<Void> result = client.connect(new InetSocketAddress(config.hostname, config.port));

            // Wait for response
            result.get(config.timeout, TimeUnit.MILLISECONDS);
            updateStateIfChanged(OnOffType.ON);
        } catch (ExecutionException | IOException | TimeoutException e) {
            if (!(e.getCause() instanceof NoRouteToHostException) && !(e.getCause() instanceof ConnectException)) {
                logger.debug("connect threw an exception", e);
            }
            updateStateIfChanged(OnOffType.OFF);
        } catch (InterruptedException e) {
            // If we get here, then our over-arching Future was cancelled and we are shutting down
            logger.debug("connect interrupted");
        }
        logger.debug("TCP port detection process completed {}", config.hostname);
    }
}
