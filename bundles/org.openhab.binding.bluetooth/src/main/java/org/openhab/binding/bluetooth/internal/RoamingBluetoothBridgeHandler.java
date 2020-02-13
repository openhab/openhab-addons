package org.openhab.binding.bluetooth.internal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDiscoveryListener;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * @author Connor Petty - Initial contribution and API
 */
@NonNullByDefault
public class RoamingBluetoothBridgeHandler extends BaseBridgeHandler
        implements BluetoothAdapter, BluetoothDiscoveryListener {

    private static final BluetoothAddress ROAMING_ADAPTER_ADDRESS = new BluetoothAddress("FF:FF:FF:FF:FF:FF");

    private final Set<BluetoothAdapter> adapters = new CopyOnWriteArraySet<>();

    /*
     * Note: this will only populate from handlers calling getDevice(BluetoothAddress), so we don't need
     * to do periodic cleanup.
     */
    private Map<BluetoothAddress, RoamingBluetoothDevice> devices = new ConcurrentHashMap<>();

    // // Internal flag for the discovery configuration
    // private boolean discoveryConfigActive = true;
    // // Actual discovery status.
    // private boolean discoveryActive = true;

    public RoamingBluetoothBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        // this.thingUpdated(thing);
        // for(Thing thing : getThing().getThings()) {
        // thing.
        // }
    }

    @Override
    public void dispose() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.add(adapter);
    }

    protected void removeBluetoothAdapter(BluetoothAdapter adapter) {
        this.adapters.remove(adapter);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void addDiscoveryListener(BluetoothDiscoveryListener listener) {
    }

    @Override
    public void removeDiscoveryListener(@Nullable BluetoothDiscoveryListener listener) {
    }

    @Override
    public void scanStart() {
        // does nothing
    }

    @Override
    public void scanStop() {
        // does nothing
    }

    public boolean isBackgroundDiscoveryEnabled() {
        return false;// TODO
    }

    @Override
    public BluetoothAddress getAddress() {
        return ROAMING_ADAPTER_ADDRESS;
    }

    public @Nullable BluetoothDevice getNearestDevice(BluetoothAddress address) {
        Optional<BluetoothDevice> optDevice = adapters.stream().map(adapter -> adapter.getDevice(address))
                .max((d1, d2) -> Integer.compare(d1.getRssi(), d2.getRssi()));
        if (optDevice.isPresent()) {
            return optDevice.get();
        }
        return null;
    }

    @Override
    public RoamingBluetoothDevice getDevice(BluetoothAddress address) {
        return devices.computeIfAbsent(address, addr -> new RoamingBluetoothDevice(this, addr));
    }

    @Override
    public void deviceDiscovered(BluetoothDevice device) {
        RoamingBluetoothDevice roamingDevice = devices.get(device.getAddress());
        if (roamingDevice != null) {
            roamingDevice.addBluetoothDevice(roamingDevice);
        }
    }

}
