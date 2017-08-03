package org.openhab.binding.bluetoothsmart.internal.discovery;

import java.util.HashSet;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bluetoothsmart.BluetoothSmartBindingConstants;
import org.openhab.binding.bluetoothsmart.BluetoothSmartDiscoveryService;
import org.openhab.binding.bluetoothsmart.internal.BluetoothSmartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.AdapterDiscoveryListener;
import org.sputnikdev.bluetooth.manager.BluetoothManager;
import org.sputnikdev.bluetooth.manager.DeviceDiscoveryListener;
import org.sputnikdev.bluetooth.manager.DiscoveredAdapter;
import org.sputnikdev.bluetooth.manager.DiscoveredDevice;
import org.sputnikdev.bluetooth.manager.DiscoveredObject;

public class BluetoothSmartDiscoveryServiceImpl extends AbstractDiscoveryService
        implements BluetoothSmartDiscoveryService, DeviceDiscoveryListener, AdapterDiscoveryListener {

    private final static int DISCOVERY_RATE_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(BluetoothSmartDiscoveryServiceImpl.class);
    private BluetoothManager bluetoothManager;

    public BluetoothSmartDiscoveryServiceImpl() throws IllegalArgumentException {
        super(new HashSet<ThingTypeUID>() {{
            add(BluetoothSmartBindingConstants.THING_TYPE_ADAPTER);
            add(BluetoothSmartBindingConstants.THING_TYPE_GENERIC);
            add(BluetoothSmartBindingConstants.THING_TYPE_BLE);
        }}, 0, true);
    }

    @Override
    public void discovered(DiscoveredDevice discoveredDevice) {
        DiscoveryResult discoveryResult;
        if (discoveredDevice.getBluetoothClass() == 0) {
            discoveryResult = getDiscoveryResult(discoveredDevice, BluetoothSmartBindingConstants.THING_TYPE_BLE);
        } else {
            discoveryResult = getDiscoveryResult(discoveredDevice, BluetoothSmartBindingConstants.THING_TYPE_GENERIC);
        }
        thingDiscovered(discoveryResult);
    }

    @Override
    public void discovered(DiscoveredAdapter discoveredAdapter) {
        thingDiscovered(getDiscoveryResult(discoveredAdapter, BluetoothSmartBindingConstants.THING_TYPE_ADAPTER));
    }

    private DiscoveryResult getDiscoveryResult(DiscoveredObject device, ThingTypeUID thingTypeUID) {
        DiscoveryResultBuilder builder = DiscoveryResultBuilder
                .create(new ThingUID(thingTypeUID, BluetoothSmartUtils.getThingUID(device.getURL())))
                .withLabel(device.getAlias() != null ? device.getAlias() : device.getName())
                .withTTL(DISCOVERY_RATE_SEC * 3);
        return builder.build();
    }

    @Override
    public void adapterLost(URL url) {
        logger.info("Adapter lost: {}", url);
        thingRemoved(new ThingUID(BluetoothSmartBindingConstants.THING_TYPE_ADAPTER,
                BluetoothSmartUtils.getThingUID(url)));
    }

    @Override
    public void deviceLost(URL url) {
        logger.info("Device lost: {}", url);
        thingRemoved(new ThingUID(BluetoothSmartBindingConstants.THING_TYPE_GENERIC,
                BluetoothSmartUtils.getThingUID(url)));
        thingRemoved(new ThingUID(BluetoothSmartBindingConstants.THING_TYPE_BLE,
                BluetoothSmartUtils.getThingUID(url)));
    }

    @Override
    protected synchronized void startScan() {
    }

    @Override
    protected void startBackgroundDiscovery() {
        this.bluetoothManager.setDiscoveryRate(DISCOVERY_RATE_SEC);
        this.bluetoothManager.setRediscover(true);
        this.bluetoothManager.addAdapterDiscoveryListener(this);
        this.bluetoothManager.addDeviceDiscoveryListener(this);
        this.bluetoothManager.start(true);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        this.bluetoothManager.removeAdapterDiscoveryListener(this);
        this.bluetoothManager.removeDeviceDiscoveryListener(this);
        this.bluetoothManager.stop();
    }

    protected void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    protected void unsetBluetoothManager(BluetoothManager bluetoothManager) {
        if (this.bluetoothManager != null) {
            this.bluetoothManager.removeAdapterDiscoveryListener(this);
            this.bluetoothManager.removeDeviceDiscoveryListener(this);
        }
        this.bluetoothManager = null;
    }

}
