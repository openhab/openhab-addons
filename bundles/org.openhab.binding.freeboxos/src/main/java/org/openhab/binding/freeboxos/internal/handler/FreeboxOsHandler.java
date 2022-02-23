/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;
import org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration;
import org.openhab.binding.freeboxos.internal.discovery.FreeboxOsDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxOsHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(FreeboxOsHandler.class);

    private @Nullable Future<?> openConnectionJob;
    private final FreeboxOsSession session;

    public FreeboxOsHandler(Bridge thing, FreeboxOsSession session) {
        super(thing);
        this.session = session;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Freebox OS API handler for thing {}.", getThing().getUID());
        Future<?> job = openConnectionJob;
        if (job == null || job.isCancelled()) {
            openConnectionJob = scheduler.submit(() -> {
                try {
                    FreeboxOsConfiguration config = getConfiguration();

                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Please accept pairing request directly on your freebox");

                    String currentAppToken = session.initialize(config);

                    if (!currentAppToken.equals(config.appToken)) {
                        Configuration thingConfig = editConfiguration();
                        thingConfig.put(FreeboxOsConfiguration.APP_TOKEN, currentAppToken);
                        updateConfiguration(thingConfig);
                        logger.info(
                                "App token updated in Configuration, please give needed permissions to openHAB app in your Freebox management console");
                    }

                    updateStatus(ThingStatus.ONLINE);
                } catch (FreeboxException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }
            });
        }
    }

    public <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        return session.getManager(clazz);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Freebox OS API handler for thing {}", getThing().getUID());
        Future<?> job = openConnectionJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            openConnectionJob = null;
        }
        session.closeSession();
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(FreeboxOsDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public FreeboxOsConfiguration getConfiguration() {
        return getConfigAs(FreeboxOsConfiguration.class);
    }
}
