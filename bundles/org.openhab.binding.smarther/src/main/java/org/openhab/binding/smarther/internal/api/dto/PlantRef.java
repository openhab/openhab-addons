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

/**
 * Smarther API PlantRef DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class PlantRef {

    private String id;
    private ModuleRef module;

    public String getId() {
        return id;
    }

    public ModuleRef getModule() {
        return module;
    }

    @Override
    public String toString() {
        return String.format("id=%s, module=[%s]", id, module);
    }

}
