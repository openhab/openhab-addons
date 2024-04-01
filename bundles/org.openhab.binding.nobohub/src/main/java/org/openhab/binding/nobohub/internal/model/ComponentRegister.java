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
package org.openhab.binding.nobohub.internal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Stores a mapping between component ids and components that exists.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public final class ComponentRegister {

    private final @NotNull Map<SerialNumber, Component> register = new HashMap<>();

    /**
     * Stores a new Component in the register. If a component exists with the same id, that value is overwritten.
     *
     * @param component The Component to store.
     */
    public void put(Component component) {
        register.put(component.getSerialNumber(), component);
    }

    /**
     * Removes a component from the registry.
     *
     * @param componentId The component to remove
     * @return The component that is removed. Null if the component is not found.
     */
    public @Nullable Component remove(SerialNumber componentId) {
        return register.remove(componentId);
    }

    /**
     * Returns a component from the registry.
     *
     * @param componentId The id of the component to return.
     * @return Returns the component, or null if it doesn't exist in the regestry.
     */
    public @Nullable Component get(SerialNumber componentId) {
        return register.get(componentId);
    }

    public Collection<Component> values() {
        return register.values();
    }
}
