package org.openhab.binding.bluetooth;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public abstract class DelegateBluetoothDevice extends BluetoothDevice {

    public DelegateBluetoothDevice(BluetoothAdapter adapter, BluetoothAddress address) {
        super(adapter, address);
    }

    protected abstract BluetoothDevice getDelegate();

    @Override
    public ZonedDateTime getLastSeenTime() {
        return getDelegate().getLastSeenTime();
    }

    @Override
    public void updateLastSeenTime() {
        getDelegate().updateLastSeenTime();
    }

    @Override
    public @Nullable String getName() {
        return getDelegate().getName();
    }

    @Override
    public @Nullable Integer getManufacturerId() {
        return getDelegate().getManufacturerId();
    }

    @Override
    public @Nullable Integer getRssi() {
        return getDelegate().getRssi();
    }

    @Override
    public @Nullable Integer getTxPower() {
        return getDelegate().getTxPower();
    }

    @Override
    public @Nullable BluetoothService getServices(UUID uuid) {
        return getDelegate().getServices(uuid);
    }

    @Override
    public Collection<BluetoothService> getServices() {
        return getDelegate().getServices();
    }

    @Override
    public boolean supportsService(UUID uuid) {
        return getDelegate().supportsService(uuid);
    }

    @Override
    public ConnectionState getConnectionState() {
        return getDelegate().getConnectionState();
    }

    @Override
    public boolean connect() {
        return getDelegate().connect();
    }

    @Override
    public boolean disconnect() {
        return getDelegate().disconnect();
    }

    @Override
    public boolean discoverServices() {
        return getDelegate().discoverServices();
    }

    @Override
    public boolean readCharacteristic(BluetoothCharacteristic characteristic) {
        return getDelegate().readCharacteristic(characteristic);
    }

    @Override
    public boolean writeCharacteristic(BluetoothCharacteristic characteristic) {
        return getDelegate().writeCharacteristic(characteristic);
    }

    @Override
    public boolean enableNotifications(BluetoothCharacteristic characteristic) {
        return getDelegate().enableNotifications(characteristic);
    }

    @Override
    public boolean disableNotifications(BluetoothCharacteristic characteristic) {
        return getDelegate().disableNotifications(characteristic);
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        return getDelegate().enableNotifications(descriptor);
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        return getDelegate().disableNotifications(descriptor);
    }

    @Override
    protected boolean addService(BluetoothService service) {
        return getDelegate().addService(service);
    }

    @Override
    public void addListener(BluetoothDeviceListener listener) {
        getDelegate().addListener(listener);
    }

    @Override
    public void removeListener(BluetoothDeviceListener listener) {
        getDelegate().removeListener(listener);
    }

    @Override
    public boolean hasListeners() {
        return getDelegate().hasListeners();
    }

    @Override
    protected void notifyListeners(BluetoothEventType event, Object... args) {
        getDelegate().notifyListeners(event, args);
    }

    @Override
    public @Nullable BluetoothCharacteristic getCharacteristic(UUID uuid) {
        return getDelegate().getCharacteristic(uuid);
    }

    @Override
    protected void dispose() {
        getDelegate().dispose();
    }

}
