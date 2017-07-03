package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.supla.SuplaCloudConfiguration;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.supla.SuplaBindingConstants.SCHEDULED_THREAD_POOL_NAME;

public final class SuplaCloudBridgeHandler extends BaseBridgeHandler {
    private static final long REFRESH_THREAD_DELAY_IN_SECONDS = 10;
    private final Logger logger = LoggerFactory.getLogger(SuplaCloudBridgeHandler.class);

    private final ReadWriteLock handlersLock = new ReentrantReadWriteLock();
    private final List<SuplaIoDeviceHandler> handlers = new ArrayList<>();
    private SuplaCloudConfiguration configuration;
    private ApplicationContext applicationContext;
    private ScheduledExecutorService scheduledPool;

    public SuplaCloudBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels - nothing to do
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        handlersLock.writeLock().lock();
        try {
            if (childHandler instanceof SuplaIoDeviceHandler) {
                handlers.add((SuplaIoDeviceHandler) childHandler);
            }
        } finally {
            handlersLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        handlersLock.writeLock().lock();
        try {
            handlers.remove(childHandler);
        } finally {
            handlersLock.writeLock().unlock();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SuplaCloudBridgeHandler");
        this.configuration = getConfigAs(SuplaCloudConfiguration.class);
        final ApplicationContext applicationContext = new ApplicationContext(configuration.toSuplaCloudServer());
        try {
            applicationContext.getIoDevicesManager().obtainIoDevices();
            // Set this after check so no one else cannot use ApplicationContext if SuplaCloudServer is malformed
            this.applicationContext = applicationContext;
        } catch (Exception e) {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR,
                    format("Supla Cloud data access is wrong! Please double check that everything is passed correctly! %s",
                            e.getLocalizedMessage()));
            return;
        }

        scheduledPool = ThreadPoolManager.getScheduledPool(SCHEDULED_THREAD_POOL_NAME);
        scheduledPool.scheduleAtFixedRate(new RefreshThread(),
                REFRESH_THREAD_DELAY_IN_SECONDS, configuration.refreshInterval, SECONDS);
        updateStatus(ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
        if(scheduledPool != null) {
            if(!scheduledPool.isShutdown()) {
                scheduledPool.shutdownNow();
            }
            scheduledPool = null;
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
                handlers.forEach(SuplaIoDeviceHandler::refreshChannels);
            } finally {
                handlersLock.readLock().unlock();
            }
        }
    }

}
