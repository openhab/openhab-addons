package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.supla.SuplaCloudConfiguration;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.discovery.SuplaDiscoveryService;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.openhab.binding.supla.internal.supla.entities.SuplaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.supla.SuplaBindingConstants.*;

@SuppressWarnings("unused")
public final class SuplaCloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SuplaCloudBridgeHandler.class);

    private SuplaCloudConfiguration configuration;
    private ApplicationContext applicationContext;

    private ScheduledFuture<?> pollingJob;
    private PoolingRunnable poolingRunnable = new PoolingRunnable();
    private SuplaDiscoveryService discoveryService;

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
        startAutomaticRefresh();
        updateStatus(ThingStatus.ONLINE);
    }

    private void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleAtFixedRate(poolingRunnable, 0, configuration.refreshInterval, SECONDS);
        }
    }

    //
    // Commands
    //
    void switchCommand(ChannelUID channelUID, OnOffType command, Thing thing) {
        logger.trace("switchCommand: {} / {}", command, thing);
        final long channelId = parseLong(thing.getProperties().get(channelUID.getId()));
        if (command == OnOffType.ON) {
            applicationContext.getChannelManager().turnOn(channelId);
        } else {
            applicationContext.getChannelManager().turnOff(channelId);
        }
    }

    // TODO
    void refreshCommand(ChannelUID channelUID, Thing thing) {
        final long deviceId = parseLong(thing.getProperties().get(SUPLA_IO_DEVICE_ID));
        final long channelId = parseLong(thing.getProperties().get(channelUID.getId()));
        final SuplaChannelStatus channelStatus = applicationContext.getChannelManager().obtainChannelStatus(channelId);
        // TODO set this state to channel
//        applicationContext.getIoDevicesManager()
//                .obtainIoDevice(deviceId)
//                .ifPresent(device -> {
//                    device.getChannels()
//                            .stream()
//                            .filter(c -> c.getId() == channelId)
//                            .findFirst()
//                            .ifPresent(c -> thing.);
//                });
//        thing.getChannels().forEach(channel -> channel.);
    }

    //
    // Service Discovery
    //

    public void registerDiscoveryService(SuplaDiscoveryService discoveryService) {
        logger.trace("Register Discovery Service {}", discoveryService);
        this.discoveryService = checkNotNull(discoveryService);
    }

    public void unregisterDiscoveryService() {
        logger.trace("Unregister Discovery Service");
        discoveryService = null;
    }

    private final class PoolingRunnable implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(PoolingRunnable.class);

        @Override
        public void run() {
            logger.debug("Starting PoolingRunnable job...");
            final IoDevicesManager ioDevicesManager = applicationContext.getIoDevicesManager();
            final List<SuplaIoDevice> list = ioDevicesManager.obtainIoDevices();
            if (list == null || list.size() == 0) {
                logger.trace("There are no SuplaIoDevices on {}", applicationContext.getSuplaCloudServer());
                return;
            }
            if (SuplaCloudBridgeHandler.this.discoveryService != null) {
                logger.debug("Has discovery service. Send all ({}) SuplaIoDevices to it", list.size());
                list.forEach(device -> discoveryService.addSuplaThing(findThingType(device), device.getId(),
                        findThingLabel(device), buildThingProperties(device)));
            }
        }
    }

    private String findThingType(SuplaIoDevice device) {
        final long relayChannelsCount = findRelayChannelsCount(device);
        if(relayChannelsCount == 2) {
            return TWO_CHANNEL_RELAY_THING_ID;
        } else if(relayChannelsCount == 1) {
            return ONE_CHANNEL_RELAY_THING_ID;
        } else {
            throw new RuntimeException(format("relayChannelsCount = %s", relayChannelsCount));
        }
    }

    private long findRelayChannelsCount(SuplaIoDevice device) {
        return device.getChannels()
                .stream()
                .map(SuplaChannel::getType)
                .map(SuplaType::getName)
                .filter(RELAY_CHANNEL_TYPE::equals)
                .count();
    }

    private String findThingLabel(SuplaIoDevice device) {
        final StringBuilder sb = new StringBuilder();

        final String name = device.getName();
        if (isValidString(name)) {
            logger.trace("Using name ad ID for {}", device);
            sb.append(name);
        }
        final String comment = device.getComment();
        if (isValidString(comment)) {
            logger.trace("Using comment ad ID for {}", device);
            sb.append("(").append(comment).append(")");
        }
        final String primaryLabel = sb.toString();
        if(isValidString(primaryLabel)) {
            return primaryLabel;
        } else {
            logger.trace("Using gUID ad ID for {}", device);
            return device.getGuid();
        }
    }

    private boolean isValidString(String string) {
        return string != null && !string.trim().isEmpty();
    }

    private Map<String, Object> buildThingProperties(SuplaIoDevice device) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("supla-cloud-id", (int) device.getId());

        // TODO support multiple channels
//        final SuplaChannel firstChannel = device.getChannels().iterator().next();
//        checkArgument(RELAY_CHANNEL_TYPE.equals(firstChannel.getType().getName()),
//                "Wrong channel name! Expected %s got %s.", RELAY_CHANNEL_TYPE, firstChannel.getType().getName());
//        properties.put(SWITCH_1_CHANNEL, String.valueOf(firstChannel.getId()));

        return properties;
    }
}
