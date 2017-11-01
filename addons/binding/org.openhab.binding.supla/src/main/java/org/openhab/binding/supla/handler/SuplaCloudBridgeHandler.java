/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.handler;

import java.util.concurrent.ScheduledFuture;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.supla.SuplaCloudConfiguration;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.supla.SuplaBindingConstants.SCHEDULED_THREAD_POOL_NAME;

/**
 * This is handler for SuplaBridge things
 *
 * @author Martin Grześlowski - initial contributor
 */
public class SuplaCloudBridgeHandler extends BaseBridgeHandler {
    private static final long REFRESH_THREAD_DELAY_IN_SECONDS = 10;
    private final Logger logger = LoggerFactory.getLogger(SuplaCloudBridgeHandler.class);

    private final ReadWriteLock handlersLock = new ReentrantReadWriteLock();
    private final Set<SuplaIoDeviceHandler> handlers = new HashSet<>();
    private final ApplicationContext.Builder applicationContextBuilder;
    private SuplaCloudConfiguration configuration;
    private ApplicationContext applicationContext;
    private ScheduledFuture<?> scheduledFuture;

    public SuplaCloudBridgeHandler(Bridge bridge, ApplicationContext.Builder applicationContextBuilder) {
        super(bridge);
        this.applicationContextBuilder = requireNonNull(applicationContextBuilder);
    }

    public SuplaCloudBridgeHandler(Bridge bridge) {
        this(bridge, ApplicationContext::new);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels - nothing to do
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof SuplaIoDeviceHandler) {
            registerSuplaIoDeviceManagerHandler((SuplaIoDeviceHandler) childHandler);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        if (childHandler instanceof SuplaIoDeviceHandler) {
            unregisterSuplaIoDeviceManagerHandler((SuplaIoDeviceHandler) childHandler);
        }
    }

    void registerSuplaIoDeviceManagerHandler(SuplaIoDeviceHandler handler) {
        handlersLock.writeLock().lock();
        try {
            handlers.add(handler);
        } finally {
            handlersLock.writeLock().unlock();
        }
    }

    void unregisterSuplaIoDeviceManagerHandler(SuplaIoDeviceHandler handler) {
        handlersLock.writeLock().lock();
        try {
            handlers.remove(handler);
        } finally {
            handlersLock.writeLock().unlock();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SuplaCloudBridgeHandler");
        this.configuration = getConfigAs(SuplaCloudConfiguration.class);
        final ApplicationContext applicationContext = applicationContextBuilder.build(configuration.toSuplaCloudServer());
        final Optional<SuplaServerInfo> suplaServerInfo = applicationContext.getServerInfoManager().obtainServerInfo();
        if(!suplaServerInfo.isPresent()) {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, "There is no server info! Please check if all configuration parameters are OK.");
            return;
        }

        // Set this after check so no one else cannot use ApplicationContext if SuplaCloudServer is malformed
        this.applicationContext = applicationContext;

        final ScheduledExecutorService scheduledPool = ThreadPoolManager.getScheduledPool(SCHEDULED_THREAD_POOL_NAME);
        this.scheduledFuture = scheduledPool.scheduleAtFixedRate(new RefreshThread(), REFRESH_THREAD_DELAY_IN_SECONDS,
                configuration.refreshInterval, SECONDS);
        updateStatus(ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    public Optional<ApplicationContext> getApplicationContext() {
        return Optional.ofNullable(applicationContext);
    }

    private final class RefreshThread implements Runnable {

        @Override
        public void run() {
            handlersLock.readLock().lock();
            try {
                handlers.forEach(SuplaIoDeviceHandler::refresh);
            } catch (Exception e) {
                logger.debug("Got error while refreshing SuplaIoDeviceHandler!", e);
            } finally {
                handlersLock.readLock().unlock();
            }
        }
    }
}
