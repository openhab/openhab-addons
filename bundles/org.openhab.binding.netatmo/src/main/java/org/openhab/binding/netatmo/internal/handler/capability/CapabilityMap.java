/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;

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
        Class<?> clazz = capability.getClass();
        if (super.get(clazz) == null) {
            super.put(clazz, capability);
            capability.initialize();
        }
    }

    public <T extends Capability> Optional<T> get(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T cap = (T) super.get(clazz);
        return Optional.ofNullable(cap);
    }

    public <T extends Capability> void remove(Class<?> clazz) {
        @Nullable
        Capability cap = super.remove(clazz);
        if (cap != null) {
            cap.dispose();
        }
    }

    public Optional<RefreshCapability> getRefresh() {
        return values().stream().filter(RefreshCapability.class::isInstance).map(RefreshCapability.class::cast)
                .findFirst();
    }

    public Optional<Capability> getParentUpdate() {
        return values().stream().filter(ParentUpdateCapability.class::isInstance).findFirst();
    }
}
