/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link CapabilityMap} is a specialized Map designed to store capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class CapabilityMap extends ConcurrentHashMap<Class<?>, Capability> {
    private static final long serialVersionUID = -3043492242108419801L;

    public void put(Capability capability) {
        Class<? extends Capability> clazz = capability.getClass();

        if (getOrDescendant(clazz).isEmpty()) {
            super.put(clazz, capability);
            capability.initialize();
        } else {
            throw new IllegalArgumentException("%s is already present".formatted(clazz.getSimpleName()));
        }
    }

    public <T extends Capability> Optional<T> get(Class<T> clazz) {
        Capability cap = super.get(clazz);
        if (cap == null) {
            return Optional.empty();
        }
        return Optional.of(clazz.cast(cap));
    }

    public <T extends Capability> Optional<T> getOrDescendant(Class<T> clazz) {
        return values().stream()
                .filter(cap -> clazz.isAssignableFrom(cap.getClass()) || cap.getClass().isAssignableFrom(clazz))
                .map(cap -> clazz.cast(cap)).findFirst();
    }
}
