package org.openhab.binding.bluetoothsmart.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bluetoothsmart.internal.BluetoothSmartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.manager.BluetoothGovernor;
import org.sputnikdev.bluetooth.manager.BluetoothManager;

class BluetoothSmartHandler<T extends BluetoothGovernor> extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BluetoothSmartHandler.class);

    private final ItemRegistry itemRegistry;
    private final BluetoothManager bluetoothManager;
    private final BluetoothGattParser gattParser;
    private final URL url;
    private final List<ChannelHandler> channelHandlers = new ArrayList<>();

    public BluetoothSmartHandler(Thing thing, ItemRegistry itemRegistry,
            BluetoothManager bluetoothManager, BluetoothGattParser gattParser) {
        super(thing);
        this.itemRegistry = itemRegistry;
        this.bluetoothManager = bluetoothManager;
        this.gattParser = gattParser;
        this.url = BluetoothSmartUtils.getURL(thing.getUID().getId());
    }

    @Override
    public void initialize() {
        super.initialize();
        synchronized (channelHandlers) {
            for (ChannelHandler channelHandler : channelHandlers) {
                channelHandler.init();
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (channelHandlers) {
            for (ChannelHandler channelHandler : channelHandlers) {
                channelHandler.handleCommand(channelUID, command);
            }
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        synchronized (channelHandlers) {
            for (ChannelHandler channelHandler : channelHandlers) {
                channelHandler.handleUpdate(channelUID, newState);
            }
        }
    }

    @Override
    public void dispose() {
        logger.info("Disposing Abstract Bluetooth Smart Handler");
        super.dispose();
        disposeChannels();
        logger.info("Disposing bluetooth object: {}", url);
        getBluetoothManager().disposeGovernor(url);
        logger.info("Abstract Bluetooth Smart Handler has been disposed");
    }

    @Override
    protected void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    protected Configuration getConfig() {
        return super.getConfig();
    }

    @Override
    protected Configuration editConfiguration() {
        return super.editConfiguration();
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    protected BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    protected BluetoothGattParser getGattParser() {
        return gattParser;
    }

    protected ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    List<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    void addChannelHandler(ChannelHandler channelHandler) {
        synchronized (channelHandlers) {
            channelHandlers.add(channelHandler);
        }
    }

    void addChannelHandlers(List<ChannelHandler> handlers) {
        synchronized (channelHandlers) {
            channelHandlers.addAll(handlers);
        }
    }

    URL getURL() {
        return url;
    }

    protected void disposeChannels() {
        logger.info("Disposing Channel Handlers...");
        for (ChannelHandler channelHandler : getChannelHandlers()) {
            try {
                channelHandler.dispose();
            } catch (Exception ex) {
                logger.error("Could not dispose channel handler: {}", channelHandler.getURL(), ex);
            }
        }
    }

    protected T getGovernor() {
        return (T) bluetoothManager.getGovernor(getURL());
    }

}
