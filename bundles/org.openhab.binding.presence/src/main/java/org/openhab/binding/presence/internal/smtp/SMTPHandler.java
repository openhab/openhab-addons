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
package org.openhab.binding.presence.internal.smtp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.presence.internal.BaseHandler;
import org.openhab.binding.presence.internal.binding.PresenceBindingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMTPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public class SMTPHandler extends BaseHandler {

    private final Logger logger = LoggerFactory.getLogger(SMTPHandler.class);

    private @NonNullByDefault({}) SMTPConfiguration config;

    public SMTPHandler(Thing thing, PresenceBindingConfiguration bindingConfiguration) {
        super(thing, 1, bindingConfiguration);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing");
        config = getConfigAs(SMTPConfiguration.class);

        super.initialize();

        updateStatus(ThingStatus.ONLINE);

        logger.debug("Finished initializing");
    }

    private static final Pattern respPattern = Pattern.compile("^(\\d{3}) .*");

    /*
     * This is the main process for detecting the presence of the SMTP service. It uses an asynchronous socket
     * to attempt to connect to, then read from the SMTP service. If a connection is made and
     * a response containing a 200 response code is returned, then the service is considered ON.
     *
     * If the underlying Future that this method is running under is
     * cancelled, then we will get an InterruptedException.
     * This only happens when the Thing is being disposed of.
     */
    @Override
    protected void getStatus() {
        logger.debug("Starting SMTP detection process for {}", config.hostname);

        try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {

            // First try to connect
            Future<Void> result = client.connect(new InetSocketAddress(config.hostname, config.port));

            // Wait for connection
            result.get(config.timeout, TimeUnit.MILLISECONDS);

            // Now read the HELO message
            ByteBuffer buffer = ByteBuffer.wrap(new byte[2048]);
            Future<Integer> readval = client.read(buffer);
            State newState = OnOffType.OFF;
            if (readval.get(config.timeout, TimeUnit.MILLISECONDS) > 0) {
                String resp = new String(buffer.array()).trim();
                logger.debug("Received from server: {}", resp);
                Matcher m = respPattern.matcher(resp);
                if (m.matches()) {
                    int rc = Integer.parseInt(m.group(1));
                    if (rc >= 200 && rc < 300) {
                        newState = OnOffType.ON;
                    }
                }
            }
            updateStateIfChanged(newState);
        } catch (ExecutionException | IOException | TimeoutException e) {
            if (!(e.getCause() instanceof NoRouteToHostException) && !(e.getCause() instanceof ConnectException)) {
                logger.debug("SMTP connect threw an exception", e);
            }
            updateStateIfChanged(OnOffType.OFF);
        } catch (InterruptedException e) {
            // If we get here, our over-arching Future was cancelled.
            // We need to go UNDEFined but there's nothing to clean up. The async socket will get closed
            logger.debug("connect interrupted");
        }
        logger.debug("SMTP detection process completed {}", config.hostname);
    }
}
