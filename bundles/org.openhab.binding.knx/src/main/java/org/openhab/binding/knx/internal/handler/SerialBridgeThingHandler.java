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
package org.openhab.binding.knx.internal.handler;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.NoOpClient;
import org.openhab.binding.knx.internal.client.SerialClient;
import org.openhab.binding.knx.internal.config.SerialBridgeConfiguration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.secure.KnxSecureException;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX Serial/USB Gateway, that either acts as a
 * conduit for other {@link DeviceThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - Refactoring and cleanup
 */
@NonNullByDefault
public class SerialBridgeThingHandler extends KNXBridgeBaseThingHandler {

    private @Nullable SerialClient client = null;
    private @Nullable Future<?> initJob = null;

    private final Logger logger = LoggerFactory.getLogger(SerialBridgeThingHandler.class);

    private final SerialPortManager serialPortManager;

    public SerialBridgeThingHandler(Bridge bridge, final SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        // create new instance using current configuration settings;
        // when a parameter change is done from UI, dispose() and initialize() are called
        SerialBridgeConfiguration config = getConfigAs(SerialBridgeConfiguration.class);
        client = new SerialClient(config.getAutoReconnectPeriod(), thing.getUID(), config.getResponseTimeout(),
                config.getReadingPause(), config.getReadRetriesLimit(), getScheduler(), config.getSerialPort(),
                config.useCemi(), serialPortManager, getCommandExtensionData(), openhabSecurity, this);

        updateStatus(ThingStatus.UNKNOWN);
        // delay actual initialization, allow for longer runtime of actual initialization
        initJob = scheduler.submit(this::initializeLater);
    }

    public void initializeLater() {
        SerialBridgeConfiguration config = getConfigAs(SerialBridgeConfiguration.class);
        try {
            if (initializeSecurity(config.getKeyringFile(), config.getKeyringPassword())) {
                if (keyring.isPresent()) {
                    logger.info("KNX secure available for {} devices, {} group addresses",
                            openhabSecurity.deviceToolKeys().size(), openhabSecurity.groupKeys().size());

                    logger.debug("Secure group addresses and associated devices: {}",
                            secHelperGetSecureGroupAddresses(openhabSecurity));
                } else {
                    logger.debug("KNX secure: keyring is not available");
                }
            } else {
                logger.debug("KNX security not configured");
            }
        } catch (KnxSecureException e) {
            logger.debug("{}, {}", thing.getUID(), e.toString());

            String message = e.getLocalizedMessage();
            if (message == null) {
                message = e.getClass().getSimpleName();
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "KNX security: " + message);
            return;
        }

        SerialClient tmpClient = client;
        if (tmpClient != null) {
            tmpClient.initialize();
        }
    }

    @Override
    public void dispose() {
        Future<?> tmpInitJob = initJob;
        if (tmpInitJob != null) {
            if (!tmpInitJob.isDone()) {
                logger.trace("Bridge {}, shutdown during init, trying to cancel", thing.getUID());
                tmpInitJob.cancel(true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.trace("Bridge {}, cancellation interrupted", thing.getUID());
                }
            }
            initJob = null;
        }

        SerialClient tmpClient = client;
        if (tmpClient != null) {
            tmpClient.dispose();
            client = null;
        }
        super.dispose();
    }

    @Override
    protected KNXClient getClient() {
        KNXClient ret = client;
        if (ret == null) {
            return new NoOpClient();
        }
        return ret;
    }
}
