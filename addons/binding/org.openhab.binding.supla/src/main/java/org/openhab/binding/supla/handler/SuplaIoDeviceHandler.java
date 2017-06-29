package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.eclipse.smarthome.core.thing.ThingStatus.UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.binding.supla.SuplaBindingConstants.LIGHT_CHANNEL_ID;

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
        getBridgeHandler().ifPresent(bridge -> this.bridgeHandler = bridge);
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        if(bridgeHandler != null) {
            updateStatus(ThingStatus.ONLINE);
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

//        ThingBuilder thingBuilder = editThing();
//        thingBuilder.withChannels(buildChannels(null));
//        updateThing(thingBuilder.build());
    }

    private static List<Channel> buildChannels(SuplaIoDevice suplaIoDevice) {
        return suplaIoDevice.getChannels()
                .stream()
                .map(SuplaIoDeviceHandler::buildChannel)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<Channel> buildChannel(SuplaChannel channel) {
        if ("TYPE_RELAY".equals(channel.getType().getName())) {
            return Optional.of(
                    ChannelBuilder.create(new ChannelUID(LIGHT_CHANNEL_ID), "String").build()
            );
        } else {
            return Optional.empty();
        }
    }

    private synchronized Optional<SuplaCloudBridgeHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(UNINITIALIZED, CONFIGURATION_ERROR, "Required bridge not defined for device");
            return Optional.empty();
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
            return Optional.empty();
        }
    }
}
