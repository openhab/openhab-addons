/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.knx.internal.client;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.mgmt.ManagementClient;

/**
 * Client for retrieving additional device descriptions.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class DeviceInfoClientImpl implements DeviceInfoClient {

    private final Logger logger = LoggerFactory.getLogger(DeviceInfoClientImpl.class);
    private final ManagementClient managementClient;

    DeviceInfoClientImpl(ManagementClient managementClient) {
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
                logger.trace("Going to {} of {} ", task, address);
                destination = managementClient.createDestination(address, true);
                byte[] result = function.apply(destination);
                logger.trace("Finished to {} of {}, result: {}", task, address, result == null ? null : result.length);
                return result;
            } catch (KNXException e) {
                logger.debug("Could not {} of {}: {}", task, address, e.getMessage());
            } catch (InterruptedException e) {
                logger.trace("Interrupted to {}", task);
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

    @Override
    public synchronized byte @Nullable [] readDeviceDescription(IndividualAddress address, int descType,
            boolean authenticate, long timeout) {
        String task = "read the device description";
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readDeviceDesc(destination, descType);
        });
    }

    @Override
    public synchronized byte @Nullable [] readDeviceMemory(IndividualAddress address, int startAddress, int bytes,
            boolean authenticate, long timeout) {
        String task = MessageFormat.format("read {0} bytes at memory location {1}", bytes, startAddress);
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readMemory(destination, startAddress, bytes);
        });
    }

    @Override
    public synchronized byte @Nullable [] readDeviceProperties(IndividualAddress address,
            final int interfaceObjectIndex, final int propertyId, final int start, final int elements,
            boolean authenticate, long timeout) {
        String task = MessageFormat.format("read device property {0} at index {1}", propertyId, interfaceObjectIndex);
        return readFromManagementClient(task, timeout, address, destination -> {
            authorize(authenticate, destination);
            return managementClient.readProperty(destination, interfaceObjectIndex, propertyId, start, elements);
        });
    }

    @Override
    public boolean isConnected() {
        return managementClient.isOpen();
    }
}
