package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.channels.ChannelBuilder;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_IO_DEVICE_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.THREAD_POOL_NAME;

public final class SuplaIoDeviceHandler extends BaseThingHandler {
    private static final int MAX_RETRIES = 20;
    private static final long WAIT_IN_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    private final Logger logger = LoggerFactory.getLogger(SuplaIoDeviceHandler.class);
    private SuplaCloudBridgeHandler bridgeHandler;
    private ApplicationContext applicationContext;

    public SuplaIoDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals("")) { // TODO pass correct ID from SuplaConstants
            executeCommandForSwitchChannel(channelUID, command);
        } else {
            logger.debug("Don't know this channel {}!", channelUID.getId());
        }
    }

    private void executeCommandForSwitchChannel(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            executeCommand(() -> bridgeHandler.switchCommand(channelUID, (OnOffType) command, thing));
        } else if (command instanceof RefreshType) {
            executeCommand(() -> bridgeHandler.refreshCommand(channelUID, thing));
        }
    }

    private void executeCommand(Runnable command) {
        try {
            command.run();
        } catch (RuntimeException e) {
            // TODO can do more generic
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        final Optional<SuplaCloudBridgeHandler> bridgeHandler = getBridgeHandler();
        if (bridgeHandler.isPresent()) {
            this.bridgeHandler = bridgeHandler.get();
            ThreadPoolManager.getPool(THREAD_POOL_NAME).submit(this::internalInitialize);
        }
    }

    private void internalInitialize() {
        try {
            final Optional<ApplicationContext> optional = getApplicationContextWithRetries();
            if (optional.isPresent()) {
                this.applicationContext = optional.get();
                getSuplaIoDevice(applicationContext.getIoDevicesManager())
                        .ifPresent(device -> setChannelsForThing(applicationContext.getChannelBuilder(), device));
            } else {
                updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("Bridge, \"%s\" is not fully initialized, there is no ApplicationContext!", this.bridgeHandler.getThing().getUID()));
                return;
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("Error occurred during initialization! %s", e.getLocalizedMessage()));
        }
    }

    private Optional<ApplicationContext> getApplicationContextWithRetries() {
        for(int i = 1; i <= MAX_RETRIES; i++) {
            final Optional<ApplicationContext> applicationContext = bridgeHandler.getApplicationContext();
            if(applicationContext.isPresent()) {
                return applicationContext;
            } else {
                logger.trace("BridgeHandler does not have ApplicationContext. Trying {}/{}", i, MAX_RETRIES);
                try {
                    Thread.sleep(WAIT_IN_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return empty();
    }

    private Optional<SuplaIoDevice> getSuplaIoDevice(IoDevicesManager ioDevicesManager) {
        final String stringId = thing.getProperties().get(SUPLA_IO_DEVICE_ID);
        if (!isNullOrEmpty(stringId)) {
            try {
                final long id = Long.parseLong(stringId);
                final Optional<SuplaIoDevice> suplaIoDevice = ioDevicesManager.obtainIoDevice(id);
                if (!suplaIoDevice.isPresent()) {
                    updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("Can not find Supla device with ID \"%s\"!", id));
                }
                return suplaIoDevice;
            } catch (NumberFormatException e) {
                updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("ID \"%s\" is not valid long! %s", stringId, e.getLocalizedMessage()));
                return empty();
            }
        } else {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("At property \"%s\" should be Supla device ID", SUPLA_IO_DEVICE_ID));
            return empty();
        }
    }

    private void setChannelsForThing(ChannelBuilder channelBuilder, SuplaIoDevice device) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelBuilder.buildChannels(device.getChannels()));
        updateThing(thingBuilder.build());
    }

    private synchronized Optional<SuplaCloudBridgeHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, "Required bridge not defined for device");
            return empty();
        } else {
            return getBridgeHandler(bridge);
        }

    }

    private synchronized Optional<SuplaCloudBridgeHandler> getBridgeHandler(Bridge bridge) {
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof SuplaCloudBridgeHandler) {
            return Optional.of((SuplaCloudBridgeHandler) handler);
        } else {
            updateStatus(UNINITIALIZED,
                    CONFIGURATION_ERROR,
                    format("Bridge has wrong class! Should be %s instead of %s.",
                            SuplaCloudBridgeHandler.class.getSimpleName(), bridge.getClass().getSimpleName()));
            return empty();
        }
    }
}
