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
package org.openhab.binding.smarther.internal.api.dto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code Modules} class defines the dto for Smarther API list of modules.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Modules {

    private List<Module> modules;

    public List<Module> getModules() {
        return modules;
    }

    /**
     * Converts the list into a string with comma separated module names.
     *
     * @param plants The modules list to be converted.
     * @return A string containing the module names, comma separated (or null, if the list is null or empty).
     */
    public static String toNameString(List<Module> modules) {
        if (modules == null || modules.isEmpty()) {
            return null;
        } else {
            return modules.stream().map(a -> String.valueOf(a.getName())).collect(Collectors.joining(", "));
        }
    }

}
