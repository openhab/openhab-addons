package org.openhab.binding.bluetooth.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

@NonNullByDefault
public class RoamingBluetoothDevice extends BluetoothDevice implements BluetoothDeviceListener {

    // private Set<BluetoothDevice> devices = new CopyOnWriteArraySet<>();

    private RoamingBluetoothBridgeHandler roamingAdapter;

    private Set<BluetoothDevice> devices = new HashSet<>();

    private @Nullable BluetoothDevice currentDelegate;

    public RoamingBluetoothDevice(RoamingBluetoothBridgeHandler roamingAdapter, BluetoothAddress address) {
        super(roamingAdapter, address);
        this.roamingAdapter = roamingAdapter;
    }

    public void addBluetoothDevice(BluetoothDevice device) {
        devices.add(device);
        device.addListener(this);
    }

    public void removeBluetoothDevice(BluetoothDevice device) {
        devices.remove(device);
        device.removeListener(this);
    }

    @Override
    public boolean connect() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.connect() : false;
    }

    @Override
    public boolean disconnect() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disconnect() : false;
    }

    @Override
    public boolean enableNotifications(@Nullable BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.enableNotifications(characteristic) : false;
    }

    @Override
    public boolean enableNotifications(@Nullable BluetoothDescriptor descriptor) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.enableNotifications(descriptor) : false;
    }

    @Override
    public boolean disableNotifications(@Nullable BluetoothCharacteristic characteristic) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disableNotifications(characteristic) : false;
    }

    @Override
    public boolean disableNotifications(@Nullable BluetoothDescriptor descriptor) {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.disableNotifications(descriptor) : false;
    }

    @Override
    public boolean discoverServices() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.discoverServices() : false;
    }

    @Override
    public @Nullable ConnectionState getConnectionState() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getConnectionState() : null;
    }

    @Override
    public @Nullable Integer getRssi() {
        BluetoothDevice delegate = getDelegate();
        return delegate != null ? delegate.getRssi() : null;
    }

    private @Nullable BluetoothDevice getDelegate() {
        BluetoothDevice oldDelegate = currentDelegate;
        // use previous delegate if we are connected to it
        if (oldDelegate != null) {
            switch (oldDelegate.getConnectionState()) {
                case CONNECTED:
                case CONNECTING:
                    return oldDelegate;
            }
        }
        // we aren't tied to the delegate right now so we can change it.
        BluetoothDevice newDelegate = roamingAdapter.getNearestDevice(address);
        if (newDelegate == oldDelegate) {
            return oldDelegate;
        }

        // if (oldDelegate != null) {
        // oldDelegate.removeListener(this);
        // }
        //
        // if (newDelegate != null) {
        // newDelegate.addListener(this);
        // }
        return currentDelegate = newDelegate;
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        notifyListeners(BluetoothEventType.SCAN_RECORD, scanNotification);
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        notifyListeners(BluetoothEventType.CONNECTION_STATE, connectionNotification);
    }

    @Override
    public void onServicesDiscovered() {
        notifyListeners(BluetoothEventType.SERVICES_DISCOVERED);
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        notifyListeners(BluetoothEventType.CHARACTERISTIC_READ_COMPLETE, characteristic, status);
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic);
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        notifyListeners(BluetoothEventType.CHARACTERISTIC_UPDATED, characteristic);
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor) {
        notifyListeners(BluetoothEventType.DESCRIPTOR_UPDATED, bluetoothDescriptor);
    }

}
