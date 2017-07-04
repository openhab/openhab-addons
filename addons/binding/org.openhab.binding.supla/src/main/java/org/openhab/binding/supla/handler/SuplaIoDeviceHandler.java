package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.channels.ChannelBuilder;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.NONE;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_IO_DEVICE_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.THREAD_POOL_NAME;

public final class SuplaIoDeviceHandler extends BaseThingHandler {
    private static final int MAX_RETRIES = 20;
    private static final long WAIT_IN_MILLISECONDS = SECONDS.toMillis(1);

    private final Logger logger = LoggerFactory.getLogger(SuplaIoDeviceHandler.class);
    private SuplaCloudBridgeHandler bridgeHandler;
    private ApplicationContext applicationContext;
    private Map<Channel, SuplaChannel> suplaChannelChannelMap;

    public SuplaIoDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Optional<SuplaChannel> suplaChannel = findSuplaChannel(channelUID);
        if (suplaChannel.isPresent()) {
            applicationContext.getCommandExecutorFactory().findCommand(suplaChannel.get(), channelUID)
                    .ifPresent(executor -> executor.execute(buildStateConsumer(channelUID), command));
        } else {
            logger.debug("There is no SuplaChannel for {}!", channelUID);
        }
    }

    private Consumer<State> buildStateConsumer(ChannelUID channelUID) {
        return state -> this.updateState(channelUID, state);
    }

    private Optional<SuplaChannel> findSuplaChannel(ChannelUID channelUID) {
        return suplaChannelChannelMap.entrySet().stream().filter(entry -> entry.getKey().getUID().equals(channelUID))
                .map(Map.Entry::getValue).findFirst();
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
        final Optional<ApplicationContext> optional = getApplicationContextWithRetries();
        if (optional.isPresent()) {
            this.applicationContext = optional.get();
            final Optional<SuplaIoDevice> suplaIoDevice = getSuplaIoDevice(
                    applicationContext.getIoDevicesManager());
            if (suplaIoDevice.isPresent()) {
                setChannelsForThing(applicationContext.getChannelBuilder(), suplaIoDevice.get());
                refreshDeviceStatus(suplaIoDevice.get());
                bridgeHandler.registerSuplaIoDeviceManagerHandler(this);
            } else {
                // configuration is not good
                return;
            }
        } else {
            updateStatus(UNKNOWN, CONFIGURATION_ERROR,
                    format("Bridge, \"%s\" is not fully initialized, there is no ApplicationContext!",
                            this.bridgeHandler.getThing().getUID()));
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private Optional<ApplicationContext> getApplicationContextWithRetries() {
        for (int i = 1; i <= MAX_RETRIES; i++) {
            final Optional<ApplicationContext> applicationContext = bridgeHandler.getApplicationContext();
            if (applicationContext.isPresent()) {
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
        final Configuration config = this.getConfig();
        if (config.containsKey(SUPLA_IO_DEVICE_ID)) {
            final Object decimalId = config.get(SUPLA_IO_DEVICE_ID);
            if (decimalId instanceof BigDecimal) {
                long id = ((BigDecimal) decimalId).longValue();
                final Optional<SuplaIoDevice> suplaIoDevice = ioDevicesManager.obtainIoDevice(id);
                if (!suplaIoDevice.isPresent()) {
                    updateStatus(UNKNOWN, CONFIGURATION_ERROR,
                            format("Can not find Supla device with ID \"%s\"!", id));
                }
                return suplaIoDevice;
            } else {
                updateStatus(UNKNOWN, CONFIGURATION_ERROR,
                        format("ID \"%s\" is not valid long! Current type is %s.", decimalId,
                                decimalId.getClass().getSimpleName()));
                return empty();
            }
        } else {
            updateStatus(UNKNOWN, CONFIGURATION_ERROR, format("At property \"%s\" should be Supla device ID", SUPLA_IO_DEVICE_ID));
            return empty();
        }
    }

    private void setChannelsForThing(ChannelBuilder channelBuilder, SuplaIoDevice device) {
        suplaChannelChannelMap = channelBuilder.buildChannels(getThing().getUID(), device.getChannels());

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(new ArrayList<>(suplaChannelChannelMap.keySet()));
        updateThing(thingBuilder.build());
    }

    private synchronized Optional<SuplaCloudBridgeHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(UNKNOWN, CONFIGURATION_ERROR, "Required bridge not defined for device");
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
            updateStatus(UNKNOWN,
                    CONFIGURATION_ERROR,
                    format("Bridge has wrong class! Should be %s instead of %s.",
                            SuplaCloudBridgeHandler.class.getSimpleName(), bridge.getClass().getSimpleName()));
            return empty();
        }
    }

    void refresh() {
        final boolean refreshed = refreshDeviceStatus();
        if (refreshed) {
            refreshChannels();
        }
    }

    private boolean refreshDeviceStatus() {
        if (applicationContext != null) {
            final Optional<SuplaIoDevice> suplaIoDevice = getSuplaIoDevice(applicationContext.getIoDevicesManager());
            if (suplaIoDevice.isPresent()) {
                return refreshDeviceStatus(suplaIoDevice.get());
            } else {
                updateStatus(UNKNOWN, NONE, "Could not find device in Supla Cloud!");
                return false;
            }
        } else {
            updateStatus(UNKNOWN, NONE, "ApplicationContext is not present!");
            return false;
        }
    }

    private boolean refreshDeviceStatus(SuplaIoDevice device) {
        if (device.isEnabled()) {
            updateStatus(ONLINE);
            return true;
        } else {
            updateStatus(OFFLINE, NONE,
                    "Device was disabled in Supla Cloud. If you want to enable it check https://cloud.supla.org/");
            return false;
        }
    }

    private void refreshChannels() {
        suplaChannelChannelMap.keySet().stream().map(Channel::getUID).forEach(uuid -> handleCommand(uuid, REFRESH));
    }

    @Override
    public void dispose() {
        super.dispose();
        bridgeHandler.unregisterSuplaIoDeviceManagerHandler(this);
    }
}
