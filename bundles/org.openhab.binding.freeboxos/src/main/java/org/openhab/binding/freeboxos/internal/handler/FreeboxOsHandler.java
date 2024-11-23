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
package org.openhab.binding.freeboxos.internal.handler;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;
import org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration;
import org.openhab.binding.freeboxos.internal.discovery.FreeboxOsDiscoveryService;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
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
    private final FreeboxOsSession session;
    private final String callbackURL;
    private final BundleContext bundleContext;
    private final AudioHTTPServer audioHTTPServer;

    private Optional<Future<?>> openConnectionJob = Optional.empty();
    private Optional<Future<?>> grantingJob = Optional.empty();

    public FreeboxOsHandler(Bridge thing, FreeboxOsSession session, String callbackURL, BundleContext bundleContext,
            AudioHTTPServer audioHTTPServer) {
        super(thing);
        this.session = session;
        this.callbackURL = callbackURL;
        this.bundleContext = bundleContext;
        this.audioHTTPServer = audioHTTPServer;
    }

    @Override
    public void initialize() {
        freeConnectionJob();

        FreeboxOsConfiguration config = getConfiguration();
        openConnectionJob = Optional.of(scheduler.submit(() -> {
            try {
                session.initialize(config);
                if (config.appToken.isBlank()) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "@text/info-conf-pending");
                    grantingJob = Optional.of(scheduler.schedule(this::processGranting, 2, TimeUnit.SECONDS));
                    return;
                } else {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
                    session.openSession(config.appToken);
                }
                updateStatus(ThingStatus.ONLINE);
            } catch (FreeboxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }));
    }

    private void processGranting() {
        try {
            String appToken = session.grant();
            if (appToken.isBlank()) {
                grantingJob = Optional.of(scheduler.schedule(this::processGranting, 2, TimeUnit.SECONDS));
            } else {
                Configuration thingConfig = editConfiguration();
                thingConfig.put(FreeboxOsConfiguration.APP_TOKEN, appToken);
                updateConfiguration(thingConfig);
                logger.info("AppToken updated, ensure giving permissions in the Freebox management console");
                initialize();
            }
        } catch (FreeboxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    public <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        return session.getManager(clazz);
    }

    private void freeConnectionJob() {
        openConnectionJob.ifPresent(job -> job.cancel(true));
        openConnectionJob = Optional.empty();
        grantingJob.ifPresent(job -> job.cancel(true));
        grantingJob = Optional.empty();
    }

    @Override
    public void dispose() {
        freeConnectionJob();
        session.closeSession();

        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(FreeboxOsDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public FreeboxOsConfiguration getConfiguration() {
        return getConfigAs(FreeboxOsConfiguration.class);
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public AudioHTTPServer getAudioHTTPServer() {
        return audioHTTPServer;
    }
}
