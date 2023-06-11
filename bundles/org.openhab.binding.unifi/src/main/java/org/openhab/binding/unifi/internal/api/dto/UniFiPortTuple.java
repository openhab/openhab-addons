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
package org.openhab.binding.unifi.internal.api.dto;

/**
 * Tuple to store both the {@link UniFiPortTable}, which contains the all information related to the port,
 * and the {@link UnfiPortOverrideJsonObject}, which contains the raw JSON data of the port override.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UniFiPortTuple {

    private UniFiDevice device;

    private UniFiPortTable table;

    private UnfiPortOverrideJsonObject jsonElement;

    public UniFiDevice getDevice() {
        return device;
    }

    public void setDevice(final UniFiDevice device) {
        this.device = device;
    }

    public int getPortIdx() {
        return table == null ? 0 : table.getPortIdx();
    }

    public UniFiPortTable getTable() {
        return table;
    }

    public void setTable(final UniFiPortTable table) {
        this.table = table;
    }

    public UnfiPortOverrideJsonObject getJsonElement() {
        return jsonElement;
    }

    public void setJsonElement(final UnfiPortOverrideJsonObject jsonElement) {
        this.jsonElement = jsonElement;
    }
}
