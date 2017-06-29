package org.openhab.binding.supla.handler;

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

import java.util.Optional;

import static java.lang.String.format;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

public final class SuplaCloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SuplaCloudBridgeHandler.class);

    private SuplaCloudConfiguration configuration;
    private ApplicationContext applicationContext;

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
        // TODO add to pool that updates all child handlers
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        // TODO remove from pool that updates all child handlers
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
            updateStatus(ONLINE);
        } catch (Exception e) {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR,
                    format("Supla Cloud data access is wrong! Please double check that everything is passed correctly! %s",
                            e.getLocalizedMessage()));
        }
    }

    public Optional<ApplicationContext> getApplicationContext() {
        return Optional.ofNullable(applicationContext);
    }

}
