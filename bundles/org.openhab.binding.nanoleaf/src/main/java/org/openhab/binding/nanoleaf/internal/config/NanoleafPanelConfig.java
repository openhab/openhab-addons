/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.config;

/**
 * The {@link NanoleafPanelConfig} class contains fields mapping an individual panel configuration parameters.
 *
 * @author Martin Raepple - Initial contribution
 */
public class NanoleafPanelConfig {
    /** ID of the light panel assigned by the controller */
    public static final String ID = "id";
    public Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
