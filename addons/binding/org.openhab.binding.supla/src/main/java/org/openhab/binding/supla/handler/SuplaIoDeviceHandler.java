package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_IO_DEVICE_ID;

public final class SuplaIoDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SuplaIoDeviceHandler.class);
    private SuplaCloudBridgeHandler bridgeHandler;

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
        // TODO should be done on separate thread to prevent stopping everything...
        final Optional<SuplaCloudBridgeHandler> bridgeHandler = getBridgeHandler();
        if (bridgeHandler.isPresent()) {
            this.bridgeHandler = bridgeHandler.get();
            final Optional<ApplicationContext> optional = this.bridgeHandler.getApplicationContext();
            if (optional.isPresent()) {
                final ApplicationContext applicationContext = optional.get();
                final Optional<SuplaIoDevice> suplaIoDevice = getSuplaIoDevice(applicationContext.getIoDevicesManager());
                if (suplaIoDevice.isPresent()) {
                    setChannelsForThing(applicationContext, suplaIoDevice.get());
                } else {
                    updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, "Can not find Supla device!");
                    return;
                }
            } else {
                updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("Bridge, %s is not fully initialized, there is no ApplicationContext!", this.bridgeHandler.getThing().getUID()));
                return;
            }

            updateStatus(ThingStatus.ONLINE);
        }
    }

    private Optional<SuplaIoDevice> getSuplaIoDevice(IoDevicesManager ioDevicesManager) {
        final String stringId = thing.getProperties().get(SUPLA_IO_DEVICE_ID);
        if (!isNullOrEmpty(stringId)) {
            try {
                final long id = Long.parseLong(stringId);
                return ioDevicesManager.obtainIoDevice(id);
            } catch (NumberFormatException e) {
                updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("ID \"%s\" is not valid long! %s", stringId, e.getLocalizedMessage()));
                return empty();
            }
        } else {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, format("At property \"%s\" should be Supla device ID", SUPLA_IO_DEVICE_ID));
            return empty();
        }
    }

    private void setChannelsForThing(ApplicationContext ctx, SuplaIoDevice device) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(buildChannels(ctx, device));
        updateThing(thingBuilder.build());
    }

    private static List<Channel> buildChannels(ApplicationContext ctx, SuplaIoDevice suplaIoDevice) {
        return suplaIoDevice.getChannels()
                .stream()
                .map(channel -> ctx.getChannelBuilder().buildChannel(channel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
