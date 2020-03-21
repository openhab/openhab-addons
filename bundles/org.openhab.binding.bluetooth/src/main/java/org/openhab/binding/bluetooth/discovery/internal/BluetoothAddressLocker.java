/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.discovery.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 * The {@link BluetoothAddressLocker} handles global locking of BluetoothAddress.
 * This is used to make sure that devices with handlers are not connected to during discovery.
 *
 * @author Connor Petty - Initial Contribution
 */
public class BluetoothAddressLocker {

    private static Map<BluetoothAddress, LockReference> locks = new ConcurrentHashMap<>();

    public static void lock(BluetoothAddress address) {
        locks.compute(address, (addr, oldRef) -> {
            LockReference ref = oldRef;
            if (ref == null) {
                ref = new LockReference();
            }
            ref.count++;
            return ref;
        }).lock.lock();
    }

    public static void unlock(BluetoothAddress address) {
        locks.computeIfPresent(address, (addr, ref) -> {
            ref.count--;
            ref.lock.unlock();
            return ref.count <= 0 ? null : ref;
        });
    }

    private static class LockReference {
        private int count = 0;
        private Lock lock = new ReentrantLock();
    }
}
