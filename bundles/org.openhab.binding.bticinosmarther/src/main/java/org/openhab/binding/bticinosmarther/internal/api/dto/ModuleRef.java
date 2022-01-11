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
package org.openhab.binding.bticinosmarther.internal.api.dto;

/**
 * The {@code ModuleRef} class defines the dto for Smarther API chronothermostat module reference object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class ModuleRef {

    private String id;

    /**
     * Returns the identifier of the chronothermostat module.
     *
     * @return a string containing the module identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("id=%s", id);
    }
}
