/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jsupla.internal;

import org.openhab.binding.jsupla.handler.SuplaDeviceHandler;
import org.osgi.service.component.annotations.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.requireNonNull;

/**
 * @author Grzeslowski - Initial contribution
 */
@Component(service = SuplaDeviceRegistry.class, immediate = true, configurationPid = "binding.jsupla")
public class SuplaDeviceRegistryImpl implements SuplaDeviceRegistry {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Set<SuplaDeviceHandler> suplaDeviceHandlers = new HashSet<>();

    @Override
    public void addSuplaDevice(final SuplaDeviceHandler suplaDeviceHandler) {
        lock.writeLock().lock();
        try {
            suplaDeviceHandlers.add(suplaDeviceHandler);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<SuplaDeviceHandler> getSuplaDevice(final String guid) {
        requireNonNull(guid);
        lock.readLock().lock();
        try {
            return suplaDeviceHandlers.stream()
                           .filter(device -> guid.equals(device.getThing().getUID().getId()))
                           .findAny();
        } finally {
            lock.readLock().unlock();
        }
    }
}