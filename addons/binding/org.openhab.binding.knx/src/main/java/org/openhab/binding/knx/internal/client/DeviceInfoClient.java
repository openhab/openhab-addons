package org.openhab.binding.knx.internal.client;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.mgmt.ManagementClient;

@NonNullByDefault
public class DeviceInfoClient {

    private final Logger logger = LoggerFactory.getLogger(DeviceInfoClient.class);
    private final ManagementClient managementClient;

    public DeviceInfoClient(ManagementClient managementClient) {
        this.managementClient = managementClient;
    }

    @FunctionalInterface
    private interface ReadFunction<T, R> {
        @Nullable
        R apply(T t) throws KNXException, InterruptedException;
    }

    private byte @Nullable [] readFromManagementClient(String task, long timeout, IndividualAddress address,
            ReadFunction<Destination, byte[]> function) {
        final long start = System.nanoTime();
        while ((System.nanoTime() - start) < TimeUnit.MILLISECONDS.toNanos(timeout)) {
            Destination destination = null;
            try {
                logger.debug("Going to {} of {} ", task, address);
                destination = managementClient.createDestination(address, true);
                byte[] result = function.apply(destination);
                logger.debug("Finished to {} of {}, result: {}", task, address, result == null ? null : result.length);
                return result;
            } catch (KNXException e) {
                logger.error("Could not {} of {}: {}", task, address, e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.error("", e);
                }
            } catch (InterruptedException e) {
                logger.debug("Interrupted to {}", task);
                return null;
            } finally {
                if (destination != null) {
                    destination.destroy();
                }
            }
        }
        return null;
    }

    private void authorize(boolean authenticate, Destination destination) throws KNXException, InterruptedException {
        if (authenticate) {
            managementClient.authorize(destination, (ByteBuffer.allocate(4)).put((byte) 0xFF).put((byte) 0xFF)
                    .put((byte) 0xFF).put((byte) 0xFF).array());
        }
    }

    public synchronized byte @Nullable [] readDeviceDescription(IndividualAddress address, int descType,
            boolean authenticate, long timeout) {
        String task = "read the device description";
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readDeviceDesc(destination, descType);
        });
    }

    public synchronized byte @Nullable [] readDeviceMemory(IndividualAddress address, int startAddress, int bytes,
            boolean authenticate, long timeout) {
        String task = MessageFormat.format("read {0} bytes at memory location {1}", bytes, startAddress);
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readMemory(destination, startAddress, bytes);
        });
    }

    public synchronized byte @Nullable [] readDeviceProperties(IndividualAddress address,
            final int interfaceObjectIndex, final int propertyId, final int start, final int elements,
            boolean authenticate, long timeout) {
        String task = MessageFormat.format("read device property {} at index {}", propertyId, interfaceObjectIndex);
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readProperty(destination, interfaceObjectIndex, propertyId, start, elements);
        });
    }

    public boolean isConnected() {
        return managementClient.isOpen();
    }

}
