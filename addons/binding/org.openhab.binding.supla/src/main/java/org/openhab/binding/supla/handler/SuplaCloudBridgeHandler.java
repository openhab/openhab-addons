package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.supla.SuplaCloudConfiguration;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
    public void initialize() {
        // TODO add checking if all data is correct for bridge, i.e. can we connect to supla cloud
        logger.debug("Initializing SuplaCloudBridgeHandler");
        this.configuration = getConfigAs(SuplaCloudConfiguration.class);
        this.applicationContext = new ApplicationContext(configuration.toSuplaCloudServer());
        updateStatus(ThingStatus.ONLINE);
    }

    public Optional<ApplicationContext> getApplicationContext() {
        return Optional.ofNullable(applicationContext);
    }

}
