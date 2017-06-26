package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.supla.SuplaCloudConfiguration;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressWarnings("unused")
public final class SuplaCloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SuplaCloudBridgeHandler.class);

    private SuplaCloudConfiguration configuration;
    private ApplicationContext applicationContext;

    private ScheduledFuture<?> pollingJob;
    private PoolingRunnable poolingRunnable = new PoolingRunnable();

    public SuplaCloudBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no channels - nothing to do
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SuplaCloudBridgeHandler");
        this.configuration = getConfigAs(SuplaCloudConfiguration.class);
        this.applicationContext = new ApplicationContext(configuration.toSuplaCloudServer());
        startAutomaticRefresh();
    }

    private void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleAtFixedRate(poolingRunnable, 0, configuration.getRefreshInterval(), SECONDS);
        }
    }

    private final class PoolingRunnable implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(PoolingRunnable.class);

        @Override
        public void run() {
            logger.debug("Starting PoolingRunnable job...");
            final IoDevicesManager ioDevicesManager = applicationContext.getIoDevicesManager();
            final List<SuplaIoDevice> list = ioDevicesManager.obtainIoDevices();
        }
    }


}
