/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.NAME_SEPARATOR;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code Modules} class defines the dto for Smarther API list of modules.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Modules {

    private List<Module> modules;

    /**
     * Returns the list of modules contained in this object.
     *
     * @return the list of modules
     */
    public @Nullable List<Module> getModules() {
        return modules;
    }

    /**
     * Converts a list of {@link Module} objects into a string containing the module names, comma separated.
     *
     * @param modules
     *            the list of module objects to be converted, may be {@code null}
     *
     * @return a string containing the comma separated module names, or {@code null} if the list is {@code null} or
     *         empty.
     */
    public static @Nullable String toNameString(@Nullable List<Module> modules) {
        if (modules == null || modules.isEmpty()) {
            return null;
        }
        return modules.stream().map(a -> a.getName()).collect(Collectors.joining(NAME_SEPARATOR));
    }
}
