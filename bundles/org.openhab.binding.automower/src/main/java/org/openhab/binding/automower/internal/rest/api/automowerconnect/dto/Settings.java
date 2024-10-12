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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class Settings {
    private int cuttingHeight;
    private Headlight headlight;

    public int getCuttingHeight() {
        return cuttingHeight;
    }

    public void setCuttingHeight(int cuttingHeight) {
        this.cuttingHeight = cuttingHeight;
    }

    public Headlight getHeadlight() {
        return headlight;
    }

    public void setHeadlight(Headlight headlight) {
        this.headlight = headlight;
    }
}
